package com.jvn.focus.client.hud;

import com.jvn.focus.Focus;
import com.jvn.focus.client.FocusClientConfig;
import com.jvn.focus.client.LockOnHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
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
        LocalPlayer player = minecraft.player;
        if (player == null) {
            return;
        }

        LivingEntity target = LockOnHandler.getLockedTarget();
        if (target == null) {
            return;
        }

        Font font = minecraft.font;
        int y = BASE_Y;
        float distance = player.distanceTo(target);
        boolean hasLineOfSight = LockOnHandler.hasLineOfSightToLockedTarget(player);
        boolean inHitRange = LockOnHandler.isLockedTargetWithinHitRange(player);
        boolean canHitNow = LockOnHandler.canHitLockedTarget(player);

        y = drawLine(guiGraphics, font, Component.translatable("debug.focus.lock_on.title"), y);
        y = drawLine(guiGraphics, font, Component.translatable("debug.focus.lock_on.target", target.getName()), y);
        y = drawLine(guiGraphics, font, Component.translatable("debug.focus.lock_on.distance", String.format("%.1f", distance)), y);
        y = drawLine(guiGraphics, font, Component.translatable("debug.focus.lock_on.camera", minecraft.options.getCameraType().name()), y);
        y = drawLine(guiGraphics, font, Component.translatable("debug.focus.lock_on.auto_third_person", onOff(FocusClientConfig.autoSwitchToThirdPerson())), y);
        y = drawLine(guiGraphics, font, Component.translatable("debug.focus.lock_on.allow_first_person", onOff(FocusClientConfig.allowFirstPersonWhileTargeting())), y);
        y = drawLine(guiGraphics, font, Component.translatable("debug.focus.lock_on.allow_front_facing_third_person", onOff(FocusClientConfig.allowFrontFacingThirdPersonWhileTargeting())), y);
        y = drawLine(guiGraphics, font, Component.translatable("debug.focus.lock_on.can_hit", yesNo(canHitNow)), y);
        y = drawLine(guiGraphics, font, Component.translatable("debug.focus.lock_on.in_range", yesNo(inHitRange)), y);
        drawLine(guiGraphics, font, Component.translatable("debug.focus.lock_on.has_line_of_sight", yesNo(hasLineOfSight)), y);
    }

    private static int drawLine(GuiGraphics guiGraphics, Font font, Component text, int y) {
        guiGraphics.drawString(font, text, BASE_X, y, TEXT_COLOR, true);
        return y + font.lineHeight + LINE_GAP;
    }

    private static Component onOff(boolean value) {
        return Component.translatable(value ? "debug.focus.lock_on.on" : "debug.focus.lock_on.off");
    }

    private static Component yesNo(boolean value) {
        return Component.translatable(value ? "debug.focus.lock_on.yes" : "debug.focus.lock_on.no");
    }
}
