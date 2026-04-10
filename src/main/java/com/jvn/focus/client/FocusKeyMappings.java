package com.jvn.focus.client;

import org.lwjgl.glfw.GLFW;

import com.jvn.focus.Focus;
import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@SuppressWarnings("removal")
@EventBusSubscriber(modid = Focus.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class FocusKeyMappings {
    public static final String CATEGORY = "key.categories.focus";
    public static final KeyMapping LOCK_ON =
            new KeyMapping("key.focus.lock_on", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, CATEGORY);

    private FocusKeyMappings() {}

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(LOCK_ON);
    }
}
