package com.jvn.focus.client.camera;

import com.jvn.focus.client.FocusClientConfig;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

final class FocusDefaultCameraShoulderPolicy implements FocusCameraShoulderPolicy {
    private static final double DYNAMIC_NEAR_DISTANCE = 2.0D;
    private static final double DYNAMIC_FAR_DISTANCE = 12.0D;
    private static final double DYNAMIC_EXTRA_OFFSET_Z_NEAR = 1.2D;
    private static final double DYNAMIC_EXTRA_OFFSET_Y_NEAR = 0.35D;
    private static final double DYNAMIC_EXTRA_OFFSET_X_NEAR = 0.4D;
    private static final double DYNAMIC_SHOULDER_SWAP_MIN_ANGLE_RADIANS = Math.toRadians(2.0D);
    private static final double CONTINUITY_PENALTY = 0.08D;
    private final FocusCameraOcclusionEvaluator occlusionEvaluator = new FocusCameraOcclusionEvaluator();

    @Override
    public FocusCameraPose resolveShoulderPose(FocusCameraTargetContext context, FocusCameraState state) {
        if (FocusClientConfig.cameraMode() == FocusClientConfig.CameraMode.DYNAMIC) {
            return buildDynamicPose(context, state);
        }

        state.dynamicAutoTargetBlend = 0.0D;
        state.dynamicAutoCurrentBlend = 0.0D;
        state.previousDynamicTargetOffset = Vec3.ZERO;
        state.dynamicSwapReferenceInitialized = false;
        return buildStaticPose(context.targetPoint(), state);
    }

    void initializeDynamicAutoShoulderBlend(LocalPlayer player, LivingEntity target, FocusCameraState state, Vec3 targetPoint) {
        double initialBlend = 0.0D;
        state.previousDynamicTargetOffset = Vec3.ZERO;
        state.dynamicSwapReferenceInitialized = false;
        if (FocusClientConfig.cameraMode() != FocusClientConfig.CameraMode.DYNAMIC) {
            state.dynamicAutoTargetBlend = 0.0D;
            state.dynamicAutoCurrentBlend = 0.0D;
            return;
        }

        Vec3 toTargetFlat = targetPoint.subtract(player.position()).multiply(1.0D, 0.0D, 1.0D);
        if (toTargetFlat.lengthSqr() <= 1.0E-6D) {
            state.dynamicAutoTargetBlend = 0.0D;
            state.dynamicAutoCurrentBlend = 0.0D;
            return;
        }

        float yawRad = player.getYRot() * ((float) Math.PI / 180.0F);
        Vec3 right = new Vec3(Mth.cos(yawRad), 0.0D, Mth.sin(yawRad));
        double lateral = toTargetFlat.normalize().dot(right);
        if (lateral > 0.45D) {
            initialBlend = 1.0D;
        }
        state.dynamicAutoTargetBlend = initialBlend;
        state.dynamicAutoCurrentBlend = initialBlend;
    }

    FocusClientConfig.Shoulder dynamicDisplayedShoulder(FocusCameraState state, double blend) {
        return blend >= 0.5D ? state.activeShoulder.opposite() : state.activeShoulder;
    }

