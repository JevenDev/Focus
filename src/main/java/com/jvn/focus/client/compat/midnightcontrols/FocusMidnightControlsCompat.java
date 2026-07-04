package com.jvn.focus.client.compat.midnightcontrols;

import com.jvn.focus.Focus;
import com.jvn.focus.client.LockOnHandler;
import eu.midnightdust.midnightcontrols.client.compat.CompatHandler;
import eu.midnightdust.midnightcontrols.client.compat.MidnightControlsCompat;
import net.minecraft.client.Minecraft;

public final class FocusMidnightControlsCompat {
    private static boolean registered;

    private FocusMidnightControlsCompat() {}

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        MidnightControlsCompat.registerCompatHandler(new FocusCompatHandler());
        Focus.LOGGER.debug("Registered Focus MidnightControls compatibility.");
    }

    private static final class FocusCompatHandler implements CompatHandler {
        @Override
        public void handleCamera(Minecraft minecraft, double targetYaw, double targetPitch) {
            if (LockOnHandler.shouldSuppressVanillaMouseTurn()) {
                LockOnHandler.onMidnightControlsLookInput(targetYaw, targetPitch);
            }
        }
    }
}
