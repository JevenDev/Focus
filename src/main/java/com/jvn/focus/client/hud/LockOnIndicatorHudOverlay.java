package com.jvn.focus.client.hud;

import com.jvn.focus.Focus;
import com.jvn.focus.client.FocusClientConfig;
import com.jvn.focus.client.LockOnHandler;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import org.joml.Vector3f;

@SuppressWarnings("removal")
@EventBusSubscriber(modid = Focus.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class LockOnIndicatorHudOverlay {
    private static final ResourceLocation INDICATOR_LAYER = ResourceLocation.fromNamespaceAndPath(Focus.MOD_ID, "lock_on_indicator");
    private static final int SOURCE_TEXTURE_SIZE = 32;
    private static final double MIN_DEPTH = 0.01D;
    private static final float TARGET_HEIGHT_FACTOR = 0.75F;

    private LockOnIndicatorHudOverlay() {}

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.CROSSHAIR, INDICATOR_LAYER, LockOnIndicatorHudOverlay::render);
    }

    private static void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null || minecraft.options.hideGui) {
            return;
        }

        LivingEntity target = LockOnHandler.getLockedTarget();
        if (target == null) {
            return;
        }

        float partialTick = deltaTracker.getGameTimeDeltaPartialTick(minecraft.level.tickRateManager().runsNormally());
        Vec3 targetPoint = target.getPosition(partialTick).add(0.0D, target.getBbHeight() * TARGET_HEIGHT_FACTOR, 0.0D);
        ScreenPoint projectedPoint = projectToScreen(minecraft, targetPoint, partialTick, guiGraphics.guiWidth(), guiGraphics.guiHeight());
        if (projectedPoint == null) {
            return;
        }

        FocusClientConfig.LockOnIndicatorStyle style = FocusClientConfig.lockOnIndicatorStyle();
        if (style.usesOotTriangleOrbit()) {
            float animationTicks = minecraft.level.getGameTime() + partialTick;
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            LockOnIndicatorAnimationUtil.renderOotTriangleOrbit(
                    guiGraphics,
                    projectedPoint.x,
                    projectedPoint.y,
                    style.texture(),
                    style.drawSize(),
                    animationTicks,
                    SOURCE_TEXTURE_SIZE);
            RenderSystem.disableBlend();
            return;
        }
        drawCenteredIndicator(guiGraphics, projectedPoint, style);
    }

    private static void drawCenteredIndicator(
            GuiGraphics guiGraphics,
            ScreenPoint center,
            FocusClientConfig.LockOnIndicatorStyle style) {
        ResourceLocation texture = style.texture();
        int drawSize = style.drawSize();
        float halfSize = drawSize * 0.5F;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(center.x - halfSize, center.y - halfSize, 0.0F);
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

    private static ScreenPoint projectToScreen(Minecraft minecraft, Vec3 worldPos, float partialTick, int guiWidth, int guiHeight) {
        Camera camera = minecraft.gameRenderer.getMainCamera();
        Vec3 relativePos = worldPos.subtract(camera.getPosition());

        Vector3f leftVector = camera.getLeftVector();
        Vector3f upVector = camera.getUpVector();
        Vector3f lookVector = camera.getLookVector();

        double depth = dot(relativePos, lookVector);
        if (depth <= MIN_DEPTH) {
            return null;
        }

        double horizontal = -dot(relativePos, leftVector);
        double vertical = dot(relativePos, upVector);
        double configuredFov = minecraft.options.fov().get();
        double fovDegrees = ClientHooks.getFieldOfView(minecraft.gameRenderer, camera, partialTick, configuredFov, true);
        double tanHalfFov = Math.tan(Math.toRadians(fovDegrees * 0.5D));
        if (tanHalfFov <= 0.0D) {
            return null;
        }

        double aspectRatio = (double) guiWidth / (double) guiHeight;
        double ndcX = horizontal / (depth * tanHalfFov * aspectRatio);
        double ndcY = vertical / (depth * tanHalfFov);
        if (Math.abs(ndcX) > 1.0D || Math.abs(ndcY) > 1.0D) {
            return null;
        }

        float screenX = (float) ((ndcX + 1.0D) * 0.5D * guiWidth);
        float screenY = (float) ((1.0D - ndcY) * 0.5D * guiHeight);
        return new ScreenPoint(screenX, screenY);
    }

    private static double dot(Vec3 vector, Vector3f other) {
        return vector.x * other.x() + vector.y * other.y() + vector.z * other.z();
    }

    private record ScreenPoint(float x, float y) {}
}
