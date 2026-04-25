package com.jvn.focus.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

public final class FocusConfigScreen extends Screen {
    private static final int BUTTON_WIDTH = 260;
    private static final int ROW_HEIGHT = 24;
    private static final int CONTROL_HEIGHT = 20;
    private static final int TOP_MARGIN = 48;
    private static final int BOTTOM_MARGIN = 30;

    private final Screen parent;

    public FocusConfigScreen(Screen parent) {
        super(Component.translatable("text.config.focus-config.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int left = this.width / 2 - BUTTON_WIDTH / 2;
        int y = TOP_MARGIN;

        addPageButton(left, y, Component.translatable("text.config.focus-config.section.general"),
                new OptionsPage(this, Component.translatable("text.config.focus-config.section.general"), Page.CORE));
        y += ROW_HEIGHT;
        addPageButton(left, y, Component.translatable("text.config.focus-config.section.camera"),
                new OptionsPage(this, Component.translatable("text.config.focus-config.section.camera"), Page.CAMERA));
        y += ROW_HEIGHT;
        addPageButton(left, y, Component.translatable("text.config.focus-config.section.targetSwap"),
                new OptionsPage(this, Component.translatable("text.config.focus-config.section.targetSwap"), Page.TARGETING));
        y += ROW_HEIGHT;
        addPageButton(left, y, Component.translatable("text.config.focus-config.section.targetFilters"),
                new OptionsPage(this, Component.translatable("text.config.focus-config.section.targetFilters"), Page.FILTERS));
        y += ROW_HEIGHT + 8;

        this.addRenderableWidget(Button.builder(
                        Component.translatable("key.focus.open_camera_editor"),
                        button -> LockOnCameraEditorScreen.openFromCurrentScreen())
                .bounds(left, y, BUTTON_WIDTH, CONTROL_HEIGHT)
                .build());

        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(left, this.height - BOTTOM_MARGIN, BUTTON_WIDTH, CONTROL_HEIGHT)
                .build());
    }

    private void addPageButton(int left, int y, Component label, Screen screen) {
        this.addRenderableWidget(Button.builder(label, button -> Minecraft.getInstance().setScreen(screen))
                .bounds(left, y, BUTTON_WIDTH, CONTROL_HEIGHT)
                .build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        guiGraphics.drawCenteredString(this.font,
                Component.translatable("screen.focus.config.hint"),
                this.width / 2, 32, 0xA0A0A0);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(parent);
        }
    }

    private enum Page {
        CORE,
        CAMERA,
        CAMERA_BEHAVIOR,
        CAMERA_FOLLOWING,
        SHOULDER_SWAP,
        PLAYER_TRANSPARENCY,
        CROSSHAIR,
        TARGETING,
        FILTERS
    }

    private static final class OptionsPage extends Screen {
        private final Screen parent;
        private final Page page;
        private final List<Runnable> messageRefreshers = new ArrayList<>();

        private OptionsPage(Screen parent, Component title, Page page) {
            super(title);
            this.parent = parent;
            this.page = page;
        }

        @Override
        protected void init() {
            messageRefreshers.clear();
            int left = this.width / 2 - BUTTON_WIDTH / 2;
            int y = TOP_MARGIN;

            switch (page) {
                case CORE -> y = addCoreOptions(left, y);
                case CAMERA -> y = addCameraOptions(left, y);
                case CAMERA_BEHAVIOR -> y = addCameraBehaviorOptions(left, y);
                case CAMERA_FOLLOWING -> y = addCameraFollowingOptions(left, y);
                case SHOULDER_SWAP -> y = addShoulderSwapOptions(left, y);
                case PLAYER_TRANSPARENCY -> y = addPlayerTransparencyOptions(left, y);
                case CROSSHAIR -> y = addCrosshairOptions(left, y);
                case TARGETING -> y = addTargetingOptions(left, y);
                case FILTERS -> y = addFilterOptions(left, y);
            }

            int bottomButtonWidth = (BUTTON_WIDTH - 6) / 2;
            this.addRenderableWidget(Button.builder(Component.translatable("text.config.focus-config.resetPage"), button -> {
                        resetPage();
                        FocusClientConfig.saveConfig();
                        rebuildOptions();
                    })
                    .bounds(left, this.height - BOTTOM_MARGIN, bottomButtonWidth, CONTROL_HEIGHT)
                    .build());
            this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                    .bounds(left + bottomButtonWidth + 6, this.height - BOTTOM_MARGIN, BUTTON_WIDTH - bottomButtonWidth - 6, CONTROL_HEIGHT)
                    .build());
        }

        private int addCoreOptions(int left, int y) {
            y = addToggle(left, y, "text.config.focus-config.option.autoSwitchToThirdPerson",
                    FocusClientConfig::autoSwitchToThirdPerson,
                    value -> FocusClientConfig.configInstance().autoSwitchToThirdPerson(value));
            y = addToggle(left, y, "text.config.focus-config.option.allowFirstPersonWhileTargeting",
                    FocusClientConfig::allowFirstPersonWhileTargeting,
                    value -> FocusClientConfig.configInstance().allowFirstPersonWhileTargeting(value));
            y = addToggle(left, y, "text.config.focus-config.option.allowFrontFacingThirdPersonWhileTargeting",
                    FocusClientConfig::allowFrontFacingThirdPersonWhileTargeting,
                    value -> FocusClientConfig.configInstance().allowFrontFacingThirdPersonWhileTargeting(value));
            y = addToggle(left, y, "text.config.focus-config.option.showLockOnStatusMessages",
                    FocusClientConfig::showLockOnStatusMessages,
                    value -> FocusClientConfig.configInstance().showLockOnStatusMessages(value));
            y = addToggle(left, y, "text.config.focus-config.option.showLockOnDebugText",
                    FocusClientConfig::showLockOnDebugText,
                    value -> FocusClientConfig.configInstance().showLockOnDebugText(value));
            y = addToggle(left, y, "text.config.focus-config.option.experimentalPersistentWalkthroughBackwardsWalking",
                    FocusClientConfig::experimentalPersistentWalkthroughBackwardsWalking,
                    value -> FocusClientConfig.configInstance().experimentalPersistentWalkthroughBackwardsWalking(value));
            y = addToggle(left, y, "text.config.focus-config.option.cinematicBarsWhileLockedOn",
                    FocusClientConfig::cinematicBarsWhileLockedOn,
                    value -> FocusClientConfig.configInstance().cinematicBarsWhileLockedOn(value));
            y = addToggle(left, y, "text.config.focus-config.option.cinematicBarsUnderHud",
                    FocusClientConfig::cinematicBarsUnderHud,
                    value -> FocusClientConfig.configInstance().cinematicBarsUnderHud(value));
            y = addToggle(left, y, "text.config.focus-config.option.crosshair.renderCorrectedCrosshair",
                    FocusClientConfig::renderCorrectedCrosshair,
                    value -> FocusClientConfig.configInstance().crosshair.renderCorrectedCrosshair(value));
            y = addToggle(left, y, "text.config.focus-config.option.crosshair.hideVanillaCrosshair",
                    FocusClientConfig::hideVanillaCrosshair,
                    value -> FocusClientConfig.configInstance().crosshair.hideVanillaCrosshair(value));
            y = addToggle(left, y, "text.config.focus-config.option.enableTargetFilters",
                    FocusClientConfig::enableTargetFilters,
                    value -> FocusClientConfig.configInstance().enableTargetFilters(value));
            return addEnum(left, y, "text.config.focus-config.option.lockOnIndicatorStyle",
                    FocusClientConfig.LockOnIndicatorStyle.values(),
                    FocusClientConfig::lockOnIndicatorStyle,
                    value -> FocusClientConfig.configInstance().lockOnIndicatorStyle(value));
        }

        private int addCameraOptions(int left, int y) {
            y = addToggle(left, y, "text.config.focus-config.option.useCustomSwappedShoulderValues",
                    FocusClientConfig::useCustomSwappedShoulderValues,
                    value -> FocusClientConfig.setUseCustomSwappedShoulderValues(value, FocusClientConfig.Shoulder.LEFT));
            y = addEnum(left, y, "text.config.focus-config.option.cameraMode",
                    FocusClientConfig.CameraMode.values(),
                    FocusClientConfig::cameraMode,
                    value -> FocusClientConfig.configInstance().cameraMode(value));
            y = addSubPage(left, y, "text.config.focus-config.category.cameraBehavior", Page.CAMERA_BEHAVIOR);
            y = addSubPage(left, y, "text.config.focus-config.category.cameraFollowing", Page.CAMERA_FOLLOWING);
            y = addSubPage(left, y, "text.config.focus-config.category.shoulderSwap", Page.SHOULDER_SWAP);
            y = addSubPage(left, y, "text.config.focus-config.category.playerTransparency", Page.PLAYER_TRANSPARENCY);
            return addSubPage(left, y, "text.config.focus-config.category.crosshair", Page.CROSSHAIR);
        }

        private int addCameraBehaviorOptions(int left, int y) {
            y = addSlider(left, y, "text.config.focus-config.option.cameraBehavior.cameraFloatiness",
                    FocusClientConfig.MIN_CAMERA_FLOATINESS, FocusClientConfig.MAX_CAMERA_FLOATINESS, 2,
                    FocusClientConfig::cameraFloatiness,
                    value -> FocusClientConfig.configInstance().cameraBehavior.cameraFloatiness(value));
            y = addSlider(left, y, "text.config.focus-config.option.cameraBehavior.cameraDrag",
                    FocusClientConfig.MIN_CAMERA_DRAG, FocusClientConfig.MAX_CAMERA_DRAG, 2,
                    FocusClientConfig::cameraDrag,
                    value -> FocusClientConfig.configInstance().cameraBehavior.cameraDrag(value));
            y = addSlider(left, y, "text.config.focus-config.option.cameraBehavior.cameraStepSize",
                    FocusClientConfig.MIN_CAMERA_STEP_SIZE, FocusClientConfig.MAX_CAMERA_STEP_SIZE, 3,
                    FocusClientConfig::cameraStepSize,
                    value -> FocusClientConfig.configInstance().cameraBehavior.cameraStepSize(value));
            y = addToggle(left, y, "text.config.focus-config.option.cameraBehavior.dynamicallyAdjustOffsets",
                    FocusClientConfig::dynamicallyAdjustOffsets,
                    value -> FocusClientConfig.configInstance().cameraBehavior.dynamicallyAdjustOffsets(value));
            y = addToggle(left, y, "text.config.focus-config.option.cameraBehavior.smoothCameraTransition",
                    FocusClientConfig::smoothCameraTransition,
                    value -> FocusClientConfig.configInstance().cameraBehavior.smoothCameraTransition(value));
            return addSlider(left, y, "text.config.focus-config.option.cameraBehavior.cameraTransitionSpeed",
                    FocusClientConfig.MIN_CAMERA_TRANSITION_SPEED, FocusClientConfig.MAX_CAMERA_TRANSITION_SPEED, 2,
                    FocusClientConfig::cameraTransitionSpeed,
                    value -> FocusClientConfig.configInstance().cameraBehavior.cameraTransitionSpeed(value));
        }

        private int addCameraFollowingOptions(int left, int y) {
            y = addToggle(left, y, "text.config.focus-config.option.cameraFollowing.followPlayerRotations",
                    FocusClientConfig::followPlayerRotations,
                    FocusClientConfig::setFollowPlayerRotations);
            y = addSlider(left, y, "text.config.focus-config.option.cameraFollowing.followPlayerRotationsDelay",
                    FocusClientConfig.MIN_FOLLOW_PLAYER_ROTATIONS_DELAY, FocusClientConfig.MAX_FOLLOW_PLAYER_ROTATIONS_DELAY, 0,
                    () -> FocusClientConfig.configInstance().cameraFollowing.followPlayerRotationsDelay(),
                    value -> FocusClientConfig.configInstance().cameraFollowing.followPlayerRotationsDelay(value));
            y = addSlider(left, y, "text.config.focus-config.option.cameraFollowing.cameraHeadFollowResponsiveness",
                    FocusClientConfig.MIN_CAMERA_HEAD_FOLLOW_RESPONSIVENESS, FocusClientConfig.MAX_CAMERA_HEAD_FOLLOW_RESPONSIVENESS, 2,
                    FocusClientConfig::cameraHeadFollowResponsiveness,
                    value -> FocusClientConfig.configInstance().cameraFollowing.cameraHeadFollowResponsiveness(value));
            y = addSlider(left, y, "text.config.focus-config.option.cameraFollowing.cameraBodyFollowResponsiveness",
                    FocusClientConfig.MIN_CAMERA_BODY_FOLLOW_RESPONSIVENESS, FocusClientConfig.MAX_CAMERA_BODY_FOLLOW_RESPONSIVENESS, 2,
                    FocusClientConfig::cameraBodyFollowResponsiveness,
                    value -> FocusClientConfig.configInstance().cameraFollowing.cameraBodyFollowResponsiveness(value));
            return addToggle(left, y, "text.config.focus-config.option.cameraFollowing.fullBodyFollowEnabled",
                    FocusClientConfig::fullBodyFollowEnabled,
                    value -> FocusClientConfig.configInstance().cameraFollowing.fullBodyFollowEnabled(value));
        }

        private int addShoulderSwapOptions(int left, int y) {
            y = addSlider(left, y, "text.config.focus-config.option.shoulderSwap.cameraSwapSpeed",
                    FocusClientConfig.MIN_CAMERA_SWAP_SPEED, FocusClientConfig.MAX_CAMERA_SWAP_SPEED, 2,
                    FocusClientConfig::cameraSwapSpeed,
                    value -> FocusClientConfig.configInstance().shoulderSwap.cameraSwapSpeed(value));
            y = addSlider(left, y, "text.config.focus-config.option.shoulderSwap.cameraSwapSmoothness",
                    FocusClientConfig.MIN_CAMERA_SWAP_SMOOTHNESS, FocusClientConfig.MAX_CAMERA_SWAP_SMOOTHNESS, 2,
                    FocusClientConfig::cameraSwapSmoothness,
                    value -> FocusClientConfig.configInstance().cameraSwapSmoothness(value));
            y = addSlider(left, y, "text.config.focus-config.option.shoulderSwap.dynamicCameraSwapSpeed",
                    FocusClientConfig.MIN_DYNAMIC_CAMERA_SWAP_SPEED, FocusClientConfig.MAX_DYNAMIC_CAMERA_SWAP_SPEED, 2,
                    FocusClientConfig::dynamicCameraSwapSpeed,
                    value -> FocusClientConfig.configInstance().shoulderSwap.dynamicCameraSwapSpeed(value));
            y = addSlider(left, y, "text.config.focus-config.option.shoulderSwap.dynamicCameraSwapSmoothness",
                    FocusClientConfig.MIN_DYNAMIC_CAMERA_SWAP_SMOOTHNESS, FocusClientConfig.MAX_DYNAMIC_CAMERA_SWAP_SMOOTHNESS, 2,
                    FocusClientConfig::dynamicCameraSwapSmoothness,
                    value -> FocusClientConfig.configInstance().dynamicCameraSwapSmoothness(value));
            y = addToggle(left, y, "text.config.focus-config.option.shoulderSwap.dynamicShoulderAutoSwapEnabled",
                    FocusClientConfig::dynamicShoulderAutoSwapEnabled,
                    value -> FocusClientConfig.configInstance().shoulderSwap.dynamicShoulderAutoSwapEnabled(value));
            y = addSlider(left, y, "text.config.focus-config.option.shoulderSwap.dynamicShoulderSwitchThreshold",
                    FocusClientConfig.MIN_DYNAMIC_SHOULDER_SWITCH_THRESHOLD, FocusClientConfig.MAX_DYNAMIC_SHOULDER_SWITCH_THRESHOLD, 2,
                    FocusClientConfig::dynamicShoulderSwitchThreshold,
                    value -> FocusClientConfig.configInstance().shoulderSwap.dynamicShoulderSwitchThreshold(value));
            y = addSlider(left, y, "text.config.focus-config.option.shoulderSwap.dynamicShoulderVisibilityWeight",
                    FocusClientConfig.MIN_DYNAMIC_SHOULDER_VISIBILITY_WEIGHT, FocusClientConfig.MAX_DYNAMIC_SHOULDER_VISIBILITY_WEIGHT, 2,
                    FocusClientConfig::dynamicShoulderVisibilityWeight,
                    value -> FocusClientConfig.configInstance().shoulderSwap.dynamicShoulderVisibilityWeight(value));
            return addSlider(left, y, "text.config.focus-config.option.shoulderSwap.dynamicShoulderManualOverrideCooldownTicks",
                    FocusClientConfig.MIN_DYNAMIC_SHOULDER_MANUAL_OVERRIDE_COOLDOWN_TICKS,
                    FocusClientConfig.MAX_DYNAMIC_SHOULDER_MANUAL_OVERRIDE_COOLDOWN_TICKS, 0,
                    () -> FocusClientConfig.configInstance().shoulderSwap.dynamicShoulderManualOverrideCooldownTicks(),
                    value -> FocusClientConfig.configInstance().shoulderSwap.dynamicShoulderManualOverrideCooldownTicks(value));
        }

        private int addPlayerTransparencyOptions(int left, int y) {
            y = addToggle(left, y, "text.config.focus-config.option.playerTransparency.adjustPlayerTransparency",
                    FocusClientConfig::adjustPlayerTransparency,
                    value -> FocusClientConfig.configInstance().playerTransparency.adjustPlayerTransparency(value));
            y = addSlider(left, y, "text.config.focus-config.option.playerTransparency.playerTransparencyMinAlpha",
                    FocusClientConfig.MIN_PLAYER_TRANSPARENCY_MIN_ALPHA, FocusClientConfig.MAX_PLAYER_TRANSPARENCY_MIN_ALPHA, 2,
                    FocusClientConfig::playerTransparencyMinAlpha,
                    value -> FocusClientConfig.configInstance().playerTransparency.playerTransparencyMinAlpha(value));
            y = addSlider(left, y, "text.config.focus-config.option.playerTransparency.playerTransparencyFadeSpeed",
                    FocusClientConfig.MIN_PLAYER_TRANSPARENCY_FADE_SPEED, FocusClientConfig.MAX_PLAYER_TRANSPARENCY_FADE_SPEED, 2,
                    FocusClientConfig::playerTransparencyFadeSpeed,
                    value -> FocusClientConfig.configInstance().playerTransparency.playerTransparencyFadeSpeed(value));
            y = addToggle(left, y, "text.config.focus-config.option.playerTransparency.playerTransparencyWhenTargetObscuredOnly",
                    FocusClientConfig::playerTransparencyWhenTargetObscuredOnly,
                    value -> FocusClientConfig.configInstance().playerTransparency.playerTransparencyWhenTargetObscuredOnly(value));
            return addToggle(left, y, "text.config.focus-config.option.playerTransparency.playerTransparencyInPreview",
                    FocusClientConfig::playerTransparencyInPreview,
                    value -> FocusClientConfig.configInstance().playerTransparency.playerTransparencyInPreview(value));
        }

        private int addCrosshairOptions(int left, int y) {
            y = addEnum(left, y, "text.config.focus-config.option.crosshair.crosshairCorrectionMode",
                    FocusClientConfig.CrosshairCorrectionMode.values(),
                    FocusClientConfig::crosshairCorrectionMode,
                    value -> FocusClientConfig.configInstance().crosshair.crosshairCorrectionMode(value));
            y = addToggle(left, y, "text.config.focus-config.option.crosshair.renderCorrectedCrosshair",
                    FocusClientConfig::renderCorrectedCrosshair,
                    value -> FocusClientConfig.configInstance().crosshair.renderCorrectedCrosshair(value));
            y = addToggle(left, y, "text.config.focus-config.option.crosshair.correctBlockPlacementRay",
                    FocusClientConfig::correctBlockPlacementRay,
                    value -> FocusClientConfig.configInstance().crosshair.correctBlockPlacementRay(value));
            y = addToggle(left, y, "text.config.focus-config.option.crosshair.correctEntityHitRay",
                    FocusClientConfig::correctEntityHitRay,
                    value -> FocusClientConfig.configInstance().crosshair.correctEntityHitRay(value));
            y = addToggle(left, y, "text.config.focus-config.option.crosshair.correctCrosshairOnlyWhileLockedOn",
                    FocusClientConfig::correctCrosshairOnlyWhileLockedOn,
                    value -> FocusClientConfig.configInstance().crosshair.correctCrosshairOnlyWhileLockedOn(value));
            y = addToggle(left, y, "text.config.focus-config.option.crosshair.hideVanillaCrosshair",
                    FocusClientConfig::hideVanillaCrosshair,
                    value -> FocusClientConfig.configInstance().crosshair.hideVanillaCrosshair(value));
            return addToggle(left, y, "text.config.focus-config.option.crosshair.hideVanillaCrosshairOutOfRange",
                    FocusClientConfig::hideVanillaCrosshairOutOfRange,
                    value -> FocusClientConfig.configInstance().crosshair.hideVanillaCrosshairOutOfRange(value));
        }

        private int addTargetingOptions(int left, int y) {
            y = addSlider(left, y, "text.config.focus-config.option.targetSwapMouseDeadzone",
                    FocusClientConfig.MIN_TARGET_SWAP_MOUSE_DEADZONE, FocusClientConfig.MAX_TARGET_SWAP_MOUSE_DEADZONE, 1,
                    () -> FocusClientConfig.configInstance().targetSwapMouseDeadzone(),
                    value -> FocusClientConfig.configInstance().targetSwapMouseDeadzone(value));
            y = addSlider(left, y, "text.config.focus-config.option.targetSwapMouseActivation",
                    FocusClientConfig.MIN_TARGET_SWAP_MOUSE_ACTIVATION, FocusClientConfig.MAX_TARGET_SWAP_MOUSE_ACTIVATION, 1,
                    () -> FocusClientConfig.configInstance().targetSwapMouseActivation(),
                    value -> FocusClientConfig.configInstance().targetSwapMouseActivation(value));
            y = addSlider(left, y, "text.config.focus-config.option.targetSwapDirectionThreshold",
                    FocusClientConfig.MIN_TARGET_SWAP_DIRECTION_THRESHOLD, FocusClientConfig.MAX_TARGET_SWAP_DIRECTION_THRESHOLD, 2,
                    FocusClientConfig::targetSwapDirectionThreshold,
                    value -> FocusClientConfig.configInstance().targetSwapDirectionThreshold(value));
            y = addSlider(left, y, "text.config.focus-config.option.targetSwapMinScreenSeparation",
                    FocusClientConfig.MIN_TARGET_SWAP_MIN_SCREEN_SEPARATION, FocusClientConfig.MAX_TARGET_SWAP_MIN_SCREEN_SEPARATION, 2,
                    FocusClientConfig::targetSwapMinScreenSeparation,
                    value -> FocusClientConfig.configInstance().targetSwapMinScreenSeparation(value));
            y = addSlider(left, y, "text.config.focus-config.option.targetSwapInputDecay",
                    FocusClientConfig.MIN_TARGET_SWAP_INPUT_DECAY, FocusClientConfig.MAX_TARGET_SWAP_INPUT_DECAY, 2,
                    FocusClientConfig::targetSwapInputDecay,
                    value -> FocusClientConfig.configInstance().targetSwapInputDecay(value));
            y = addSlider(left, y, "text.config.focus-config.option.targetSwapCooldownTicks",
                    FocusClientConfig.MIN_TARGET_SWAP_COOLDOWN_TICKS, FocusClientConfig.MAX_TARGET_SWAP_COOLDOWN_TICKS, 0,
                    () -> FocusClientConfig.configInstance().targetSwapCooldownTicks(),
                    value -> FocusClientConfig.configInstance().targetSwapCooldownTicks(value));
            y = addSlider(left, y, "text.config.focus-config.option.targetSwapSmoothTicks",
                    FocusClientConfig.MIN_TARGET_SWAP_SMOOTH_TICKS, FocusClientConfig.MAX_TARGET_SWAP_SMOOTH_TICKS, 0,
                    () -> FocusClientConfig.configInstance().targetSwapSmoothTicks(),
                    value -> FocusClientConfig.configInstance().targetSwapSmoothTicks(value));
            y = addSlider(left, y, "text.config.focus-config.option.targetSwapLookYawResponsiveness",
                    FocusClientConfig.MIN_TARGET_SWAP_LOOK_RESPONSIVENESS_YAW, FocusClientConfig.MAX_TARGET_SWAP_LOOK_RESPONSIVENESS_YAW, 1,
                    FocusClientConfig::targetSwapLookYawResponsiveness,
                    value -> FocusClientConfig.configInstance().targetSwapLookYawResponsiveness(value));
            y = addSlider(left, y, "text.config.focus-config.option.targetSwapLookPitchResponsiveness",
                    FocusClientConfig.MIN_TARGET_SWAP_LOOK_RESPONSIVENESS_PITCH, FocusClientConfig.MAX_TARGET_SWAP_LOOK_RESPONSIVENESS_PITCH, 1,
                    FocusClientConfig::targetSwapLookPitchResponsiveness,
                    value -> FocusClientConfig.configInstance().targetSwapLookPitchResponsiveness(value));
            y = addSlider(left, y, "text.config.focus-config.option.targetSwapLookMaxYawStepPerTick",
                    FocusClientConfig.MIN_TARGET_SWAP_LOOK_MAX_YAW_STEP_PER_TICK, FocusClientConfig.MAX_TARGET_SWAP_LOOK_MAX_YAW_STEP_PER_TICK, 1,
                    FocusClientConfig::targetSwapLookMaxYawStepPerTick,
                    value -> FocusClientConfig.configInstance().targetSwapLookMaxYawStepPerTick(value));
            y = addSlider(left, y, "text.config.focus-config.option.targetSwapLookMaxPitchStepPerTick",
                    FocusClientConfig.MIN_TARGET_SWAP_LOOK_MAX_PITCH_STEP_PER_TICK, FocusClientConfig.MAX_TARGET_SWAP_LOOK_MAX_PITCH_STEP_PER_TICK, 1,
                    FocusClientConfig::targetSwapLookMaxPitchStepPerTick,
                    value -> FocusClientConfig.configInstance().targetSwapLookMaxPitchStepPerTick(value));
            y = addSlider(left, y, "text.config.focus-config.option.targetSwapTargetPointResponsiveness",
                    FocusClientConfig.MIN_TARGET_SWAP_TARGET_POINT_RESPONSIVENESS, FocusClientConfig.MAX_TARGET_SWAP_TARGET_POINT_RESPONSIVENESS, 1,
                    FocusClientConfig::targetSwapTargetPointResponsiveness,
                    value -> FocusClientConfig.configInstance().targetSwapTargetPointResponsiveness(value));
            return addSlider(left, y, "text.config.focus-config.option.targetSwapPlayerLookFollow",
                    FocusClientConfig.MIN_TARGET_SWAP_PLAYER_LOOK_FOLLOW, FocusClientConfig.MAX_TARGET_SWAP_PLAYER_LOOK_FOLLOW, 2,
                    FocusClientConfig::targetSwapPlayerLookFollow,
                    value -> FocusClientConfig.configInstance().targetSwapPlayerLookFollow(value));
        }

        private int addFilterOptions(int left, int y) {
            y = addToggle(left, y, "text.config.focus-config.option.enableTargetFilters",
                    FocusClientConfig::enableTargetFilters,
                    value -> FocusClientConfig.configInstance().enableTargetFilters(value));
            y = addEnum(left, y, "text.config.focus-config.option.targetFilterMode",
                    FocusClientConfig.TargetFilterMode.values(),
                    FocusClientConfig::targetFilterMode,
                    value -> FocusClientConfig.configInstance().targetFilterMode(value));
            y = addToggle(left, y, "text.config.focus-config.option.filterPlayers",
                    FocusClientConfig::filterPlayers,
                    value -> FocusClientConfig.configInstance().filterPlayers(value));
            y = addToggle(left, y, "text.config.focus-config.option.filterPassiveMobs",
                    FocusClientConfig::filterPassiveMobs,
                    value -> FocusClientConfig.configInstance().filterPassiveMobs(value));
            y = addToggle(left, y, "text.config.focus-config.option.filterNeutralMobs",
                    FocusClientConfig::filterNeutralMobs,
                    value -> FocusClientConfig.configInstance().filterNeutralMobs(value));
            y = addToggle(left, y, "text.config.focus-config.option.filterHostileMobs",
                    FocusClientConfig::filterHostileMobs,
                    value -> FocusClientConfig.configInstance().filterHostileMobs(value));
            Button button = Button.builder(Component.empty(), clicked ->
                            Minecraft.getInstance().setScreen(new EntityIdFilterScreen(this)))
                    .bounds(left, y, BUTTON_WIDTH, CONTROL_HEIGHT)
                    .build();
            this.addRenderableWidget(button);
            messageRefreshers.add(() -> button.setMessage(CommonComponents.optionNameValue(
                    Component.translatable("text.config.focus-config.option.targetFilterEntityIds"),
                    Component.literal(String.valueOf(FocusClientConfig.targetFilterEntityIds().size())))));
            refreshMessages();
            return y + ROW_HEIGHT;
        }

        private int addSubPage(int left, int y, String labelKey, Page targetPage) {
            this.addRenderableWidget(Button.builder(Component.translatable(labelKey), button ->
                            Minecraft.getInstance().setScreen(new OptionsPage(this, Component.translatable(labelKey), targetPage)))
                    .bounds(left, y, BUTTON_WIDTH, CONTROL_HEIGHT)
                    .build());
            return y + ROW_HEIGHT;
        }

        private int addToggle(int left, int y, String labelKey, Supplier<Boolean> getter, Consumer<Boolean> setter) {
            Component label = Component.translatable(labelKey);
            Button button = Button.builder(Component.empty(), clicked -> {
                setter.accept(!getter.get());
                FocusClientConfig.saveConfig();
                refreshMessages();
            }).bounds(left, y, BUTTON_WIDTH, CONTROL_HEIGHT).build();
            this.addRenderableWidget(button);
            messageRefreshers.add(() -> button.setMessage(CommonComponents.optionNameValue(label, onOff(getter.get()))));
            refreshMessages();
            return y + ROW_HEIGHT;
        }

        private <T extends Enum<T>> int addEnum(int left, int y, String labelKey, T[] values, Supplier<T> getter, Consumer<T> setter) {
            Component label = Component.translatable(labelKey);
            Button button = Button.builder(Component.empty(), clicked -> {
                T current = getter.get();
                int next = 0;
                for (int i = 0; i < values.length; i++) {
                    if (values[i] == current) {
                        next = (i + 1) % values.length;
                        break;
                    }
                }
                setter.accept(values[next]);
                FocusClientConfig.saveConfig();
                refreshMessages();
            }).bounds(left, y, BUTTON_WIDTH, CONTROL_HEIGHT).build();
            this.addRenderableWidget(button);
            messageRefreshers.add(() -> button.setMessage(CommonComponents.optionNameValue(label, enumName(getter.get()))));
            refreshMessages();
            return y + ROW_HEIGHT;
        }

        private int addSlider(int left, int y, String labelKey, double min, double max, int precision,
                              DoubleSupplier getter, DoubleConsumer setter) {
            this.addRenderableWidget(new ConfigSlider(left, y, BUTTON_WIDTH, labelKey, min, max, precision, getter, value -> {
                setter.accept(value);
                FocusClientConfig.saveConfig();
            }));
            return y + ROW_HEIGHT;
        }

        private void resetPage() {
            FocusConfig config = FocusClientConfig.configInstance();
            switch (page) {
                case CORE -> {
                    config.autoSwitchToThirdPerson(true);
                    config.allowFirstPersonWhileTargeting(true);
                    config.allowFrontFacingThirdPersonWhileTargeting(false);
                    config.showLockOnStatusMessages(FocusClientConfig.DEFAULT_SHOW_LOCK_ON_STATUS_MESSAGES);
                    config.showLockOnDebugText(false);
                    config.experimentalPersistentWalkthroughBackwardsWalking(FocusClientConfig.DEFAULT_EXPERIMENTAL_PERSISTENT_WALKTHROUGH_BACKWARDS_WALKING);
                    config.cinematicBarsWhileLockedOn(FocusClientConfig.DEFAULT_CINEMATIC_BARS_WHILE_LOCKED_ON);
                    config.cinematicBarsUnderHud(FocusClientConfig.DEFAULT_CINEMATIC_BARS_UNDER_HUD);
                    config.crosshair.renderCorrectedCrosshair(FocusClientConfig.DEFAULT_RENDER_CORRECTED_CROSSHAIR);
                    config.crosshair.hideVanillaCrosshair(FocusClientConfig.DEFAULT_HIDE_VANILLA_CROSSHAIR);
                    config.enableTargetFilters(FocusClientConfig.DEFAULT_TARGET_FILTERS_ENABLED);
                    config.lockOnIndicatorStyle(FocusClientConfig.LockOnIndicatorStyle.OOT_16X);
                }
                case CAMERA -> {
                    FocusClientConfig.setUseCustomSwappedShoulderValues(false, FocusClientConfig.Shoulder.LEFT);
                    config.cameraMode(FocusClientConfig.CameraMode.DYNAMIC);
                }
                case CAMERA_BEHAVIOR -> {
                    config.cameraBehavior.cameraFloatiness(FocusClientConfig.DEFAULT_CAMERA_FLOATINESS);
                    config.cameraBehavior.cameraDrag(FocusClientConfig.DEFAULT_CAMERA_DRAG);
                    config.cameraBehavior.cameraStepSize(FocusClientConfig.DEFAULT_CAMERA_STEP_SIZE);
                    config.cameraBehavior.dynamicallyAdjustOffsets(FocusClientConfig.DEFAULT_DYNAMICALLY_ADJUST_OFFSETS);
                    config.cameraBehavior.smoothCameraTransition(FocusClientConfig.DEFAULT_SMOOTH_CAMERA_TRANSITION);
                    config.cameraBehavior.cameraTransitionSpeed(FocusClientConfig.DEFAULT_CAMERA_TRANSITION_SPEED);
                }
                case CAMERA_FOLLOWING -> {
                    config.cameraFollowing.followPlayerRotations(FocusClientConfig.DEFAULT_FOLLOW_PLAYER_ROTATIONS);
                    config.cameraFollowing.followPlayerRotationsDelay(FocusClientConfig.DEFAULT_FOLLOW_PLAYER_ROTATIONS_DELAY);
                    config.cameraFollowing.cameraHeadFollowResponsiveness(FocusClientConfig.DEFAULT_CAMERA_HEAD_FOLLOW_RESPONSIVENESS);
                    config.cameraFollowing.cameraBodyFollowResponsiveness(FocusClientConfig.DEFAULT_CAMERA_BODY_FOLLOW_RESPONSIVENESS);
                    config.cameraFollowing.fullBodyFollowEnabled(FocusClientConfig.DEFAULT_FULL_BODY_FOLLOW_ENABLED);
                }
                case SHOULDER_SWAP -> {
                    config.shoulderSwap.cameraSwapSpeed(FocusClientConfig.DEFAULT_CAMERA_SWAP_SPEED);
                    config.cameraSwapSmoothness(FocusClientConfig.DEFAULT_CAMERA_SWAP_SMOOTHNESS);
                    config.shoulderSwap.dynamicCameraSwapSpeed(FocusClientConfig.DEFAULT_DYNAMIC_CAMERA_SWAP_SPEED);
                    config.dynamicCameraSwapSmoothness(FocusClientConfig.DEFAULT_DYNAMIC_CAMERA_SWAP_SMOOTHNESS);
                    config.shoulderSwap.dynamicShoulderAutoSwapEnabled(FocusClientConfig.DEFAULT_DYNAMIC_SHOULDER_AUTO_SWAP_ENABLED);
                    config.shoulderSwap.dynamicShoulderSwitchThreshold(FocusClientConfig.DEFAULT_DYNAMIC_SHOULDER_SWITCH_THRESHOLD);
                    config.shoulderSwap.dynamicShoulderVisibilityWeight(FocusClientConfig.DEFAULT_DYNAMIC_SHOULDER_VISIBILITY_WEIGHT);
                    config.shoulderSwap.dynamicShoulderManualOverrideCooldownTicks(FocusClientConfig.DEFAULT_DYNAMIC_SHOULDER_MANUAL_OVERRIDE_COOLDOWN_TICKS);
                }
                case PLAYER_TRANSPARENCY -> {
                    config.playerTransparency.adjustPlayerTransparency(FocusClientConfig.DEFAULT_ADJUST_PLAYER_TRANSPARENCY);
                    config.playerTransparency.playerTransparencyMinAlpha(FocusClientConfig.DEFAULT_PLAYER_TRANSPARENCY_MIN_ALPHA);
                    config.playerTransparency.playerTransparencyFadeSpeed(FocusClientConfig.DEFAULT_PLAYER_TRANSPARENCY_FADE_SPEED);
                    config.playerTransparency.playerTransparencyWhenTargetObscuredOnly(FocusClientConfig.DEFAULT_PLAYER_TRANSPARENCY_WHEN_TARGET_OBSCURED_ONLY);
                    config.playerTransparency.playerTransparencyInPreview(FocusClientConfig.DEFAULT_PLAYER_TRANSPARENCY_IN_PREVIEW);
                }
                case CROSSHAIR -> {
                    config.crosshair.crosshairCorrectionMode(FocusClientConfig.DEFAULT_CROSSHAIR_CORRECTION_MODE);
                    config.crosshair.renderCorrectedCrosshair(FocusClientConfig.DEFAULT_RENDER_CORRECTED_CROSSHAIR);
                    config.crosshair.correctBlockPlacementRay(FocusClientConfig.DEFAULT_CORRECT_BLOCK_PLACEMENT_RAY);
                    config.crosshair.correctEntityHitRay(FocusClientConfig.DEFAULT_CORRECT_ENTITY_HIT_RAY);
                    config.crosshair.correctCrosshairOnlyWhileLockedOn(FocusClientConfig.DEFAULT_CORRECT_CROSSHAIR_ONLY_WHILE_LOCKED_ON);
                    config.crosshair.hideVanillaCrosshair(FocusClientConfig.DEFAULT_HIDE_VANILLA_CROSSHAIR);
                    config.crosshair.hideVanillaCrosshairOutOfRange(FocusClientConfig.DEFAULT_HIDE_VANILLA_CROSSHAIR_OUT_OF_RANGE);
                }
                case TARGETING -> {
                    config.targetSwapMouseDeadzone(FocusClientConfig.DEFAULT_TARGET_SWAP_MOUSE_DEADZONE);
                    config.targetSwapMouseActivation(FocusClientConfig.DEFAULT_TARGET_SWAP_MOUSE_ACTIVATION);
                    config.targetSwapDirectionThreshold(FocusClientConfig.DEFAULT_TARGET_SWAP_DIRECTION_THRESHOLD);
                    config.targetSwapMinScreenSeparation(FocusClientConfig.DEFAULT_TARGET_SWAP_MIN_SCREEN_SEPARATION);
                    config.targetSwapInputDecay(FocusClientConfig.DEFAULT_TARGET_SWAP_INPUT_DECAY);
                    config.targetSwapCooldownTicks(FocusClientConfig.DEFAULT_TARGET_SWAP_COOLDOWN_TICKS);
                    config.targetSwapSmoothTicks(FocusClientConfig.DEFAULT_TARGET_SWAP_SMOOTH_TICKS);
                    config.targetSwapLookYawResponsiveness(FocusClientConfig.DEFAULT_TARGET_SWAP_LOOK_RESPONSIVENESS_YAW);
                    config.targetSwapLookPitchResponsiveness(FocusClientConfig.DEFAULT_TARGET_SWAP_LOOK_RESPONSIVENESS_PITCH);
                    config.targetSwapLookMaxYawStepPerTick(FocusClientConfig.DEFAULT_TARGET_SWAP_LOOK_MAX_YAW_STEP_PER_TICK);
                    config.targetSwapLookMaxPitchStepPerTick(FocusClientConfig.DEFAULT_TARGET_SWAP_LOOK_MAX_PITCH_STEP_PER_TICK);
                    config.targetSwapTargetPointResponsiveness(FocusClientConfig.DEFAULT_TARGET_SWAP_TARGET_POINT_RESPONSIVENESS);
                    config.targetSwapPlayerLookFollow(FocusClientConfig.DEFAULT_TARGET_SWAP_PLAYER_LOOK_FOLLOW);
                }
                case FILTERS -> {
                    config.enableTargetFilters(FocusClientConfig.DEFAULT_TARGET_FILTERS_ENABLED);
                    config.targetFilterMode(FocusClientConfig.TargetFilterMode.EXCLUDE);
                    config.filterPlayers(FocusClientConfig.DEFAULT_FILTER_PLAYERS);
                    config.filterPassiveMobs(FocusClientConfig.DEFAULT_FILTER_PASSIVE_MOBS);
                    config.filterNeutralMobs(FocusClientConfig.DEFAULT_FILTER_NEUTRAL_MOBS);
                    config.filterHostileMobs(FocusClientConfig.DEFAULT_FILTER_HOSTILE_MOBS);
                    config.targetFilterEntityIds(FocusClientConfig.DEFAULT_TARGET_FILTER_ENTITY_IDS);
                }
            }
        }

        private void refreshMessages() {
            for (Runnable refresher : messageRefreshers) {
                refresher.run();
            }
        }

        private void rebuildOptions() {
            this.clearWidgets();
            init();
        }

        @Override
        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            this.renderBackground(guiGraphics);
            guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
            super.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        @Override
        public void onClose() {
            if (this.minecraft != null) {
                this.minecraft.setScreen(parent);
            }
        }
    }

