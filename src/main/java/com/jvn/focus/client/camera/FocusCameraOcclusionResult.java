package com.jvn.focus.client.camera;

public record FocusCameraOcclusionResult(
        double safeDistance,
        double leftShoulderVisibilityScore,
        double rightShoulderVisibilityScore,
        boolean playerBlocksCameraRay,
        boolean targetVisibilityCompromised) {
    public static final FocusCameraOcclusionResult NONE =
            new FocusCameraOcclusionResult(Double.POSITIVE_INFINITY, 1.0D, 1.0D, false, false);
}
