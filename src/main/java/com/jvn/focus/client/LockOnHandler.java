package com.jvn.focus.client;

import com.jvn.focus.Focus;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.ClipContext;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.CalculateDetachedCameraDistanceEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

@EventBusSubscriber(modid = Focus.MOD_ID, value = Dist.CLIENT)
public final class LockOnHandler {
    private static final double MAX_LOCK_DISTANCE = 20.0D;
    private static final double MAX_LOCK_DISTANCE_SQR = MAX_LOCK_DISTANCE * MAX_LOCK_DISTANCE;
    private static final double OCCLUDED_LOCK_DISTANCE = 10.0D;
    private static final double OCCLUDED_LOCK_DISTANCE_SQR = OCCLUDED_LOCK_DISTANCE * OCCLUDED_LOCK_DISTANCE;
    private static final double LOCK_ON_FOV_THRESHOLD = 0.35D;
    private static final float LOOK_RESPONSIVENESS_YAW = 10.0F;
    private static final float LOOK_RESPONSIVENESS_PITCH = 8.0F;
    private static final float LOOK_MAX_YAW_STEP_PER_TICK = 12.0F;
    private static final float LOOK_MAX_PITCH_STEP_PER_TICK = 9.0F;
    private static final float BODY_MAX_STRAFE_OFFSET = 16.0F;
    private static final float BODY_TURN_RESPONSIVENESS = 10.0F;
    private static final float BODY_FORWARD_DAMPING = 0.65F;
    private static final float TARGET_POINT_RESPONSIVENESS = 16.0F;
    private static final double DYNAMIC_NEAR_DISTANCE = 2.0D;
    private static final double DYNAMIC_FAR_DISTANCE = 12.0D;
    private static final double DYNAMIC_EXTRA_OFFSET_Z_NEAR = 1.2D;
    private static final double DYNAMIC_EXTRA_OFFSET_Y_NEAR = 0.35D;
    private static final double DYNAMIC_EXTRA_OFFSET_X_NEAR = 0.4D;
    private static final double DYNAMIC_SHOULDER_SWAP_MIN_ANGLE_RADIANS = Math.toRadians(2.0D);

    private static LivingEntity lockedTarget;
    private static CameraType previousCameraType;
    private static float smoothedLookYaw;
    private static float smoothedLookPitch;
    private static float smoothedBodyYawOffset;
    private static Vec3 smoothedTargetPoint = Vec3.ZERO;
    private static boolean smoothingInitialized;
    private static boolean cameraEditorPreviewActive;
    private static FocusClientConfig.Shoulder activeShoulder = FocusClientConfig.Shoulder.LEFT;
    private static FocusClientConfig.Shoulder staticSwapSourceShoulder = FocusClientConfig.Shoulder.LEFT;
    private static double staticSwapBlend = 1.0D;
    private static double dynamicAutoTargetBlend;
    private static double dynamicAutoCurrentBlend;
    private static Vec3 previousDynamicTargetOffset = Vec3.ZERO;
    private static boolean dynamicSwapReferenceInitialized;

