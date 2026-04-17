package com.jvn.focus.client.camera;

import com.jvn.focus.client.FocusClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;

final class FocusDefaultCameraRotationFollowPolicy implements FocusCameraRotationFollowPolicy {
    private static final float TARGET_SWAP_HEAD_FOLLOW_BONUS = 0.25F;
    private static final float CLOSE_RANGE_BODY_SMOOTHING_FACTOR = 0.25F;
    private static final float FIRST_PERSON_MIN_TARGET_SWAP_FOLLOW = 0.72F;
    private static final float CLOSE_RANGE_TURN_MAX_YAW_STEP_NEAR = 11.0F;
    private static final float CLOSE_RANGE_TURN_MAX_YAW_STEP_FAR = 18.0F;

    @Override
    public void applyPlayerRotation(FocusCameraTargetContext context, FocusCameraState state, float desiredYaw, float desiredPitch) {
        LocalPlayer player = context.player();
        boolean followRotations = FocusClientConfig.followPlayerRotations();
        float clampedPitch = Mth.clamp(desiredPitch, -90.0F, 90.0F);
        if (!followRotations) {
            if (FocusClientConfig.fullBodyFollowEnabled()) {
                float previousBodyYaw = state.smoothedBodyYaw;
                float bodyYaw = Mth.rotLerp(
                        FocusClientConfig.cameraBodyFollowResponsiveness(),
                        state.smoothedBodyYaw,
                        player.getYHeadRot());
                state.previousSmoothedBodyYaw = previousBodyYaw;
                state.smoothedBodyYaw = bodyYaw;
                player.yBodyRotO = previousBodyYaw;
                player.setYBodyRot(bodyYaw);
            }
            return;
        }

        float targetSwapBlend = currentTargetSwapBlendToNormal(state);
        float swapFollowAlpha = Mth.lerp(targetSwapBlend, FocusClientConfig.targetSwapPlayerLookFollow(), 1.0F);
        if (Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
            swapFollowAlpha = Math.max(swapFollowAlpha, FIRST_PERSON_MIN_TARGET_SWAP_FOLLOW);
        }
        applyCoupledFollow(context, player, state, desiredYaw, clampedPitch, swapFollowAlpha);
    }

    private void applyCoupledFollow(
            FocusCameraTargetContext context,
            LocalPlayer player,
            FocusCameraState state,
            float desiredYaw,
            float desiredPitch,
            float swapFollowAlpha) {
        float yaw = Mth.rotLerp(swapFollowAlpha, player.getYRot(), desiredYaw);
        if (!Minecraft.getInstance().options.getCameraType().isFirstPerson()
                && FocusClientConfig.experimentalPersistentWalkthroughBackwardsWalking()
                && state.closeRangeHeadingLocked && state.closeRangeProximityFactor < 1.0F) {
            float proximity = Mth.clamp(state.closeRangeProximityFactor, 0.0F, 1.0F);
            float maxYawStepPerTick = Mth.lerp(proximity, CLOSE_RANGE_TURN_MAX_YAW_STEP_NEAR, CLOSE_RANGE_TURN_MAX_YAW_STEP_FAR);
            float maxYawStep = maxYawStepPerTick * Math.max(0.01F, context.deltaTicks());
            float yawDelta = Mth.wrapDegrees(yaw - player.getYRot());
            yaw = player.getYRot() + Mth.clamp(yawDelta, -maxYawStep, maxYawStep);
        }
        float pitch = Mth.lerp(swapFollowAlpha, player.getXRot(), desiredPitch);
        float headFollowAlpha = Mth.clamp(swapFollowAlpha + TARGET_SWAP_HEAD_FOLLOW_BONUS, 0.0F, 1.0F);
        float headYaw = Mth.rotLerp(headFollowAlpha, state.smoothedHeadYaw, desiredYaw);
        applyPlayerRotation(player, state, yaw, pitch, headYaw);
    }

    private void applyPlayerRotation(LocalPlayer player, FocusCameraState state, float yaw, float pitch, float headYaw) {
        // Heading lock state is managed in LockOnHandler.onMovementInput using the raw
        // (pre-rotation) input, so we don't read player.input here.

        float clampedPitch = Mth.clamp(pitch, -90.0F, 90.0F);
        float previousHeadYaw = state.smoothedHeadYaw;
        state.previousSmoothedHeadYaw = previousHeadYaw;
        state.smoothedHeadYaw = headYaw;
        player.yRotO = yaw;
        player.xRotO = clampedPitch;
        player.yHeadRotO = previousHeadYaw;
        player.setYRot(yaw);
        player.setXRot(clampedPitch);
        player.setYHeadRot(headYaw);
        if (FocusClientConfig.fullBodyFollowEnabled()) {
            float desiredBodyYaw = yaw + state.smoothedBodyYawOffset;
            float bodyResponsiveness = FocusClientConfig.cameraBodyFollowResponsiveness();
            if (state.closeRangeProximityFactor < 1.0F) {
                bodyResponsiveness *= CLOSE_RANGE_BODY_SMOOTHING_FACTOR
                        + (1.0F - CLOSE_RANGE_BODY_SMOOTHING_FACTOR) * state.closeRangeProximityFactor;
            }
            float previousBodyYaw = state.smoothedBodyYaw;
            float bodyYaw = Mth.rotLerp(bodyResponsiveness, state.smoothedBodyYaw, desiredBodyYaw);
            state.previousSmoothedBodyYaw = previousBodyYaw;
            state.smoothedBodyYaw = bodyYaw;
            player.yBodyRotO = previousBodyYaw;
            player.setYBodyRot(bodyYaw);
        } else {
            float bodyYaw = yaw + state.smoothedBodyYawOffset;
            state.previousSmoothedBodyYaw = state.smoothedBodyYaw;
            state.smoothedBodyYaw = bodyYaw;
            player.yBodyRotO = state.previousSmoothedBodyYaw;
            player.setYBodyRot(bodyYaw);
        }
    }

    private static float currentTargetSwapBlendToNormal(FocusCameraState state) {
        return FocusCameraMath.targetSwapBlendToNormal(state);
    }
}
