package com.jvn.focus.client;

import com.jvn.focus.Focus;

import me.fzzyhmstrs.fzzy_config.api.ConfigApiJava;
import me.fzzyhmstrs.fzzy_config.api.RegisterType;
import me.fzzyhmstrs.fzzy_config.config.Config;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedBoolean;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedCondition;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class FocusClientConfig extends Config {
    private static FocusClientConfig INSTANCE;

    public ValidatedBoolean autoSwitchToThirdPerson = new ValidatedBoolean(true);
    public ValidatedBoolean allowFirstPersonWhileTargeting = new ValidatedBoolean(false);
    public ValidatedCondition<Boolean> allowFrontFacingThirdPersonWhileTargeting =
            new ValidatedBoolean(false).toCondition(
                    allowFirstPersonWhileTargeting,
                    Component.translatable("focus.lock_on_client.allowFrontFacingThirdPersonWhileTargeting.condition"),
                    () -> false);
    public ValidatedBoolean showLockOnDebugText = new ValidatedBoolean(false);

    public FocusClientConfig() {
        super(ResourceLocation.fromNamespaceAndPath(Focus.MOD_ID, "lock_on_client"));
    }

    public static void init() {
        if (INSTANCE == null) {
            INSTANCE = ConfigApiJava.registerAndLoadConfig(FocusClientConfig::new, RegisterType.CLIENT);
        }
    }

    public static boolean autoSwitchToThirdPerson() {
        return config().autoSwitchToThirdPerson.get();
    }

    public static boolean allowFirstPersonWhileTargeting() {
        return config().allowFirstPersonWhileTargeting.get();
    }

    public static boolean allowFrontFacingThirdPersonWhileTargeting() {
        return config().allowFrontFacingThirdPersonWhileTargeting.get();
    }

    public static boolean showLockOnDebugText() {
        return config().showLockOnDebugText.get();
    }

    private static FocusClientConfig config() {
        if (INSTANCE == null) {
            init();
        }
        return INSTANCE;
    }
}
