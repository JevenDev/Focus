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
import net.minecraft.Util;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

public final class LockOnCameraEditorScreen extends Screen {
    private static final int CONTROL_WIDTH = 220;
    private static final int CONTROL_LEFT_MARGIN = 14;
    private static final int ROW_HEIGHT = 19;
    private static final int HEADER_HEIGHT = 18;
    private static final int HIDE_SHOW_BUTTON_WIDTH = 72;
    private static final int ACTION_BUTTON_HEIGHT = 18;
    private static final int CONTROL_ROW_COUNT = 11;
    private static final int VALUE_PRECISION = 1;
    private static final int CONTROLS_BOTTOM_MARGIN = 10;
    private static final long STATUS_MESSAGE_DURATION_MS = 2500L;

    private final Screen parent;
    private final CameraType previousCameraType;
    private Slider xSlider;
    private Slider ySlider;
    private Slider zSlider;
    private Slider rotationSlider;
    private Button swapShoulderButton;
    private Button customSwapValuesButton;
    private Button savePresetButton;
    private Button importPresetButton;
    private Button resetButton;
    private Button doneButton;
    private Button toggleUiButton;
    private boolean controlsVisible = true;
    private int controlsX;
    private int controlsTop;
    private int sliderWidth;
    private Component statusMessage = Component.empty();
    private long statusMessageUntilMs;
    private FocusClientConfig.Shoulder editedShoulder = FocusClientConfig.Shoulder.LEFT;

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

        editedShoulder = LockOnHandler.getActiveShoulder();
        sliderWidth = Math.min(CONTROL_WIDTH, Math.max(170, this.width - 20));
        controlsX = CONTROL_LEFT_MARGIN;
        controlsTop = Math.max(18, this.height - (ROW_HEIGHT * CONTROL_ROW_COUNT) - CONTROLS_BOTTOM_MARGIN);

        xSlider = addRenderableWidget(new Slider(
                controlsX, controlsTop + HEADER_HEIGHT + ROW_HEIGHT * 0, sliderWidth,
                "screen.focus.camera_editor.offset_x",
                FocusClientConfig.MIN_CAMERA_OFFSET_X, FocusClientConfig.MAX_CAMERA_OFFSET_X,
                () -> FocusClientConfig.cameraOffsetX(editedShoulder),
                value -> {
                    FocusClientConfig.setCameraOffsetX(editedShoulder, value);
                    FocusClientConfig.saveConfig();
                }));

        ySlider = addRenderableWidget(new Slider(
                controlsX, controlsTop + HEADER_HEIGHT + ROW_HEIGHT * 1, sliderWidth,
                "screen.focus.camera_editor.offset_y",
                FocusClientConfig.MIN_CAMERA_OFFSET_Y, FocusClientConfig.MAX_CAMERA_OFFSET_Y,
                () -> FocusClientConfig.cameraOffsetY(editedShoulder),
                value -> {
                    FocusClientConfig.setCameraOffsetY(editedShoulder, value);
                    FocusClientConfig.saveConfig();
                }));

        zSlider = addRenderableWidget(new Slider(
                controlsX, controlsTop + HEADER_HEIGHT + ROW_HEIGHT * 2, sliderWidth,
                "screen.focus.camera_editor.offset_z",
                FocusClientConfig.MIN_CAMERA_OFFSET_Z, FocusClientConfig.MAX_CAMERA_OFFSET_Z,
                () -> FocusClientConfig.cameraOffsetZ(editedShoulder),
                value -> {
                    FocusClientConfig.setCameraOffsetZ(editedShoulder, value);
                    FocusClientConfig.saveConfig();
                }));

        rotationSlider = addRenderableWidget(new Slider(
                controlsX, controlsTop + HEADER_HEIGHT + ROW_HEIGHT * 3, sliderWidth,
                "screen.focus.camera_editor.rotation",
                FocusClientConfig.MIN_CAMERA_ROTATION, FocusClientConfig.MAX_CAMERA_ROTATION,
                () -> FocusClientConfig.cameraRotation(editedShoulder),
                value -> {
                    FocusClientConfig.setCameraRotation(editedShoulder, value);
                    FocusClientConfig.saveConfig();
                }));

        int swapButtonY = controlsTop + HEADER_HEIGHT + ROW_HEIGHT * 4;
        int customValuesY = controlsTop + HEADER_HEIGHT + ROW_HEIGHT * 5;
        int presetButtonY = controlsTop + HEADER_HEIGHT + ROW_HEIGHT * 6;
        int buttonY = controlsTop + HEADER_HEIGHT + ROW_HEIGHT * 7;
        int buttonWidth = (sliderWidth - 8) / 2;
        swapShoulderButton = addRenderableWidget(Button.builder(Component.empty(), button -> swapEditedShoulder(true))
                .bounds(controlsX, swapButtonY, sliderWidth, ACTION_BUTTON_HEIGHT)
                .build());
        customSwapValuesButton = addRenderableWidget(Button.builder(Component.empty(), button -> toggleCustomSwapValues())
                .bounds(controlsX, customValuesY, sliderWidth, ACTION_BUTTON_HEIGHT)
                .build());

