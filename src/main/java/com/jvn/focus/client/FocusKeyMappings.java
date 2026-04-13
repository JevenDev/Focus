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
    public static final KeyMapping SWAP_SHOULDER =
            new KeyMapping("key.focus.swap_shoulder", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_X, CATEGORY);
    public static final KeyMapping OPEN_CAMERA_EDITOR =
            new KeyMapping("key.focus.open_camera_editor", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, CATEGORY);
    public static final KeyMapping CAMERA_LEFT =
            new KeyMapping("key.focus.adjust_camera_left", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT, CATEGORY);
    public static final KeyMapping CAMERA_RIGHT =
            new KeyMapping("key.focus.adjust_camera_right", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT, CATEGORY);
    public static final KeyMapping CAMERA_IN =
            new KeyMapping("key.focus.adjust_camera_in", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UP, CATEGORY);
    public static final KeyMapping CAMERA_OUT =
            new KeyMapping("key.focus.adjust_camera_out", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_DOWN, CATEGORY);
    public static final KeyMapping CAMERA_UP =
            new KeyMapping("key.focus.adjust_camera_up", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_PAGE_UP, CATEGORY);
    public static final KeyMapping CAMERA_DOWN =
            new KeyMapping("key.focus.adjust_camera_down", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_PAGE_DOWN, CATEGORY);

    private FocusKeyMappings() {}

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(LOCK_ON);
        event.register(SWAP_SHOULDER);
        event.register(OPEN_CAMERA_EDITOR);
        event.register(CAMERA_LEFT);
        event.register(CAMERA_RIGHT);
        event.register(CAMERA_IN);
        event.register(CAMERA_OUT);
        event.register(CAMERA_UP);
        event.register(CAMERA_DOWN);
    }
}
