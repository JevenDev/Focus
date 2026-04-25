package com.jvn.focus.client;

import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public final class FocusClientBootstrap {
    private FocusClientBootstrap() {}

    public static void init() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(FocusClientBootstrap::onClientSetup);
        ModLoadingContext.get().registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(FocusClientConfig::createConfigScreen));
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        FocusClientConfig.init();
    }
}
