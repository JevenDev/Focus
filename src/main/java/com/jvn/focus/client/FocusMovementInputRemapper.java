package com.jvn.focus.client;

/**
 * Converts camera-relative movement input into a preserved world-space heading.
 */
final class FocusMovementInputRemapper {
    private FocusMovementInputRemapper() {}

    static MovementInput rotateToHeading(float forward, float left, float yawDifferenceDegrees) {
        double yawDifferenceRadians = Math.toRadians(yawDifferenceDegrees);
        float cos = (float) Math.cos(yawDifferenceRadians);
        float sin = (float) Math.sin(yawDifferenceRadians);

        return new MovementInput(
                forward * cos + left * sin,
                left * cos - forward * sin);
    }

    record MovementInput(float forward, float left) {}
}
