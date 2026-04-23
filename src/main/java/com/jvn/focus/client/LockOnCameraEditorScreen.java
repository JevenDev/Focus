package com.jvn.focus.client;

import java.util.List;
import java.util.Locale;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.Util;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

public final class LockOnCameraEditorScreen extends Screen {
    private static final int CONTROL_WIDTH = 220;
    private static final int CONTROL_LEFT_MARGIN = 14;
    private static final int ROW_HEIGHT = 20;
    private static final int HEADER_HEIGHT = 26;
    private static final int HIDE_SHOW_BUTTON_WIDTH = 72;
    private static final int PRESETS_TOGGLE_BUTTON_WIDTH = 96;
    private static final int ACTION_BUTTON_HEIGHT = 18;
    private static final int CONTROL_GAP = ROW_HEIGHT - ACTION_BUTTON_HEIGHT;
    private static final int GROUP_GAP = 6;
    private static final int PANEL_PADDING = 8;
    private static final int PRESETS_PANEL_DEFAULT_WIDTH = 238;
    private static final int VALUE_PRECISION = 3;
    private static final int CONTROLS_BOTTOM_MARGIN = 10;
    private static final long STATUS_MESSAGE_DURATION_MS = 2500L;
    private static final double KEY_ADJUST_STEP_MULTIPLIER = 5.0D;

    private final Screen parent;
    private final CameraType previousCameraType;
    private Slider xSlider;
    private Slider ySlider;
    private Slider zSlider;
    private Slider rotationSlider;
    private Button swapShoulderButton;
    private Button customSwapValuesButton;
    private Button togglePresetsPanelButton;
    private EditBox profileNameInput;
    private Button saveProfileButton;
    private Button loadProfileButton;
    private Button deleteProfileButton;
    private Button cycleProfileButton;
    private Button savePresetButton;
    private Button importPresetButton;
    private Button resetButton;
    private Button doneButton;
    private Button toggleUiButton;
    private Button followRotationsButton;
    private boolean controlsVisible = true;
    private boolean presetsPanelExpanded;
    private int controlsX;
    private int controlsTop;
    private int sliderWidth;
    private int presetsPanelX;
    private int presetsPanelTop;
    private int presetsPanelWidth;
    private int statusMessageY;
    private int hideUiY;
    private int panelBackdropLeft;
    private int panelBackdropTop;
    private int panelBackdropRight;
    private int panelBackdropBottom;
    private int headerHeight;
    private int halfButtonWidth;
    private Component statusMessage = Component.empty();
    private long statusMessageUntilMs;
    private FocusClientConfig.Shoulder editedShoulder = FocusClientConfig.Shoulder.LEFT;
    private boolean previewStopped;

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

        // Compute dynamic header height — taller if hint text wraps within slider width
        List<FormattedCharSequence> adjustHintLines = this.font.split(
                Component.translatable("screen.focus.camera_editor.adjust_hint"), sliderWidth);
        int extraHintLines = Math.max(0, adjustHintLines.size() - 1);
        headerHeight = HEADER_HEIGHT + extraHintLines * (this.font.lineHeight + 1);

        // Layout: header + 4 sliders + gap + 3 toggles + gap + actions + gap + hide-ui
        int panelContentHeight = headerHeight + ROW_HEIGHT * 8 + GROUP_GAP * 2;
        int totalContentHeight = panelContentHeight + GROUP_GAP + ROW_HEIGHT;
        controlsTop = Math.max(4, this.height - totalContentHeight - CONTROLS_BOTTOM_MARGIN);

        // Compute section Y positions
        int firstRowY = controlsTop + headerHeight;
        int sliderGroupY = firstRowY;
        int toggleGroupY = sliderGroupY + ROW_HEIGHT * 4 + GROUP_GAP;
        int actionGroupY = toggleGroupY + ROW_HEIGHT * 3 + GROUP_GAP;
        hideUiY = actionGroupY + ROW_HEIGHT + GROUP_GAP;
        statusMessageY = actionGroupY + ACTION_BUTTON_HEIGHT + 3;

