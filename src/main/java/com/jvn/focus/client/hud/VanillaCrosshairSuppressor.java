package com.jvn.focus.client.hud;

import com.jvn.focus.Focus;
import com.jvn.focus.client.FocusClientConfig;
import com.jvn.focus.client.LockOnHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Focus.MOD_ID, value = Dist.CLIENT)
public final class VanillaCrosshairSuppressor {

    private VanillaCrosshairSuppressor() {}

    @SubscribeEvent
    public static void onPreRenderGuiLayer(RenderGuiOverlayEvent.Pre event) {
        if (!event.getOverlay().id().equals(VanillaGuiOverlay.CROSSHAIR.id())) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.options.getCameraType().isFirstPerson()) {
            return;
        }

        if (shouldHideVanillaCrosshair()) {
            event.setCanceled(true);
            return;
        }

        if (FocusClientConfig.hideVanillaCrosshairOutOfRange()) {
            LocalPlayer player = minecraft.player;
            if (player != null && LockOnHandler.getLockedTarget() != null && !LockOnHandler.canHitLockedTarget(player)) {
                event.setCanceled(true);
            }
        }
    }

    private static boolean shouldHideVanillaCrosshair() {
        if (!FocusClientConfig.hideVanillaCrosshair()) {
            return false;
        }
        if (LockOnHandler.getLockedTarget() != null) {
            return true;
        }
        return FocusClientConfig.renderCorrectedCrosshair()
                && FocusClientConfig.crosshairCorrectionMode() != FocusClientConfig.CrosshairCorrectionMode.VANILLA
                && !FocusClientConfig.correctCrosshairOnlyWhileLockedOn();
    }
}
