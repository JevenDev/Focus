package com.jvn.focus.client.camera;

import com.jvn.focus.client.FocusClientConfig;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class FocusShoulderSurfingCameraSystem {
    private static final double CAMERA_DYNAMIC_OFFSET_STEP = 0.03125D;
    private static final double CAMERA_COLLISION_PROBE_RADIUS = 0.15D;
    private static final double CAMERA_EPSILON = 1.0E-6D;
    private static final FocusShoulderSurfingCameraSystem INSTANCE = new FocusShoulderSurfingCameraSystem();

    private Vec3 smoothedOffset = Vec3.ZERO;
    private boolean hasOffsetState;
    private double maxCameraDistance;
    private double maxCameraDistancePrevious;
    private float smoothedYaw;
    private float smoothedPitch;
    private boolean hasSmoothedRotation;
    private float smoothedOrbitYaw;
    private float smoothedOrbitPitch;
    private boolean hasSmoothedOrbitRotation;
    private boolean wasActive;

    // Entry transition state: blends from the vanilla/SS camera to the Focus lock-on camera.
    private Vec3 transitionStartOffset = Vec3.ZERO;
    private float transitionStartYaw;
    private float transitionStartPitch;
    private float transitionBlend = 1.0F;
    private boolean inTransition;

    // Return transition state: blends from the Focus lock-on camera back to vanilla/SS on deactivation.
    private Vec3 returnStartOffset = Vec3.ZERO;
    private float returnStartYaw;
    private float returnStartPitch;
    private float returnBlend = 1.0F;
    private boolean inReturnTransition;

    // Last output pose, used as the starting point for the return transition.
    private Vec3 lastOutputPosition = Vec3.ZERO;
    private float lastOutputYaw;
    private float lastOutputPitch;

    private FocusShoulderSurfingCameraSystem() {}

    public static FocusShoulderSurfingCameraSystem getInstance() {
        return INSTANCE;
    }

    public void reset() {
        smoothedOffset = Vec3.ZERO;
        hasOffsetState = false;
        maxCameraDistance = 0.0D;
        maxCameraDistancePrevious = 0.0D;
        hasSmoothedRotation = false;
        hasSmoothedOrbitRotation = false;
        wasActive = false;
        transitionStartOffset = Vec3.ZERO;
        transitionStartYaw = 0.0F;
        transitionStartPitch = 0.0F;
        transitionBlend = 1.0F;
        inTransition = false;
        returnStartOffset = Vec3.ZERO;
        returnStartYaw = 0.0F;
        returnStartPitch = 0.0F;
        returnBlend = 1.0F;
        inReturnTransition = false;
        lastOutputPosition = Vec3.ZERO;
        lastOutputYaw = 0.0F;
        lastOutputPitch = 0.0F;
    }

    public boolean wasActive() {
        return wasActive;
    }

    public boolean isInReturnTransition() {
        return inReturnTransition;
    }

    /**
     * Begins a smooth return transition from the last Focus camera pose back to vanilla.
     * Resets the main lock-on state but preserves the return transition.
     */
    public void beginReturnTransition(Vec3 pivotPoint) {
        if (wasActive && FocusClientConfig.smoothCameraTransition()) {
            returnStartOffset = lastOutputPosition.subtract(pivotPoint);
            returnStartYaw = lastOutputYaw;
            returnStartPitch = lastOutputPitch;
            returnBlend = 0.0F;
            inReturnTransition = true;
        }
        // Reset the main lock-on state.
        smoothedOffset = Vec3.ZERO;
        hasOffsetState = false;
        maxCameraDistance = 0.0D;
        maxCameraDistancePrevious = 0.0D;
        hasSmoothedRotation = false;
        hasSmoothedOrbitRotation = false;
        wasActive = false;
        transitionStartOffset = Vec3.ZERO;
        transitionStartYaw = 0.0F;
        transitionStartPitch = 0.0F;
        transitionBlend = 1.0F;
        inTransition = false;
    }

    /**
     * Advances the return transition, blending from the captured Focus pose toward the vanilla camera.
     * Returns the blended pose, or the vanilla pose once the transition completes.
     */
    public CameraPose updateReturnTransition(Vec3 vanillaPos, float vanillaYaw, float vanillaPitch, Vec3 pivotPoint, float deltaTicks) {
        double transitionSpeed = Mth.clamp(
                FocusClientConfig.cameraTransitionSpeed(),
                FocusClientConfig.MIN_CAMERA_TRANSITION_SPEED,
                FocusClientConfig.MAX_CAMERA_TRANSITION_SPEED);
        float alpha = 1.0F - (float) Math.pow(1.0D - transitionSpeed, deltaTicks);
        returnBlend = returnBlend + (1.0F - returnBlend) * alpha;

        if (returnBlend >= 0.999F) {
            returnBlend = 1.0F;
            inReturnTransition = false;
            return new CameraPose(vanillaPos, vanillaYaw, Mth.clamp(vanillaPitch, -90.0F, 90.0F));
        }

        float t = returnBlend;
        float easedBlend = t * t * (3.0F - 2.0F * t);

        Vec3 vanillaOffset = vanillaPos.subtract(pivotPoint);
        Vec3 blendedOffset = returnStartOffset.lerp(vanillaOffset, easedBlend);
        Vec3 blendedPosition = pivotPoint.add(blendedOffset);
        float blendedYaw = Mth.rotLerp(easedBlend, returnStartYaw, vanillaYaw);
        float blendedPitch = Mth.lerp(easedBlend, returnStartPitch, vanillaPitch);

        return new CameraPose(blendedPosition, blendedYaw, Mth.clamp(blendedPitch, -90.0F, 90.0F));
    }

    public CameraPose update(
            BlockGetter level,
            Entity entity,
            Vec3 pivotPoint,
            FocusCameraPose lockData,
            float partialTick,
            float deltaTicks,
            float fallbackYaw,
            float fallbackPitch,
            double positionLerpAlpha,
            float rotationLerpAlpha,
            boolean forceSnap,
            float targetSwapBlendToNormal,
            Vec3 vanillaCameraPos,
            float vanillaCameraYaw,
            float vanillaCameraPitch) {
        float swapBlendToNormal = Mth.clamp(targetSwapBlendToNormal, 0.0F, 1.0F);
        boolean targetSwapActive = swapBlendToNormal < 0.999F;
        // Convert per-tick lerp factors to FPS-independent equivalents.
        double adjustedPositionAlpha = 1.0D - Math.pow(1.0D - positionLerpAlpha, deltaTicks);
        float adjustedRotationAlpha = 1.0F - (float) Math.pow(1.0F - rotationLerpAlpha, deltaTicks);

        FocusCameraBasisUtil.Rotation pivotRotation = FocusCameraBasisUtil.rotationToTarget(pivotPoint, lockData.targetPoint(), fallbackYaw, fallbackPitch);
        float targetOrbitYaw = pivotRotation.yaw() + lockData.rotationDegrees();
        float targetOrbitPitch = pivotRotation.pitch();
        boolean snapNow = forceSnap || !wasActive || !hasOffsetState;
        boolean previouslyActive = wasActive;

        // Cancel any ongoing return transition since lock-on is active again.
        if (inReturnTransition) {
            inReturnTransition = false;
            returnBlend = 1.0F;
        }
        if (snapNow || !hasSmoothedOrbitRotation) {
            smoothedOrbitYaw = targetOrbitYaw;
            smoothedOrbitPitch = targetOrbitPitch;
            hasSmoothedOrbitRotation = true;
        } else {
            float orbitLerpAlpha = Math.max(adjustedRotationAlpha, 0.025F);
            // Adaptive orbit smoothing: catch up faster when deviation is large.
            // Blend this back in over the swap window to avoid a late transition snap.
            float orbitDevYaw = Math.abs(Mth.wrapDegrees(smoothedOrbitYaw - targetOrbitYaw));
            float orbitDevPitch = Math.abs(smoothedOrbitPitch - targetOrbitPitch);
            float orbitDevMax = Math.max(orbitDevYaw, orbitDevPitch);
            if (orbitDevMax > 12.0F) {
                float catchup = Mth.clamp(orbitDevMax / 12.0F, 1.0F, 5.0F);
                float blendedCatchup = Mth.lerp(swapBlendToNormal, 1.0F, catchup);
                orbitLerpAlpha = 1.0F - (float) Math.pow(1.0F - orbitLerpAlpha, blendedCatchup);
            }
            smoothedOrbitYaw = Mth.rotLerp(orbitLerpAlpha, smoothedOrbitYaw, targetOrbitYaw);
            smoothedOrbitPitch = Mth.lerp(orbitLerpAlpha, smoothedOrbitPitch, targetOrbitPitch);
        }
        FocusCameraBasisUtil.CameraBasis orbitBasis = FocusCameraBasisUtil.basisFromRotation(smoothedOrbitYaw, smoothedOrbitPitch);

        Vec3 desiredOffset = new Vec3(lockData.offsetX(), lockData.offsetY(), lockData.offsetZ());
        if (FocusClientConfig.dynamicallyAdjustOffsets()) {
            desiredOffset = applyDynamicOffsetCollision(level, entity, pivotPoint, orbitBasis, desiredOffset);
        }

        if (snapNow) {
            smoothedOffset = desiredOffset;
            hasOffsetState = true;
            maxCameraDistance = desiredOffset.length();
            maxCameraDistancePrevious = maxCameraDistance;
        } else {
            smoothedOffset = smoothedOffset.lerp(desiredOffset, adjustedPositionAlpha);
            maxCameraDistancePrevious = maxCameraDistance;
            maxCameraDistance += (smoothedOffset.length() - maxCameraDistance) * adjustedPositionAlpha;
        }

        double maxDistance = maxZoomDistance(level, entity, pivotPoint, orbitBasis, smoothedOrbitYaw, smoothedOrbitPitch, smoothedOffset);
        if (targetSwapActive) {
            // Avoid inheriting stale zoom state from the previous target during swap transitions.
            maxCameraDistance = maxDistance;
            maxCameraDistancePrevious = maxDistance;
        } else {
            if (maxDistance < maxCameraDistance) {
                maxCameraDistance = maxDistance;
            }
        }
        double normalDistanceLerp = Mth.lerp(partialTick, maxCameraDistancePrevious, maxCameraDistance);
        double maxDistanceLerped = Mth.lerp(swapBlendToNormal, maxDistance, normalDistanceLerp);
        double resolvedDistance = Math.min(maxDistance, maxDistanceLerped);
        Vec3 renderOffset = smoothedOffset.lengthSqr() < CAMERA_EPSILON ? Vec3.ZERO : smoothedOffset.normalize().scale(resolvedDistance);

        Vec3 cameraPosition = pivotPoint.add(toWorldOffset(orbitBasis, renderOffset));
        FocusCameraBasisUtil.Rotation desiredRotation = FocusCameraBasisUtil.rotationToTarget(cameraPosition, lockData.targetPoint(), pivotRotation.yaw(), pivotRotation.pitch());
        if (snapNow || !hasSmoothedRotation) {
            smoothedYaw = desiredRotation.yaw();
            smoothedPitch = desiredRotation.pitch();
            hasSmoothedRotation = true;
        } else {
            // Adaptive rotation: accelerate when the target deviates far from screen center.
            // Blend this back in over the swap window to prevent abrupt snapping.
            float effectiveAlpha = adjustedRotationAlpha;
            float rotDevYaw = Math.abs(Mth.wrapDegrees(smoothedYaw - desiredRotation.yaw()));
            float rotDevPitch = Math.abs(smoothedPitch - desiredRotation.pitch());
            float rotDevMax = Math.max(rotDevYaw, rotDevPitch);
            if (rotDevMax > 12.0F) {
                float catchup = Mth.clamp(rotDevMax / 12.0F, 1.0F, 5.0F);
                float blendedCatchup = Mth.lerp(swapBlendToNormal, 1.0F, catchup);
                effectiveAlpha = 1.0F - (float) Math.pow(1.0F - adjustedRotationAlpha, blendedCatchup);
            }
            smoothedYaw = Mth.rotLerp(effectiveAlpha, smoothedYaw, desiredRotation.yaw());
            smoothedPitch = Mth.lerp(effectiveAlpha, smoothedPitch, desiredRotation.pitch());
            // Hard clamp: blend from a permissive limit back to the normal 40 degrees.
            float maxAngularDeviation = Mth.lerp(swapBlendToNormal, 180.0F, 40.0F);
            float clampedYawDev = Mth.wrapDegrees(smoothedYaw - desiredRotation.yaw());
            float clampedPitchDev = smoothedPitch - desiredRotation.pitch();
            if (Math.abs(clampedYawDev) > maxAngularDeviation) {
                smoothedYaw = desiredRotation.yaw() + Math.signum(clampedYawDev) * maxAngularDeviation;
            }
            if (Math.abs(clampedPitchDev) > maxAngularDeviation) {
                smoothedPitch = desiredRotation.pitch() + Math.signum(clampedPitchDev) * maxAngularDeviation;
            }
        }

        wasActive = true;

        Vec3 focusPosition = cameraPosition;
        float focusYaw = smoothedYaw;
        float focusPitch = Mth.clamp(smoothedPitch, -90.0F, 90.0F);

        boolean smoothTransition = FocusClientConfig.smoothCameraTransition();
        if (smoothTransition && snapNow && !previouslyActive) {
            // First frame of lock-on with smooth transition: begin blending from the vanilla camera.
            transitionStartOffset = vanillaCameraPos.subtract(pivotPoint);
            transitionStartYaw = vanillaCameraYaw;
            transitionStartPitch = vanillaCameraPitch;
            transitionBlend = 0.0F;
            inTransition = true;
        }

        if (inTransition) {
            double transitionSpeed = Mth.clamp(
                    FocusClientConfig.cameraTransitionSpeed(),
                    FocusClientConfig.MIN_CAMERA_TRANSITION_SPEED,
                    FocusClientConfig.MAX_CAMERA_TRANSITION_SPEED);
            float alpha = 1.0F - (float) Math.pow(1.0D - transitionSpeed, deltaTicks);
            transitionBlend = transitionBlend + (1.0F - transitionBlend) * alpha;

            if (transitionBlend >= 0.999F) {
                transitionBlend = 1.0F;
                inTransition = false;
            }

            // Smoothstep easing for natural acceleration and deceleration.
            float t = transitionBlend;
            float easedBlend = t * t * (3.0F - 2.0F * t);

            Vec3 focusOffset = focusPosition.subtract(pivotPoint);
            Vec3 blendedOffset = transitionStartOffset.lerp(focusOffset, easedBlend);
            Vec3 blendedPosition = pivotPoint.add(blendedOffset);
            float blendedYaw = Mth.rotLerp(easedBlend, transitionStartYaw, focusYaw);
            float blendedPitch = Mth.lerp(easedBlend, transitionStartPitch, focusPitch);

            return storeLastOutput(new CameraPose(blendedPosition, blendedYaw, Mth.clamp(blendedPitch, -90.0F, 90.0F)));
        }

        return storeLastOutput(new CameraPose(focusPosition, focusYaw, focusPitch));
    }

    private CameraPose storeLastOutput(CameraPose pose) {
        lastOutputPosition = pose.position();
        lastOutputYaw = pose.yaw();
        lastOutputPitch = pose.pitch();
        return pose;
    }

    private static Vec3 applyDynamicOffsetCollision(BlockGetter level, Entity entity, Vec3 eyePosition, FocusCameraBasisUtil.CameraBasis basis, Vec3 desiredOffset) {
        double offsetZAbs = Math.abs(desiredOffset.z());
        if (offsetZAbs < CAMERA_EPSILON) {
            return desiredOffset;
        }

        Vec3 worldXYOffset = basis.up().scale(desiredOffset.y()).add(basis.right().scale(desiredOffset.x()));
        Vec3 worldOffset = worldXYOffset.add(basis.look().scale(-desiredOffset.z()));

        double targetX = Math.abs(desiredOffset.x());
        double targetY = Math.abs(desiredOffset.y());
        double offsetXAbs = targetX;
        double offsetYAbs = targetY;
        double clearance = entity.getBbWidth() / 3.0D;

        for (double dz = 0.0D; dz <= offsetZAbs; dz += CAMERA_DYNAMIC_OFFSET_STEP) {
            double scale = dz / offsetZAbs;
            Vec3 rayFrom = eyePosition.add(worldOffset.scale(scale));
            Vec3 rayTo = eyePosition.add(worldXYOffset).add(basis.look().scale(-dz));
            HitResult hitResult = level.clip(new ClipContext(rayFrom, rayTo, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, entity));
            if (hitResult.getType() != HitResult.Type.MISS) {
                double distance = hitResult.getLocation().distanceTo(rayFrom);
                double targetXCandidate = Math.max(distance + offsetXAbs * scale - clearance, 0.0D);
                double targetYCandidate = Math.max(distance + offsetYAbs * scale - clearance, 0.0D);
                targetX = Math.min(targetX, targetXCandidate);
                targetY = Math.min(targetY, targetYCandidate);
            }
        }

        return new Vec3(
                Math.signum(desiredOffset.x()) * targetX,
                Math.signum(desiredOffset.y()) * targetY,
                desiredOffset.z());
    }

    private static double maxZoomDistance(
            BlockGetter level,
            Entity entity,
            Vec3 eyePosition,
            FocusCameraBasisUtil.CameraBasis basis,
            float yaw,
            float pitch,
            Vec3 cameraOffset) {
        double distance = cameraOffset.length();
        if (distance < CAMERA_EPSILON) {
            return 0.0D;
        }

        Vec3 worldOffset = toWorldOffset(basis, cameraOffset);
        float probeStartRadius = Mth.clamp(entity.getBbWidth() / 2.0F / Mth.sqrt(2.0F), 0.0F, (float) CAMERA_COLLISION_PROBE_RADIUS);
        for (int i = 0; i < 8; i++) {
            Vec3 sample = new Vec3(i & 1, i >> 1 & 1, i >> 2 & 1).scale(2.0D).subtract(1.0D, 1.0D, 1.0D);
            Vec3 fromOffset = sample.scale(probeStartRadius)
                    .xRot(-pitch * Mth.DEG_TO_RAD)
                    .yRot(-yaw * Mth.DEG_TO_RAD);
            Vec3 toOffset = sample.scale(CAMERA_COLLISION_PROBE_RADIUS)
                    .xRot(-pitch * Mth.DEG_TO_RAD)
                    .yRot(-yaw * Mth.DEG_TO_RAD);

            Vec3 rayFrom = eyePosition.add(fromOffset);
            Vec3 rayTo = eyePosition.add(toOffset).add(worldOffset);
            HitResult hitResult = level.clip(new ClipContext(rayFrom, rayTo, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, entity));
            if (hitResult.getType() != HitResult.Type.MISS) {
                distance = Math.min(distance, hitResult.getLocation().distanceTo(eyePosition));
            }
        }

        return distance;
    }

    private static Vec3 toWorldOffset(FocusCameraBasisUtil.CameraBasis basis, Vec3 localOffset) {
        return basis.up().scale(localOffset.y())
                .add(basis.right().scale(localOffset.x()))
                .add(basis.look().scale(-localOffset.z()));
    }

    public record CameraPose(Vec3 position, float yaw, float pitch) {}
}