        // Panel backdrop bounds
        panelBackdropLeft = controlsX - PANEL_PADDING;
        panelBackdropTop = controlsTop - PANEL_PADDING + 2;
        panelBackdropRight = controlsX + sliderWidth + PANEL_PADDING;
        panelBackdropBottom = hideUiY + ACTION_BUTTON_HEIGHT + PANEL_PADDING;

        presetsPanelWidth = Math.min(PRESETS_PANEL_DEFAULT_WIDTH, Math.max(188, this.width - sliderWidth - CONTROL_LEFT_MARGIN - 28));
        presetsPanelX = this.width - presetsPanelWidth - CONTROL_LEFT_MARGIN;
        // Presets panel — bottom-aligned with main panel
        int presetsPanelContentHeight = HEADER_HEIGHT + ROW_HEIGHT * 4;
        presetsPanelTop = panelBackdropBottom - PANEL_PADDING - presetsPanelContentHeight;

        xSlider = addRenderableWidget(new Slider(
                controlsX, sliderGroupY, sliderWidth,
                "screen.focus.camera_editor.offset_x",
                FocusClientConfig.MIN_CAMERA_OFFSET_X, FocusClientConfig.MAX_CAMERA_OFFSET_X,
                () -> FocusClientConfig.cameraOffsetX(editedShoulder),
                value -> {
                    FocusClientConfig.setCameraOffsetX(editedShoulder, value);
                    FocusClientConfig.saveConfig();
                }));

        ySlider = addRenderableWidget(new Slider(
                controlsX, sliderGroupY + ROW_HEIGHT, sliderWidth,
                "screen.focus.camera_editor.offset_y",
                FocusClientConfig.MIN_CAMERA_OFFSET_Y, FocusClientConfig.MAX_CAMERA_OFFSET_Y,
                () -> FocusClientConfig.cameraOffsetY(editedShoulder),
                value -> {
                    FocusClientConfig.setCameraOffsetY(editedShoulder, value);
                    FocusClientConfig.saveConfig();
                }));

        zSlider = addRenderableWidget(new Slider(
                controlsX, sliderGroupY + ROW_HEIGHT * 2, sliderWidth,
                "screen.focus.camera_editor.offset_z",
                FocusClientConfig.MIN_CAMERA_OFFSET_Z, FocusClientConfig.MAX_CAMERA_OFFSET_Z,
                () -> FocusClientConfig.cameraOffsetZ(editedShoulder),
                value -> {
                    FocusClientConfig.setCameraOffsetZ(editedShoulder, value);
                    FocusClientConfig.saveConfig();
                }));

        rotationSlider = addRenderableWidget(new Slider(
                controlsX, sliderGroupY + ROW_HEIGHT * 3, sliderWidth,
                "screen.focus.camera_editor.rotation",
                FocusClientConfig.MIN_CAMERA_ROTATION, FocusClientConfig.MAX_CAMERA_ROTATION,
                () -> FocusClientConfig.cameraRotation(editedShoulder),
                value -> {
                    FocusClientConfig.setCameraRotation(editedShoulder, value);
                    FocusClientConfig.saveConfig();
                }));

        int swapButtonY = toggleGroupY;
        int customValuesY = toggleGroupY + ROW_HEIGHT;
        int followRotationsY = toggleGroupY + ROW_HEIGHT * 2;
        halfButtonWidth = (sliderWidth - CONTROL_GAP) / 2;
        int rightHalfButtonWidth = sliderWidth - halfButtonWidth - CONTROL_GAP;

