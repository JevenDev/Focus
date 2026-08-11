package com.jvn.focus.client;

/**
 * Accumulates look input into deliberate target-swap flicks.
 * A successful flick must return through the deadzone before another can fire.
 */
final class FocusTargetSwapInput {
    private static final int MIN_COOLDOWN_TICKS = 8;
    private static final double DOMINANT_AXIS_RATIO = 1.25D;

    private double accumulatedX;
    private double accumulatedY;
    private double sampledX;
    private double sampledY;
    private int cooldownTicks;
    private boolean releasedSinceSwap;
    private boolean armed = true;

    void record(double deltaX, double deltaY) {
        if (Double.isFinite(deltaX)) {
            accumulatedX += deltaX;
            sampledX += deltaX;
        }
        if (Double.isFinite(deltaY)) {
            accumulatedY += deltaY;
            sampledY += deltaY;
        }
    }

    void tick(double deadzone) {
        if (cooldownTicks > 0) {
            cooldownTicks--;
        }

        if (armed) {
            clearSampledInput();
            return;
        }

        // Re-arm from recent input, not the historical accumulator. Input held
        // during cooldown used to keep the swap disarmed long after release.
        boolean released = sampledMagnitudeSquared() < square(nonNegative(deadzone));
        if (released) {
            releasedSinceSwap = true;
        }
        clearSampledInput();

        if (cooldownTicks == 0 && releasedSinceSwap) {
            armed = true;
            // Preserve a new flick that begins exactly as cooldown expires. If this
            // interval is quiet, discard its sub-deadzone drift instead.
            if (released) {
                clearAccumulatedInput();
            }
            return;
        }

        clearAccumulatedInput();
    }

    Direction pollDirection(double deadzone, double activation, double decay) {
        if (!armed || cooldownTicks > 0) {
            return null;
        }

        double activationThreshold = Math.max(nonNegative(deadzone), nonNegative(activation));
        if (magnitudeSquared() < square(activationThreshold)) {
            dampen(decay);
            return null;
        }
        return stabilizedDirection();
    }

    private Direction stabilizedDirection() {
        double absX = Math.abs(accumulatedX);
        double absY = Math.abs(accumulatedY);
        if (absX > absY * DOMINANT_AXIS_RATIO) {
            return new Direction(accumulatedX, 0.0D);
        }
        if (absY > absX * DOMINANT_AXIS_RATIO) {
            return new Direction(0.0D, accumulatedY);
        }

        // Preserve deliberate diagonal flicks while removing incidental drift
        // from clearly horizontal or vertical gestures.
        return new Direction(accumulatedX, accumulatedY);
    }

    void markSwapped(int configuredCooldownTicks) {
        clearAccumulatedInput();
        clearSampledInput();
        cooldownTicks = Math.max(MIN_COOLDOWN_TICKS, configuredCooldownTicks);
        releasedSinceSwap = false;
        armed = false;
    }

    void dampen(double factor) {
        double safeFactor = Math.max(0.0D, Math.min(1.0D, factor));
        accumulatedX *= safeFactor;
        accumulatedY *= safeFactor;
    }

    void reset() {
        clearAccumulatedInput();
        clearSampledInput();
        cooldownTicks = 0;
        releasedSinceSwap = false;
        armed = true;
    }

    boolean isArmed() {
        return armed;
    }

    int cooldownTicks() {
        return cooldownTicks;
    }

    private void clearAccumulatedInput() {
        accumulatedX = 0.0D;
        accumulatedY = 0.0D;
    }

    private double magnitudeSquared() {
        return square(accumulatedX) + square(accumulatedY);
    }

    private void clearSampledInput() {
        sampledX = 0.0D;
        sampledY = 0.0D;
    }

    private double sampledMagnitudeSquared() {
        return square(sampledX) + square(sampledY);
    }

    private static double nonNegative(double value) {
        return Double.isFinite(value) ? Math.max(0.0D, value) : 0.0D;
    }

    private static double square(double value) {
        return value * value;
    }

    record Direction(double x, double y) {}
}
