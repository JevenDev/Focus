package com.jvn.focus.client.camera;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

final class FocusCameraMath {
    private FocusCameraMath() {}

    static float computeTargetYaw(LocalPlayer player, Vec3 targetPoint, float partialTick) {
        Vec3 to = targetPoint.subtract(player.getEyePosition(partialTick));
        return (float) (Mth.atan2(to.z, to.x) * (180.0D / Math.PI)) - 90.0F;
    }

    static float computeTargetPitch(LocalPlayer player, Vec3 targetPoint, float partialTick) {
        Vec3 to = targetPoint.subtract(player.getEyePosition(partialTick));
        double horizontal = Math.sqrt(to.x * to.x + to.z * to.z);
        return (float) -(Mth.atan2(to.y, horizontal) * (180.0D / Math.PI));
    }

    static float smoothAngle(float current, float target, float responsiveness, float maxStepPerTick, float deltaTicks) {
        float delta = Mth.wrapDegrees(target - current);
        float alpha = 1.0F - (float) Math.exp(-responsiveness * deltaTicks);
        float step = delta * alpha;
        float maxStep = maxStepPerTick * deltaTicks;
        return current + Mth.clamp(step, -maxStep, maxStep);
    }

    static float smoothValue(float current, float target, float responsiveness, float deltaTicks) {
        float alpha = 1.0F - (float) Math.exp(-responsiveness * deltaTicks);
        return Mth.lerp(alpha, current, target);
    }

    static Vec3 smoothVec(Vec3 current, Vec3 target, float responsiveness, float deltaTicks) {
        float alpha = 1.0F - (float) Math.exp(-responsiveness * deltaTicks);
        return current.lerp(target, alpha);
    }

    static float computeDesiredBodyYawOffset(LocalPlayer player, float bodyForwardDamping, float bodyMaxStrafeOffset) {
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

    static double smoothTowards(double current, double target, double speed, double minSpeed, double maxSpeed) {
        return Mth.lerp(Mth.clamp(speed, minSpeed, maxSpeed), current, target);
    }

    /**
     * Frame-rate-independent variant of {@link #smoothTowards}.
     * Uses exponential decay so the convergence rate is stable across refresh rates.
     * {@code deltaTicks} is in game ticks (1.0 = 50 ms at 20 TPS).
     */
    static double smoothTowardsTimeAdjusted(double current, double target, double speed, float deltaTicks, double minSpeed, double maxSpeed) {
        double clampedSpeed = Mth.clamp(speed, minSpeed, maxSpeed);
        double alpha = 1.0D - Math.pow(1.0D - clampedSpeed, deltaTicks);
        return Mth.lerp(alpha, current, target);
    }

    static double applyBlendSmoothing(double blend, double smoothness, double minSmoothness, double maxSmoothness) {
        double clampedBlend = Mth.clamp(blend, 0.0D, 1.0D);
        double clampedSmoothness = Mth.clamp(smoothness, minSmoothness, maxSmoothness);
        double easedBlend = clampedBlend * clampedBlend * (3.0D - 2.0D * clampedBlend);
        return Mth.lerp(clampedSmoothness, clampedBlend, easedBlend);
    }

    static float targetSwapBlendToNormal(FocusCameraState state) {
        if (state.targetSwapSmoothingTicks <= 0 || state.targetSwapSmoothingDurationTicks <= 0) {
            return 1.0F;
        }

        // Linear ramp from 0→1 over the swap duration.  The underlying exponential-decay
        // smoothing (smoothAngle / smoothVec) already provides natural ease-in/ease-out,
        // so a linear blend distributes the responsiveness ramp evenly across ticks and
        // avoids the mid-transition velocity spike that smoothstep produced.
        float blendToNormal = 1.0F - (float) state.targetSwapSmoothingTicks / (float) state.targetSwapSmoothingDurationTicks;
        return Mth.clamp(blendToNormal, 0.0F, 1.0F);
    }
}
