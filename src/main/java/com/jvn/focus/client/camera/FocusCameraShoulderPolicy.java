package com.jvn.focus.client.camera;

public interface FocusCameraShoulderPolicy {
    FocusCameraPose resolveShoulderPose(FocusCameraTargetContext context, FocusCameraState state);
}
