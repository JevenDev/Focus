package com.jvn.focus.client.camera;

import com.jvn.focus.Focus;
import com.jvn.focus.client.LockOnHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;

@EventBusSubscriber(modid = Focus.MOD_ID, value = Dist.CLIENT)
public final class FocusPlayerVisibilityHandler {
    private static boolean alphaApplied;

    private FocusPlayerVisibilityHandler() {}

    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer localPlayer = minecraft.player;
        if (localPlayer == null || event.getEntity() != localPlayer) {
            return;
        }

        float alpha = LockOnHandler.playerTransparencyAlpha();
        if (alpha >= 0.999F) {
            return;
        }
        alphaApplied = true;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
    }

    @SubscribeEvent
    public static void onRenderPlayerPost(RenderPlayerEvent.Post event) {
        if (!alphaApplied) {
            return;
        }
        alphaApplied = false;
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