        swapShoulderButton = addRenderableWidget(Button.builder(Component.empty(), button -> swapEditedShoulder(true))
                .bounds(controlsX, swapButtonY, sliderWidth, ACTION_BUTTON_HEIGHT)
                .build());
        customSwapValuesButton = addRenderableWidget(Button.builder(Component.empty(), button -> toggleCustomSwapValues())
                .bounds(controlsX, customValuesY, sliderWidth, ACTION_BUTTON_HEIGHT)
                .build());
        followRotationsButton = addRenderableWidget(Button.builder(Component.empty(), button -> {
            FocusClientConfig.setFollowPlayerRotations(!FocusClientConfig.followPlayerRotations());
            FocusClientConfig.saveConfig();
            refreshAdvancedCameraButtons();
            showStatus(FocusClientConfig.followPlayerRotations()
                    ? "screen.focus.camera_editor.follow_rotations_enabled"
                    : "screen.focus.camera_editor.follow_rotations_disabled");
        }).bounds(controlsX, followRotationsY, sliderWidth, ACTION_BUTTON_HEIGHT).build());

        togglePresetsPanelButton = addRenderableWidget(Button.builder(Component.empty(), button -> {
                    presetsPanelExpanded = !presetsPanelExpanded;
                    applyControlVisibility();
                }).bounds(controlsX + halfButtonWidth + CONTROL_GAP, hideUiY, rightHalfButtonWidth, ACTION_BUTTON_HEIGHT)
                .build());

        int profileNameY = presetsPanelTop + HEADER_HEIGHT;
        int profileCycleY = profileNameY + ROW_HEIGHT;
        int profileActionY = profileCycleY + ROW_HEIGHT;
        int presetButtonY = profileActionY + ROW_HEIGHT;
        int panelTripleButtonWidth = (presetsPanelWidth - (CONTROL_GAP * 2)) / 3;
        int panelThirdButtonWidth = presetsPanelWidth - (panelTripleButtonWidth * 2) - (CONTROL_GAP * 2);
        int panelLeftDualButtonWidth = (presetsPanelWidth - CONTROL_GAP) / 2;
        int panelRightDualButtonWidth = presetsPanelWidth - panelLeftDualButtonWidth - CONTROL_GAP;

        profileNameInput = addRenderableWidget(new EditBox(
                this.font,
                presetsPanelX,
                profileNameY,
                presetsPanelWidth,
                ACTION_BUTTON_HEIGHT,
                Component.translatable("screen.focus.camera_editor.profile_name")));
        profileNameInput.setMaxLength(FocusClientConfig.MAX_CAMERA_PROFILE_NAME_LENGTH);
        profileNameInput.setResponder(value -> refreshProfileControls());

        saveProfileButton = addRenderableWidget(Button.builder(Component.translatable("screen.focus.camera_editor.save_profile"), button ->
                        saveCurrentProfile())
                .bounds(presetsPanelX, profileActionY, panelTripleButtonWidth, ACTION_BUTTON_HEIGHT)
                .build());
        loadProfileButton = addRenderableWidget(Button.builder(Component.translatable("screen.focus.camera_editor.load_profile"), button ->
                        loadSelectedProfile())
                .bounds(presetsPanelX + panelTripleButtonWidth + CONTROL_GAP, profileActionY, panelTripleButtonWidth, ACTION_BUTTON_HEIGHT)
                .build());
        deleteProfileButton = addRenderableWidget(Button.builder(Component.translatable("screen.focus.camera_editor.delete_profile"), button ->
                        deleteSelectedProfile())
                .bounds(presetsPanelX + (panelTripleButtonWidth * 2) + (CONTROL_GAP * 2), profileActionY, panelThirdButtonWidth, ACTION_BUTTON_HEIGHT)
                .build());
        cycleProfileButton = addRenderableWidget(Button.builder(Component.empty(), button -> cycleSavedProfile())
                .bounds(presetsPanelX, profileCycleY, presetsPanelWidth, ACTION_BUTTON_HEIGHT)
                .build());