    private static final class EntityIdFilterScreen extends Screen {
        private final Screen parent;
        private final List<Button> entryButtons = new ArrayList<>();
        private EditBox input;
        private Component status = Component.empty();
        private int scrollOffset;

        private EntityIdFilterScreen(Screen parent) {
            super(Component.translatable("text.config.focus-config.option.targetFilterEntityIds"));
            this.parent = parent;
        }

        @Override
        protected void init() {
            entryButtons.clear();
            int left = this.width / 2 - BUTTON_WIDTH / 2;
            input = this.addRenderableWidget(new EditBox(this.font, left, TOP_MARGIN, BUTTON_WIDTH, CONTROL_HEIGHT,
                    Component.translatable("text.config.focus-config.entityIds.add")));
            input.setMaxLength(128);

            int addWidth = (BUTTON_WIDTH - 6) / 2;
            this.addRenderableWidget(Button.builder(Component.translatable("text.config.focus-config.entityIds.add"), button -> addEntry())
                    .bounds(left, TOP_MARGIN + ROW_HEIGHT, addWidth, CONTROL_HEIGHT)
                    .build());
            this.addRenderableWidget(Button.builder(Component.translatable("text.config.focus-config.entityIds.clear"), button -> {
                        FocusClientConfig.configInstance().targetFilterEntityIds(List.of());
                        FocusClientConfig.saveConfig();
                        status = Component.translatable("text.config.focus-config.entityIds.cleared");
                        rebuildEntries();
                    })
                    .bounds(left + addWidth + 6, TOP_MARGIN + ROW_HEIGHT, BUTTON_WIDTH - addWidth - 6, CONTROL_HEIGHT)
                    .build());

            rebuildEntries();

            this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                    .bounds(left, this.height - BOTTOM_MARGIN, BUTTON_WIDTH, CONTROL_HEIGHT)
                    .build());
        }

