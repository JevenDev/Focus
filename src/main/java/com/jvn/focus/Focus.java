package com.jvn.focus;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.fml.common.Mod;

@Mod(Focus.MOD_ID)
public final class Focus {
    public static final String MOD_ID = "focus";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Focus() {
        LOGGER.debug("Initializing {}", MOD_ID);
    }
}
