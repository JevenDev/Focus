package com.jvn.focus.client.camera;

import com.jvn.focus.client.FocusClientConfig;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;

final class FocusDefaultCameraRotationFollowPolicy implements FocusCameraRotationFollowPolicy {
    private static final float TARGET_SWAP_HEAD_FOLLOW_BONUS = 0.25F;
    private static final float CLOSE_RANGE_RECENTER_SPEED = 0.12F;

    @Override
    public void applyPlayerRotation(FocusCameraTargetContext context, FocusCameraState state, float desiredYaw, float desiredPitch) {
        LocalPlayer player = context.player();
        FocusCameraMode mode = FocusClientConfig.cameraOwnershipMode();
        boolean followRotations = FocusClientConfig.followPlayerRotations();
        float clampedPitch = Mth.clamp(desiredPitch, -90.0F, 90.0F);
        if (!followRotations) {
            if (FocusClientConfig.fullBodyFollowEnabled()) {
                player.setYBodyRot(Mth.rotLerp(
                        FocusClientConfig.cameraBodyFollowResponsiveness(),
                        player.getYRot(),
                        player.getYHeadRot()));
            }
            return;
        }

        float targetSwapBlend = currentTargetSwapBlendToNormal(state);
        float swapFollowAlpha = Mth.lerp(targetSwapBlend, FocusClientConfig.targetSwapPlayerLookFollow(), 1.0F);
        switch (mode) {
            case COUPLED -> applyCoupledFollow(player, state, desiredYaw, clampedPitch, swapFollowAlpha);
            case DELAYED_FOLLOW -> applyDelayedFollow(player, state, desiredYaw, clampedPitch, swapFollowAlpha);
            case FREE_LOOK -> applyFreeLookFollow(player, state, desiredYaw, clampedPitch, swapFollowAlpha);
        }
    }

    private void applyCoupledFollow(LocalPlayer player, FocusCameraState state, float desiredYaw, float desiredPitch, float swapFollowAlpha) {
        float yaw = Mth.rotLerp(swapFollowAlpha, player.getYRot(), desiredYaw);
        float pitch = Mth.lerp(swapFollowAlpha, player.getXRot(), desiredPitch);
        float headFollowAlpha = Mth.clamp(swapFollowAlpha + TARGET_SWAP_HEAD_FOLLOW_BONUS, 0.0F, 1.0F);
        float headYaw = Mth.rotLerp(headFollowAlpha, player.getYHeadRot(), desiredYaw);
        applyPlayerRotation(player, state, yaw, pitch, headYaw);
    }

    private void applyDelayedFollow(LocalPlayer player, FocusCameraState state, float desiredYaw, float desiredPitch, float swapFollowAlpha) {
        int configuredDelay = Math.max(0, FocusClientConfig.followPlayerRotationsDelay());
        if (configuredDelay > 0 && state.playerFollowDelayTicks < configuredDelay) {
            state.playerFollowDelayTicks++;
            float headYaw = Mth.rotLerp(
                    FocusClientConfig.cameraHeadFollowResponsiveness() * 0.5F,
                    player.getYHeadRot(),
                    desiredYaw);
            applyPlayerRotation(player, state, player.getYRot(), player.getXRot(), headYaw);
            return;
        }

        float yaw = Mth.rotLerp(
                FocusClientConfig.cameraBodyFollowResponsiveness() * swapFollowAlpha,
                player.getYRot(),
                desiredYaw);
        float pitch = Mth.lerp(
                FocusClientConfig.cameraHeadFollowResponsiveness() * swapFollowAlpha,
                player.getXRot(),
                desiredPitch);
        float headYaw = Mth.rotLerp(
                Mth.clamp(FocusClientConfig.cameraHeadFollowResponsiveness() + TARGET_SWAP_HEAD_FOLLOW_BONUS, 0.0F, 1.0F),
                player.getYHeadRot(),
                desiredYaw);
        applyPlayerRotation(player, state, yaw, pitch, headYaw);
    }

