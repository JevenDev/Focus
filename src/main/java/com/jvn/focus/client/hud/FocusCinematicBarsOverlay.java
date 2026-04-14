package com.jvn.focus.client.hud;

import com.jvn.focus.Focus;
import com.jvn.focus.client.FocusClientConfig;
import com.jvn.focus.client.LockOnHandler;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = Focus.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class FocusCinematicBarsOverlay {
    private static final ResourceLocation CINEMATIC_BARS_LAYER_ABOVE =
            ResourceLocation.fromNamespaceAndPath(Focus.MOD_ID, "cinematic_bars_above_hud");
    private static final ResourceLocation CINEMATIC_BARS_LAYER_UNDER =
            ResourceLocation.fromNamespaceAndPath(Focus.MOD_ID, "cinematic_bars_under_hud");
    private static final float MAX_BAR_HEIGHT_FACTOR = 0.12F;
    private static final float VISIBILITY_STEP = 0.12F;
    private static float visibilityProgress;

    private FocusCinematicBarsOverlay() {}

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(CINEMATIC_BARS_LAYER_ABOVE, FocusCinematicBarsOverlay::renderAboveHud);
        event.registerBelow(VanillaGuiLayers.HOTBAR, CINEMATIC_BARS_LAYER_UNDER, FocusCinematicBarsOverlay::renderUnderHud);
    }

    private static void renderAboveHud(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        render(guiGraphics, false);
    }

    private static void renderUnderHud(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        render(guiGraphics, true);
    }

    private static void render(GuiGraphics guiGraphics, boolean underHudPass) {
        Minecraft minecraft = Minecraft.getInstance();
        if (FocusClientConfig.cinematicBarsUnderHud() != underHudPass) {
            return;
        }

        if (minecraft.player == null || minecraft.level == null) {
            visibilityProgress = 0.0F;
            return;
        }

        boolean shouldDisplay = !minecraft.options.getCameraType().isFirstPerson()
                && FocusClientConfig.cinematicBarsWhileLockedOn()
                && LockOnHandler.getLockedTarget() != null;
        float targetVisibility = shouldDisplay ? 1.0F : 0.0F;
        float delta = targetVisibility - visibilityProgress;
        float step = Mth.clamp(delta, -VISIBILITY_STEP, VISIBILITY_STEP);
        visibilityProgress = Mth.clamp(visibilityProgress + step, 0.0F, 1.0F);

        if (visibilityProgress <= 0.0F) {
            return;
        }

        float eased = easeInOut(visibilityProgress);
        int alpha = Mth.clamp(Math.round(255.0F * eased), 0, 255);
        if (alpha <= 0) {
            return;
        }

        int width = guiGraphics.guiWidth();
        int height = guiGraphics.guiHeight();
        int barHeight = Math.max(1, Math.round(height * MAX_BAR_HEIGHT_FACTOR * eased));
        int color = alpha << 24;
        guiGraphics.fill(0, 0, width, barHeight, color);
        guiGraphics.fill(0, height - barHeight, width, height, color);
    }

    private static float easeInOut(float progress) {
        return progress * progress * (3.0F - 2.0F * progress);
    }
}
