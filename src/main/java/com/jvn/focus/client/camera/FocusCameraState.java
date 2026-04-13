package com.jvn.focus.client.camera;

import com.jvn.focus.client.FocusClientConfig;
import net.minecraft.world.phys.Vec3;

public final class FocusCameraState {
    float smoothedLookYaw;
    float smoothedLookPitch;
    float smoothedBodyYawOffset;
    Vec3 smoothedTargetPoint = Vec3.ZERO;
    boolean smoothingInitialized;
    boolean cameraEditorPreviewActive;

    FocusClientConfig.Shoulder activeShoulder = FocusClientConfig.Shoulder.LEFT;
    FocusClientConfig.Shoulder staticSwapSourceShoulder = FocusClientConfig.Shoulder.LEFT;
    double staticSwapBlend = 1.0D;
    double dynamicAutoTargetBlend;
    double dynamicAutoCurrentBlend;
    Vec3 previousDynamicTargetOffset = Vec3.ZERO;
    boolean dynamicSwapReferenceInitialized;

    int targetSwapSmoothingTicks;
    int targetSwapSmoothingDurationTicks;
    int initialLockCameraSnapTicks;
    int playerFollowDelayTicks;

    float freeLookYaw;
    float freeLookPitch;
    boolean freeLookInputActive;
    boolean freeLookRecentering;
    float playerTransparencyAlpha = 1.0F;
    int dynamicManualShoulderOverrideTicks;

    public void resetForWorldUnload() {
        cameraEditorPreviewActive = false;
        resetForLockEnd();
        activeShoulder = FocusClientConfig.Shoulder.LEFT;
        staticSwapSourceShoulder = FocusClientConfig.Shoulder.LEFT;
    }

    public void resetForLockEnd() {
        smoothingInitialized = false;
        smoothedBodyYawOffset = 0.0F;
        smoothedTargetPoint = Vec3.ZERO;
        staticSwapSourceShoulder = activeShoulder;
        staticSwapBlend = 1.0D;
        dynamicAutoTargetBlend = 0.0D;
        dynamicAutoCurrentBlend = 0.0D;
        previousDynamicTargetOffset = Vec3.ZERO;
        dynamicSwapReferenceInitialized = false;
        targetSwapSmoothingTicks = 0;
        targetSwapSmoothingDurationTicks = 0;
        initialLockCameraSnapTicks = 0;
        playerFollowDelayTicks = 0;
        freeLookYaw = 0.0F;
        freeLookPitch = 0.0F;
        freeLookInputActive = false;
        freeLookRecentering = false;
        playerTransparencyAlpha = 1.0F;
        dynamicManualShoulderOverrideTicks = 0;
    }
}
