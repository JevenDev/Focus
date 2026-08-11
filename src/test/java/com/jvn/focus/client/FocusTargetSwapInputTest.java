package com.jvn.focus.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class FocusTargetSwapInputTest {
    @Test
    void ignoresLookInputBelowActivationThreshold() {
        FocusTargetSwapInput input = new FocusTargetSwapInput();
        input.record(12.0D, 0.0D);

        assertNull(input.pollDirection(8.0D, 20.0D, 0.8D));
    }

    @Test
    void emitsDirectionForAnIntentionalFlick() {
        FocusTargetSwapInput input = new FocusTargetSwapInput();
        input.record(18.0D, -10.0D);

        assertNotNull(input.pollDirection(8.0D, 20.0D, 0.8D));
    }

    @Test
    void removesIncidentalVerticalDriftFromHorizontalFlick() {
        FocusTargetSwapInput input = new FocusTargetSwapInput();
        input.record(24.0D, 6.0D);

        FocusTargetSwapInput.Direction direction = input.pollDirection(8.0D, 20.0D, 0.8D);

        assertNotNull(direction);
        assertEquals(24.0D, direction.x());
        assertEquals(0.0D, direction.y());
    }

    @Test
    void preservesDeliberateDiagonalFlick() {
        FocusTargetSwapInput input = new FocusTargetSwapInput();
        input.record(18.0D, 16.0D);

        FocusTargetSwapInput.Direction direction = input.pollDirection(8.0D, 20.0D, 0.8D);

        assertNotNull(direction);
        assertEquals(18.0D, direction.x());
        assertEquals(16.0D, direction.y());
    }

    @Test
    void heldInputCannotRepeatAfterCooldown() {
        FocusTargetSwapInput input = new FocusTargetSwapInput();
        input.record(25.0D, 0.0D);
        input.markSwapped(2);

        for (int tick = 0; tick < 12; tick++) {
            input.record(12.0D, 0.0D);
            input.tick(8.0D);
        }

        assertFalse(input.isArmed());
        assertNull(input.pollDirection(8.0D, 20.0D, 0.82D));
    }

    @Test
    void releaseRearmsImmediatelyAfterCooldownDespiteEarlierHeldInput() {
        FocusTargetSwapInput input = new FocusTargetSwapInput();
        input.markSwapped(8);

        for (int tick = 0; tick < 7; tick++) {
            input.record(30.0D, 0.0D);
            input.tick(8.0D);
        }
        input.tick(8.0D);

        assertTrue(input.isArmed());
        input.record(-25.0D, 0.0D);
        FocusTargetSwapInput.Direction secondFlick =
                input.pollDirection(8.0D, 20.0D, 0.82D);
        assertNotNull(secondFlick);
        assertTrue(secondFlick.x() < 0.0D);
    }

    @Test
    void acceptsNewFlickThatStartsAsCooldownExpiresAfterEarlierRelease() {
        FocusTargetSwapInput input = new FocusTargetSwapInput();
        input.markSwapped(8);

        input.tick(8.0D);
        for (int tick = 0; tick < 6; tick++) {
            input.tick(8.0D);
        }
        input.record(-25.0D, 0.0D);
        input.tick(8.0D);

        assertTrue(input.isArmed());
        FocusTargetSwapInput.Direction secondFlick =
                input.pollDirection(8.0D, 20.0D, 0.82D);
        assertNotNull(secondFlick);
        assertTrue(secondFlick.x() < 0.0D);
    }

    @Test
    void releasingInputRearmsAfterCooldown() {
        FocusTargetSwapInput input = new FocusTargetSwapInput();
        input.markSwapped(8);

        for (int tick = 0; tick < 8; tick++) {
            input.tick(8.0D);
        }

        assertTrue(input.isArmed());
        assertTrue(input.cooldownTicks() == 0);
    }

    @Test
    void rejectsNonFiniteDeviceInput() {
        FocusTargetSwapInput input = new FocusTargetSwapInput();
        input.record(Double.NaN, Double.POSITIVE_INFINITY);

        assertNull(input.pollDirection(0.0D, 1.0D, 0.8D));
    }
}
