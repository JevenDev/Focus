package com.jvn.focus;

import org.slf4j.Logger;

import com.jvn.focus.client.FocusClientConfig;
import com.mojang.logging.LogUtils;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.bus.api.IEventBus;

@Mod(Focus.MOD_ID)
public final class Focus {
    public static final String MOD_ID = "focus";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Focus(IEventBus modEventBus, ModContainer modContainer) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            // Defer config init to FMLClientSetupEvent so it does not run in parallel with
            // MidnightLib's own MidnightConfig.init() call (which happens in its mod constructor).
            // Both would otherwise race on MidnightConfig's static shared `entries` map and
            // trigger a ConcurrentModificationException.
            modEventBus.addListener(this::onClientSetup);
        }
        LOGGER.debug("Initializing {}", MOD_ID);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        FocusClientConfig.init();
    }
}
