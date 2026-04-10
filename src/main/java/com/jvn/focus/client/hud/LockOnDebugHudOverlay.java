package com.jvn.focus.client.hud;

import com.jvn.focus.Focus;
import com.jvn.focus.client.FocusClientConfig;
import com.jvn.focus.client.LockOnHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@SuppressWarnings("removal")
@EventBusSubscriber(modid = Focus.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class LockOnDebugHudOverlay {
    private static final ResourceLocation DEBUG_LAYER = ResourceLocation.fromNamespaceAndPath(Focus.MOD_ID, "lock_on_debug");
    private static final int TEXT_COLOR = 0xFFFFFF;
    private static final int BASE_X = 8;
    private static final int BASE_Y = 8;
    private static final int LINE_GAP = 2;

    private LockOnDebugHudOverlay() {}

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, DEBUG_LAYER, (guiGraphics, partialTick) -> render(guiGraphics));
    }

    private static void render(GuiGraphics guiGraphics) {
        if (!FocusClientConfig.showLockOnDebugText()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }

        LivingEntity target = LockOnHandler.getLockedTarget();
        if (target == null) {
            return;
        }

        Font font = minecraft.font;
        int y = BASE_Y;
        float distance = minecraft.player.distanceTo(target);

        y = drawLine(guiGraphics, font, "Lock-On Debug", y);
        y = drawLine(guiGraphics, font, "Target: " + target.getName().getString(), y);
        y = drawLine(guiGraphics, font, String.format("Distance: %.1f", distance), y);
        y = drawLine(guiGraphics, font, "Camera: " + minecraft.options.getCameraType().name(), y);
        y = drawLine(guiGraphics, font, "Auto 3P: " + onOff(FocusClientConfig.autoSwitchToThirdPerson()), y);
        drawLine(guiGraphics, font, "Allow 1P: " + onOff(FocusClientConfig.allowFirstPersonWhileTargeting()), y);
    }

    private static int drawLine(GuiGraphics guiGraphics, Font font, String text, int y) {
        guiGraphics.drawString(font, text, BASE_X, y, TEXT_COLOR, true);
        return y + font.lineHeight + LINE_GAP;
    }

    private static String onOff(boolean value) {
        return value ? "ON" : "OFF";
    }
}