        private void rebuildEntries() {
            for (Button button : entryButtons) {
                this.removeWidget(button);
            }
            entryButtons.clear();

            List<String> ids = FocusClientConfig.targetFilterEntityIds();
            int left = this.width / 2 - BUTTON_WIDTH / 2;
            int y = TOP_MARGIN + ROW_HEIGHT * 3 - scrollOffset;
            for (String id : ids) {
                Button button = Button.builder(Component.translatable("text.config.focus-config.entityIds.remove", id), clicked -> removeEntry(id))
                        .bounds(left, y, BUTTON_WIDTH, CONTROL_HEIGHT)
                        .build();
                button.visible = y >= TOP_MARGIN + ROW_HEIGHT * 2 && y <= this.height - BOTTOM_MARGIN - ROW_HEIGHT;
                this.addRenderableWidget(button);
                entryButtons.add(button);
                y += ROW_HEIGHT;
            }
        }

        private void addEntry() {
            String raw = input.getValue().trim();
            ResourceLocation id = ResourceLocation.tryParse(raw);
            if (id == null) {
                status = Component.translatable("text.config.focus-config.entityIds.invalid");
                return;
            }

            List<String> ids = new ArrayList<>(FocusClientConfig.targetFilterEntityIds());
            String normalized = id.toString();
            if (!ids.contains(normalized)) {
                ids.add(normalized);
                FocusClientConfig.configInstance().targetFilterEntityIds(ids);
                FocusClientConfig.saveConfig();
            }
            input.setValue("");
            status = Component.translatable("text.config.focus-config.entityIds.added", normalized);
            rebuildEntries();
        }

