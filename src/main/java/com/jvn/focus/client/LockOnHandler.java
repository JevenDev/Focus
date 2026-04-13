package com.jvn.focus.client;

import com.jvn.focus.Focus;
import com.jvn.focus.client.camera.FocusCameraController;
import com.jvn.focus.client.camera.FocusCameraPose;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
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
    private static final int TARGET_SWAP_MIN_COOLDOWN_TICKS = 8;
    private static final int OCCLUDED_GRACE_TICKS = 15;
    private static final int OUT_OF_RANGE_GRACE_TICKS = 10;

    private static final FocusCameraController CAMERA_CONTROLLER = FocusCameraController.getInstance();

    private static LivingEntity lockedTarget;
    private static CameraType previousCameraType;
    private static double pendingMouseDeltaX;
    private static double pendingMouseDeltaY;
    private static int targetSwapCooldownTicks;
    private static boolean targetSwapReadyForNewFlick = true;
    private static int occlusionGraceTicks;
    private static int outOfRangeGraceTicks;
    private static CameraType lockOnPreferredCameraType;
    private static CameraType lastEnforcedCameraType;

    private LockOnHandler() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.level == null) {
            lockedTarget = null;
            previousCameraType = null;
            lastEnforcedCameraType = null;
            lockOnPreferredCameraType = null;
            occlusionGraceTicks = 0;
            outOfRangeGraceTicks = 0;
            CAMERA_CONTROLLER.resetForWorldUnload();
            resetTargetSwapInput();
            return;
        }

        while (FocusKeyMappings.LOCK_ON.consumeClick()) {
            toggleLockOn(minecraft, player);
        }
        while (FocusKeyMappings.SWAP_SHOULDER.consumeClick()) {
            swapShoulder(player, true);
        }
        handleOwnershipAndFreeLookInput(player);
        boolean freeLookActive = isFreeLookActive()
                || (lockedTarget == null && isCameraEditorPreviewActive() && FocusKeyMappings.FREE_LOOK.isDown());
        CAMERA_CONTROLLER.setFreeLookInputActive(freeLookActive);
        handleCameraAdjustmentInput(player);

        if (lockedTarget != null && !lockedTarget.isAlive()) {
            LivingEntity replacementTarget = Targeting.findClosestTarget(player, lockedTarget);
            if (replacementTarget != null) {
                resetTargetSwapInput();
                setLockedTarget(player, replacementTarget, true);
                occlusionGraceTicks = 0;
                outOfRangeGraceTicks = 0;
            } else {
                lockedTarget = null;
                restoreCamera(minecraft);
                player.displayClientMessage(Component.translatable("message.focus.lock_on.lost"), true);
            }
        } else if (lockedTarget != null && (isLockOnHiddenFromPlayer(player, lockedTarget) || !Targeting.isTargetAllowed(lockedTarget))) {
            lockedTarget = null;
            restoreCamera(minecraft);
            player.displayClientMessage(Component.translatable("message.focus.lock_on.lost"), true);
        } else if (lockedTarget != null) {
            boolean obstructed = !hasDirectSight(player, lockedTarget);
            double distanceSqr = player.distanceToSqr(lockedTarget);
            double maxDistanceSqr = obstructed ? OCCLUDED_LOCK_DISTANCE_SQR : MAX_LOCK_DISTANCE_SQR;

            if (distanceSqr > maxDistanceSqr) {
                outOfRangeGraceTicks++;
            } else {
                outOfRangeGraceTicks = 0;
            }
            if (obstructed) {
                occlusionGraceTicks++;
            } else {
                occlusionGraceTicks = 0;
            }

            if (outOfRangeGraceTicks > OUT_OF_RANGE_GRACE_TICKS) {
                lockedTarget = null;
                restoreCamera(minecraft);
                player.displayClientMessage(
                        Component.translatable(obstructed ? "message.focus.lock_on.obstructed" : "message.focus.lock_on.lost"),
                        true);
            } else if (occlusionGraceTicks > OCCLUDED_GRACE_TICKS) {
                lockedTarget = null;
                restoreCamera(minecraft);
                player.displayClientMessage(Component.translatable("message.focus.lock_on.obstructed"), true);
            }
        }
        CAMERA_CONTROLLER.updatePlayerVisibility(player, lockedTarget, 1.0F);

        if (targetSwapCooldownTicks > 0) {
            targetSwapCooldownTicks--;
            if (targetSwapCooldownTicks == 0 && !targetSwapReadyForNewFlick) {
                targetSwapReadyForNewFlick = true;
                pendingMouseDeltaX = 0.0D;
                pendingMouseDeltaY = 0.0D;
            }
        }
        CAMERA_CONTROLLER.onClientTick(lockedTarget != null);
        if (lockedTarget != null && !freeLookActive) {
            tryDirectionalTargetSwap(player);
        } else {
            resetTargetSwapInput();
        }

        if (lockedTarget != null) {
            CameraType cameraType = minecraft.options.getCameraType();
            // Detect user-initiated perspective changes (F5) and remember their preference
            boolean userChangedPerspective = lastEnforcedCameraType != null && cameraType != lastEnforcedCameraType;
            boolean allowFirstPerson = FocusClientConfig.allowFirstPersonWhileTargeting();
            boolean allowFrontFacing = FocusClientConfig.allowFrontFacingThirdPersonWhileTargeting();
            if (cameraType.isFirstPerson() && !allowFirstPerson) {
                minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK);
            } else if (cameraType == CameraType.THIRD_PERSON_FRONT && !allowFrontFacing) {
                minecraft.options.setCameraType(allowFirstPerson ? CameraType.FIRST_PERSON : CameraType.THIRD_PERSON_BACK);
            }
            CameraType enforcedType = minecraft.options.getCameraType();
            if (userChangedPerspective) {
                lockOnPreferredCameraType = enforcedType;
            }
            lastEnforcedCameraType = enforcedType;
        } else if (isCameraEditorPreviewActive() && minecraft.options.getCameraType() != CameraType.THIRD_PERSON_BACK) {
            minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK);
        }
    }

    @SubscribeEvent
    public static void onRenderFrame(RenderFrameEvent.Pre event) {
        float deltaTicks = Math.max(0.01F, event.getPartialTick().getRealtimeDeltaTicks());
        // Always store frame timing for FPS-independent smoothing, even when not locked on.
        CAMERA_CONTROLLER.storeRenderDeltaTicks(deltaTicks);

        if (lockedTarget == null) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.level == null) {
            return;
        }

        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(true);
        CAMERA_CONTROLLER.onRenderFrame(player, lockedTarget, partialTick, deltaTicks);
    }

    @SubscribeEvent
    public static void onDetachedCameraDistance(CalculateDetachedCameraDistanceEvent event) {
        if (lockedTarget != null || isCameraEditorPreviewActive()) {
            event.setDistance((float) Math.max(CAMERA_CONTROLLER.currentDetachedCameraDistance(lockedTarget != null), 4.0D));
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
            CameraType preferredLockPerspective = lockOnPreferredCameraType != null
                    ? lockOnPreferredCameraType
                    : CameraType.THIRD_PERSON_BACK;
            minecraft.options.setCameraType(preferredLockPerspective);
        }
        lastEnforcedCameraType = minecraft.options.getCameraType();
        occlusionGraceTicks = 0;
        outOfRangeGraceTicks = 0;
        CAMERA_CONTROLLER.onTargetSet(player, nextTarget, false);
        CAMERA_CONTROLLER.onLockStarted(player, nextTarget);
        resetTargetSwapInput();
        player.displayClientMessage(Component.translatable("message.focus.lock_on.enabled", nextTarget.getDisplayName()), true);
    }

    private static void restoreCamera(Minecraft minecraft) {
        clearLockState(true, minecraft);
    }

    private static void clearLockState(boolean restorePreviousCameraType, Minecraft minecraft) {
        if (restorePreviousCameraType && previousCameraType != null) {
            if (minecraft == null) {
                minecraft = Minecraft.getInstance();
            }
            minecraft.options.setCameraType(previousCameraType);
        }
        previousCameraType = null;
        lastEnforcedCameraType = null;
        occlusionGraceTicks = 0;
        outOfRangeGraceTicks = 0;
        CAMERA_CONTROLLER.onLockEnded();
        resetTargetSwapInput();
    }

    private static void setLockedTarget(LocalPlayer player, LivingEntity nextTarget, boolean applySwapSmoothing) {
        lockedTarget = nextTarget;
        CAMERA_CONTROLLER.onTargetSet(player, nextTarget, applySwapSmoothing);
    }

    private static LivingEntity findTarget(LocalPlayer player) {
        return Targeting.findTarget(player);
    }

    private static boolean hasDirectSight(LocalPlayer player, LivingEntity target) {
        return Targeting.hasDirectSight(player, target);
    }

    private static boolean isLockOnHiddenFromPlayer(LocalPlayer player, LivingEntity target) {
        return Targeting.isLockOnHiddenFromPlayer(player, target);
    }

    public static void onRawMouseInput(double deltaX, double deltaY) {
        boolean previewFreeLook = lockedTarget == null && isCameraEditorPreviewActive() && FocusKeyMappings.FREE_LOOK.isDown();
        if (lockedTarget == null && !previewFreeLook) {
            return;
        }

        if (isFreeLookActive() || previewFreeLook) {
            // Priority: free-look consumes raw mouse deltas before directional target-swap logic.
            float sensitivity = FocusClientConfig.freeLookSensitivity();
            CAMERA_CONTROLLER.addFreeLookDelta((float) deltaX * sensitivity, (float) -deltaY * sensitivity);
            return;
        }

        pendingMouseDeltaX += deltaX;
        pendingMouseDeltaY += deltaY;
    }

    public static LivingEntity getLockedTarget() {
        return lockedTarget;
    }

    public static FocusCameraPose getActiveCameraData(LocalPlayer player, float partialTick) {
        if (player == null) {
            return null;
        }

        return CAMERA_CONTROLLER.getActiveCameraPose(player, lockedTarget, partialTick);
    }

    public static float getTargetSwapBlendToNormal() {
        return CAMERA_CONTROLLER.getTargetSwapBlendToNormal();
    }

    public static float getTargetSwapCameraPositionFactor() {
        return CAMERA_CONTROLLER.getTargetSwapCameraPositionFactor();
    }

    public static float getTargetSwapCameraRotationFactor() {
        return CAMERA_CONTROLLER.getTargetSwapCameraRotationFactor();
    }

    public static boolean isInitialLockCameraSnapActive() {
        return CAMERA_CONTROLLER.isInitialLockCameraSnapActive();
    }

    public static void startCameraEditorPreview() {
        CAMERA_CONTROLLER.startCameraEditorPreview();
    }

    public static void stopCameraEditorPreview(CameraType previousCameraType) {
        CAMERA_CONTROLLER.stopCameraEditorPreview();
        if (lockedTarget == null && previousCameraType != null) {
            Minecraft minecraft = Minecraft.getInstance();
            minecraft.options.setCameraType(previousCameraType);
        }
    }

    public static boolean isCameraEditorPreviewActive() {
        return CAMERA_CONTROLLER.isCameraEditorPreviewActive();
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
        return CAMERA_CONTROLLER.getActiveShoulder();
    }

    public static FocusClientConfig.Shoulder getDisplayedShoulder() {
        return CAMERA_CONTROLLER.getDisplayedShoulder(lockedTarget != null);
    }

    public static double getDynamicAutoCurrentBlend() {
        return CAMERA_CONTROLLER.getDynamicAutoCurrentBlend();
    }

    public static double getStaticSwapBlend() {
        return CAMERA_CONTROLLER.getStaticSwapBlend();
    }

    public static void setActiveShoulder(FocusClientConfig.Shoulder shoulder) {
        CAMERA_CONTROLLER.setActiveShoulder(shoulder);
    }

    public static FocusClientConfig.Shoulder swapShoulder(LocalPlayer player, boolean showMessage) {
        return CAMERA_CONTROLLER.swapShoulder(player, showMessage, lockedTarget != null);
    }

    public static float playerTransparencyAlpha() {
        return CAMERA_CONTROLLER.playerTransparencyAlpha();
    }

    public static boolean shouldSuppressVanillaMouseTurn() {
        return lockedTarget != null
                || (isCameraEditorPreviewActive() && FocusKeyMappings.FREE_LOOK.isDown());
    }

    private static void tryDirectionalTargetSwap(LocalPlayer player) {
        if (!targetSwapReadyForNewFlick) {
            return;
        }
        if (targetSwapCooldownTicks > 0) {
            pendingMouseDeltaX = 0.0D;
            pendingMouseDeltaY = 0.0D;
            return;
        }

        double mouseInputDecay = FocusClientConfig.targetSwapInputDecay();
        double mouseMagnitudeSqr =
                pendingMouseDeltaX * pendingMouseDeltaX + pendingMouseDeltaY * pendingMouseDeltaY;
        double deadzone = FocusClientConfig.targetSwapMouseDeadzone();
        if (mouseMagnitudeSqr < deadzone * deadzone) {
            dampenTargetSwapInput(mouseInputDecay);
            return;
        }
        double activation = FocusClientConfig.targetSwapMouseActivation();
        if (mouseMagnitudeSqr < activation * activation) {
            dampenTargetSwapInput(mouseInputDecay);
            return;
        }

        Vec2 mouseDirection = new Vec2((float) pendingMouseDeltaX, (float) -pendingMouseDeltaY);
        Vec3 cameraLookDir = CAMERA_CONTROLLER.getSmoothedLookDirection();
        LivingEntity swappedTarget = Targeting.findDirectionalTarget(player, lockedTarget, mouseDirection, cameraLookDir);
        if (swappedTarget == null) {
            dampenTargetSwapInput(0.45D);
            return;
        }

        resetTargetSwapInput();
        targetSwapCooldownTicks = Math.max(TARGET_SWAP_MIN_COOLDOWN_TICKS, FocusClientConfig.targetSwapCooldownTicks());
        targetSwapReadyForNewFlick = false;
        setLockedTarget(player, swappedTarget, true);
    }

    private static void dampenTargetSwapInput(double factor) {
        pendingMouseDeltaX *= factor;
        pendingMouseDeltaY *= factor;
    }

    private static void resetTargetSwapInput() {
        pendingMouseDeltaX = 0.0D;
        pendingMouseDeltaY = 0.0D;
        targetSwapCooldownTicks = 0;
        targetSwapReadyForNewFlick = true;
    }

    private static void handleOwnershipAndFreeLookInput(LocalPlayer player) {
        while (FocusKeyMappings.FREE_LOOK_TOGGLE.consumeClick()) {
            FocusClientConfig.setFreeLookToggled(!FocusClientConfig.freeLookToggled());
            FocusClientConfig.saveConfig();
            player.displayClientMessage(
                    Component.translatable(
                            FocusClientConfig.freeLookToggled()
                                    ? "message.focus.free_look.enabled"
                                    : "message.focus.free_look.disabled"),
                    true);
        }
        while (FocusKeyMappings.CYCLE_CAMERA_OWNERSHIP_MODE.consumeClick()) {
            FocusClientConfig.cycleCameraOwnershipMode();
            FocusClientConfig.saveConfig();
            player.displayClientMessage(
                    Component.translatable(
                            "message.focus.camera_ownership_mode",
                            Component.translatable(FocusClientConfig.cameraOwnershipMode().getSerializedName())),
                    true);
        }
        while (FocusKeyMappings.RECENTER_CAMERA.consumeClick()) {
            CAMERA_CONTROLLER.smoothRecenterFreeLook();
        }
    }

    private static boolean isFreeLookActive() {
        if (!FocusClientConfig.allowFreeLookWhileLockedOn()) {
            return false;
        }
        if (FocusClientConfig.cameraOwnershipMode() != com.jvn.focus.client.camera.FocusCameraMode.FREE_LOOK) {
            return false;
        }
        return FocusKeyMappings.FREE_LOOK.isDown() || FocusClientConfig.freeLookToggled();
    }

    private static void handleCameraAdjustmentInput(LocalPlayer player) {
        if (lockedTarget == null && !isCameraEditorPreviewActive()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen instanceof LockOnCameraEditorScreen) {
            return;
        }
        if (minecraft.screen != null) {
            return;
        }

        FocusClientConfig.Shoulder shoulder = getActiveShoulder();
        boolean changed = false;
        while (FocusKeyMappings.CAMERA_LEFT.consumeClick()) {
            FocusClientConfig.adjustCameraLeft(shoulder);
            changed = true;
        }
        while (FocusKeyMappings.CAMERA_RIGHT.consumeClick()) {
            FocusClientConfig.adjustCameraRight(shoulder);
            changed = true;
        }
        while (FocusKeyMappings.CAMERA_IN.consumeClick()) {
            FocusClientConfig.adjustCameraIn(shoulder);
            changed = true;
        }
        while (FocusKeyMappings.CAMERA_OUT.consumeClick()) {
            FocusClientConfig.adjustCameraOut(shoulder);
            changed = true;
        }
        while (FocusKeyMappings.CAMERA_UP.consumeClick()) {
            FocusClientConfig.adjustCameraUp(shoulder);
            changed = true;
        }
        while (FocusKeyMappings.CAMERA_DOWN.consumeClick()) {
            FocusClientConfig.adjustCameraDown(shoulder);
            changed = true;
        }
        if (changed) {
            FocusClientConfig.saveConfig();
            if (player != null && isCameraEditorPreviewActive()) {
                player.displayClientMessage(Component.translatable("screen.focus.camera_editor.adjusted"), true);
            }
        }
    }

    private static final class Targeting {
        private Targeting() {}

        private static LivingEntity findTarget(LocalPlayer player) {
            Vec3 eyePosition = player.getEyePosition();
            Vec3 lookDirection = player.getLookAngle().normalize();
            TargetFilterSettings targetFilterSettings = readTargetFilterSettings();
            LivingEntity bestTarget = null;
            double bestAlignment = LOCK_ON_FOV_THRESHOLD;
            double bestDistanceSqr = Double.MAX_VALUE;

            for (LivingEntity entity : player.level().getEntitiesOfClass(
                    LivingEntity.class,
                    player.getBoundingBox().inflate(MAX_LOCK_DISTANCE),
                    candidate -> candidate != player && candidate.isAlive() && isTargetAllowed(candidate, targetFilterSettings))) {
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

        private static LivingEntity findClosestTarget(LocalPlayer player, LivingEntity excludedTarget) {
            TargetFilterSettings targetFilterSettings = readTargetFilterSettings();
            LivingEntity bestTarget = null;
            double bestDistanceSqr = Double.MAX_VALUE;

            for (LivingEntity entity : player.level().getEntitiesOfClass(
                    LivingEntity.class,
                    player.getBoundingBox().inflate(MAX_LOCK_DISTANCE),
                    candidate -> candidate != player
                            && candidate.isAlive()
                            && candidate != excludedTarget
                            && isTargetAllowed(candidate, targetFilterSettings))) {
                if (isLockOnHiddenFromPlayer(player, entity) || !isWithinAllowedLockDistance(player, entity)) {
                    continue;
                }

                double distanceSqr = player.distanceToSqr(entity);
                if (distanceSqr < bestDistanceSqr) {
                    bestDistanceSqr = distanceSqr;
                    bestTarget = entity;
                }
            }

            return bestTarget;
        }

        private static LivingEntity findDirectionalTarget(LocalPlayer player, LivingEntity currentTarget, Vec2 mouseDirection, Vec3 cameraLookDirection) {
            if (currentTarget == null) {
                return null;
            }

            float mouseMagnitude = Mth.sqrt(
                    mouseDirection.x * mouseDirection.x + mouseDirection.y * mouseDirection.y);
            if (mouseMagnitude < 1.0E-4F) {
                return null;
            }

            float mouseDirX = mouseDirection.x / mouseMagnitude;
            float mouseDirY = mouseDirection.y / mouseMagnitude;
            Vec3 eyePosition = player.getEyePosition();
            // Use camera look direction for accurate screen-space projection during lock-on
            Vec3 lookDirection = cameraLookDirection.lengthSqr() > 1.0E-6D
                    ? cameraLookDirection.normalize()
                    : player.getLookAngle().normalize();
            Vec3 right = lookDirection.cross(new Vec3(0.0D, 1.0D, 0.0D));
            if (right.lengthSqr() < 1.0E-6D) {
                right = new Vec3(1.0D, 0.0D, 0.0D);
            } else {
                right = right.normalize();
            }
            Vec3 up = right.cross(lookDirection).normalize();

            Vec2 currentTargetScreen = projectToScreenSpace(eyePosition, lookDirection, right, up, currentTarget);
            if (currentTargetScreen == null) {
                return null;
            }

            double directionThreshold = FocusClientConfig.targetSwapDirectionThreshold();
            double minScreenSeparation = FocusClientConfig.targetSwapMinScreenSeparation();
            TargetFilterSettings targetFilterSettings = readTargetFilterSettings();
            LivingEntity bestTarget = null;
            float bestScreenDistance = Float.MAX_VALUE;
            float bestAlignment = (float) directionThreshold;
            double bestDistanceSqr = Double.MAX_VALUE;

            for (LivingEntity entity : player.level().getEntitiesOfClass(
                    LivingEntity.class,
                    player.getBoundingBox().inflate(MAX_LOCK_DISTANCE),
                    candidate -> candidate != player
                            && candidate.isAlive()
                            && candidate != currentTarget
                            && isTargetAllowed(candidate, targetFilterSettings))) {
                if (isLockOnHiddenFromPlayer(player, entity) || !isWithinAllowedLockDistance(player, entity)) {
                    continue;
                }

                Vec2 candidateScreen = projectToScreenSpace(eyePosition, lookDirection, right, up, entity);
                if (candidateScreen == null) {
                    continue;
                }

                float deltaX = candidateScreen.x - currentTargetScreen.x;
                float deltaY = candidateScreen.y - currentTargetScreen.y;
                float candidateScreenDistance = Mth.sqrt(deltaX * deltaX + deltaY * deltaY);
                if (candidateScreenDistance < minScreenSeparation) {
                    continue;
                }

                float candidateDirX = deltaX / candidateScreenDistance;
                float candidateDirY = deltaY / candidateScreenDistance;
                float directionalAlignment = candidateDirX * mouseDirX + candidateDirY * mouseDirY;
                if (directionalAlignment < directionThreshold) {
                    continue;
                }

                double distanceSqr = player.distanceToSqr(entity);
                if (candidateScreenDistance < bestScreenDistance - 1.0E-4F
                        || (Math.abs(candidateScreenDistance - bestScreenDistance) <= 1.0E-4F
                                && directionalAlignment > bestAlignment + 1.0E-4F)
                        || (Math.abs(candidateScreenDistance - bestScreenDistance) <= 1.0E-4F
                                && Math.abs(directionalAlignment - bestAlignment) <= 1.0E-4F
                                && distanceSqr < bestDistanceSqr)) {
                    bestScreenDistance = candidateScreenDistance;
                    bestAlignment = directionalAlignment;
                    bestDistanceSqr = distanceSqr;
                    bestTarget = entity;
                }
            }

            return bestTarget;
        }

        private static Vec3 getTargetAimPoint(LivingEntity target, float partialTick) {
            return CAMERA_CONTROLLER.targetPointFor(target, partialTick);
        }

        private static double getTargetAlignment(Vec3 eyePosition, Vec3 lookDirection, LivingEntity target) {
            Vec3 toTarget = getTargetAimPoint(target, 1.0F).subtract(eyePosition);
            double lengthSqr = toTarget.lengthSqr();
            if (lengthSqr <= 1.0E-6D) {
                return -1.0D;
            }

            return toTarget.scale(1.0D / Math.sqrt(lengthSqr)).dot(lookDirection);
        }

        private static Vec2 projectToScreenSpace(
                Vec3 eyePosition,
                Vec3 lookDirection,
                Vec3 right,
                Vec3 up,
                LivingEntity target) {
            Vec3 toTarget = getTargetAimPoint(target, 1.0F).subtract(eyePosition);
            double forward = toTarget.dot(lookDirection);
            if (forward <= 1.0E-3D) {
                return null;
            }

            double screenX = toTarget.dot(right) / forward;
            double screenY = toTarget.dot(up) / forward;
            return new Vec2((float) screenX, (float) screenY);
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

        private static boolean isTargetAllowed(LivingEntity target) {
            return isTargetAllowed(target, readTargetFilterSettings());
        }

        private static boolean isTargetAllowed(LivingEntity target, TargetFilterSettings targetFilterSettings) {
            if (!targetFilterSettings.enabled()) {
                return true;
            }

            boolean matchesAnyFilter = matchesAnyFilter(target, targetFilterSettings);
            if (targetFilterSettings.mode() == FocusClientConfig.TargetFilterMode.EXCLUSIVE) {
                return matchesAnyFilter;
            }
            return !matchesAnyFilter;
        }

        private static boolean matchesAnyFilter(LivingEntity target, TargetFilterSettings targetFilterSettings) {
            if (targetFilterSettings.filterPlayers() && target instanceof Player) {
                return true;
            }
            if (targetFilterSettings.filterHostileMobs() && target instanceof Enemy) {
                return true;
            }
            if (targetFilterSettings.filterNeutralMobs() && target instanceof NeutralMob && !(target instanceof Enemy)) {
                return true;
            }
            if (targetFilterSettings.filterPassiveMobs() && isPassiveMob(target)) {
                return true;
            }
            if (!targetFilterSettings.entityTypeIds().isEmpty()) {
                ResourceLocation entityTypeId = BuiltInRegistries.ENTITY_TYPE.getKey(target.getType());
                if (entityTypeId != null && targetFilterSettings.entityTypeIds().contains(entityTypeId)) {
                    return true;
                }
            }
            return false;
        }

        private static boolean isPassiveMob(LivingEntity target) {
            return target instanceof Mob && !(target instanceof Enemy) && !(target instanceof NeutralMob);
        }

        private static TargetFilterSettings readTargetFilterSettings() {
            if (!FocusClientConfig.enableTargetFilters()) {
                return new TargetFilterSettings(
                        false,
                        FocusClientConfig.TargetFilterMode.EXCLUDE,
                        false,
                        false,
                        false,
                        false,
                        Set.of());
            }

            return new TargetFilterSettings(
                    true,
                    FocusClientConfig.targetFilterMode(),
                    FocusClientConfig.filterPlayers(),
                    FocusClientConfig.filterPassiveMobs(),
                    FocusClientConfig.filterNeutralMobs(),
                    FocusClientConfig.filterHostileMobs(),
                    parseEntityTypeIds(FocusClientConfig.targetFilterEntityIds()));
        }

        private static Set<ResourceLocation> parseEntityTypeIds(List<String> configuredEntityTypeIds) {
            if (configuredEntityTypeIds.isEmpty()) {
                return Set.of();
            }

            Set<ResourceLocation> parsedEntityTypeIds = new HashSet<>();
            for (String configuredEntityTypeId : configuredEntityTypeIds) {
                ResourceLocation parsedId = ResourceLocation.tryParse(configuredEntityTypeId);
                if (parsedId != null) {
                    parsedEntityTypeIds.add(parsedId);
                }
            }
            return parsedEntityTypeIds;
        }

        private record TargetFilterSettings(
                boolean enabled,
                FocusClientConfig.TargetFilterMode mode,
                boolean filterPlayers,
                boolean filterPassiveMobs,
                boolean filterNeutralMobs,
                boolean filterHostileMobs,
                Set<ResourceLocation> entityTypeIds) {
        }
    }
}
