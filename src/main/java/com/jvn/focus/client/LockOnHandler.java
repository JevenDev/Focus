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
    private static final float CAMERA_BACK_DISTANCE = 2.5F;
    private static final float LOOK_RESPONSIVENESS_YAW = 10.0F;
    private static final float LOOK_RESPONSIVENESS_PITCH = 8.0F;
    private static final float LOOK_MAX_YAW_STEP_PER_TICK = 12.0F;
    private static final float LOOK_MAX_PITCH_STEP_PER_TICK = 9.0F;
    private static final float BODY_MAX_STRAFE_OFFSET = 16.0F;
    private static final float BODY_TURN_RESPONSIVENESS = 10.0F;
    private static final float BODY_FORWARD_DAMPING = 0.65F;
    private static final float TARGET_POINT_RESPONSIVENESS = 16.0F;

    private static LivingEntity lockedTarget;
    private static CameraType previousCameraType;
    private static float smoothedLookYaw;
    private static float smoothedLookPitch;
    private static float smoothedBodyYawOffset;
    private static Vec3 smoothedTargetPoint = Vec3.ZERO;
    private static boolean smoothingInitialized;

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
            return;
        }

        while (FocusKeyMappings.LOCK_ON.consumeClick()) {
            toggleLockOn(minecraft, player);
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
        if (lockedTarget != null) {
            event.setDistance(Math.max(CAMERA_BACK_DISTANCE, 4.0F));
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

    public static boolean hasLineOfSightToLockedTarget(LocalPlayer player) {
        return lockedTarget != null && player != null && hasDirectSight(player, lockedTarget);
    }

    public static boolean isLockedTargetWithinHitRange(LocalPlayer player) {
        return lockedTarget != null && player != null && player.canInteractWithEntity(lockedTarget, 0.0D);
    }

    public static boolean canHitLockedTarget(LocalPlayer player) {
        return hasLineOfSightToLockedTarget(player) && isLockedTargetWithinHitRange(player);
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
}
