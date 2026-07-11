package com.jvn.focus.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class FocusMovementInputRemapperTest {
    private static final float EPSILON = 1.0E-5F;

    @Test
    void leavesMovementUnchangedWhenHeadingsMatch() {
        FocusMovementInputRemapper.MovementInput result =
                FocusMovementInputRemapper.rotateToHeading(0.75F, -0.4F, 0.0F);

        assertEquals(0.75F, result.forward(), EPSILON);
        assertEquals(-0.4F, result.left(), EPSILON);
    }

    @Test
    void preservesForwardHeadingAcrossQuarterTurn() {
        FocusMovementInputRemapper.MovementInput result =
                FocusMovementInputRemapper.rotateToHeading(1.0F, 0.0F, 90.0F);

        assertEquals(0.0F, result.forward(), EPSILON);
        assertEquals(-1.0F, result.left(), EPSILON);
    }

    @Test
    void preservesInputMagnitudeWhileRemapping() {
        float forward = 0.8F;
        float left = 0.35F;
        FocusMovementInputRemapper.MovementInput result =
                FocusMovementInputRemapper.rotateToHeading(forward, left, -137.0F);

        float beforeMagnitudeSquared = forward * forward + left * left;
        float afterMagnitudeSquared = result.forward() * result.forward() + result.left() * result.left();
        assertEquals(beforeMagnitudeSquared, afterMagnitudeSquared, EPSILON);
    }
}