    private FocusCameraPose buildStaticPose(Vec3 targetPoint, FocusCameraState state) {
        FocusClientConfig.PerspectivePreset sourcePreset = FocusClientConfig.currentPreset(state.staticSwapSourceShoulder);
        FocusClientConfig.PerspectivePreset targetPreset = FocusClientConfig.currentPreset(state.activeShoulder);
        state.staticSwapBlend = FocusCameraMath.smoothTowardsTimeAdjusted(
                state.staticSwapBlend,
                1.0D,
                FocusClientConfig.cameraSwapSpeed(),
                state.lastRenderDeltaTicks,
                FocusClientConfig.MIN_CAMERA_SWAP_SPEED,
                FocusClientConfig.MAX_CAMERA_SWAP_SPEED);
        double swapBlend = FocusCameraMath.applyBlendSmoothing(
                state.staticSwapBlend,
                FocusClientConfig.cameraSwapSmoothness(),
                FocusClientConfig.MIN_CAMERA_SWAP_SMOOTHNESS,
                FocusClientConfig.MAX_CAMERA_SWAP_SMOOTHNESS);
        if (state.staticSwapBlend >= 0.999D) {
            state.staticSwapSourceShoulder = state.activeShoulder;
            state.staticSwapBlend = 1.0D;
        }
        return new FocusCameraPose(
                targetPoint,
                Mth.lerp(swapBlend, sourcePreset.offsetX(), targetPreset.offsetX()),
                Mth.lerp(swapBlend, sourcePreset.offsetY(), targetPreset.offsetY()),
                Mth.lerp(swapBlend, sourcePreset.offsetZ(), targetPreset.offsetZ()),
                (float) Mth.lerp(swapBlend, sourcePreset.rotation(), targetPreset.rotation()));
    }

    private FocusCameraPose buildDynamicPose(FocusCameraTargetContext context, FocusCameraState state) {
        LocalPlayer player = context.player();
        FocusClientConfig.PerspectivePreset basePreset = FocusClientConfig.currentPreset(state.activeShoulder);
        // Use the actual opposite-shoulder preset when the user has configured custom values;
        // otherwise mirror the active shoulder preset for symmetry.
        FocusClientConfig.PerspectivePreset swappedPreset = FocusClientConfig.useCustomSwappedShoulderValues()
                ? FocusClientConfig.currentPreset(state.activeShoulder.opposite())
                : basePreset.mirroredForOppositeShoulder();
        double targetDistance = context.lockedTarget() != null ? player.distanceTo(context.lockedTarget()) : player.getEyePosition().distanceTo(context.targetPoint());
        double nearFactor = 1.0D - Mth.clamp(
                (targetDistance - DYNAMIC_NEAR_DISTANCE) / (DYNAMIC_FAR_DISTANCE - DYNAMIC_NEAR_DISTANCE),
                0.0D,
                1.0D);
        if (FocusClientConfig.dynamicShoulderAutoSwapEnabled()) {
            updateDynamicShoulderTargetBlend(context, state, basePreset, swappedPreset, nearFactor);
        } else {
            state.dynamicAutoTargetBlend = 0.0D;
        }

        if (state.initialLockCameraSnapTicks > 0) {
            Vec3 currentOffset = context.targetPoint().subtract(player.position()).multiply(1.0D, 0.0D, 1.0D);
            if (currentOffset.lengthSqr() > 1.0E-6D) {
                state.previousDynamicTargetOffset = currentOffset;
                state.dynamicSwapReferenceInitialized = true;
            }
            state.dynamicAutoCurrentBlend = state.dynamicAutoTargetBlend;
        } else {
            if (!FocusClientConfig.dynamicShoulderAutoSwapEnabled()) {
                updateDynamicAutoTargetBlend(player, context.targetPoint(), state);
            }
            state.dynamicAutoCurrentBlend = FocusCameraMath.smoothTowardsTimeAdjusted(
                    state.dynamicAutoCurrentBlend,
                    state.dynamicAutoTargetBlend,
                    FocusClientConfig.dynamicCameraSwapSpeed(),
                    state.lastRenderDeltaTicks,
                    FocusClientConfig.MIN_DYNAMIC_CAMERA_SWAP_SPEED,
                    FocusClientConfig.MAX_DYNAMIC_CAMERA_SWAP_SPEED);
        }

        double swapBlend = FocusCameraMath.applyBlendSmoothing(
                state.dynamicAutoCurrentBlend,
                FocusClientConfig.dynamicCameraSwapSmoothness(),
                FocusClientConfig.MIN_DYNAMIC_CAMERA_SWAP_SMOOTHNESS,
                FocusClientConfig.MAX_DYNAMIC_CAMERA_SWAP_SMOOTHNESS);
        double presetOffsetX = Mth.lerp(swapBlend, basePreset.offsetX(), swappedPreset.offsetX());
        double presetOffsetY = Mth.lerp(swapBlend, basePreset.offsetY(), swappedPreset.offsetY());
        double presetOffsetZ = Mth.lerp(swapBlend, basePreset.offsetZ(), swappedPreset.offsetZ());
        double presetRotation = Mth.lerp(swapBlend, basePreset.rotation(), swappedPreset.rotation());

        double sourceSign = state.activeShoulder == FocusClientConfig.Shoulder.LEFT ? -1.0D : 1.0D;
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

        return new FocusCameraPose(
                context.targetPoint(),
                offsetX,
                offsetY,
                offsetZ,
                (float) presetRotation);
    }