        savePresetButton = addRenderableWidget(Button.builder(Component.translatable("screen.focus.camera_editor.save_preset"), button -> {
            if (this.minecraft == null) {
                return;
            }
            String preset = FocusClientConfig.serializeCameraSetup(FocusClientConfig.currentCameraSetupPreset());
            this.minecraft.keyboardHandler.setClipboard(preset);
            showStatus("screen.focus.camera_editor.preset_saved");
        }).bounds(controlsX, presetButtonY, buttonWidth, ACTION_BUTTON_HEIGHT).build());

        importPresetButton = addRenderableWidget(Button.builder(Component.translatable("screen.focus.camera_editor.import_preset"), button ->
                importPresetFromClipboard()).bounds(controlsX + buttonWidth + 8, presetButtonY, buttonWidth, ACTION_BUTTON_HEIGHT).build());

        resetButton = addRenderableWidget(Button.builder(Component.translatable("screen.focus.camera_editor.reset_defaults"), button -> {
            FocusClientConfig.resetCameraOffsetsToDefaults(editedShoulder);
            FocusClientConfig.saveConfig();
            refreshSlidersFromConfig();
            showStatus("screen.focus.camera_editor.preset_reset_defaults");
        }).bounds(controlsX, buttonY, buttonWidth, ACTION_BUTTON_HEIGHT).build());

        doneButton = addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(controlsX + buttonWidth + 8, buttonY, buttonWidth, ACTION_BUTTON_HEIGHT)
                .build());

        toggleUiButton = addRenderableWidget(Button.builder(Component.empty(), button -> {
            controlsVisible = !controlsVisible;
            applyControlVisibility();
        }).bounds(controlsX, buttonY + ROW_HEIGHT, HIDE_SHOW_BUTTON_WIDTH, ACTION_BUTTON_HEIGHT).build());

        refreshShoulderButtons();
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
            if (isStatusVisible()) {
                guiGraphics.drawString(this.font, statusMessage, controlsX, controlsTop + HEADER_HEIGHT + ROW_HEIGHT * 10, 0xC8FACC, true);
            }
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
        if (keyCode == GLFW.GLFW_KEY_X) {
            swapEditedShoulder(true);
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
        if (swapShoulderButton != null) {
            swapShoulderButton.visible = controlsVisible;
            swapShoulderButton.active = controlsVisible;
        }
        if (customSwapValuesButton != null) {
            customSwapValuesButton.visible = controlsVisible;
            customSwapValuesButton.active = controlsVisible;
        }
        if (savePresetButton != null) {
            savePresetButton.visible = controlsVisible;
            savePresetButton.active = controlsVisible;
        }
        if (importPresetButton != null) {
            importPresetButton.visible = controlsVisible;
            importPresetButton.active = controlsVisible;
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

    private void importPresetFromClipboard() {
        if (this.minecraft == null) {
            return;
        }

        String clipboard = this.minecraft.keyboardHandler.getClipboard();
        if (clipboard == null || clipboard.isBlank()) {
            showStatus("screen.focus.camera_editor.preset_import_empty");
            return;
        }

        try {
            FocusClientConfig.CameraSetupPreset setup = FocusClientConfig.deserializeCameraSetup(clipboard);
            FocusClientConfig.applyCameraSetupPreset(setup);
            FocusClientConfig.saveConfig();
            refreshSlidersFromConfig();
            refreshShoulderButtons();
            showStatus("screen.focus.camera_editor.preset_imported");
        } catch (IllegalArgumentException e) {
            showStatus("screen.focus.camera_editor.preset_import_failed");
        }
    }

    private void refreshSlidersFromConfig() {
        xSlider.refreshFromConfig();
        ySlider.refreshFromConfig();
        zSlider.refreshFromConfig();
        rotationSlider.refreshFromConfig();
    }

    private void showStatus(String translationKey) {
        showStatus(translationKey, new Object[0]);
    }

    private void showStatus(String translationKey, Object... args) {
        statusMessage = Component.translatable(translationKey, args);
        statusMessageUntilMs = Util.getMillis() + STATUS_MESSAGE_DURATION_MS;
    }

    private boolean isStatusVisible() {
        return Util.getMillis() <= statusMessageUntilMs && !statusMessage.getString().isEmpty();
    }

    private void refreshShoulderButtons() {
        if (swapShoulderButton != null) {
            swapShoulderButton.setMessage(Component.translatable("screen.focus.camera_editor.swap_shoulder", editedShoulder.displayName()));
        }
        if (customSwapValuesButton != null) {
            customSwapValuesButton.setMessage(Component.translatable(
                    "screen.focus.camera_editor.custom_swap_values",
                    FocusClientConfig.useCustomSwappedShoulderValues() ? "x" : " "));
        }
    }

    private void swapEditedShoulder(boolean showMessage) {
        editedShoulder = editedShoulder.opposite();
        LockOnHandler.setActiveShoulder(editedShoulder);
        refreshSlidersFromConfig();
        refreshShoulderButtons();
        if (showMessage) {
            showStatus("screen.focus.camera_editor.shoulder_swapped", editedShoulder.displayName());
        }
    }

    private void toggleCustomSwapValues() {
        boolean useCustomValues = !FocusClientConfig.useCustomSwappedShoulderValues();
        FocusClientConfig.setUseCustomSwappedShoulderValues(useCustomValues, editedShoulder);
        FocusClientConfig.saveConfig();
        refreshSlidersFromConfig();
        refreshShoulderButtons();
        showStatus(useCustomValues
                        ? "screen.focus.camera_editor.custom_swap_values_enabled"
                        : "screen.focus.camera_editor.custom_swap_values_disabled");
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
