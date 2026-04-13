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
    int occlusionGraceTicks;
    int outOfRangeGraceTicks;

    float freeLookYaw;
    float freeLookPitch;
    boolean freeLookInputActive;
    boolean freeLookRecentering;
    float playerTransparencyAlpha = 1.0F;
    int dynamicManualShoulderOverrideTicks;
    /** How close the player is to the target horizontally (0 = on top, 1 = far enough). */
    float closeRangeProximityFactor = 1.0F;
    /** True when the player entered close range while holding forward — locks their heading. */
    boolean closeRangeHeadingLocked;
    /** The locked movement yaw, preserved until forward input is released. */
    float closeRangeLockedHeadingYaw;
    /** Stores the most recent render-frame delta (in game ticks) for FPS-independent smoothing. */
    float lastRenderDeltaTicks = 1.0F;

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
        occlusionGraceTicks = 0;
        outOfRangeGraceTicks = 0;
        freeLookYaw = 0.0F;
        freeLookPitch = 0.0F;
        freeLookInputActive = false;
        freeLookRecentering = false;
        playerTransparencyAlpha = 1.0F;
        dynamicManualShoulderOverrideTicks = 0;
        closeRangeProximityFactor = 1.0F;
        closeRangeHeadingLocked = false;
        closeRangeLockedHeadingYaw = 0.0F;
    }
}
