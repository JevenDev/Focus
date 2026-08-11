package com.jvn.focus.client;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class FocusTargetScoringTest {
    private static final double ACQUISITION_THRESHOLD = 0.35D;
    private static final double MAX_DISTANCE = 15.0D;

    @Test
    void acquisitionPrefersNearbyTargetWhenBothAreCloseToCrosshair() {
        double nearbyCost = FocusTargetScoring.acquisitionCost(
                Math.cos(Math.toRadians(10.0D)), ACQUISITION_THRESHOLD, 3.0D, MAX_DISTANCE);
        double distantCost = FocusTargetScoring.acquisitionCost(
                1.0D, ACQUISITION_THRESHOLD, 15.0D, MAX_DISTANCE);

        assertTrue(nearbyCost < distantCost);
    }

    @Test
    void acquisitionKeepsStrongCenterBiasForClearlyOffAxisTargets() {
        double nearbyOffAxisCost = FocusTargetScoring.acquisitionCost(
                Math.cos(Math.toRadians(35.0D)), ACQUISITION_THRESHOLD, 3.0D, MAX_DISTANCE);
        double distantCenteredCost = FocusTargetScoring.acquisitionCost(
                1.0D, ACQUISITION_THRESHOLD, 15.0D, MAX_DISTANCE);

        assertTrue(distantCenteredCost < nearbyOffAxisCost);
    }

    @Test
    void replacementBalancesContinuityAgainstProximity() {
        double nearbySideCost = FocusTargetScoring.replacementCost(
                Math.cos(Math.toRadians(40.0D)), 0.0D, 3.0D, MAX_DISTANCE);
        double distantCenteredCost = FocusTargetScoring.replacementCost(
                1.0D, 0.0D, 15.0D, MAX_DISTANCE);

        assertTrue(nearbySideCost < distantCenteredCost);
    }

    @Test
    void directionalSwapPrefersObviousAdjacentTargetWithinFlickCone() {
        double adjacentCost = FocusTargetScoring.directionalSwapCost(
                0.60D, 0.56D, 0.07D, 3.0D, MAX_DISTANCE);
        double distantCost = FocusTargetScoring.directionalSwapCost(
                1.0D, 0.56D, 0.24D, 14.0D, MAX_DISTANCE);

        assertTrue(adjacentCost < distantCost);
    }

    @Test
    void directionalSwapStillRewardsClearInputDirection() {
        double weakDirectionCost = FocusTargetScoring.directionalSwapCost(
                0.56D, 0.56D, 0.20D, 6.0D, MAX_DISTANCE);
        double strongDirectionCost = FocusTargetScoring.directionalSwapCost(
                1.0D, 0.56D, 0.20D, 6.0D, MAX_DISTANCE);

        assertTrue(strongDirectionCost < weakDirectionCost);
    }
}
