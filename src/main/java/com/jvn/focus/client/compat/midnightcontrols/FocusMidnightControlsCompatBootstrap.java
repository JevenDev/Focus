package com.jvn.focus.client.compat.midnightcontrols;

import com.jvn.focus.Focus;
import java.lang.reflect.InvocationTargetException;
import net.neoforged.fml.ModList;

public final class FocusMidnightControlsCompatBootstrap {
    private static final String MIDNIGHTCONTROLS_MOD_ID = "midnightcontrols";
    private static final String COMPAT_CLASS =
            "com.jvn.focus.client.compat.midnightcontrols.FocusMidnightControlsCompat";

    private static boolean initialized;

    private FocusMidnightControlsCompatBootstrap() {}

    public static void init() {
        if (initialized || !ModList.get().isLoaded(MIDNIGHTCONTROLS_MOD_ID)) {
            return;
        }
        initialized = true;

        try {
            Class.forName(COMPAT_CLASS)
                    .getMethod("register")
                    .invoke(null);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException
                | InvocationTargetException | LinkageError exception) {
            Focus.LOGGER.warn("Failed to initialize MidnightControls compatibility.", exception);
        }
    }
}