        private void removeEntry(String id) {
            List<String> ids = new ArrayList<>(FocusClientConfig.targetFilterEntityIds());
            ids.remove(id);
            FocusClientConfig.configInstance().targetFilterEntityIds(ids);
            FocusClientConfig.saveConfig();
            status = Component.translatable("text.config.focus-config.entityIds.removed", id);
            rebuildEntries();
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
            int maxScroll = Math.max(0, FocusClientConfig.targetFilterEntityIds().size() * ROW_HEIGHT
                    - (this.height - TOP_MARGIN - BOTTOM_MARGIN - ROW_HEIGHT * 4));
            scrollOffset = Mth.clamp(scrollOffset - (int) (delta * ROW_HEIGHT), 0, maxScroll);
            rebuildEntries();
            return true;
        }

        @Override
        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            this.renderBackground(guiGraphics);
            guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
            guiGraphics.drawCenteredString(this.font, status, this.width / 2, TOP_MARGIN + ROW_HEIGHT * 2 + 4, 0xA0A0A0);
            super.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        @Override
        public void onClose() {
            if (this.minecraft != null) {
                this.minecraft.setScreen(parent);
            }
        }
    }

    private static final class ConfigSlider extends AbstractSliderButton {
        private final String labelKey;
        private final double min;
        private final double max;
        private final int precision;
        private final DoubleConsumer setter;

        private ConfigSlider(int x, int y, int width, String labelKey, double min, double max, int precision,
                             DoubleSupplier getter, DoubleConsumer setter) {
            super(x, y, width, CONTROL_HEIGHT, Component.empty(), sliderValue(min, max, getter.getAsDouble()));
            this.labelKey = labelKey;
            this.min = min;
            this.max = max;
            this.precision = precision;
            this.setter = setter;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            this.setMessage(CommonComponents.optionNameValue(
                    Component.translatable(labelKey),
                    Component.literal(format(actualValue(), precision))));
        }

        @Override
        protected void applyValue() {
            setter.accept(actualValue());
        }

        private double actualValue() {
            double value = Mth.clampedLerp(min, max, this.value);
            if (precision == 0) {
                return Math.round(value);
            }
            return value;
        }

        private static double sliderValue(double min, double max, double configuredValue) {
            if (max <= min) {
                return 0.0D;
            }
            return Mth.clamp((configuredValue - min) / (max - min), 0.0D, 1.0D);
        }
    }

    private static Component onOff(boolean value) {
        return value ? Component.translatable("options.on") : Component.translatable("options.off");
    }

    private static Component enumName(Enum<?> value) {
        if (value instanceof FocusClientConfig.CameraMode) {
            return Component.translatable("focus.lock_on_client.camera_mode." + value.name());
        }
        if (value instanceof FocusClientConfig.CrosshairCorrectionMode) {
            return Component.translatable("focus.lock_on_client.crosshair_correction_mode." + value.name());
        }
        if (value instanceof FocusClientConfig.TargetFilterMode) {
            return Component.translatable("focus.lock_on_client.target_filter_mode." + value.name());
        }
        if (value instanceof FocusClientConfig.LockOnIndicatorStyle) {
            return Component.translatable("focus.lock_on_client.lock_on_indicator_style." + value.name());
        }
        return Component.literal(value.name());
    }

    private static String format(double value, int precision) {
        return String.format(Locale.ROOT, "%." + precision + "f", value);
    }
}