    private LockOnHandler() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.level == null) {
            lockedTarget = null;
            previousCameraType = null;
            smoothingInitialized = false;
            smoothedBodyYawOffset = 0.0F;
            cameraEditorPreviewActive = false;
            staticSwapSourceShoulder = activeShoulder;
            staticSwapBlend = 1.0D;
            dynamicAutoTargetBlend = 0.0D;
            dynamicAutoCurrentBlend = 0.0D;
            previousDynamicTargetOffset = Vec3.ZERO;
            dynamicSwapReferenceInitialized = false;
            return;
        }

        while (FocusKeyMappings.LOCK_ON.consumeClick()) {
            toggleLockOn(minecraft, player);
        }
        while (FocusKeyMappings.SWAP_SHOULDER.consumeClick()) {
            swapShoulder(player, true);
        }

        if (lockedTarget != null && (!lockedTarget.isAlive() || isLockOnHiddenFromPlayer(player, lockedTarget))) {
            lockedTarget = null;
            restoreCamera(minecraft);
            player.displayClientMessage(Component.translatable("message.focus.lock_on.lost"), true);
        } else if (lockedTarget != null) {
            boolean obstructed = !hasDirectSight(player, lockedTarget);
            double maxDistanceSqr = obstructed ? OCCLUDED_LOCK_DISTANCE_SQR : MAX_LOCK_DISTANCE_SQR;
            if (player.distanceToSqr(lockedTarget) > maxDistanceSqr) {
                lockedTarget = null;
                restoreCamera(minecraft);
                player.displayClientMessage(
                        Component.translatable(obstructed ? "message.focus.lock_on.obstructed" : "message.focus.lock_on.lost"),
                        true);
            }
        }

        if (lockedTarget != null) {
            CameraType cameraType = minecraft.options.getCameraType();
            boolean allowFirstPerson = FocusClientConfig.allowFirstPersonWhileTargeting();
            boolean allowFrontFacing = FocusClientConfig.allowFrontFacingThirdPersonWhileTargeting();
            if (cameraType.isFirstPerson() && !allowFirstPerson) {
                minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK);
            } else if (cameraType == CameraType.THIRD_PERSON_FRONT && !allowFrontFacing) {
                minecraft.options.setCameraType(allowFirstPerson ? CameraType.FIRST_PERSON : CameraType.THIRD_PERSON_BACK);
            }
        } else if (cameraEditorPreviewActive && minecraft.options.getCameraType() != CameraType.THIRD_PERSON_BACK) {
            minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK);
        }
    }

    @SubscribeEvent
    public static void onRenderFrame(RenderFrameEvent.Pre event) {
        if (lockedTarget == null) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.level == null) {
            return;
        }

        if (!smoothingInitialized) {
            initializeSmoothing(player, getTargetAimPoint(lockedTarget, 1.0F));
        }

        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(true);
        float deltaTicks = Math.max(0.01F, event.getPartialTick().getRealtimeDeltaTicks());
        updateSmoothedOrientation(player, lockedTarget, partialTick, deltaTicks);
        applyPlayerLook(player, smoothedLookYaw, smoothedLookPitch);
    }

    @SubscribeEvent
    public static void onDetachedCameraDistance(CalculateDetachedCameraDistanceEvent event) {
        if (lockedTarget != null || cameraEditorPreviewActive) {
            event.setDistance((float) Math.max(currentDetachedCameraDistance(), 4.0D));
        }
    }

    private static void toggleLockOn(Minecraft minecraft, LocalPlayer player) {
        if (lockedTarget != null) {
            lockedTarget = null;
            restoreCamera(minecraft);
            player.displayClientMessage(Component.translatable("message.focus.lock_on.disabled"), true);
            return;
        }

        LivingEntity nextTarget = findTarget(player);
        if (nextTarget == null) {
            player.displayClientMessage(Component.translatable("message.focus.lock_on.no_target"), true);
            return;
        }

        lockedTarget = nextTarget;
        previousCameraType = minecraft.options.getCameraType();
        if (FocusClientConfig.autoSwitchToThirdPerson()) {
            minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK);
        }
        initializeSmoothing(player, getTargetAimPoint(nextTarget, 1.0F));
        staticSwapSourceShoulder = activeShoulder;
        staticSwapBlend = 1.0D;
        dynamicAutoTargetBlend = 0.0D;
        dynamicAutoCurrentBlend = 0.0D;
        previousDynamicTargetOffset = Vec3.ZERO;
        dynamicSwapReferenceInitialized = false;
        player.displayClientMessage(Component.translatable("message.focus.lock_on.enabled", nextTarget.getDisplayName()), true);
    }

    private static void initializeSmoothing(LocalPlayer player, Vec3 targetPoint) {
        smoothedLookYaw = player.getYRot();
        smoothedLookPitch = player.getXRot();
        smoothedBodyYawOffset = 0.0F;
        smoothedTargetPoint = targetPoint;
        smoothingInitialized = true;
    }

    private static void updateSmoothedOrientation(LocalPlayer player, LivingEntity target, float partialTick, float deltaTicks) {
        Vec3 currentTargetPoint = getTargetAimPoint(target, partialTick);
        smoothedTargetPoint = SmoothingMath.smoothVec(smoothedTargetPoint, currentTargetPoint, TARGET_POINT_RESPONSIVENESS, deltaTicks);

        float targetYaw = SmoothingMath.computeTargetYaw(player, smoothedTargetPoint, partialTick);
        float targetPitch = SmoothingMath.computeTargetPitch(player, smoothedTargetPoint, partialTick);

        smoothedLookYaw = SmoothingMath.smoothAngle(
                smoothedLookYaw, targetYaw, LOOK_RESPONSIVENESS_YAW, LOOK_MAX_YAW_STEP_PER_TICK, deltaTicks);
        smoothedLookPitch = SmoothingMath.smoothAngle(
                smoothedLookPitch, targetPitch, LOOK_RESPONSIVENESS_PITCH, LOOK_MAX_PITCH_STEP_PER_TICK, deltaTicks);
        smoothedBodyYawOffset = SmoothingMath.smoothValue(
                smoothedBodyYawOffset,
                SmoothingMath.computeDesiredBodyYawOffset(player, BODY_FORWARD_DAMPING, BODY_MAX_STRAFE_OFFSET),
                BODY_TURN_RESPONSIVENESS,
                deltaTicks);
    }

    private static void applyPlayerLook(LocalPlayer player, float yaw, float pitch) {
        player.setYRot(yaw);
        player.setXRot(Mth.clamp(pitch, -90.0F, 90.0F));
        player.setYHeadRot(yaw);
        player.setYBodyRot(yaw + smoothedBodyYawOffset);
    }

    private static void restoreCamera(Minecraft minecraft) {
        if (previousCameraType != null) {
            minecraft.options.setCameraType(previousCameraType);
            previousCameraType = null;
        }
        smoothingInitialized = false;
        smoothedBodyYawOffset = 0.0F;
        staticSwapSourceShoulder = activeShoulder;
        staticSwapBlend = 1.0D;
        dynamicAutoTargetBlend = 0.0D;
        dynamicAutoCurrentBlend = 0.0D;
        previousDynamicTargetOffset = Vec3.ZERO;
        dynamicSwapReferenceInitialized = false;
    }

    private static LivingEntity findTarget(LocalPlayer player) {
        return Targeting.findTarget(player);
    }

    private static Vec3 getTargetAimPoint(LivingEntity target, float partialTick) {
        return Targeting.getTargetAimPoint(target, partialTick);
    }

    private static boolean hasDirectSight(LocalPlayer player, LivingEntity target) {
        return Targeting.hasDirectSight(player, target);
    }

    private static boolean isLockOnHiddenFromPlayer(LocalPlayer player, LivingEntity target) {
        return Targeting.isLockOnHiddenFromPlayer(player, target);
    }

    public static LivingEntity getLockedTarget() {
        return lockedTarget;
    }

    public static CameraLockData getActiveCameraData(LocalPlayer player, float partialTick) {
        if (player == null) {
            return null;
        }

        if (lockedTarget != null) {
            return getLockedTargetCameraData(player, partialTick);
        }

        if (cameraEditorPreviewActive) {
            return getEditorPreviewCameraData(player, partialTick);
        }

        return null;
    }

    public static void startCameraEditorPreview() {
        cameraEditorPreviewActive = true;
    }

    public static void stopCameraEditorPreview(CameraType previousCameraType) {
        cameraEditorPreviewActive = false;
        if (lockedTarget == null && previousCameraType != null) {
            Minecraft minecraft = Minecraft.getInstance();
            minecraft.options.setCameraType(previousCameraType);
        }
    }

    public static boolean isCameraEditorPreviewActive() {
        return cameraEditorPreviewActive;
    }

    public static boolean hasLineOfSightToLockedTarget(LocalPlayer player) {
        return lockedTarget != null && player != null && hasDirectSight(player, lockedTarget);
    }

    public static boolean isLockedTargetWithinHitRange(LocalPlayer player) {
        return lockedTarget != null && player != null && player.canInteractWithEntity(lockedTarget, 0.0D);
    }

    public static boolean canHitLockedTarget(LocalPlayer player) {
        return hasLineOfSightToLockedTarget(player) && isLockedTargetWithinHitRange(player);
    }

    public static FocusClientConfig.Shoulder getActiveShoulder() {
        return activeShoulder;
    }

    public static FocusClientConfig.Shoulder getDisplayedShoulder() {
        if (lockedTarget != null && FocusClientConfig.cameraMode() == FocusClientConfig.CameraMode.DYNAMIC) {
            return dynamicDisplayedShoulder(dynamicAutoCurrentBlend);
        }
        return activeShoulder;
    }

    public static void setActiveShoulder(FocusClientConfig.Shoulder shoulder) {
        if (shoulder != null) {
            activeShoulder = shoulder;
            staticSwapSourceShoulder = shoulder;
            staticSwapBlend = 1.0D;
            dynamicAutoTargetBlend = 0.0D;
            dynamicAutoCurrentBlend = 0.0D;
            previousDynamicTargetOffset = Vec3.ZERO;
            dynamicSwapReferenceInitialized = false;
        }
    }

    public static FocusClientConfig.Shoulder swapShoulder(LocalPlayer player, boolean showMessage) {
        if (FocusClientConfig.cameraMode() == FocusClientConfig.CameraMode.DYNAMIC) {
            FocusClientConfig.Shoulder displayedShoulder = dynamicDisplayedShoulder(dynamicAutoCurrentBlend);
            FocusClientConfig.Shoulder desiredDisplayedShoulder = displayedShoulder.opposite();
            double preservedBlend = Mth.clamp(dynamicAutoCurrentBlend, 0.0D, 1.0D);
            if (desiredDisplayedShoulder != activeShoulder) {
                // Keep the current camera pose continuous while changing basis.
                activeShoulder = desiredDisplayedShoulder;
                dynamicAutoCurrentBlend = 1.0D - preservedBlend;
            } else {
                dynamicAutoCurrentBlend = preservedBlend;
            }
            dynamicAutoTargetBlend = 0.0D;
            previousDynamicTargetOffset = Vec3.ZERO;
            dynamicSwapReferenceInitialized = false;
        } else {
            FocusClientConfig.Shoulder previousShoulder = activeShoulder;
            activeShoulder = activeShoulder.opposite();
            staticSwapSourceShoulder = previousShoulder;
            staticSwapBlend = 0.0D;
        }
        if (showMessage && player != null) {
            player.displayClientMessage(
                    Component.translatable("message.focus.lock_on.shoulder_swapped", activeShoulder.displayName()),
                    true);
        }
        return activeShoulder;
    }

    private static CameraLockData getLockedTargetCameraData(LocalPlayer player, float partialTick) {
        if (!smoothingInitialized) {
            initializeSmoothing(player, getTargetAimPoint(lockedTarget, partialTick));
        }

        Vec3 targetPoint = smoothedTargetPoint.lengthSqr() > 0.0D ? smoothedTargetPoint : getTargetAimPoint(lockedTarget, partialTick);
        if (FocusClientConfig.cameraMode() == FocusClientConfig.CameraMode.DYNAMIC) {
            return buildDynamicCameraLockData(player, targetPoint);
        }

        dynamicAutoTargetBlend = 0.0D;
        dynamicAutoCurrentBlend = 0.0D;
        previousDynamicTargetOffset = Vec3.ZERO;
        dynamicSwapReferenceInitialized = false;
        return buildStaticCameraLockData(targetPoint);
    }

    private static CameraLockData getEditorPreviewCameraData(LocalPlayer player, float partialTick) {
        Vec3 eye = player.getEyePosition(partialTick);
        Vec3 forward = player.getViewVector(partialTick);
        Vec3 targetPoint = eye.add(forward.scale(8.0D));
        return buildCameraLockData(targetPoint, activeShoulder);
    }

    private static CameraLockData buildCameraLockData(Vec3 targetPoint, FocusClientConfig.Shoulder shoulder) {
        FocusClientConfig.PerspectivePreset preset = FocusClientConfig.currentPreset(shoulder);
        return new CameraLockData(
                targetPoint,
                preset.offsetX(),
                preset.offsetY(),
                preset.offsetZ(),
                (float) preset.rotation());
    }

    private static CameraLockData buildStaticCameraLockData(Vec3 targetPoint) {
        FocusClientConfig.PerspectivePreset sourcePreset = FocusClientConfig.currentPreset(staticSwapSourceShoulder);
        FocusClientConfig.PerspectivePreset targetPreset = FocusClientConfig.currentPreset(activeShoulder);
        staticSwapBlend = smoothTowards(
                staticSwapBlend,
                1.0D,
                FocusClientConfig.cameraSwapSpeed(),
                FocusClientConfig.MIN_CAMERA_SWAP_SPEED,
                FocusClientConfig.MAX_CAMERA_SWAP_SPEED);
        double swapBlend = applyBlendSmoothing(
                staticSwapBlend,
                FocusClientConfig.cameraSwapSmoothness(),
                FocusClientConfig.MIN_CAMERA_SWAP_SMOOTHNESS,
                FocusClientConfig.MAX_CAMERA_SWAP_SMOOTHNESS);
        if (staticSwapBlend >= 0.999D) {
            staticSwapSourceShoulder = activeShoulder;
            staticSwapBlend = 1.0D;
        }
        return new CameraLockData(
                targetPoint,
                Mth.lerp(swapBlend, sourcePreset.offsetX(), targetPreset.offsetX()),
                Mth.lerp(swapBlend, sourcePreset.offsetY(), targetPreset.offsetY()),
                Mth.lerp(swapBlend, sourcePreset.offsetZ(), targetPreset.offsetZ()),
                (float) Mth.lerp(swapBlend, sourcePreset.rotation(), targetPreset.rotation()));
    }

    private static CameraLockData buildDynamicCameraLockData(LocalPlayer player, Vec3 targetPoint) {
        updateDynamicAutoTargetBlend(player, targetPoint);

        FocusClientConfig.PerspectivePreset basePreset = FocusClientConfig.currentPreset(activeShoulder);
        FocusClientConfig.PerspectivePreset swappedPreset = basePreset.mirroredForOppositeShoulder();
        dynamicAutoCurrentBlend = smoothTowards(
                dynamicAutoCurrentBlend,
                dynamicAutoTargetBlend,
                FocusClientConfig.dynamicCameraSwapSpeed(),
                FocusClientConfig.MIN_DYNAMIC_CAMERA_SWAP_SPEED,
                FocusClientConfig.MAX_DYNAMIC_CAMERA_SWAP_SPEED);
        double swapBlend = applyBlendSmoothing(
                dynamicAutoCurrentBlend,
                FocusClientConfig.dynamicCameraSwapSmoothness(),
                FocusClientConfig.MIN_DYNAMIC_CAMERA_SWAP_SMOOTHNESS,
                FocusClientConfig.MAX_DYNAMIC_CAMERA_SWAP_SMOOTHNESS);
        double presetOffsetX = Mth.lerp(swapBlend, basePreset.offsetX(), swappedPreset.offsetX());
        double presetOffsetY = Mth.lerp(swapBlend, basePreset.offsetY(), swappedPreset.offsetY());
        double presetOffsetZ = Mth.lerp(swapBlend, basePreset.offsetZ(), swappedPreset.offsetZ());
        double presetRotation = Mth.lerp(swapBlend, basePreset.rotation(), swappedPreset.rotation());

        double targetDistance = lockedTarget != null ? player.distanceTo(lockedTarget) : player.getEyePosition().distanceTo(targetPoint);
        double nearFactor = 1.0D - Mth.clamp(
                (targetDistance - DYNAMIC_NEAR_DISTANCE) / (DYNAMIC_FAR_DISTANCE - DYNAMIC_NEAR_DISTANCE),
                0.0D,
                1.0D);

        double sourceSign = activeShoulder == FocusClientConfig.Shoulder.LEFT ? -1.0D : 1.0D;
        double targetSign = -sourceSign;
        double offsetXSign = Mth.lerp(swapBlend, sourceSign, targetSign);
        double offsetX = clamp(
                presetOffsetX + offsetXSign * DYNAMIC_EXTRA_OFFSET_X_NEAR * nearFactor,
                FocusClientConfig.MIN_CAMERA_OFFSET_X,
                FocusClientConfig.MAX_CAMERA_OFFSET_X);
        double offsetY = clamp(
                presetOffsetY + DYNAMIC_EXTRA_OFFSET_Y_NEAR * nearFactor,
                FocusClientConfig.MIN_CAMERA_OFFSET_Y,
                FocusClientConfig.MAX_CAMERA_OFFSET_Y);
        double offsetZ = clamp(
                presetOffsetZ + DYNAMIC_EXTRA_OFFSET_Z_NEAR * nearFactor,
                FocusClientConfig.MIN_CAMERA_OFFSET_Z,
                FocusClientConfig.MAX_CAMERA_OFFSET_Z);

        return new CameraLockData(
                targetPoint,
                offsetX,
                offsetY,
                offsetZ,
                (float) presetRotation);
    }

    private static void updateDynamicAutoTargetBlend(LocalPlayer player, Vec3 targetPoint) {
        Vec3 currentOffset = targetPoint.subtract(player.position()).multiply(1.0D, 0.0D, 1.0D);
        if (currentOffset.lengthSqr() < 1.0E-6D) {
            return;
        }
        if (dynamicSwapReferenceInitialized) {
            double cross = previousDynamicTargetOffset.x * currentOffset.z - previousDynamicTargetOffset.z * currentOffset.x;
            double dot = previousDynamicTargetOffset.x * currentOffset.x + previousDynamicTargetOffset.z * currentOffset.z;
            double deltaAngle = Math.atan2(cross, dot);

            FocusClientConfig.Shoulder displayedShoulder = dynamicDisplayedShoulder(dynamicAutoCurrentBlend);
            if (displayedShoulder == activeShoulder && deltaAngle > DYNAMIC_SHOULDER_SWAP_MIN_ANGLE_RADIANS) {
                dynamicAutoTargetBlend = 1.0D;
            } else if (displayedShoulder != activeShoulder && deltaAngle < -DYNAMIC_SHOULDER_SWAP_MIN_ANGLE_RADIANS) {
                dynamicAutoTargetBlend = 0.0D;
            }
        }
        previousDynamicTargetOffset = currentOffset;
        dynamicSwapReferenceInitialized = true;
    }

    private static FocusClientConfig.Shoulder dynamicDisplayedShoulder(double blend) {
        return blend >= 0.5D ? activeShoulder.opposite() : activeShoulder;
    }

    private static double currentDetachedCameraDistance() {
        if (lockedTarget == null) {
            return FocusClientConfig.cameraOffsetZ(activeShoulder);
        }

        if (FocusClientConfig.cameraMode() == FocusClientConfig.CameraMode.DYNAMIC) {
            FocusClientConfig.PerspectivePreset basePreset = FocusClientConfig.currentPreset(activeShoulder);
            FocusClientConfig.PerspectivePreset swappedPreset = basePreset.mirroredForOppositeShoulder();
            double swapBlend = applyBlendSmoothing(
                    dynamicAutoCurrentBlend,
                    FocusClientConfig.dynamicCameraSwapSmoothness(),
                    FocusClientConfig.MIN_DYNAMIC_CAMERA_SWAP_SMOOTHNESS,
                    FocusClientConfig.MAX_DYNAMIC_CAMERA_SWAP_SMOOTHNESS);
            return Mth.lerp(swapBlend, basePreset.offsetZ(), swappedPreset.offsetZ());
        }

        FocusClientConfig.PerspectivePreset sourcePreset = FocusClientConfig.currentPreset(staticSwapSourceShoulder);
        FocusClientConfig.PerspectivePreset targetPreset = FocusClientConfig.currentPreset(activeShoulder);
        double swapBlend = applyBlendSmoothing(
                staticSwapBlend,
                FocusClientConfig.cameraSwapSmoothness(),
                FocusClientConfig.MIN_CAMERA_SWAP_SMOOTHNESS,
                FocusClientConfig.MAX_CAMERA_SWAP_SMOOTHNESS);
        return Mth.lerp(swapBlend, sourcePreset.offsetZ(), targetPreset.offsetZ());
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double smoothTowards(double current, double target, double speed, double minSpeed, double maxSpeed) {
        return Mth.lerp(Mth.clamp(speed, minSpeed, maxSpeed), current, target);
    }

    private static double applyBlendSmoothing(double blend, double smoothness, double minSmoothness, double maxSmoothness) {
        double clampedBlend = Mth.clamp(blend, 0.0D, 1.0D);
        double clampedSmoothness = Mth.clamp(smoothness, minSmoothness, maxSmoothness);
        double easedBlend = clampedBlend * clampedBlend * (3.0D - 2.0D * clampedBlend);
        return Mth.lerp(clampedSmoothness, clampedBlend, easedBlend);
    }

    private static final class SmoothingMath {
        private SmoothingMath() {}

        private static float computeTargetYaw(LocalPlayer player, Vec3 targetPoint, float partialTick) {
            Vec3 to = targetPoint.subtract(player.getEyePosition(partialTick));
            return (float) (Mth.atan2(to.z, to.x) * (180.0D / Math.PI)) - 90.0F;
        }

        private static float computeTargetPitch(LocalPlayer player, Vec3 targetPoint, float partialTick) {
            Vec3 to = targetPoint.subtract(player.getEyePosition(partialTick));
            double horizontal = Math.sqrt(to.x * to.x + to.z * to.z);
            return (float) -(Mth.atan2(to.y, horizontal) * (180.0D / Math.PI));
        }

        private static float smoothAngle(float current, float target, float responsiveness, float maxStepPerTick, float deltaTicks) {
            float delta = Mth.wrapDegrees(target - current);
            float alpha = 1.0F - (float) Math.exp(-responsiveness * deltaTicks);
            float step = delta * alpha;
            float maxStep = maxStepPerTick * deltaTicks;
            return current + Mth.clamp(step, -maxStep, maxStep);
        }

        private static float smoothValue(float current, float target, float responsiveness, float deltaTicks) {
            float alpha = 1.0F - (float) Math.exp(-responsiveness * deltaTicks);
            return Mth.lerp(alpha, current, target);
        }

        private static float computeDesiredBodyYawOffset(LocalPlayer player, float bodyForwardDamping, float bodyMaxStrafeOffset) {
            Vec2 move = player.input.getMoveVector();
            float strafe = move.x;
            float forward = move.y;
            if (Math.abs(strafe) < 1.0E-3F && Math.abs(forward) < 1.0E-3F) {
                return 0.0F;
            }

            float forwardFactor = 1.0F - Math.min(1.0F, Math.abs(forward) * bodyForwardDamping);
            float desired = -strafe * bodyMaxStrafeOffset * forwardFactor;
            return Mth.clamp(desired, -bodyMaxStrafeOffset, bodyMaxStrafeOffset);
        }

        private static Vec3 smoothVec(Vec3 current, Vec3 target, float responsiveness, float deltaTicks) {
            float alpha = 1.0F - (float) Math.exp(-responsiveness * deltaTicks);
            return current.lerp(target, alpha);
        }
    }

    private static final class Targeting {
        private Targeting() {}

        private static LivingEntity findTarget(LocalPlayer player) {
            Vec3 eyePosition = player.getEyePosition();
            Vec3 lookDirection = player.getLookAngle().normalize();
            LivingEntity bestTarget = null;
            double bestAlignment = LOCK_ON_FOV_THRESHOLD;
            double bestDistanceSqr = Double.MAX_VALUE;

            for (LivingEntity entity : player.level().getEntitiesOfClass(
                    LivingEntity.class,
                    player.getBoundingBox().inflate(MAX_LOCK_DISTANCE),
                    candidate -> candidate != player && candidate.isAlive())) {
                if (isLockOnHiddenFromPlayer(player, entity) || !isWithinAllowedLockDistance(player, entity)) {
                    continue;
                }

                double alignment = getTargetAlignment(eyePosition, lookDirection, entity);
                if (alignment < LOCK_ON_FOV_THRESHOLD) {
                    continue;
                }

                double distanceSqr = player.distanceToSqr(entity);
                if (alignment > bestAlignment + 1.0E-4D
                        || (Math.abs(alignment - bestAlignment) <= 1.0E-4D && distanceSqr < bestDistanceSqr)) {
                    bestAlignment = alignment;
                    bestDistanceSqr = distanceSqr;
                    bestTarget = entity;
                }
            }

            return bestTarget;
        }

        private static Vec3 getTargetAimPoint(LivingEntity target, float partialTick) {
            return target.getPosition(partialTick).add(0.0D, target.getBbHeight() * 0.75D, 0.0D);
        }

        private static double getTargetAlignment(Vec3 eyePosition, Vec3 lookDirection, LivingEntity target) {
            Vec3 toTarget = getTargetAimPoint(target, 1.0F).subtract(eyePosition);
            double lengthSqr = toTarget.lengthSqr();
            if (lengthSqr <= 1.0E-6D) {
                return -1.0D;
            }

            return toTarget.scale(1.0D / Math.sqrt(lengthSqr)).dot(lookDirection);
        }

        private static boolean isWithinAllowedLockDistance(LocalPlayer player, LivingEntity target) {
            boolean obstructed = !hasDirectSight(player, target);
            double maxDistanceSqr = obstructed ? OCCLUDED_LOCK_DISTANCE_SQR : MAX_LOCK_DISTANCE_SQR;
            return player.distanceToSqr(target) <= maxDistanceSqr;
        }

        private static boolean hasDirectSight(LocalPlayer player, LivingEntity target) {
            Vec3 from = player.getEyePosition();
            Vec3 to = getTargetAimPoint(target, 1.0F);
            BlockHitResult hitResult = player.level().clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
            return hitResult.getType() != HitResult.Type.BLOCK;
        }

        private static boolean isLockOnHiddenFromPlayer(LocalPlayer player, LivingEntity target) {
            return target.isInvisible() || target.isInvisibleTo(player);
        }
    }

    public record CameraLockData(Vec3 targetPoint, double offsetX, double offsetY, double offsetZ, float rotationDegrees) {}
}
