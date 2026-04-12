package com.jvn.focus.mixin;

import com.jvn.focus.client.LockOnHandler;
import com.jvn.focus.client.LockOnHandler.CameraLockData;
import com.jvn.focus.client.FocusClientConfig;

import net.minecraft.client.Camera;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Unique
    private static final double CAMERA_COLLISION_BUFFER = 0.12D;
    @Unique
    private static final double MIN_CAMERA_ROTATION_LERP = 0.01D;

    @Unique
    private static Vec3 focus$smoothedCameraPosition;
    @Unique
    private static boolean focus$hasSmoothedCameraPosition;
    @Unique
    private static float focus$smoothedCameraYaw;
    @Unique
    private static float focus$smoothedCameraPitch;
    @Unique
    private static boolean focus$hasSmoothedCameraRotation;
    @Unique
    private static boolean focus$wasLockCameraActive;

    @Shadow
    private float yRot;

    @Shadow
    private float xRot;

    @Shadow
    protected abstract void setPosition(Vec3 pos);

    @Shadow
    public abstract Vec3 getPosition();

    @Shadow
    protected abstract void setRotation(float yRot, float xRot, float roll);

    @Shadow
    public abstract float getRoll();

    @Inject(method = "setup", at = @At("TAIL"))
    private void focus$applyLockOnCamera(BlockGetter level, Entity entity, boolean detached, boolean thirdPersonReverse, float partialTick, CallbackInfo ci) {
        boolean detachedBackCamera = detached && !thirdPersonReverse;

        if (!(entity instanceof LocalPlayer player) || !detachedBackCamera) {
            focus$hasSmoothedCameraPosition = false;
            focus$hasSmoothedCameraRotation = false;
            focus$wasLockCameraActive = false;
            return;
        }

        CameraLockData lockData = LockOnHandler.getActiveCameraData(player, partialTick);
        if (lockData == null) {
            focus$hasSmoothedCameraPosition = false;
            focus$hasSmoothedCameraRotation = false;
            focus$wasLockCameraActive = false;
            return;
        }

        Vec3 pivotPoint = player.getEyePosition(partialTick);
        Vec3 desiredPosition = computeDesiredCameraPosition(pivotPoint, lockData);
        desiredPosition = clipCameraToWorld(level, entity, pivotPoint, desiredPosition);
        boolean snapCameraNow = !focus$wasLockCameraActive || LockOnHandler.isInitialLockCameraSnapActive();
        if (snapCameraNow) {
            Vec3 firstLookVector = lockData.targetPoint().subtract(desiredPosition);
            if (firstLookVector.lengthSqr() < 1.0E-6D) {
                focus$smoothedCameraPosition = desiredPosition;
                focus$hasSmoothedCameraPosition = true;
                focus$hasSmoothedCameraRotation = false;
                this.setPosition(desiredPosition);
                focus$wasLockCameraActive = true;
                return;
            }

            double firstHorizontal = Math.sqrt(firstLookVector.x * firstLookVector.x + firstLookVector.z * firstLookVector.z);
            float firstLockYaw = (float) (Mth.atan2(firstLookVector.z, firstLookVector.x) * (180.0D / Math.PI)) - 90.0F;
            float firstLockPitch = (float) -(Mth.atan2(firstLookVector.y, firstHorizontal) * (180.0D / Math.PI));
            focus$smoothedCameraPosition = desiredPosition;
            focus$hasSmoothedCameraPosition = true;
            focus$smoothedCameraYaw = firstLockYaw;
            focus$smoothedCameraPitch = firstLockPitch;
            focus$hasSmoothedCameraRotation = true;
            this.setPosition(desiredPosition);
            this.setRotation(firstLockYaw, Mth.clamp(firstLockPitch, -90.0F, 90.0F), this.getRoll());
            focus$wasLockCameraActive = true;
            return;
        }

        if (!focus$hasSmoothedCameraPosition) {
            focus$smoothedCameraPosition = this.getPosition();
            focus$hasSmoothedCameraPosition = true;
        }

        double cameraFloatiness = Mth.clamp(
                FocusClientConfig.cameraFloatiness(),
                FocusClientConfig.MIN_CAMERA_FLOATINESS,
                FocusClientConfig.MAX_CAMERA_FLOATINESS);
        float positionFactor = LockOnHandler.getTargetSwapCameraPositionFactor();
        double effectiveFloatiness = cameraFloatiness * positionFactor;
        focus$smoothedCameraPosition = focus$smoothedCameraPosition.lerp(desiredPosition, effectiveFloatiness);

        Vec3 lockPosition = clipCameraToWorld(level, entity, pivotPoint, focus$smoothedCameraPosition);
        Vec3 lookVector = lockData.targetPoint().subtract(lockPosition);
        if (lookVector.lengthSqr() < 1.0E-6D) {
            return;
        }

        double horizontal = Math.sqrt(lookVector.x * lookVector.x + lookVector.z * lookVector.z);
        float lockYaw = (float) (Mth.atan2(lookVector.z, lookVector.x) * (180.0D / Math.PI)) - 90.0F;
        float lockPitch = (float) -(Mth.atan2(lookVector.y, horizontal) * (180.0D / Math.PI));
        if (!focus$hasSmoothedCameraRotation) {
            focus$smoothedCameraYaw = this.yRot;
            focus$smoothedCameraPitch = this.xRot;
            focus$hasSmoothedCameraRotation = true;
        }

        double cameraDrag = Mth.clamp(
                FocusClientConfig.cameraDrag(),
                FocusClientConfig.MIN_CAMERA_DRAG,
                FocusClientConfig.MAX_CAMERA_DRAG);
        float baseRotationLerp = (float) Math.max(MIN_CAMERA_ROTATION_LERP, 1.0D - cameraDrag);
        float rotationFactor = LockOnHandler.getTargetSwapCameraRotationFactor();
        float rotationLerp = Math.max((float) MIN_CAMERA_ROTATION_LERP, baseRotationLerp * rotationFactor);
        focus$smoothedCameraYaw = Mth.rotLerp(rotationLerp, focus$smoothedCameraYaw, lockYaw);
        focus$smoothedCameraPitch = Mth.lerp(rotationLerp, focus$smoothedCameraPitch, lockPitch);

        this.setPosition(lockPosition);
        this.setRotation(focus$smoothedCameraYaw, Mth.clamp(focus$smoothedCameraPitch, -90.0F, 90.0F), this.getRoll());
        focus$smoothedCameraPosition = lockPosition;
        focus$wasLockCameraActive = true;
    }

    @Unique
    private static Vec3 computeDesiredCameraPosition(Vec3 pivotPoint, CameraLockData lockData) {
        Vec3 forward = safeNormalize(lockData.targetPoint().subtract(pivotPoint), new Vec3(0.0D, 0.0D, 1.0D));
        Vec3 right = safeNormalize(new Vec3(forward.z, 0.0D, -forward.x), new Vec3(1.0D, 0.0D, 0.0D));

        Vec3 basePosition = pivotPoint
                .subtract(forward.scale(lockData.offsetZ()))
                .add(right.scale(lockData.offsetX()))
                .add(0.0D, lockData.offsetY(), 0.0D);

        float rotationDegrees = lockData.rotationDegrees();
        if (Math.abs(rotationDegrees) < 1.0E-4F) {
            return basePosition;
        }

        Vec3 relative = basePosition.subtract(pivotPoint);
        double radians = Math.toRadians(rotationDegrees);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        Vec3 rotated = new Vec3(
                relative.x * cos - relative.z * sin,
                relative.y,
                relative.x * sin + relative.z * cos);
        return pivotPoint.add(rotated);
    }

    @Unique
    private static Vec3 clipCameraToWorld(BlockGetter level, Entity entity, Vec3 from, Vec3 to) {
        BlockHitResult hitResult = level.clip(new ClipContext(from, to, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, entity));
        if (hitResult.getType() != HitResult.Type.BLOCK) {
            return to;
        }

        Vec3 direction = to.subtract(from);
        if (direction.lengthSqr() < 1.0E-6D) {
            return hitResult.getLocation();
        }

        return hitResult.getLocation().subtract(direction.normalize().scale(CAMERA_COLLISION_BUFFER));
    }

    @Unique
    private static Vec3 safeNormalize(Vec3 value, Vec3 fallback) {
        return value.lengthSqr() < 1.0E-6D ? fallback : value.normalize();
    }
}
