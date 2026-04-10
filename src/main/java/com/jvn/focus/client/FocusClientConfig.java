package com.jvn.focus.client;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class FocusClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue AUTO_SWITCH_TO_THIRD_PERSON;
    public static final ModConfigSpec.BooleanValue ALLOW_FIRST_PERSON_WHILE_TARGETING;
    public static final ModConfigSpec.BooleanValue SHOW_LOCK_ON_DEBUG_TEXT;
    public static final ModConfigSpec SPEC;

    static {
        BUILDER.push("lockOn");

        AUTO_SWITCH_TO_THIRD_PERSON = BUILDER
                .comment("Automatically switch to third-person back camera when lock-on is enabled.")
                .define("autoSwitchToThirdPerson", true);

        ALLOW_FIRST_PERSON_WHILE_TARGETING = BUILDER
                .comment("Allow entering and staying in first-person while lock-on is active.")
                .define("allowFirstPersonWhileTargeting", false);

        SHOW_LOCK_ON_DEBUG_TEXT = BUILDER
                .comment("Show lock-on debug text in the HUD while a target is locked.")
                .define("showLockOnDebugText", false);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    private FocusClientConfig() {}

    public static boolean autoSwitchToThirdPerson() {
        return AUTO_SWITCH_TO_THIRD_PERSON.get();
    }

    public static boolean allowFirstPersonWhileTargeting() {
        return ALLOW_FIRST_PERSON_WHILE_TARGETING.get();
    }

    public static boolean showLockOnDebugText() {
        return SHOW_LOCK_ON_DEBUG_TEXT.get();
    }
}