    private void applyFreeLookFollow(LocalPlayer player, FocusCameraState state, float desiredYaw, float desiredPitch, float swapFollowAlpha) {
        if (!FocusClientConfig.allowFreeLookWhileLockedOn()) {
            applyCoupledFollow(player, state, desiredYaw, desiredPitch, swapFollowAlpha);
            return;
        }

        float desiredBodyYaw = desiredYaw - state.freeLookYaw;
        float yaw = Mth.rotLerp(
                FocusClientConfig.cameraBodyFollowResponsiveness() * swapFollowAlpha,
                player.getYRot(),
                desiredBodyYaw);
        float pitch = Mth.lerp(
                FocusClientConfig.cameraHeadFollowResponsiveness() * swapFollowAlpha,
                player.getXRot(),
                desiredPitch - state.freeLookPitch);
        float headYaw = Mth.rotLerp(FocusClientConfig.cameraHeadFollowResponsiveness(), player.getYHeadRot(), desiredYaw);
        applyPlayerRotation(player, state, yaw, pitch, headYaw);
    }

    private void applyPlayerRotation(LocalPlayer player, FocusCameraState state, float yaw, float pitch, float headYaw) {
        // yaw = desired target-tracking yaw (faces the locked-on entity).
        // Decouple movement direction from visual facing near the target:
        //   - When entering close range while holding forward, lock the current movement heading.
        //   - The lock persists as long as forward is held, even after leaving close range,
        //     so the player walks straight through/past the entity.
        //   - Body/head visually face the target throughout (lock-on feel preserved).
        //   - On forward release, smoothly recenter movement toward the target.
        float movementYaw = yaw;
        float visualYaw = yaw;

        Vec2 move = player.input.getMoveVector();
        float forwardInput = Math.max(0.0F, move.y);

        if (state.closeRangeProximityFactor < 1.0F && forwardInput > 0.0F && !state.closeRangeHeadingLocked) {
            // Entering close range while moving forward — capture current heading
            state.closeRangeHeadingLocked = true;
            state.closeRangeLockedHeadingYaw = player.getYRot();
        }

        if (state.closeRangeHeadingLocked) {
            if (forwardInput > 0.0F) {
                // Keep walking in the locked direction
                movementYaw = state.closeRangeLockedHeadingYaw;
            } else {
                // Forward released — unlock heading and smoothly recenter toward target
                state.closeRangeHeadingLocked = false;
                movementYaw = Mth.rotLerp(CLOSE_RANGE_RECENTER_SPEED, player.getYRot(), yaw);
            }
            visualYaw = yaw;
        }

        player.setYRot(movementYaw);
        player.setXRot(Mth.clamp(pitch, -90.0F, 90.0F));
        player.setYHeadRot(headYaw);
        if (FocusClientConfig.fullBodyFollowEnabled()) {
            float bodyResponsiveness = FocusClientConfig.cameraBodyFollowResponsiveness();
            float desiredBodyYaw = visualYaw + state.smoothedBodyYawOffset;
            player.setYBodyRot(Mth.rotLerp(bodyResponsiveness, player.yBodyRot, desiredBodyYaw));
        } else {
            player.setYBodyRot(visualYaw + state.smoothedBodyYawOffset);
        }
    }

    private static float currentTargetSwapBlendToNormal(FocusCameraState state) {
        if (state.targetSwapSmoothingTicks <= 0 || state.targetSwapSmoothingDurationTicks <= 0) {
            return 1.0F;
        }

        float blendToNormal = 1.0F - (float) state.targetSwapSmoothingTicks / (float) state.targetSwapSmoothingDurationTicks;
        blendToNormal = Mth.clamp(blendToNormal, 0.0F, 1.0F);
        return blendToNormal * blendToNormal * (3.0F - 2.0F * blendToNormal);
    }
}
