package com.jvn.focus.client.hud;

import com.jvn.focus.Focus;
import com.jvn.focus.client.FocusClientConfig;
import com.jvn.focus.client.LockOnHandler;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@SuppressWarnings("removal")
@Mod.EventBusSubscriber(modid = Focus.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class LockOnIndicatorHudOverlay {
    private static final String INDICATOR_LAYER = "lock_on_indicator";
    private static final int SOURCE_TEXTURE_SIZE = 32;
    private static final float TARGET_HEIGHT_FACTOR = 0.75F;
    private static final String TEXTURES_PREFIX = "textures/";
    private static ResourceLocation cachedConfiguredTexture;
    private static ResourceLocation cachedResolvedTexture;

    private LockOnIndicatorHudOverlay() {}

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.CROSSHAIR.id(), INDICATOR_LAYER, LockOnIndicatorHudOverlay::render);
    }

    private static void render(ForgeGui forgeGui, GuiGraphics guiGraphics, float partialTick, int width, int height) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null || minecraft.options.hideGui) {
            return;
        }

        LivingEntity target = LockOnHandler.getLockedTarget();
        if (target == null) {
            return;
        }

        Vec3 targetPoint = target.getPosition(partialTick).add(0.0D, target.getBbHeight() * TARGET_HEIGHT_FACTOR, 0.0D);
        FocusScreenProjectionUtil.ScreenPoint projectedPoint =
                FocusScreenProjectionUtil.projectToScreen(minecraft, targetPoint, partialTick, width, height);
        if (projectedPoint == null) {
            return;
        }

        FocusClientConfig.LockOnIndicatorStyle style = FocusClientConfig.lockOnIndicatorStyle();
        ResourceLocation texture = resolveTextureResource(minecraft, style.texture());
        if (style.usesOotTriangleOrbit()) {
            float animationTicks = minecraft.level.getGameTime() + partialTick;
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            LockOnIndicatorAnimationUtil.renderOotTriangleOrbit(
                    guiGraphics,
                    projectedPoint.x(),
                    projectedPoint.y(),
                    texture,
                    style.drawSize(),
                    animationTicks,
                    SOURCE_TEXTURE_SIZE);
            RenderSystem.disableBlend();
            return;
        }
        drawCenteredIndicator(guiGraphics, projectedPoint, style, texture);
    }

    private static void drawCenteredIndicator(
            GuiGraphics guiGraphics,
            FocusScreenProjectionUtil.ScreenPoint center,
            FocusClientConfig.LockOnIndicatorStyle style,
            ResourceLocation texture) {
        int drawSize = style.drawSize();
        float halfSize = drawSize * 0.5F;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(center.x() - halfSize, center.y() - halfSize, 0.0F);
        guiGraphics.blit(
                texture,
                0,
                0,
                drawSize,
                drawSize,
                0.0F,
                0.0F,
                SOURCE_TEXTURE_SIZE,
                SOURCE_TEXTURE_SIZE,
                SOURCE_TEXTURE_SIZE,
                SOURCE_TEXTURE_SIZE);
        guiGraphics.pose().popPose();
        RenderSystem.disableBlend();
    }

    private static ResourceLocation resolveTextureResource(Minecraft minecraft, ResourceLocation configuredTexture) {
        if (configuredTexture.equals(cachedConfiguredTexture) && cachedResolvedTexture != null) {
            return cachedResolvedTexture;
        }

        ResourceLocation resolvedTexture = configuredTexture;
        if (minecraft.getResourceManager().getResource(configuredTexture).isPresent()) {
            resolvedTexture = configuredTexture;
        } else {
            String configuredPath = configuredTexture.getPath();
            ResourceLocation alternateTexture = configuredPath.startsWith(TEXTURES_PREFIX)
                    ? new ResourceLocation(configuredTexture.getNamespace(), configuredPath.substring(TEXTURES_PREFIX.length()))
                    : new ResourceLocation(configuredTexture.getNamespace(), TEXTURES_PREFIX + configuredPath);

            if (minecraft.getResourceManager().getResource(alternateTexture).isPresent()) {
                resolvedTexture = alternateTexture;
            }
        }

        cachedConfiguredTexture = configuredTexture;
        cachedResolvedTexture = resolvedTexture;
        return resolvedTexture;
    }
}
