package com.jvn.focus;

import org.slf4j.Logger;

import com.jvn.focus.client.FocusClientConfig;
import com.mojang.logging.LogUtils;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.bus.api.IEventBus;

@Mod(Focus.MOD_ID)
public final class Focus {
    public static final String MOD_ID = "focus";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Focus(IEventBus modEventBus, ModContainer modContainer) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            FocusClientConfig.init();
        }
        LOGGER.debug("Initializing {}", MOD_ID);
    }
}
