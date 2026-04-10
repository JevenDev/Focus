package com.jvn.focus.client;

import java.util.Locale;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

public final class LockOnCameraEditorScreen extends Screen {
    private static final int CONTROL_WIDTH = 220;
    private static final int CONTROL_LEFT_MARGIN = 14;
    private static final int ROW_HEIGHT = 19;
    private static final int HEADER_HEIGHT = 18;
    private static final int HIDE_SHOW_BUTTON_WIDTH = 72;
    private static final int ACTION_BUTTON_HEIGHT = 18;
    private static final int VALUE_PRECISION = 2;
    private static final int CONTROLS_BOTTOM_MARGIN = 10;

    private final Screen parent;
    private final CameraType previousCameraType;
    private Slider xSlider;
    private Slider ySlider;
    private Slider zSlider;
    private Slider rotationSlider;
    private Button resetButton;
    private Button doneButton;
    private Button toggleUiButton;
    private boolean controlsVisible = true;
    private int controlsX;
    private int controlsTop;
    private int sliderWidth;

    private LockOnCameraEditorScreen(Screen parent, CameraType previousCameraType) {
        super(Component.translatable("screen.focus.camera_editor.title"));
        this.parent = parent;
        this.previousCameraType = previousCameraType;
    }

    public static void openFromCurrentScreen() {
        Minecraft minecraft = Minecraft.getInstance();
        Screen parent = minecraft.screen;
        CameraType previous = minecraft.options.getCameraType();
        minecraft.setScreen(new LockOnCameraEditorScreen(parent, previous));
    }

    @Override
    protected void init() {
        super.init();
        LockOnHandler.startCameraEditorPreview();
        // Ensure we render directly over the live game view with no inherited blur.
        if (this.minecraft != null) {
            this.minecraft.gameRenderer.shutdownEffect();
        }

        sliderWidth = Math.min(CONTROL_WIDTH, Math.max(170, this.width - 20));
        controlsX = CONTROL_LEFT_MARGIN;
        controlsTop = Math.max(18, this.height - (ROW_HEIGHT * 7) - CONTROLS_BOTTOM_MARGIN);

        xSlider = addRenderableWidget(new Slider(
                controlsX, controlsTop + HEADER_HEIGHT + ROW_HEIGHT * 0, sliderWidth,
                "screen.focus.camera_editor.offset_x",
                FocusClientConfig.MIN_CAMERA_OFFSET_X, FocusClientConfig.MAX_CAMERA_OFFSET_X,
                FocusClientConfig::cameraOffsetX,
                value -> {
                    FocusClientConfig.setCameraOffsetX(value);
                    FocusClientConfig.saveConfig();
                }));

        ySlider = addRenderableWidget(new Slider(
                controlsX, controlsTop + HEADER_HEIGHT + ROW_HEIGHT * 1, sliderWidth,
                "screen.focus.camera_editor.offset_y",
                FocusClientConfig.MIN_CAMERA_OFFSET_Y, FocusClientConfig.MAX_CAMERA_OFFSET_Y,
                FocusClientConfig::cameraOffsetY,
                value -> {
                    FocusClientConfig.setCameraOffsetY(value);
                    FocusClientConfig.saveConfig();
                }));

        zSlider = addRenderableWidget(new Slider(
                controlsX, controlsTop + HEADER_HEIGHT + ROW_HEIGHT * 2, sliderWidth,
                "screen.focus.camera_editor.offset_z",
                FocusClientConfig.MIN_CAMERA_OFFSET_Z, FocusClientConfig.MAX_CAMERA_OFFSET_Z,
                FocusClientConfig::cameraOffsetZ,
                value -> {
                    FocusClientConfig.setCameraOffsetZ(value);
                    FocusClientConfig.saveConfig();
                }));

        rotationSlider = addRenderableWidget(new Slider(
                controlsX, controlsTop + HEADER_HEIGHT + ROW_HEIGHT * 3, sliderWidth,
                "screen.focus.camera_editor.rotation",
                FocusClientConfig.MIN_CAMERA_ROTATION, FocusClientConfig.MAX_CAMERA_ROTATION,
                FocusClientConfig::cameraRotation,
                value -> {
                    FocusClientConfig.setCameraRotation(value);
                    FocusClientConfig.saveConfig();
                }));

        int buttonY = controlsTop + HEADER_HEIGHT + ROW_HEIGHT * 4;
        int buttonWidth = (sliderWidth - 8) / 2;
        resetButton = addRenderableWidget(Button.builder(Component.translatable("screen.focus.camera_editor.reset_defaults"), button -> {
            FocusClientConfig.resetCameraOffsetsToDefaults();
            FocusClientConfig.saveConfig();
            xSlider.refreshFromConfig();
            ySlider.refreshFromConfig();
            zSlider.refreshFromConfig();
            rotationSlider.refreshFromConfig();
        }).bounds(controlsX, buttonY, buttonWidth, ACTION_BUTTON_HEIGHT).build());

        doneButton = addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(controlsX + buttonWidth + 8, buttonY, buttonWidth, ACTION_BUTTON_HEIGHT)
                .build());

