package com.jvn.focus.mixin;

import com.jvn.focus.client.FocusClientConfig;
import com.jvn.focus.client.LockOnHandler;
import com.jvn.focus.client.camera.FocusCameraController;
import com.jvn.focus.client.camera.FocusCameraPose;
import com.jvn.focus.client.camera.FocusShoulderSurfingCameraSystem;
import com.jvn.focus.client.camera.FocusShoulderSurfingCameraSystem.CameraPose;
import com.jvn.focus.client.compat.FocusShoulderSurfingCompat;
import net.minecraft.client.Camera;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
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
    private static final double MIN_POSITION_LERP = 0.01D;
    @Unique
    private static final float MIN_ROTATION_LERP = 0.01F;
    @Unique
    private static final FocusShoulderSurfingCameraSystem focus$cameraSystem = FocusShoulderSurfingCameraSystem.getInstance();
    @Unique
    private static final FocusCameraController focus$cameraController = FocusCameraController.getInstance();
    @Unique
    private boolean focus$wasThirdPersonBack;

    @Shadow
    private float yRot;

    @Shadow
    private float xRot;

    @Shadow
    protected abstract void setPosition(Vec3 pos);

    @Shadow
    protected abstract void setRotation(float yRot, float xRot, float roll);

    @Shadow
    public abstract float getRoll();

    @Shadow
    public abstract Vec3 getPosition();

    @Inject(method = "setup", at = @At("TAIL"))
    private void focus$applyLockOnCamera(BlockGetter level, Entity entity, boolean detached, boolean thirdPersonReverse, float partialTick, CallbackInfo ci) {
        boolean detachedBackCamera = detached && !thirdPersonReverse;
        if (!(entity instanceof LocalPlayer player) || !detachedBackCamera) {
            focus$cameraSystem.reset();
            focus$wasThirdPersonBack = false;
            return;
        }

        boolean wasThirdPerson = focus$wasThirdPersonBack;
        focus$wasThirdPersonBack = true;

        FocusCameraPose lockData = LockOnHandler.getActiveCameraData(player, partialTick);
        if (lockData == null) {
            Vec3 pivotPoint = player.getEyePosition(partialTick);

            if (focus$cameraSystem.wasActive()) {
                focus$cameraSystem.beginReturnTransition(pivotPoint);
            }

            if (focus$cameraSystem.isInReturnTransition()) {
                Vec3 vanillaPos = this.getPosition();
                float deltaTicks = focus$cameraController.getLastRenderDeltaTicks();
                CameraPose pose = focus$cameraSystem.updateReturnTransition(
                        vanillaPos, this.yRot, this.xRot, pivotPoint, deltaTicks);
                this.setPosition(pose.position());
                this.setRotation(pose.yaw(), pose.pitch(), this.getRoll());
                if (!FocusShoulderSurfingCompat.isShoulderSurfingActive()) {
                    FocusShoulderSurfingCompat.syncCameraRotation(pose.yaw(), pose.pitch());
                }
            }
            return;
        }

        // Capture vanilla/SS camera state before Focus overrides it.
        Vec3 vanillaCameraPos = this.getPosition();
        float vanillaCameraYaw = this.yRot;
        float vanillaCameraPitch = this.xRot;

        double cameraFloatiness = Mth.clamp(
                FocusClientConfig.cameraFloatiness(),
                FocusClientConfig.MIN_CAMERA_FLOATINESS,
                FocusClientConfig.MAX_CAMERA_FLOATINESS);
        float positionFactor = LockOnHandler.getTargetSwapCameraPositionFactor();
        double positionLerp = Mth.clamp(cameraFloatiness * positionFactor, MIN_POSITION_LERP, 1.0D);

        double cameraDrag = Mth.clamp(
                FocusClientConfig.cameraDrag(),
                FocusClientConfig.MIN_CAMERA_DRAG,
                FocusClientConfig.MAX_CAMERA_DRAG);
        float baseRotationLerp = (float) Math.max(MIN_ROTATION_LERP, 1.0D - cameraDrag);
        float rotationFactor = LockOnHandler.getTargetSwapCameraRotationFactor();
        float rotationLerp = Math.max(MIN_ROTATION_LERP, baseRotationLerp * rotationFactor);

        float deltaTicks = focus$cameraController.getLastRenderDeltaTicks();
        CameraPose pose = focus$cameraSystem.update(
                level,
                entity,
                player.getEyePosition(partialTick),
                lockData,
                partialTick,
                deltaTicks,
                this.yRot,
                this.xRot,
                positionLerp,
                rotationLerp,
                LockOnHandler.isInitialLockCameraSnapActive(),
                LockOnHandler.getTargetSwapBlendToNormal(),
                vanillaCameraPos,
                vanillaCameraYaw,
                vanillaCameraPitch,
                wasThirdPerson);

        this.setPosition(pose.position());
        this.setRotation(pose.yaw(), pose.pitch(), this.getRoll());
        FocusShoulderSurfingCompat.syncCameraRotation(pose.yaw(), pose.pitch());
    }
}
