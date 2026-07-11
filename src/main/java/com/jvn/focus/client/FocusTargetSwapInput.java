package com.jvn.focus.client;

/**
 * Accumulates look input into deliberate target-swap flicks.
 * A successful flick must return through the deadzone before another can fire.
 */
final class FocusTargetSwapInput {
    private static final int MIN_COOLDOWN_TICKS = 8;

    private double accumulatedX;
    private double accumulatedY;
    private int cooldownTicks;
    private boolean armed = true;

    void record(double deltaX, double deltaY) {
        if (Double.isFinite(deltaX)) {
            accumulatedX += deltaX;
        }
        if (Double.isFinite(deltaY)) {
            accumulatedY += deltaY;
        }
    }

    void tick(double deadzone, double decay) {
        if (cooldownTicks > 0) {
            cooldownTicks--;
        }
        if (armed) {
            return;
        }

        dampen(decay);
        if (cooldownTicks == 0 && magnitudeSquared() < square(nonNegative(deadzone))) {
            clearAccumulatedInput();
            armed = true;
        }
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
        return new Direction(accumulatedX, accumulatedY);
    }

    void markSwapped(int configuredCooldownTicks) {
        clearAccumulatedInput();
        cooldownTicks = Math.max(MIN_COOLDOWN_TICKS, configuredCooldownTicks);
        armed = false;
    }

    void dampen(double factor) {
        double safeFactor = Math.max(0.0D, Math.min(1.0D, factor));
        accumulatedX *= safeFactor;
        accumulatedY *= safeFactor;
    }

    void reset() {
        clearAccumulatedInput();
        cooldownTicks = 0;
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

    private static double nonNegative(double value) {
        return Double.isFinite(value) ? Math.max(0.0D, value) : 0.0D;
    }

    private static double square(double value) {
        return value * value;
    }

    record Direction(double x, double y) {}
}
