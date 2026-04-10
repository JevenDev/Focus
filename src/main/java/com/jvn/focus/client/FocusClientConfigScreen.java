package com.jvn.focus.client;

import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

public final class FocusClientConfigScreen {
    private FocusClientConfigScreen() {}

    public static void register(ModContainer modContainer) {
        IConfigScreenFactory factory = (container, modListScreen) -> new ConfigurationScreen(container, modListScreen);
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, factory);
    }
}
