package com.jvn.focus.client.hud;

import com.jvn.focus.Focus;
import com.jvn.focus.client.FocusClientConfig;
import com.jvn.focus.client.LockOnHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = Focus.MOD_ID, value = Dist.CLIENT)
public final class VanillaCrosshairSuppressor {

    private VanillaCrosshairSuppressor() {}

    @SubscribeEvent
    public static void onPreRenderGuiLayer(RenderGuiLayerEvent.Pre event) {
        if (!event.getName().equals(VanillaGuiLayers.CROSSHAIR)) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.options.getCameraType().isFirstPerson()) {
            return;
        }

        if (FocusClientConfig.hideVanillaCrosshair()) {
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
}
