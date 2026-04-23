package com.jvn.focus.client.hud;

import com.jvn.focus.Focus;
import com.jvn.focus.client.FocusClientConfig;
import com.jvn.focus.client.LockOnHandler;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Focus.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class FocusCorrectedCrosshairOverlay {
    private static final String CORRECTED_CROSSHAIR_LAYER = "corrected_crosshair";
    private static final ResourceLocation GUI_ICONS_LOCATION = new ResourceLocation("textures/gui/icons.png");
    private static final double CAMERA_RAY_DISTANCE = 64.0D;

    private FocusCorrectedCrosshairOverlay() {}

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.CROSSHAIR.id(), CORRECTED_CROSSHAIR_LAYER, FocusCorrectedCrosshairOverlay::render);
    }

    private static void render(ForgeGui forgeGui, GuiGraphics guiGraphics, float partialTick, int width, int height) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null || minecraft.options.hideGui) {
            return;
        }
        if (minecraft.options.getCameraType().isFirstPerson()) {
            return;
        }
        if (!FocusClientConfig.renderCorrectedCrosshair()) {
            return;
        }
        if (FocusClientConfig.crosshairCorrectionMode() == FocusClientConfig.CrosshairCorrectionMode.VANILLA) {
            return;
        }

        LivingEntity lockedTarget = LockOnHandler.getLockedTarget();
        if (FocusClientConfig.correctCrosshairOnlyWhileLockedOn() && lockedTarget == null) {
            return;
        }
        if (FocusClientConfig.hideVanillaCrosshairOutOfRange() && lockedTarget != null
                && !LockOnHandler.canHitLockedTarget(minecraft.player)) {
            return;
        }

        Vec3 correctedPoint;
        if (FocusClientConfig.hideVanillaCrosshairOutOfRange() && lockedTarget != null) {
            // Snap directly to the target center so the crosshair appears on the
            // target the instant it enters range instead of sliding in from the
            // hit-result position.
            correctedPoint = lockedTarget.getPosition(partialTick)
                    .add(0, lockedTarget.getBbHeight() * 0.5, 0);
        } else {
            correctedPoint = resolveCorrectedPoint(minecraft, lockedTarget, partialTick);
        }
        if (correctedPoint == null) {
            return;
        }

        FocusScreenProjectionUtil.ScreenPoint projected =
                FocusScreenProjectionUtil.projectToScreen(minecraft, correctedPoint, partialTick, width, height);
        if (projected == null) {
            return;
        }

        renderVanillaStyleCrosshair(guiGraphics, minecraft, projected);
    }

    private static Vec3 resolveCorrectedPoint(Minecraft minecraft, LivingEntity lockedTarget, float partialTick) {
        // Keep overlay aligned with the same corrected pick result used by interactions.
        if (minecraft.hitResult != null && minecraft.hitResult.getType() != HitResult.Type.MISS) {
            return minecraft.hitResult.getLocation();
        }

        if (!FocusClientConfig.correctBlockPlacementRay() && !FocusClientConfig.correctEntityHitRay()) {
            return null;
        }

        Camera camera = minecraft.gameRenderer.getMainCamera();
        Vec3 from = minecraft.player.getEyePosition(partialTick);
        Vec3 look = FocusScreenProjectionUtil.vectorToVec3(camera.getLookVector()).normalize();
        Vec3 to = from.add(look.scale(CAMERA_RAY_DISTANCE));
        BlockHitResult hitResult = minecraft.level.clip(new ClipContext(from, to, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, minecraft.player));
        if (hitResult.getType() == HitResult.Type.MISS) {
            return to;
        }
        return hitResult.getLocation();
    }

    private static void renderVanillaStyleCrosshair(GuiGraphics guiGraphics, Minecraft minecraft, FocusScreenProjectionUtil.ScreenPoint point) {
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR,
                GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);

        int crosshairX = Mth.floor(point.x() - 7.5F);
        int crosshairY = Mth.floor(point.y() - 7.5F);
        guiGraphics.blit(GUI_ICONS_LOCATION, crosshairX, crosshairY, 0, 0, 15, 15);

        if (minecraft.options.attackIndicator().get() == AttackIndicatorStatus.CROSSHAIR) {
            float attackStrength = minecraft.player.getAttackStrengthScale(0.0F);
            boolean showFullIndicator = false;
            if (minecraft.crosshairPickEntity instanceof LivingEntity livingTarget && attackStrength >= 1.0F) {
                showFullIndicator = minecraft.player.getCurrentItemAttackStrengthDelay() > 5.0F;
                showFullIndicator &= livingTarget.isAlive();
            }

            int centerX = Mth.floor(point.x());
            int centerY = Mth.floor(point.y());
            int indicatorY = centerY - 7 + 16;
            int indicatorX = centerX - 8;
            if (showFullIndicator) {
                guiGraphics.blit(GUI_ICONS_LOCATION, indicatorX, indicatorY, 68, 94, 16, 16);
            } else if (attackStrength < 1.0F) {
                int progressWidth = (int) (attackStrength * 17.0F);
                guiGraphics.blit(GUI_ICONS_LOCATION, indicatorX, indicatorY, 36, 94, 16, 4);
                guiGraphics.blit(GUI_ICONS_LOCATION, indicatorX, indicatorY, 52, 94, progressWidth, 4);
            }
        }

        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }
}
