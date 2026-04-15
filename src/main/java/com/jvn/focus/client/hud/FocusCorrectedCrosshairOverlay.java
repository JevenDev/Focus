package com.jvn.focus.client.hud;

import com.jvn.focus.Focus;
import com.jvn.focus.client.FocusClientConfig;
import com.jvn.focus.client.LockOnHandler;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = Focus.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class FocusCorrectedCrosshairOverlay {
    private static final ResourceLocation CORRECTED_CROSSHAIR_LAYER =
            ResourceLocation.fromNamespaceAndPath(Focus.MOD_ID, "corrected_crosshair");
    private static final ResourceLocation CROSSHAIR_SPRITE = ResourceLocation.withDefaultNamespace("hud/crosshair");
    private static final ResourceLocation CROSSHAIR_ATTACK_INDICATOR_FULL_SPRITE =
            ResourceLocation.withDefaultNamespace("hud/crosshair_attack_indicator_full");
    private static final ResourceLocation CROSSHAIR_ATTACK_INDICATOR_BACKGROUND_SPRITE =
            ResourceLocation.withDefaultNamespace("hud/crosshair_attack_indicator_background");
    private static final ResourceLocation CROSSHAIR_ATTACK_INDICATOR_PROGRESS_SPRITE =
            ResourceLocation.withDefaultNamespace("hud/crosshair_attack_indicator_progress");
    private static final double CAMERA_RAY_DISTANCE = 64.0D;

    private FocusCorrectedCrosshairOverlay() {}

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.CROSSHAIR, CORRECTED_CROSSHAIR_LAYER, FocusCorrectedCrosshairOverlay::render);
    }

    private static void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
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
        if (FocusClientConfig.hideVanillaCrosshair()) {
            return;
        }
        if (FocusClientConfig.correctCrosshairOnlyWhileLockedOn() && lockedTarget == null) {
            return;
        }
        if (FocusClientConfig.hideVanillaCrosshairOutOfRange() && lockedTarget != null
                && !LockOnHandler.canHitLockedTarget(minecraft.player)) {
            return;
        }

        float partialTick = deltaTracker.getGameTimeDeltaPartialTick(minecraft.level.tickRateManager().runsNormally());

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

        FocusScreenProjectionUtil.ScreenPoint projected = FocusScreenProjectionUtil.projectToScreen(minecraft, correctedPoint, partialTick, guiGraphics.guiWidth(), guiGraphics.guiHeight());
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
        guiGraphics.blitSprite(CROSSHAIR_SPRITE, crosshairX, crosshairY, 15, 15);

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
                guiGraphics.blitSprite(CROSSHAIR_ATTACK_INDICATOR_FULL_SPRITE, indicatorX, indicatorY, 16, 16);
            } else if (attackStrength < 1.0F) {
                int progressWidth = (int) (attackStrength * 17.0F);
                guiGraphics.blitSprite(CROSSHAIR_ATTACK_INDICATOR_BACKGROUND_SPRITE, indicatorX, indicatorY, 16, 4);
                guiGraphics.blitSprite(
                        CROSSHAIR_ATTACK_INDICATOR_PROGRESS_SPRITE,
                        16,
                        4,
                        0,
                        0,
                        indicatorX,
                        indicatorY,
                        progressWidth,
                        4);
            }
        }

        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }
}