    private void updateDynamicShoulderTargetBlend(
            FocusCameraTargetContext context,
            FocusCameraState state,
            FocusClientConfig.PerspectivePreset basePreset,
            FocusClientConfig.PerspectivePreset swappedPreset,
            double nearFactor) {
        if (state.dynamicManualShoulderOverrideTicks > 0) {
            return;
        }

        FocusCameraPose leftCandidatePose = dynamicCandidatePose(context, state, basePreset, swappedPreset, nearFactor, FocusClientConfig.Shoulder.LEFT);
        FocusCameraPose rightCandidatePose = dynamicCandidatePose(context, state, basePreset, swappedPreset, nearFactor, FocusClientConfig.Shoulder.RIGHT);
        FocusCameraOcclusionResult occlusionResult = occlusionEvaluator.evaluate(
                context.player(),
                context.partialTick(),
                context.targetPoint(),
                leftCandidatePose,
                rightCandidatePose,
                state.activeShoulder);

        double lateral = computeTargetLateral(context.player(), context.targetPoint());
        FocusClientConfig.Shoulder currentDisplayed = dynamicDisplayedShoulder(state, state.dynamicAutoCurrentBlend);
        FocusCameraCandidateEvaluation leftCandidate = candidateEvaluation(
                context,
                FocusClientConfig.Shoulder.LEFT,
                leftCandidatePose,
                occlusionResult.leftShoulderVisibilityScore(),
                lateral,
                currentDisplayed);
        FocusCameraCandidateEvaluation rightCandidate = candidateEvaluation(
                context,
                FocusClientConfig.Shoulder.RIGHT,
                rightCandidatePose,
                occlusionResult.rightShoulderVisibilityScore(),
                lateral,
                currentDisplayed);

        double threshold = FocusClientConfig.dynamicShoulderSwitchThreshold();
        if (leftCandidate.totalScore() > rightCandidate.totalScore() + threshold) {
            state.dynamicAutoTargetBlend = state.activeShoulder == FocusClientConfig.Shoulder.LEFT ? 0.0D : 1.0D;
        } else if (rightCandidate.totalScore() > leftCandidate.totalScore() + threshold) {
            state.dynamicAutoTargetBlend = state.activeShoulder == FocusClientConfig.Shoulder.RIGHT ? 0.0D : 1.0D;
        }
    }

    private FocusCameraPose dynamicCandidatePose(
            FocusCameraTargetContext context,
            FocusCameraState state,
            FocusClientConfig.PerspectivePreset basePreset,
            FocusClientConfig.PerspectivePreset swappedPreset,
            double nearFactor,
            FocusClientConfig.Shoulder displayedShoulder) {
        double blend = displayedShoulder == state.activeShoulder ? 0.0D : 1.0D;
        double presetOffsetX = Mth.lerp(blend, basePreset.offsetX(), swappedPreset.offsetX());
        double presetOffsetY = Mth.lerp(blend, basePreset.offsetY(), swappedPreset.offsetY());
        double presetOffsetZ = Mth.lerp(blend, basePreset.offsetZ(), swappedPreset.offsetZ());
        double presetRotation = Mth.lerp(blend, basePreset.rotation(), swappedPreset.rotation());
        double sourceSign = state.activeShoulder == FocusClientConfig.Shoulder.LEFT ? -1.0D : 1.0D;
        double targetSign = -sourceSign;
        double offsetXSign = Mth.lerp(blend, sourceSign, targetSign);
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
        return new FocusCameraPose(context.targetPoint(), offsetX, offsetY, offsetZ, (float) presetRotation);
    }