        savePresetButton = addRenderableWidget(Button.builder(Component.translatable("screen.focus.camera_editor.save_preset"), button -> {
            if (this.minecraft == null) {
                return;
            }
            String preset = FocusClientConfig.serializeCameraSetup(FocusClientConfig.currentCameraSetupPreset());
            this.minecraft.keyboardHandler.setClipboard(preset);
            showStatus("screen.focus.camera_editor.preset_saved");
        }).bounds(presetsPanelX, presetButtonY, panelLeftDualButtonWidth, ACTION_BUTTON_HEIGHT).build());

        importPresetButton = addRenderableWidget(Button.builder(Component.translatable("screen.focus.camera_editor.import_preset"), button ->
                importPresetFromClipboard()).bounds(presetsPanelX + panelLeftDualButtonWidth + CONTROL_GAP, presetButtonY, panelRightDualButtonWidth, ACTION_BUTTON_HEIGHT).build());

        resetButton = addRenderableWidget(Button.builder(Component.translatable("screen.focus.camera_editor.reset_defaults"), button -> {
            FocusClientConfig.resetCameraOffsetsToDefaults(editedShoulder);
            FocusClientConfig.saveConfig();
            refreshSlidersFromConfig();
            showStatus("screen.focus.camera_editor.preset_reset_defaults");
        }).bounds(controlsX, actionGroupY, halfButtonWidth, ACTION_BUTTON_HEIGHT).build());