        toggleUiButton = addRenderableWidget(Button.builder(Component.empty(), button -> {
            controlsVisible = !controlsVisible;
            applyControlVisibility();
        }).bounds(controlsX, buttonY + ROW_HEIGHT, HIDE_SHOW_BUTTON_WIDTH, ACTION_BUTTON_HEIGHT).build());

        applyControlVisibility();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        LockOnHandler.stopCameraEditorPreview(previousCameraType);
        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (controlsVisible) {
            guiGraphics.drawString(this.font, this.title, controlsX, controlsTop - 20, 0xFFFFFF, true);
            guiGraphics.drawString(this.font, Component.translatable("screen.focus.camera_editor.preview_hint"),
                    controlsX, controlsTop - 8, 0xD0D0D0, true);
        } else {
            guiGraphics.drawString(this.font, Component.translatable("screen.focus.camera_editor.preview_hint_minimized"),
                    controlsX + HIDE_SHOW_BUTTON_WIDTH + 6, this.height - CONTROLS_BOTTOM_MARGIN - ACTION_BUTTON_HEIGHT + 6, 0xD0D0D0, true);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Intentionally no background layer so the world stays fully visible for live camera preview.
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_H) {
            controlsVisible = !controlsVisible;
            applyControlVisibility();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void applyControlVisibility() {
        if (toggleUiButton != null) {
            toggleUiButton.setMessage(controlsVisible
                    ? Component.translatable("screen.focus.camera_editor.hide_ui")
                    : Component.translatable("screen.focus.camera_editor.show_ui"));
        }

        if (xSlider != null) {
            xSlider.visible = controlsVisible;
            xSlider.active = controlsVisible;
        }
        if (ySlider != null) {
            ySlider.visible = controlsVisible;
            ySlider.active = controlsVisible;
        }
        if (zSlider != null) {
            zSlider.visible = controlsVisible;
            zSlider.active = controlsVisible;
        }
        if (rotationSlider != null) {
            rotationSlider.visible = controlsVisible;
            rotationSlider.active = controlsVisible;
        }
        if (resetButton != null) {
            resetButton.visible = controlsVisible;
            resetButton.active = controlsVisible;
        }
        if (doneButton != null) {
            doneButton.visible = controlsVisible;
            doneButton.active = controlsVisible;
        }
    }

    private static final class Slider extends AbstractSliderButton {
        private final String labelKey;
        private final double min;
        private final double max;
        private final DoubleSupplier getter;
        private final DoubleConsumer setter;

        private Slider(int x, int y, int width, String labelKey, double min, double max, DoubleSupplier getter, DoubleConsumer setter) {
            super(x, y, width, 16, Component.empty(), 0.0D);
            this.labelKey = labelKey;
            this.min = min;
            this.max = max;
            this.getter = getter;
            this.setter = setter;
            refreshFromConfig();
        }

        private void refreshFromConfig() {
            this.value = toSliderValue(getter.getAsDouble());
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            this.setMessage(Component.translatable(labelKey, format(actualValue())));
        }

        @Override
        protected void applyValue() {
            setter.accept(actualValue());
        }

        private double actualValue() {
            return Mth.clampedLerp(min, max, value);
        }

        private double toSliderValue(double configuredValue) {
            if (max <= min) {
                return 0.0D;
            }
            return Mth.clamp((configuredValue - min) / (max - min), 0.0D, 1.0D);
        }

        private static String format(double value) {
            return String.format(Locale.ROOT, "%." + VALUE_PRECISION + "f", value);
        }
    }
}
