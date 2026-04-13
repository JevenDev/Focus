package com.jvn.focus.client.camera;

import com.jvn.focus.client.FocusClientConfig;
import javax.annotation.Nullable;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class FocusCameraController {
    private static final float LOOK_RESPONSIVENESS_YAW = 10.0F;
    private static final float LOOK_RESPONSIVENESS_PITCH = 8.0F;
    private static final float LOOK_MAX_YAW_STEP_PER_TICK = 12.0F;
    private static final float LOOK_MAX_PITCH_STEP_PER_TICK = 9.0F;
    private static final float BODY_MAX_STRAFE_OFFSET = 16.0F;
    private static final float BODY_TURN_RESPONSIVENESS = 10.0F;
    private static final float BODY_FORWARD_DAMPING = 0.65F;
    private static final float TARGET_POINT_RESPONSIVENESS = 16.0F;
    private static final int INITIAL_LOCK_CAMERA_SNAP_TICKS = 4;
    private static final float TARGET_SWAP_CAMERA_POSITION_FACTOR_MIN = 0.22F;
    private static final float TARGET_SWAP_CAMERA_ROTATION_FACTOR_MIN = 0.2F;
    private static final float MAX_TARGET_ANGULAR_DEVIATION = 35.0F;
    private static final float ADAPTIVE_SMOOTHING_THRESHOLD_DEGREES = 12.0F;
    private static final double ADAPTIVE_SMOOTHING_THRESHOLD_DISTANCE = 3.0D;

    private static final FocusCameraController INSTANCE = new FocusCameraController();

    private final FocusCameraState state = new FocusCameraState();
    private final FocusCameraOcclusionEvaluator occlusionEvaluator = new FocusCameraOcclusionEvaluator();
    private FocusCameraTargetPointProvider targetPointProvider = new FocusDefaultCameraTargetPointProvider();
    private FocusCameraRotationFollowPolicy rotationFollowPolicy = new FocusDefaultCameraRotationFollowPolicy();
    private final FocusDefaultCameraShoulderPolicy defaultShoulderPolicy = new FocusDefaultCameraShoulderPolicy();
    private FocusCameraShoulderPolicy shoulderPolicy = defaultShoulderPolicy;
    private FocusCameraOcclusionResult latestOcclusionResult = FocusCameraOcclusionResult.NONE;

    private FocusCameraController() {}

    public static FocusCameraController getInstance() {
        return INSTANCE;
    }

    public void resetForWorldUnload() {
        state.resetForWorldUnload();
    }

    public void onLockStarted(LocalPlayer player, LivingEntity target) {
        Vec3 targetPoint = getTargetAimPoint(target, 1.0F);
        initializeSmoothing(player, targetPoint);
        state.smoothedLookYaw = FocusCameraMath.computeTargetYaw(player, targetPoint, 1.0F);
        state.smoothedLookPitch = FocusCameraMath.computeTargetPitch(player, targetPoint, 1.0F);
        state.smoothedTargetPoint = targetPoint;
        applyRotationPolicy(player, target, 1.0F, 0.01F);
        defaultShoulderPolicy.initializeDynamicAutoShoulderBlend(player, target, state, targetPoint);
        state.initialLockCameraSnapTicks = INITIAL_LOCK_CAMERA_SNAP_TICKS;
    }

    public void onTargetSet(LocalPlayer player, LivingEntity target, boolean applySwapSmoothing) {
        if (!state.smoothingInitialized) {
            initializeSmoothing(player, getTargetAimPoint(target, 1.0F));
        }
        if (applySwapSmoothing) {
            state.targetSwapSmoothingDurationTicks = FocusClientConfig.targetSwapSmoothTicks();
            state.targetSwapSmoothingTicks = state.targetSwapSmoothingDurationTicks;
        } else {
            state.targetSwapSmoothingTicks = 0;
            state.targetSwapSmoothingDurationTicks = 0;
        }
        state.previousDynamicTargetOffset = Vec3.ZERO;
        state.dynamicSwapReferenceInitialized = false;
        state.playerFollowDelayTicks = 0;
    }

    public void onLockEnded() {
        state.resetForLockEnd();
    }

    public void onClientTick(boolean lockOnActive) {
        if (state.targetSwapSmoothingTicks > 0) {
            state.targetSwapSmoothingTicks--;
            if (state.targetSwapSmoothingTicks == 0) {
                state.targetSwapSmoothingDurationTicks = 0;
            }
        }
        if (lockOnActive) {
            if (state.initialLockCameraSnapTicks > 0) {
                state.initialLockCameraSnapTicks--;
            }
        } else {
            state.initialLockCameraSnapTicks = 0;
            state.playerFollowDelayTicks = 0;
        }

        if (state.dynamicManualShoulderOverrideTicks > 0) {
            state.dynamicManualShoulderOverrideTicks--;
        }
        if (state.freeLookRecentering && !state.freeLookInputActive) {
            float recenterSpeed = FocusClientConfig.cameraRecenteringSpeed();
            state.freeLookYaw = Mth.lerp(recenterSpeed, state.freeLookYaw, 0.0F);
            state.freeLookPitch = Mth.lerp(recenterSpeed, state.freeLookPitch, 0.0F);
            if (Math.abs(state.freeLookYaw) < 0.01F && Math.abs(state.freeLookPitch) < 0.01F) {
                state.freeLookYaw = 0.0F;
                state.freeLookPitch = 0.0F;
                state.freeLookRecentering = false;
            }
        }
    }

    public void updatePlayerVisibility(LocalPlayer player, @Nullable LivingEntity lockedTarget, float partialTick) {
        if (!FocusClientConfig.adjustPlayerTransparency()) {
            state.playerTransparencyAlpha = 1.0F;
            latestOcclusionResult = FocusCameraOcclusionResult.NONE;
            return;
        }
        boolean active = lockedTarget != null || (state.cameraEditorPreviewActive && FocusClientConfig.playerTransparencyInPreview());
        if (!active) {
            state.playerTransparencyAlpha = 1.0F;
            latestOcclusionResult = FocusCameraOcclusionResult.NONE;
            return;
        }

        Vec3 targetPoint = lockedTarget != null
                ? (state.smoothedTargetPoint.lengthSqr() > 0.0D ? state.smoothedTargetPoint : getTargetAimPoint(lockedTarget, partialTick))
                : player.getEyePosition(partialTick).add(player.getViewVector(partialTick).scale(8.0D));
        FocusCameraPose leftPose = new FocusCameraPose(
                targetPoint,
                FocusClientConfig.cameraOffsetX(FocusClientConfig.Shoulder.LEFT),
                FocusClientConfig.cameraOffsetY(FocusClientConfig.Shoulder.LEFT),
                FocusClientConfig.cameraOffsetZ(FocusClientConfig.Shoulder.LEFT),
                (float) FocusClientConfig.cameraRotation(FocusClientConfig.Shoulder.LEFT));
        FocusCameraPose rightPose = new FocusCameraPose(
                targetPoint,
                FocusClientConfig.cameraOffsetX(FocusClientConfig.Shoulder.RIGHT),
                FocusClientConfig.cameraOffsetY(FocusClientConfig.Shoulder.RIGHT),
                FocusClientConfig.cameraOffsetZ(FocusClientConfig.Shoulder.RIGHT),
                (float) FocusClientConfig.cameraRotation(FocusClientConfig.Shoulder.RIGHT));
        latestOcclusionResult = occlusionEvaluator.evaluate(
                player,
                partialTick,
                targetPoint,
                leftPose,
                rightPose,
                getDisplayedShoulder(lockedTarget != null));

        float severity = latestOcclusionResult.playerBlocksCameraRay() ? 1.0F : 0.0F;
        if (!FocusClientConfig.playerTransparencyWhenTargetObscuredOnly() && latestOcclusionResult.targetVisibilityCompromised()) {
            severity = Math.max(severity, 0.6F);
        }
        float targetAlpha = 1.0F - severity * (1.0F - FocusClientConfig.playerTransparencyMinAlpha());
        float fadeSpeed = FocusClientConfig.playerTransparencyFadeSpeed();
        state.playerTransparencyAlpha = Mth.lerp(fadeSpeed, state.playerTransparencyAlpha, Mth.clamp(targetAlpha, 0.0F, 1.0F));
    }

    public void onRenderFrame(LocalPlayer player, LivingEntity target, float partialTick, float deltaTicks) {
        state.lastRenderDeltaTicks = deltaTicks;
        if (!state.smoothingInitialized) {
            initializeSmoothing(player, getTargetAimPoint(target, 1.0F));
        }
        updateSmoothedOrientation(player, target, partialTick, deltaTicks);
        applyRotationPolicy(player, target, partialTick, deltaTicks);
    }

    public void storeRenderDeltaTicks(float deltaTicks) {
        state.lastRenderDeltaTicks = deltaTicks;
    }

    public float getLastRenderDeltaTicks() {
        return state.lastRenderDeltaTicks;
    }

    public FocusCameraPose getActiveCameraPose(LocalPlayer player, @Nullable LivingEntity lockedTarget, float partialTick) {
        if (lockedTarget != null) {
            Vec3 targetPoint = state.smoothedTargetPoint.lengthSqr() > 0.0D
                    ? state.smoothedTargetPoint
                    : getTargetAimPoint(lockedTarget, partialTick);
            FocusCameraTargetContext context = new FocusCameraTargetContext(
                    player,
                    lockedTarget,
                    targetPoint,
                    partialTick,
                    state.lastRenderDeltaTicks,
                    true,
                    false,
                    getTargetSwapBlendToNormal() < 0.999F);
            return shoulderPolicy.resolveShoulderPose(context, state);
        }

        if (state.cameraEditorPreviewActive) {
            Vec3 eye = player.getEyePosition(partialTick);
            float previewYaw = player.getYRot() + state.freeLookYaw;
            float previewPitch = Mth.clamp(player.getXRot() + state.freeLookPitch, -89.0F, 89.0F);
            Vec3 forward = Vec3.directionFromRotation(previewPitch, previewYaw);
            Vec3 targetPoint = eye.add(forward.scale(8.0D));
            FocusCameraTargetContext context = new FocusCameraTargetContext(
                    player,
                    null,
                    targetPoint,
                    partialTick,
                    state.lastRenderDeltaTicks,
                    false,
                    true,
                    false);
            return shoulderPolicy.resolveShoulderPose(context, state);
        }

        return null;
    }

    public float getTargetSwapBlendToNormal() {
        if (state.targetSwapSmoothingTicks <= 0 || state.targetSwapSmoothingDurationTicks <= 0) {
            return 1.0F;
        }

        float blendToNormal = 1.0F - (float) state.targetSwapSmoothingTicks / (float) state.targetSwapSmoothingDurationTicks;
        blendToNormal = Mth.clamp(blendToNormal, 0.0F, 1.0F);
        return blendToNormal * blendToNormal * (3.0F - 2.0F * blendToNormal);
    }

    public float getTargetSwapCameraPositionFactor() {
        return Mth.lerp(getTargetSwapBlendToNormal(), TARGET_SWAP_CAMERA_POSITION_FACTOR_MIN, 1.0F);
    }

    public float getTargetSwapCameraRotationFactor() {
        return Mth.lerp(getTargetSwapBlendToNormal(), TARGET_SWAP_CAMERA_ROTATION_FACTOR_MIN, 1.0F);
    }

    public boolean isInitialLockCameraSnapActive() {
        return state.initialLockCameraSnapTicks > 0;
    }

    public boolean isCameraEditorPreviewActive() {
        return state.cameraEditorPreviewActive;
    }

    public void startCameraEditorPreview() {
        state.cameraEditorPreviewActive = true;
    }

    public void stopCameraEditorPreview() {
        state.cameraEditorPreviewActive = false;
    }

    public double getDynamicAutoCurrentBlend() {
        return state.dynamicAutoCurrentBlend;
    }

    public double getStaticSwapBlend() {
        return state.staticSwapBlend;
    }

    public FocusClientConfig.Shoulder getActiveShoulder() {
        return state.activeShoulder;
    }

    public FocusClientConfig.Shoulder getDisplayedShoulder(boolean lockOnActive) {
        if (lockOnActive && FocusClientConfig.cameraMode() == FocusClientConfig.CameraMode.DYNAMIC) {
            return defaultShoulderPolicy.dynamicDisplayedShoulder(state, state.dynamicAutoCurrentBlend);
        }
        return state.activeShoulder;
    }

    public void setActiveShoulder(FocusClientConfig.Shoulder shoulder) {
        if (shoulder == null) {
            return;
        }

        state.activeShoulder = shoulder;
        state.staticSwapSourceShoulder = shoulder;
        state.staticSwapBlend = 1.0D;
        state.dynamicAutoTargetBlend = 0.0D;
        state.dynamicAutoCurrentBlend = 0.0D;
        state.previousDynamicTargetOffset = Vec3.ZERO;
        state.dynamicSwapReferenceInitialized = false;
        state.dynamicManualShoulderOverrideTicks = 0;
    }

    public FocusClientConfig.Shoulder swapShoulder(@Nullable LocalPlayer player, boolean showMessage, boolean lockOnActive) {
        if (FocusClientConfig.cameraMode() == FocusClientConfig.CameraMode.DYNAMIC) {
            FocusClientConfig.Shoulder displayedShoulder = defaultShoulderPolicy.dynamicDisplayedShoulder(state, state.dynamicAutoCurrentBlend);
            FocusClientConfig.Shoulder desiredDisplayedShoulder = displayedShoulder.opposite();
            double preservedBlend = Mth.clamp(state.dynamicAutoCurrentBlend, 0.0D, 1.0D);
            if (desiredDisplayedShoulder != state.activeShoulder) {
                state.activeShoulder = desiredDisplayedShoulder;
                state.dynamicAutoCurrentBlend = 1.0D - preservedBlend;
            } else {
                state.dynamicAutoCurrentBlend = preservedBlend;
            }
            state.dynamicAutoTargetBlend = 0.0D;
            state.previousDynamicTargetOffset = Vec3.ZERO;
            state.dynamicSwapReferenceInitialized = false;
        } else {
            FocusClientConfig.Shoulder previousShoulder = state.activeShoulder;
            state.activeShoulder = state.activeShoulder.opposite();
            state.staticSwapSourceShoulder = previousShoulder;
            state.staticSwapBlend = 0.0D;
        }
        if (lockOnActive) {
            state.dynamicManualShoulderOverrideTicks = FocusClientConfig.dynamicShoulderManualOverrideCooldownTicks();
        }
        if (showMessage && player != null) {
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.translatable(
                            "message.focus.lock_on.shoulder_swapped",
                            state.activeShoulder.displayName()),
                    true);
        }
        return state.activeShoulder;
    }

    public double currentDetachedCameraDistance(boolean lockOnActive) {
        if (!lockOnActive) {
            return FocusClientConfig.cameraOffsetZ(state.activeShoulder);
        }

        if (FocusClientConfig.cameraMode() == FocusClientConfig.CameraMode.DYNAMIC) {
            FocusClientConfig.PerspectivePreset basePreset = FocusClientConfig.currentPreset(state.activeShoulder);
            // Use the actual opposite-shoulder preset when the user has configured custom values.
            FocusClientConfig.PerspectivePreset swappedPreset = FocusClientConfig.useCustomSwappedShoulderValues()
                    ? FocusClientConfig.currentPreset(state.activeShoulder.opposite())
                    : basePreset.mirroredForOppositeShoulder();
            double swapBlend = FocusCameraMath.applyBlendSmoothing(
                    state.dynamicAutoCurrentBlend,
                    FocusClientConfig.dynamicCameraSwapSmoothness(),
                    FocusClientConfig.MIN_DYNAMIC_CAMERA_SWAP_SMOOTHNESS,
                    FocusClientConfig.MAX_DYNAMIC_CAMERA_SWAP_SMOOTHNESS);
            return Mth.lerp(swapBlend, basePreset.offsetZ(), swappedPreset.offsetZ());
        }

        FocusClientConfig.PerspectivePreset sourcePreset = FocusClientConfig.currentPreset(state.staticSwapSourceShoulder);
        FocusClientConfig.PerspectivePreset targetPreset = FocusClientConfig.currentPreset(state.activeShoulder);
        double swapBlend = FocusCameraMath.applyBlendSmoothing(
                state.staticSwapBlend,
                FocusClientConfig.cameraSwapSmoothness(),
                FocusClientConfig.MIN_CAMERA_SWAP_SMOOTHNESS,
                FocusClientConfig.MAX_CAMERA_SWAP_SMOOTHNESS);
        return Mth.lerp(swapBlend, sourcePreset.offsetZ(), targetPreset.offsetZ());
    }

    public void addFreeLookDelta(float yawDelta, float pitchDelta) {
        state.freeLookYaw += yawDelta;
        state.freeLookPitch = Mth.clamp(state.freeLookPitch + pitchDelta, -85.0F, 85.0F);
    }

    public void smoothRecenterFreeLook() {
        state.freeLookRecentering = true;
    }

    public void setFreeLookInputActive(boolean active) {
        state.freeLookInputActive = active;
        if (active) {
            state.freeLookRecentering = false;
        } else if (FocusClientConfig.freeLookRecenterOnRelease()) {
            state.freeLookRecentering = true;
        }
    }

    public float playerTransparencyAlpha() {
        return state.playerTransparencyAlpha;
    }

    public void setRotationFollowPolicy(FocusCameraRotationFollowPolicy rotationFollowPolicy) {
        if (rotationFollowPolicy != null) {
            this.rotationFollowPolicy = rotationFollowPolicy;
        }
    }

    public void setTargetPointProvider(FocusCameraTargetPointProvider targetPointProvider) {
        if (targetPointProvider != null) {
            this.targetPointProvider = targetPointProvider;
        }
    }

    public Vec3 targetPointFor(LivingEntity target, float partialTick) {
        return targetPointProvider.resolveTargetPoint(target, partialTick);
    }

    public Vec3 getSmoothedLookDirection() {
        if (!state.smoothingInitialized) {
            return Vec3.ZERO;
        }
        return Vec3.directionFromRotation(state.smoothedLookPitch, state.smoothedLookYaw);
    }

    public void setShoulderPolicy(FocusCameraShoulderPolicy shoulderPolicy) {
        if (shoulderPolicy != null) {
            this.shoulderPolicy = shoulderPolicy;
        }
    }

    private void initializeSmoothing(LocalPlayer player, Vec3 targetPoint) {
        state.smoothedLookYaw = player.getYRot();
        state.smoothedLookPitch = player.getXRot();
        state.smoothedBodyYawOffset = 0.0F;
        state.smoothedTargetPoint = targetPoint;
        state.smoothingInitialized = true;
    }

    private void updateSmoothedOrientation(LocalPlayer player, LivingEntity target, float partialTick, float deltaTicks) {
        float targetPointResponsiveness = TARGET_POINT_RESPONSIVENESS;
        float lookYawResponsiveness = LOOK_RESPONSIVENESS_YAW;
        float lookPitchResponsiveness = LOOK_RESPONSIVENESS_PITCH;
        float lookYawMaxStep = LOOK_MAX_YAW_STEP_PER_TICK;
        float lookPitchMaxStep = LOOK_MAX_PITCH_STEP_PER_TICK;
        if (state.targetSwapSmoothingTicks > 0 && state.targetSwapSmoothingDurationTicks > 0) {
            float easedBlend = getTargetSwapBlendToNormal();
            targetPointResponsiveness = Mth.lerp(
                    easedBlend,
                    FocusClientConfig.targetSwapTargetPointResponsiveness(),
                    TARGET_POINT_RESPONSIVENESS);
            lookYawResponsiveness = Mth.lerp(
                    easedBlend,
                    FocusClientConfig.targetSwapLookYawResponsiveness(),
                    LOOK_RESPONSIVENESS_YAW);
            lookPitchResponsiveness = Mth.lerp(
                    easedBlend,
                    FocusClientConfig.targetSwapLookPitchResponsiveness(),
                    LOOK_RESPONSIVENESS_PITCH);
            lookYawMaxStep = Mth.lerp(
                    easedBlend,
                    FocusClientConfig.targetSwapLookMaxYawStepPerTick(),
                    LOOK_MAX_YAW_STEP_PER_TICK);
            lookPitchMaxStep = Mth.lerp(
                    easedBlend,
                    FocusClientConfig.targetSwapLookMaxPitchStepPerTick(),
                    LOOK_MAX_PITCH_STEP_PER_TICK);
        }

        Vec3 currentTargetPoint = getTargetAimPoint(target, partialTick);

        // Adaptive target point smoothing: accelerate when target deviates far to prevent drift
        double targetPointDeviation = currentTargetPoint.distanceTo(state.smoothedTargetPoint);
        if (targetPointDeviation > ADAPTIVE_SMOOTHING_THRESHOLD_DISTANCE) {
            float deviationScale = (float) (targetPointDeviation / ADAPTIVE_SMOOTHING_THRESHOLD_DISTANCE);
            targetPointResponsiveness *= deviationScale;
        }

        state.smoothedTargetPoint = FocusCameraMath.smoothVec(
                state.smoothedTargetPoint,
                currentTargetPoint,
                targetPointResponsiveness,
                deltaTicks);

        float targetYaw = FocusCameraMath.computeTargetYaw(player, state.smoothedTargetPoint, partialTick);
        float targetPitch = FocusCameraMath.computeTargetPitch(player, state.smoothedTargetPoint, partialTick);

        targetYaw += state.freeLookYaw;
        targetPitch = Mth.clamp(targetPitch + state.freeLookPitch, -90.0F, 90.0F);

        // Adaptive look smoothing: speed up when angular deviation is large to keep target on screen
        float yawDeviation = Math.abs(Mth.wrapDegrees(targetYaw - state.smoothedLookYaw));
        float pitchDeviation = Math.abs(targetPitch - state.smoothedLookPitch);
        float angularDeviation = Math.max(yawDeviation, pitchDeviation);
        if (angularDeviation > ADAPTIVE_SMOOTHING_THRESHOLD_DEGREES) {
            float deviationScale = angularDeviation / ADAPTIVE_SMOOTHING_THRESHOLD_DEGREES;
            lookYawResponsiveness *= deviationScale;
            lookPitchResponsiveness *= deviationScale;
            lookYawMaxStep *= deviationScale;
            lookPitchMaxStep *= deviationScale;
        }

        state.smoothedLookYaw = FocusCameraMath.smoothAngle(
                state.smoothedLookYaw, targetYaw, lookYawResponsiveness, lookYawMaxStep, deltaTicks);
        state.smoothedLookPitch = FocusCameraMath.smoothAngle(
                state.smoothedLookPitch, targetPitch, lookPitchResponsiveness, lookPitchMaxStep, deltaTicks);

        // Hard clamp: ensure target never drifts beyond maximum deviation from screen center
        float finalYawDev = Mth.wrapDegrees(state.smoothedLookYaw - targetYaw);
        float finalPitchDev = state.smoothedLookPitch - targetPitch;
        if (Math.abs(finalYawDev) > MAX_TARGET_ANGULAR_DEVIATION) {
            state.smoothedLookYaw = targetYaw + Math.signum(finalYawDev) * MAX_TARGET_ANGULAR_DEVIATION;
        }
        if (Math.abs(finalPitchDev) > MAX_TARGET_ANGULAR_DEVIATION) {
            state.smoothedLookPitch = targetPitch + Math.signum(finalPitchDev) * MAX_TARGET_ANGULAR_DEVIATION;
        }

        state.smoothedBodyYawOffset = FocusCameraMath.smoothValue(
                state.smoothedBodyYawOffset,
                FocusCameraMath.computeDesiredBodyYawOffset(player, BODY_FORWARD_DAMPING, BODY_MAX_STRAFE_OFFSET),
                BODY_TURN_RESPONSIVENESS,
                deltaTicks);
    }

    private void applyRotationPolicy(LocalPlayer player, LivingEntity target, float partialTick, float deltaTicks) {
        FocusCameraTargetContext context = new FocusCameraTargetContext(
                player,
                target,
                state.smoothedTargetPoint,
                partialTick,
                deltaTicks,
                true,
                false,
                getTargetSwapBlendToNormal() < 0.999F);
        rotationFollowPolicy.applyPlayerRotation(context, state, state.smoothedLookYaw, state.smoothedLookPitch);
    }

    private Vec3 getTargetAimPoint(LivingEntity target, float partialTick) {
        return targetPointProvider.resolveTargetPoint(target, partialTick);
    }
}