    private FocusCameraCandidateEvaluation candidateEvaluation(
            FocusCameraTargetContext context,
            FocusClientConfig.Shoulder shoulder,
            FocusCameraPose pose,
            double visibilityScore,
            double targetLateral,
            FocusClientConfig.Shoulder currentDisplayedShoulder) {
        double screenPlacementScore = shoulder == FocusClientConfig.Shoulder.RIGHT
                ? (targetLateral + 1.0D) * 0.5D
                : (1.0D - targetLateral) * 0.5D;
        double continuityPenalty = shoulder == currentDisplayedShoulder ? 0.0D : CONTINUITY_PENALTY;
        double totalScore = (visibilityScore * FocusClientConfig.dynamicShoulderVisibilityWeight())
                + (screenPlacementScore * FocusClientConfig.dynamicShoulderScreenPlacementWeight())
                - continuityPenalty;
        return new FocusCameraCandidateEvaluation(
                shoulder,
                FocusCameraOcclusionEvaluator.computeCameraPosition(context.player().getEyePosition(context.partialTick()), pose.targetPoint(), pose),
                visibilityScore,
                screenPlacementScore,
                continuityPenalty,
                totalScore);
    }

    private double computeTargetLateral(LocalPlayer player, Vec3 targetPoint) {
        Vec3 toTargetFlat = targetPoint.subtract(player.position()).multiply(1.0D, 0.0D, 1.0D);
        if (toTargetFlat.lengthSqr() <= 1.0E-6D) {
            return 0.0D;
        }
        float yawRad = player.getYRot() * ((float) Math.PI / 180.0F);
        Vec3 right = new Vec3(Mth.cos(yawRad), 0.0D, Mth.sin(yawRad));
        return Mth.clamp(toTargetFlat.normalize().dot(right), -1.0D, 1.0D);
    }

    private void updateDynamicAutoTargetBlend(LocalPlayer player, Vec3 targetPoint, FocusCameraState state) {
        Vec3 currentOffset = targetPoint.subtract(player.position()).multiply(1.0D, 0.0D, 1.0D);
        if (currentOffset.lengthSqr() < 1.0E-6D) {
            return;
        }
        if (state.dynamicSwapReferenceInitialized) {
            double cross = state.previousDynamicTargetOffset.x * currentOffset.z - state.previousDynamicTargetOffset.z * currentOffset.x;
            double dot = state.previousDynamicTargetOffset.x * currentOffset.x + state.previousDynamicTargetOffset.z * currentOffset.z;
            double deltaAngle = Math.atan2(cross, dot);

            FocusClientConfig.Shoulder displayedShoulder = dynamicDisplayedShoulder(state, state.dynamicAutoCurrentBlend);
            if (displayedShoulder == state.activeShoulder && deltaAngle > DYNAMIC_SHOULDER_SWAP_MIN_ANGLE_RADIANS) {
                state.dynamicAutoTargetBlend = 1.0D;
            } else if (displayedShoulder != state.activeShoulder && deltaAngle < -DYNAMIC_SHOULDER_SWAP_MIN_ANGLE_RADIANS) {
                state.dynamicAutoTargetBlend = 0.0D;
            }
        }
        state.previousDynamicTargetOffset = currentOffset;
        state.dynamicSwapReferenceInitialized = true;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
