package com.jvn.focus.client;

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
    void heldInputCannotRepeatAfterCooldown() {
        FocusTargetSwapInput input = new FocusTargetSwapInput();
        input.record(25.0D, 0.0D);
        input.markSwapped(2);

        for (int tick = 0; tick < 12; tick++) {
            input.record(12.0D, 0.0D);
            input.tick(8.0D, 0.82D);
        }

        assertFalse(input.isArmed());
        assertNull(input.pollDirection(8.0D, 20.0D, 0.82D));
    }

    @Test
    void releasingInputRearmsAfterCooldown() {
        FocusTargetSwapInput input = new FocusTargetSwapInput();
        input.markSwapped(8);

        for (int tick = 0; tick < 8; tick++) {
            input.tick(8.0D, 0.82D);
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
