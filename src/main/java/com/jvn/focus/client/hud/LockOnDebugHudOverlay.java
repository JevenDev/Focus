package com.jvn.focus.client.hud;

import com.jvn.focus.Focus;
import com.jvn.focus.client.FocusClientConfig;
import com.jvn.focus.client.LockOnHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@SuppressWarnings("removal")
@Mod.EventBusSubscriber(modid = Focus.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class LockOnDebugHudOverlay {
    private static final String DEBUG_LAYER = "lock_on_debug";
    private static final int TEXT_COLOR = 0xFFFFFF;
    private static final int BASE_LEFT_MARGIN = 8;
    private static final int BASE_RIGHT_MARGIN = 8;
    private static final int BASE_Y = 8;
    private static final int LINE_GAP = 2;
    private static final double EQUALITY_TOLERANCE = 1.0E-4D;

    private LockOnDebugHudOverlay() {}

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), DEBUG_LAYER, LockOnDebugHudOverlay::render);
    }

    private static void render(ForgeGui forgeGui, GuiGraphics guiGraphics, float partialTick, int width, int height) {
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
        int leftY = BASE_Y;
        int rightY = BASE_Y;
        FocusClientConfig.Shoulder activeShoulder = LockOnHandler.getActiveShoulder();
        FocusClientConfig.Shoulder displayedShoulder = LockOnHandler.getDisplayedShoulder();
        FocusClientConfig.PerspectivePreset leftPreset = FocusClientConfig.currentPreset(FocusClientConfig.Shoulder.LEFT);
        FocusClientConfig.PerspectivePreset rightPreset = FocusClientConfig.currentPreset(FocusClientConfig.Shoulder.RIGHT);
        float distance = player.distanceTo(target);
        boolean hasLineOfSight = LockOnHandler.hasLineOfSightToLockedTarget(player);
        boolean inHitRange = LockOnHandler.isLockedTargetWithinHitRange(player);
        boolean canHitNow = LockOnHandler.canHitLockedTarget(player);

        leftY = drawLeftLine(guiGraphics, font, Component.translatable("debug.focus.lock_on.title"), leftY);
        leftY = drawLeftLine(guiGraphics, font, Component.translatable("debug.focus.lock_on.target", target.getName()), leftY);
        leftY = drawLeftLine(guiGraphics, font, Component.translatable("debug.focus.lock_on.distance", String.format("%.1f", distance)), leftY);
        leftY = drawLeftLine(guiGraphics, font, Component.translatable("debug.focus.lock_on.camera", minecraft.options.getCameraType().name()), leftY);
        leftY = drawLeftLine(guiGraphics, font, Component.translatable(
                "debug.focus.lock_on.camera_mode",
                Component.translatable("focus.lock_on_client.camera_mode." + FocusClientConfig.cameraMode().name())),
                leftY);
        leftY = drawLeftLine(guiGraphics, font, Component.translatable("debug.focus.lock_on.auto_third_person", onOff(FocusClientConfig.autoSwitchToThirdPerson())), leftY);
        leftY = drawLeftLine(guiGraphics, font, Component.translatable("debug.focus.lock_on.allow_first_person", onOff(FocusClientConfig.allowFirstPersonWhileTargeting())), leftY);
        leftY = drawLeftLine(guiGraphics, font, Component.translatable("debug.focus.lock_on.allow_front_facing_third_person", onOff(FocusClientConfig.allowFrontFacingThirdPersonWhileTargeting())), leftY);
        leftY = drawLeftLine(guiGraphics, font, Component.translatable("debug.focus.lock_on.editor_preview", onOff(LockOnHandler.isCameraEditorPreviewActive())), leftY);
        leftY = drawLeftLine(guiGraphics, font, Component.translatable("debug.focus.lock_on.shoulder", activeShoulder.displayName()), leftY);
        if (activeShoulder != displayedShoulder) {
            leftY = drawLeftLine(guiGraphics, font, Component.translatable("debug.focus.lock_on.displayed_shoulder", displayedShoulder.displayName()), leftY);
        }
        leftY = drawLeftLine(guiGraphics, font, Component.translatable("debug.focus.lock_on.can_hit", yesNo(canHitNow)), leftY);
        leftY = drawLeftLine(guiGraphics, font, Component.translatable("debug.focus.lock_on.in_range", yesNo(inHitRange)), leftY);
        drawLeftLine(guiGraphics, font, Component.translatable("debug.focus.lock_on.has_line_of_sight", yesNo(hasLineOfSight)), leftY);

        rightY = drawRightLine(guiGraphics, font, Component.translatable("debug.focus.lock_on.custom_swap_values", onOff(FocusClientConfig.useCustomSwappedShoulderValues())), rightY);
        rightY = drawRightLine(guiGraphics, font, Component.translatable("debug.focus.lock_on.camera_floatiness", format(FocusClientConfig.cameraFloatiness())), rightY);
        rightY = drawRightLine(guiGraphics, font, Component.translatable("debug.focus.lock_on.camera_drag", format(FocusClientConfig.cameraDrag())), rightY);
        rightY = drawRightLine(guiGraphics, font, Component.translatable("debug.focus.lock_on.swap_speed", format(FocusClientConfig.cameraSwapSpeed())), rightY);
        rightY = drawRightLine(guiGraphics, font, Component.translatable("debug.focus.lock_on.swap_smoothness", format(FocusClientConfig.cameraSwapSmoothness())), rightY);
        rightY = drawRightLine(guiGraphics, font, Component.translatable("debug.focus.lock_on.dynamic_swap_speed", format(FocusClientConfig.dynamicCameraSwapSpeed())), rightY);
        rightY = drawRightLine(guiGraphics, font, Component.translatable("debug.focus.lock_on.dynamic_swap_smoothness", format(FocusClientConfig.dynamicCameraSwapSmoothness())), rightY);
        rightY = drawRightLine(guiGraphics, font, Component.translatable("debug.focus.lock_on.camera_step_size", formatPrecise(FocusClientConfig.cameraStepSize())), rightY);
        rightY = drawRightLine(guiGraphics, font, Component.translatable("debug.focus.lock_on.dynamic_offsets", onOff(FocusClientConfig.dynamicallyAdjustOffsets())), rightY);
        rightY = drawRightLine(guiGraphics, font, Component.translatable("debug.focus.lock_on.swap_player_follow", format(FocusClientConfig.targetSwapPlayerLookFollow())), rightY);
        rightY = drawRightLine(guiGraphics, font, Component.translatable("debug.focus.lock_on.swap_blend", format(LockOnHandler.getTargetSwapBlendToNormal())), rightY);
        if (FocusClientConfig.cameraMode() == FocusClientConfig.CameraMode.DYNAMIC) {
            rightY = drawRightLine(guiGraphics, font, Component.translatable("debug.focus.lock_on.dynamic_blend", format(LockOnHandler.getDynamicAutoCurrentBlend())), rightY);
        } else {
            rightY = drawRightLine(guiGraphics, font, Component.translatable("debug.focus.lock_on.static_blend", format(LockOnHandler.getStaticSwapBlend())), rightY);
        }
        rightY = drawRightLine(guiGraphics, font, Component.translatable("debug.focus.lock_on.swap_camera_pos_factor", format(LockOnHandler.getTargetSwapCameraPositionFactor())), rightY);
        rightY = drawRightLine(guiGraphics, font, Component.translatable("debug.focus.lock_on.swap_camera_rot_factor", format(LockOnHandler.getTargetSwapCameraRotationFactor())), rightY);
        rightY = drawShoulderAwareRightValue(guiGraphics, font, rightY,
                leftPreset.offsetX(),
                rightPreset.offsetX(),
                "debug.focus.lock_on.offset_x",
                "debug.focus.lock_on.offset_x_left",
                "debug.focus.lock_on.offset_x_right");
        rightY = drawShoulderAwareRightValue(guiGraphics, font, rightY,
                leftPreset.offsetY(),
                rightPreset.offsetY(),
                "debug.focus.lock_on.offset_y",
                "debug.focus.lock_on.offset_y_left",
                "debug.focus.lock_on.offset_y_right");
        rightY = drawShoulderAwareRightValue(guiGraphics, font, rightY,
                leftPreset.offsetZ(),
                rightPreset.offsetZ(),
                "debug.focus.lock_on.offset_z",
                "debug.focus.lock_on.offset_z_left",
                "debug.focus.lock_on.offset_z_right");
        drawShoulderAwareRightValue(guiGraphics, font, rightY,
                leftPreset.rotation(),
                rightPreset.rotation(),
                "debug.focus.lock_on.rotation",
                "debug.focus.lock_on.rotation_left",
                "debug.focus.lock_on.rotation_right");
    }

    private static int drawLeftLine(GuiGraphics guiGraphics, Font font, Component text, int y) {
        guiGraphics.drawString(font, text, BASE_LEFT_MARGIN, y, TEXT_COLOR, true);
        return y + font.lineHeight + LINE_GAP;
    }

    private static int drawRightLine(GuiGraphics guiGraphics, Font font, Component text, int y) {
        int x = guiGraphics.guiWidth() - BASE_RIGHT_MARGIN - font.width(text);
        guiGraphics.drawString(font, text, x, y, TEXT_COLOR, true);
        return y + font.lineHeight + LINE_GAP;
    }

    private static int drawShoulderAwareRightValue(
            GuiGraphics guiGraphics,
            Font font,
            int y,
            double leftValue,
            double rightValue,
            String singleKey,
            String leftKey,
            String rightKey) {
        if (Math.abs(leftValue - rightValue) <= EQUALITY_TOLERANCE) {
            return drawRightLine(guiGraphics, font, Component.translatable(singleKey, format(leftValue)), y);
        }

        int nextY = drawRightLine(guiGraphics, font, Component.translatable(leftKey, format(leftValue)), y);
        return drawRightLine(guiGraphics, font, Component.translatable(rightKey, format(rightValue)), nextY);
    }

    private static Component onOff(boolean value) {
        return Component.translatable(value ? "debug.focus.lock_on.on" : "debug.focus.lock_on.off");
    }

    private static Component yesNo(boolean value) {
        return Component.translatable(value ? "debug.focus.lock_on.yes" : "debug.focus.lock_on.no");
    }

    private static String format(double value) {
        return String.format(java.util.Locale.ROOT, "%.1f", value);
    }

    private static String formatPrecise(double value) {
        return String.format(java.util.Locale.ROOT, "%.3f", value);
    }
}
