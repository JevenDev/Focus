package com.jvn.focus.client.camera;

public interface FocusCameraRotationFollowPolicy {
    void applyPlayerRotation(FocusCameraTargetContext context, FocusCameraState state, float desiredYaw, float desiredPitch);
}
