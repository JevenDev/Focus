package com.jvn.focus.client;

/**
 * Pure scoring helpers for lock-on target selection.
 */
final class FocusTargetScoring {
    private static final double ACQUISITION_ANGLE_WEIGHT = 0.72D;
    private static final double ACQUISITION_DISTANCE_WEIGHT = 0.28D;
    private static final double REPLACEMENT_ANGLE_WEIGHT = 0.60D;
    private static final double REPLACEMENT_DISTANCE_WEIGHT = 0.40D;
    private static final double DIRECTION_PENALTY_WEIGHT = 0.75D;
    private static final double SWAP_WORLD_DISTANCE_WEIGHT = 0.08D;

    private FocusTargetScoring() {}

    static double acquisitionCost(double alignment, double minimumAlignment, double distance, double maxDistance) {
        return normalizedAngularOffset(alignment, minimumAlignment) * ACQUISITION_ANGLE_WEIGHT
                + normalizedDistance(distance, maxDistance) * ACQUISITION_DISTANCE_WEIGHT;
    }

    static double replacementCost(double alignment, double minimumAlignment, double distance, double maxDistance) {
        return normalizedAngularOffset(alignment, minimumAlignment) * REPLACEMENT_ANGLE_WEIGHT
                + normalizedDistance(distance, maxDistance) * REPLACEMENT_DISTANCE_WEIGHT;
    }

    static double directionalSwapCost(
            double directionalAlignment,
            double minimumDirectionalAlignment,
            double screenDistance,
            double worldDistance,
            double maxWorldDistance) {
        double alignmentRange = Math.max(1.0D - clamp(minimumDirectionalAlignment, -1.0D, 1.0D), 1.0E-6D);
        double directionPenalty = clamp(
                (1.0D - clamp(directionalAlignment, -1.0D, 1.0D)) / alignmentRange,
                0.0D,
                1.0D);
        return Math.max(0.0D, screenDistance) * (1.0D + directionPenalty * DIRECTION_PENALTY_WEIGHT)
                + normalizedDistance(worldDistance, maxWorldDistance) * SWAP_WORLD_DISTANCE_WEIGHT;
    }

    private static double normalizedAngularOffset(double alignment, double minimumAlignment) {
        double maximumAngle = Math.acos(clamp(minimumAlignment, -1.0D, 1.0D));
        if (maximumAngle <= 1.0E-6D) {
            return alignment >= minimumAlignment ? 0.0D : 1.0D;
        }
        return clamp(Math.acos(clamp(alignment, -1.0D, 1.0D)) / maximumAngle, 0.0D, 1.0D);
    }

    private static double normalizedDistance(double distance, double maxDistance) {
        return clamp(Math.max(0.0D, distance) / Math.max(maxDistance, 1.0E-6D), 0.0D, 1.0D);
    }

    private static double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