        doneButton = addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(controlsX + halfButtonWidth + CONTROL_GAP, actionGroupY, rightHalfButtonWidth, ACTION_BUTTON_HEIGHT)
                .build());

        toggleUiButton = addRenderableWidget(Button.builder(Component.empty(), button -> {
            controlsVisible = !controlsVisible;
            applyControlVisibility();
        }).bounds(controlsX, hideUiY, halfButtonWidth, ACTION_BUTTON_HEIGHT).build());

        initializeProfileName();
        presetsPanelExpanded = false;
        refreshShoulderButtons();
        applyControlVisibility();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        stopPreviewIfNeeded();
        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public void removed() {
        stopPreviewIfNeeded();
        super.removed();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (controlsVisible) {
            // Semi-transparent panel backdrop for readability
            guiGraphics.fill(panelBackdropLeft, panelBackdropTop, panelBackdropRight, panelBackdropBottom, 0x90101010);

            if (presetsPanelExpanded) {
                guiGraphics.fill(
                        presetsPanelX - PANEL_PADDING,
                        presetsPanelTop - PANEL_PADDING,
                        presetsPanelX + presetsPanelWidth + PANEL_PADDING,
                        panelBackdropBottom,
                        0x90101010);
            }

            guiGraphics.drawString(this.font, this.title, controlsX, controlsTop + 2, 0xFFFFFF, true);
            List<FormattedCharSequence> hintLines = this.font.split(
                    Component.translatable("screen.focus.camera_editor.adjust_hint"), sliderWidth);
            for (int i = 0; i < hintLines.size(); i++) {
                guiGraphics.drawString(this.font, hintLines.get(i),
                        controlsX, controlsTop + 14 + i * (this.font.lineHeight + 1), 0xB0B0B0, true);
            }
            if (presetsPanelExpanded) {
                guiGraphics.drawString(this.font, Component.translatable("screen.focus.camera_editor.presets_panel_title"),
                        presetsPanelX, presetsPanelTop + 2, 0xFFFFFF, true);
                guiGraphics.drawString(this.font, Component.translatable("screen.focus.camera_editor.presets_panel_hint"),
                        presetsPanelX, presetsPanelTop + 12, 0xCFCFCF, true);
            }
            if (isStatusVisible()) {
                guiGraphics.drawString(this.font, statusMessage, controlsX, statusMessageY, 0xC8FACC, true);
            }
        } else {
            guiGraphics.drawString(this.font, Component.translatable("screen.focus.camera_editor.preview_hint_minimized"),
                    controlsX + HIDE_SHOW_BUTTON_WIDTH + 6, this.height - CONTROLS_BOTTOM_MARGIN - ACTION_BUTTON_HEIGHT + 6, 0xD0D0D0, true);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics) {
        // Intentionally no background layer so the world stays fully visible for live camera preview.
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (profileNameInput != null && profileNameInput.isFocused()) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        if (handleEditorCameraAdjustmentKey(keyCode, modifiers)) {
            return true;
        }
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

    private boolean handleEditorCameraAdjustmentKey(int keyCode, int modifiers) {
        double step = FocusClientConfig.cameraStepSize();
        if ((modifiers & GLFW.GLFW_MOD_SHIFT) != 0) {
            step *= KEY_ADJUST_STEP_MULTIPLIER;
        }

        boolean changed = switch (keyCode) {
            case GLFW.GLFW_KEY_LEFT -> {
                FocusClientConfig.adjustCameraLeft(editedShoulder, step);
                yield true;
            }
            case GLFW.GLFW_KEY_RIGHT -> {
                FocusClientConfig.adjustCameraRight(editedShoulder, step);
                yield true;
            }
            case GLFW.GLFW_KEY_UP -> {
                FocusClientConfig.adjustCameraIn(editedShoulder, step);
                yield true;
            }
            case GLFW.GLFW_KEY_DOWN -> {
                FocusClientConfig.adjustCameraOut(editedShoulder, step);
                yield true;
            }
            case GLFW.GLFW_KEY_PAGE_UP -> {
                FocusClientConfig.adjustCameraUp(editedShoulder, step);
                yield true;
            }
            case GLFW.GLFW_KEY_PAGE_DOWN -> {
                FocusClientConfig.adjustCameraDown(editedShoulder, step);
                yield true;
            }
            default -> false;
        };
        if (!changed) {
            return false;
        }

        FocusClientConfig.saveConfig();
        refreshSlidersFromConfig();
        return true;
    }

    private void applyControlVisibility() {
        if (toggleUiButton != null) {
            toggleUiButton.setMessage(controlsVisible
                    ? Component.translatable("screen.focus.camera_editor.hide_ui")
                    : Component.translatable("screen.focus.camera_editor.show_ui"));
            toggleUiButton.setY(controlsVisible ? hideUiY : this.height - CONTROLS_BOTTOM_MARGIN - ACTION_BUTTON_HEIGHT);
            toggleUiButton.setWidth(controlsVisible ? halfButtonWidth : HIDE_SHOW_BUTTON_WIDTH);
        }
        if (togglePresetsPanelButton != null) {
            if (!controlsVisible) {
                presetsPanelExpanded = false;
            }
            togglePresetsPanelButton.visible = controlsVisible;
            togglePresetsPanelButton.active = controlsVisible;
            togglePresetsPanelButton.setMessage(Component.translatable(
                    presetsPanelExpanded
                            ? "screen.focus.camera_editor.presets_panel_collapse"
                            : "screen.focus.camera_editor.presets_panel_expand"));
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
        if (profileNameInput != null && !controlsVisible) {
            profileNameInput.setFocused(false);
        }
        applyPresetsPanelVisibility();

        if (savePresetButton != null) {
            savePresetButton.visible = controlsVisible && presetsPanelExpanded;
            savePresetButton.active = controlsVisible && presetsPanelExpanded;
        }
        if (importPresetButton != null) {
            importPresetButton.visible = controlsVisible && presetsPanelExpanded;
            importPresetButton.active = controlsVisible && presetsPanelExpanded;
        }
        if (resetButton != null) {
            resetButton.visible = controlsVisible;
            resetButton.active = controlsVisible;
        }
        if (doneButton != null) {
            doneButton.visible = controlsVisible;
            doneButton.active = controlsVisible;
        }
        if (followRotationsButton != null) {
            followRotationsButton.visible = controlsVisible;
            followRotationsButton.active = controlsVisible;
        }

        refreshProfileControls();
    }

    private void applyPresetsPanelVisibility() {
        boolean panelVisible = controlsVisible && presetsPanelExpanded;
        if (profileNameInput != null) {
            profileNameInput.visible = panelVisible;
            profileNameInput.setEditable(panelVisible);
            if (!panelVisible) {
                profileNameInput.setFocused(false);
            }
        }
        if (saveProfileButton != null) {
            saveProfileButton.visible = panelVisible;
        }
        if (loadProfileButton != null) {
            loadProfileButton.visible = panelVisible;
        }
        if (deleteProfileButton != null) {
            deleteProfileButton.visible = panelVisible;
        }
        if (cycleProfileButton != null) {
            cycleProfileButton.visible = panelVisible;
        }
        if (savePresetButton != null) {
            savePresetButton.visible = panelVisible;
        }
        if (importPresetButton != null) {
            importPresetButton.visible = panelVisible;
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

    private void initializeProfileName() {
        if (profileNameInput == null) {
            return;
        }

        String selected = FocusClientConfig.resolveCameraProfileName(FocusClientConfig.selectedCameraProfileName());
        if (!selected.isEmpty()) {
            profileNameInput.setValue(selected);
        } else {
            List<String> profiles = FocusClientConfig.cameraProfileNames();
            if (!profiles.isEmpty()) {
                profileNameInput.setValue(profiles.get(0));
            }
        }
        refreshProfileControls();
    }

    private void refreshProfileControls() {
        if (profileNameInput == null) {
            return;
        }

        String typedName = FocusClientConfig.sanitizeCameraProfileName(profileNameInput.getValue());
        String resolvedName = FocusClientConfig.resolveCameraProfileName(typedName);
        List<String> profiles = FocusClientConfig.cameraProfileNames();
        boolean hasProfiles = !profiles.isEmpty();

        if (cycleProfileButton != null) {
            if (hasProfiles) {
                String displayName = resolvedName.isEmpty() ? profiles.get(0) : resolvedName;
                cycleProfileButton.setMessage(Component.translatable("screen.focus.camera_editor.profile_cycle", displayName));
                cycleProfileButton.active = controlsVisible && presetsPanelExpanded;
            } else {
                cycleProfileButton.setMessage(Component.translatable("screen.focus.camera_editor.profile_cycle_none"));
                cycleProfileButton.active = false;
            }
        }

        boolean hasTypedName = !typedName.isEmpty();
        if (saveProfileButton != null) {
            saveProfileButton.active = controlsVisible && presetsPanelExpanded && hasTypedName;
        }
        boolean hasResolvedProfile = !resolvedName.isEmpty();
        if (loadProfileButton != null) {
            loadProfileButton.active = controlsVisible && presetsPanelExpanded && hasResolvedProfile;
        }
        if (deleteProfileButton != null) {
            deleteProfileButton.active = controlsVisible && presetsPanelExpanded && hasResolvedProfile;
        }
        if (savePresetButton != null) {
            savePresetButton.active = controlsVisible && presetsPanelExpanded;
        }
        if (importPresetButton != null) {
            importPresetButton.active = controlsVisible && presetsPanelExpanded;
        }
    }

    private void cycleSavedProfile() {
        if (profileNameInput == null) {
            return;
        }

        List<String> profiles = FocusClientConfig.cameraProfileNames();
        if (profiles.isEmpty()) {
            showStatus("screen.focus.camera_editor.profile_no_profiles");
            return;
        }

        String current = FocusClientConfig.resolveCameraProfileName(profileNameInput.getValue());
        int currentIndex = profiles.indexOf(current);
        int nextIndex = currentIndex < 0 ? 0 : (currentIndex + 1) % profiles.size();
        String nextProfile = profiles.get(nextIndex);
        profileNameInput.setValue(nextProfile);
        FocusClientConfig.setSelectedCameraProfileName(nextProfile);
        FocusClientConfig.saveConfig();
        refreshProfileControls();
    }

    private void saveCurrentProfile() {
        if (profileNameInput == null) {
            return;
        }

        String sanitizedName = FocusClientConfig.sanitizeCameraProfileName(profileNameInput.getValue());
        if (sanitizedName.isEmpty()) {
            showStatus("screen.focus.camera_editor.profile_name_required");
            return;
        }
        FocusClientConfig.setSelectedCameraProfileName(sanitizedName);

        String existingName = FocusClientConfig.resolveCameraProfileName(sanitizedName);
        if (!FocusClientConfig.saveCurrentSetupAsProfile(sanitizedName)) {
            showStatus("screen.focus.camera_editor.profile_name_required");
            return;
        }

        String resolvedName = FocusClientConfig.resolveCameraProfileName(sanitizedName);
        profileNameInput.setValue(resolvedName.isEmpty() ? sanitizedName : resolvedName);
        FocusClientConfig.saveConfig();
        refreshProfileControls();

        if (existingName.isEmpty()) {
            showStatus("screen.focus.camera_editor.profile_saved", profileNameInput.getValue());
        } else {
            showStatus("screen.focus.camera_editor.profile_overwritten", profileNameInput.getValue());
        }
    }

    private void loadSelectedProfile() {
        if (profileNameInput == null) {
            return;
        }

        String resolvedName = FocusClientConfig.resolveCameraProfileName(profileNameInput.getValue());
        if (resolvedName.isEmpty()) {
            showStatus("screen.focus.camera_editor.profile_not_found");
            return;
        }
        FocusClientConfig.setSelectedCameraProfileName(resolvedName);

        if (!FocusClientConfig.loadCameraProfile(resolvedName)) {
            showStatus("screen.focus.camera_editor.profile_not_found");
            return;
        }

        FocusClientConfig.saveConfig();
        refreshSlidersFromConfig();
        refreshShoulderButtons();
        profileNameInput.setValue(resolvedName);
        refreshProfileControls();
        showStatus("screen.focus.camera_editor.profile_loaded", resolvedName);
    }

    private void deleteSelectedProfile() {
        if (profileNameInput == null) {
            return;
        }

        String resolvedName = FocusClientConfig.resolveCameraProfileName(profileNameInput.getValue());
        if (resolvedName.isEmpty()) {
            showStatus("screen.focus.camera_editor.profile_not_found");
            return;
        }
        FocusClientConfig.setSelectedCameraProfileName(resolvedName);

        List<String> profilesBeforeDelete = FocusClientConfig.cameraProfileNames();
        int removedIndex = profilesBeforeDelete.indexOf(resolvedName);
        if (!FocusClientConfig.deleteCameraProfile(resolvedName)) {
            showStatus("screen.focus.camera_editor.profile_not_found");
            return;
        }

        List<String> profilesAfterDelete = FocusClientConfig.cameraProfileNames();
        if (profilesAfterDelete.isEmpty()) {
            profileNameInput.setValue("");
        } else {
            profileNameInput.setValue(profilesAfterDelete.get(Math.min(removedIndex, profilesAfterDelete.size() - 1)));
        }

        FocusClientConfig.saveConfig();
        refreshProfileControls();
        showStatus("screen.focus.camera_editor.profile_deleted", resolvedName);
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

    private void stopPreviewIfNeeded() {
        if (previewStopped) {
            return;
        }
        previewStopped = true;
        LockOnHandler.stopCameraEditorPreview(previousCameraType);
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
        refreshAdvancedCameraButtons();
    }

    private void refreshAdvancedCameraButtons() {
        if (followRotationsButton != null) {
            followRotationsButton.setMessage(Component.translatable(
                    "screen.focus.camera_editor.follow_rotations",
                    FocusClientConfig.followPlayerRotations() ? "x" : " "));
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
            super(x, y, width, ACTION_BUTTON_HEIGHT, Component.empty(), 0.0D);
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
