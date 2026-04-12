package com.jvn.focus.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jvn.focus.Focus;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import me.fzzyhmstrs.fzzy_config.api.ConfigApiJava;
import me.fzzyhmstrs.fzzy_config.api.RegisterType;
import me.fzzyhmstrs.fzzy_config.config.ConfigAction;
import me.fzzyhmstrs.fzzy_config.config.Config;
import me.fzzyhmstrs.fzzy_config.config.ConfigSection;
import me.fzzyhmstrs.fzzy_config.validation.collection.ValidatedList;
import me.fzzyhmstrs.fzzy_config.util.EnumTranslatable;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedBoolean;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedCondition;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedEnum;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedString;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedDouble;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.loading.FMLPaths;

public final class FocusClientConfig extends Config {
    public static final double DEFAULT_CAMERA_OFFSET_X = -2.0D;
    public static final double DEFAULT_CAMERA_OFFSET_Y = 0.4D;
    public static final double DEFAULT_CAMERA_OFFSET_Z = 1.5D;
    public static final double DEFAULT_CAMERA_ROTATION = 0.0D;
    public static final double DEFAULT_CAMERA_FLOATINESS = 0.25D;
    public static final double DEFAULT_CAMERA_DRAG = 0.95D;
    public static final double DEFAULT_CAMERA_SWAP_SPEED = 0.1D;
    public static final double DEFAULT_CAMERA_SWAP_SMOOTHNESS = 1.0D;
    public static final double DEFAULT_DYNAMIC_CAMERA_SWAP_SPEED = 0.01D;
    public static final double DEFAULT_DYNAMIC_CAMERA_SWAP_SMOOTHNESS = 0.0D;
    public static final double DEFAULT_TARGET_SWAP_MOUSE_DEADZONE = 12.0D;
    public static final double DEFAULT_TARGET_SWAP_MOUSE_ACTIVATION = 21.0D;
    public static final double DEFAULT_TARGET_SWAP_DIRECTION_THRESHOLD = 0.56D;
    public static final double DEFAULT_TARGET_SWAP_MIN_SCREEN_SEPARATION = 0.06D;
    public static final double DEFAULT_TARGET_SWAP_INPUT_DECAY = 0.82D;
    public static final double DEFAULT_TARGET_SWAP_COOLDOWN_TICKS = 8.0D;
    public static final double DEFAULT_TARGET_SWAP_SMOOTH_TICKS = 12.0D;
    public static final double DEFAULT_TARGET_SWAP_LOOK_RESPONSIVENESS_YAW = 2.8D;
    public static final double DEFAULT_TARGET_SWAP_LOOK_RESPONSIVENESS_PITCH = 2.4D;
    public static final double DEFAULT_TARGET_SWAP_LOOK_MAX_YAW_STEP_PER_TICK = 3.8D;
    public static final double DEFAULT_TARGET_SWAP_LOOK_MAX_PITCH_STEP_PER_TICK = 2.8D;
    public static final double DEFAULT_TARGET_SWAP_TARGET_POINT_RESPONSIVENESS = 4.2D;
    public static final double DEFAULT_TARGET_SWAP_PLAYER_LOOK_FOLLOW = 0.3D;
    public static final boolean DEFAULT_TARGET_FILTERS_ENABLED = false;
    public static final boolean DEFAULT_FILTER_PLAYERS = true;
    public static final boolean DEFAULT_FILTER_PASSIVE_MOBS = true;
    public static final boolean DEFAULT_FILTER_NEUTRAL_MOBS = false;
    public static final boolean DEFAULT_FILTER_HOSTILE_MOBS = false;
    public static final List<String> DEFAULT_TARGET_FILTER_ENTITY_IDS = List.of();
    public static final double MIN_CAMERA_OFFSET_X = -4.0D;
    public static final double MAX_CAMERA_OFFSET_X = 4.0D;
    public static final double MIN_CAMERA_OFFSET_Y = -2.0D;
    public static final double MAX_CAMERA_OFFSET_Y = 2.5D;
    public static final double MIN_CAMERA_OFFSET_Z = 0.5D;
    public static final double MAX_CAMERA_OFFSET_Z = 8.0D;
    public static final double MIN_CAMERA_ROTATION = -180.0D;
    public static final double MAX_CAMERA_ROTATION = 180.0D;
    public static final double MIN_CAMERA_FLOATINESS = 0.01D;
    public static final double MAX_CAMERA_FLOATINESS = 1.0D;
    public static final double MIN_CAMERA_DRAG = 0.0D;
    public static final double MAX_CAMERA_DRAG = 0.95D;
    public static final double MIN_CAMERA_SWAP_SPEED = 0.01D;
    public static final double MAX_CAMERA_SWAP_SPEED = 1.0D;
    public static final double MIN_CAMERA_SWAP_SMOOTHNESS = 0.0D;
    public static final double MAX_CAMERA_SWAP_SMOOTHNESS = 1.0D;
    public static final double MIN_DYNAMIC_CAMERA_SWAP_SPEED = 0.01D;
    public static final double MAX_DYNAMIC_CAMERA_SWAP_SPEED = 1.0D;
    public static final double MIN_DYNAMIC_CAMERA_SWAP_SMOOTHNESS = 0.0D;
    public static final double MAX_DYNAMIC_CAMERA_SWAP_SMOOTHNESS = 1.0D;
    public static final double MIN_TARGET_SWAP_MOUSE_DEADZONE = 0.0D;
    public static final double MAX_TARGET_SWAP_MOUSE_DEADZONE = 60.0D;
    public static final double MIN_TARGET_SWAP_MOUSE_ACTIVATION = 0.0D;
    public static final double MAX_TARGET_SWAP_MOUSE_ACTIVATION = 80.0D;
    public static final double MIN_TARGET_SWAP_DIRECTION_THRESHOLD = 0.0D;
    public static final double MAX_TARGET_SWAP_DIRECTION_THRESHOLD = 1.0D;
    public static final double MIN_TARGET_SWAP_MIN_SCREEN_SEPARATION = 0.0D;
    public static final double MAX_TARGET_SWAP_MIN_SCREEN_SEPARATION = 0.5D;
    public static final double MIN_TARGET_SWAP_INPUT_DECAY = 0.0D;
    public static final double MAX_TARGET_SWAP_INPUT_DECAY = 1.0D;
    public static final double MIN_TARGET_SWAP_COOLDOWN_TICKS = 0.0D;
    public static final double MAX_TARGET_SWAP_COOLDOWN_TICKS = 40.0D;
    public static final double MIN_TARGET_SWAP_SMOOTH_TICKS = 0.0D;
    public static final double MAX_TARGET_SWAP_SMOOTH_TICKS = 40.0D;
    public static final double MIN_TARGET_SWAP_LOOK_RESPONSIVENESS_YAW = 0.1D;
    public static final double MAX_TARGET_SWAP_LOOK_RESPONSIVENESS_YAW = 20.0D;
    public static final double MIN_TARGET_SWAP_LOOK_RESPONSIVENESS_PITCH = 0.1D;
    public static final double MAX_TARGET_SWAP_LOOK_RESPONSIVENESS_PITCH = 20.0D;
    public static final double MIN_TARGET_SWAP_LOOK_MAX_YAW_STEP_PER_TICK = 0.1D;
    public static final double MAX_TARGET_SWAP_LOOK_MAX_YAW_STEP_PER_TICK = 30.0D;
    public static final double MIN_TARGET_SWAP_LOOK_MAX_PITCH_STEP_PER_TICK = 0.1D;
    public static final double MAX_TARGET_SWAP_LOOK_MAX_PITCH_STEP_PER_TICK = 30.0D;
    public static final double MIN_TARGET_SWAP_TARGET_POINT_RESPONSIVENESS = 0.1D;
    public static final double MAX_TARGET_SWAP_TARGET_POINT_RESPONSIVENESS = 30.0D;
    public static final double MIN_TARGET_SWAP_PLAYER_LOOK_FOLLOW = 0.0D;
    public static final double MAX_TARGET_SWAP_PLAYER_LOOK_FOLLOW = 1.0D;
    public static final double CAMERA_SLIDER_INCREMENT = 0.1D;
    public static final double ROTATION_SLIDER_INCREMENT = 1.0D;
    public static final int MAX_CAMERA_PROFILE_NAME_LENGTH = 40;
    private static final int CAMERA_VALUE_SCALE = 1;
    private static final Path CAMERA_PRESET_PATH = FMLPaths.CONFIGDIR.get().resolve(Focus.MOD_ID + "_lock_on_camera.json");

    private static FocusClientConfig INSTANCE;
    private static PerspectivePreset leftShoulderPreset = defaultLeftPreset();
    private static PerspectivePreset rightShoulderPreset = defaultLeftPreset().mirroredForOppositeShoulder();
    private static final Map<String, CameraSetupPreset> CAMERA_SETUP_PROFILES = new LinkedHashMap<>();

    public enum Shoulder {
        LEFT("focus.shoulder.left"),
        RIGHT("focus.shoulder.right");

        private final String translationKey;

        Shoulder(String translationKey) {
            this.translationKey = translationKey;
        }

        public Shoulder opposite() {
            return this == LEFT ? RIGHT : LEFT;
        }

        public Component displayName() {
            return Component.translatable(translationKey);
        }
    }

    public enum CameraMode implements EnumTranslatable {
        STATIC,
        DYNAMIC;

        @Override
        public String prefix() {
            return "focus.lock_on_client.camera_mode";
        }
    }

    public enum TargetFilterMode implements EnumTranslatable {
        EXCLUDE,
        EXCLUSIVE;

        @Override
        public String prefix() {
            return "focus.lock_on_client.target_filter_mode";
        }
    }

    public enum LockOnIndicatorStyle implements EnumTranslatable {
        OOT_16X("textures/ui/hud/lock_on_indicators/oot_lock_on_16x.png", 8, IndicatorType.OOT_TRIANGLES),
        OOT_32X("textures/ui/hud/lock_on_indicators/oot_lock_on_32x.png", 8, IndicatorType.OOT_TRIANGLES),
        DS2_16X("textures/ui/hud/lock_on_indicators/ds2_lock_on_16x.png", 16, IndicatorType.STATIC_CENTERED),
        DS2_32X("textures/ui/hud/lock_on_indicators/ds2_lock_on_32x.png", 8, IndicatorType.STATIC_CENTERED);

        private final ResourceLocation texture;
        private final int drawSize;
        private final IndicatorType indicatorType;

        LockOnIndicatorStyle(String texturePath, int drawSize, IndicatorType indicatorType) {
            this.texture = ResourceLocation.fromNamespaceAndPath(Focus.MOD_ID, texturePath);
            this.drawSize = drawSize;
            this.indicatorType = indicatorType;
        }

        @Override
        public String prefix() {
            return "focus.lock_on_client.lock_on_indicator_style";
        }

        public ResourceLocation texture() {
            return texture;
        }

        public int drawSize() {
            return drawSize;
        }

        public IndicatorType indicatorType() {
            return indicatorType;
        }

        public boolean usesOotTriangleOrbit() {
            return indicatorType == IndicatorType.OOT_TRIANGLES;
        }

        public enum IndicatorType {
            OOT_TRIANGLES,
            STATIC_CENTERED
        }
    }

    public GeneralSection general = new GeneralSection();
    public CameraSection camera = new CameraSection();
    public TargetSwapSection targetSwap = new TargetSwapSection();
    public TargetFilterSection targetFilters = new TargetFilterSection();

    public FocusClientConfig() {
        super(ResourceLocation.fromNamespaceAndPath(Focus.MOD_ID, "lock_on_client"));
    }

    public static class GeneralSection extends ConfigSection {
        public ValidatedBoolean autoSwitchToThirdPerson = new ValidatedBoolean(true);
        public ValidatedBoolean allowFirstPersonWhileTargeting = new ValidatedBoolean(true);
        public ValidatedCondition<Boolean> allowFrontFacingThirdPersonWhileTargeting =
                new ValidatedBoolean(false).toCondition(
                        allowFirstPersonWhileTargeting,
                        Component.translatable("focus.lock_on_client.general.allowFrontFacingThirdPersonWhileTargeting.condition"),
                        () -> false);
        public ValidatedBoolean showLockOnDebugText = new ValidatedBoolean(false);
        public ValidatedEnum<LockOnIndicatorStyle> lockOnIndicatorStyle =
                new ValidatedEnum<>(LockOnIndicatorStyle.OOT_16X, ValidatedEnum.WidgetType.CYCLING);
    }

    public static class CameraSection extends ConfigSection {
        public ValidatedBoolean useCustomSwappedShoulderValues = new ValidatedBoolean(false);
        public ValidatedEnum<CameraMode> cameraMode = new ValidatedEnum<>(CameraMode.DYNAMIC, ValidatedEnum.WidgetType.CYCLING);
        public ValidatedDouble cameraFloatiness = new ValidatedDouble(
                DEFAULT_CAMERA_FLOATINESS, MAX_CAMERA_FLOATINESS, MIN_CAMERA_FLOATINESS);
        public ValidatedDouble cameraDrag = new ValidatedDouble(
                DEFAULT_CAMERA_DRAG, MAX_CAMERA_DRAG, MIN_CAMERA_DRAG);
        public ValidatedDouble cameraSwapSpeed = new ValidatedDouble(
                DEFAULT_CAMERA_SWAP_SPEED, MAX_CAMERA_SWAP_SPEED, MIN_CAMERA_SWAP_SPEED);
        public ValidatedDouble cameraSwapSmoothness = new ValidatedDouble(
                DEFAULT_CAMERA_SWAP_SMOOTHNESS, MAX_CAMERA_SWAP_SMOOTHNESS, MIN_CAMERA_SWAP_SMOOTHNESS);
        public ValidatedDouble dynamicCameraSwapSpeed = new ValidatedDouble(
                DEFAULT_DYNAMIC_CAMERA_SWAP_SPEED, MAX_DYNAMIC_CAMERA_SWAP_SPEED, MIN_DYNAMIC_CAMERA_SWAP_SPEED);
        public ValidatedDouble dynamicCameraSwapSmoothness = new ValidatedDouble(
                DEFAULT_DYNAMIC_CAMERA_SWAP_SMOOTHNESS, MAX_DYNAMIC_CAMERA_SWAP_SMOOTHNESS, MIN_DYNAMIC_CAMERA_SWAP_SMOOTHNESS);
        public CameraPresetToolsSection presetTools = new CameraPresetToolsSection();
    }

    public static class CameraPresetToolsSection extends ConfigSection {
        public ConfigAction openCameraPositionEditor = new ConfigAction.Builder()
                .title(() -> Component.translatable("focus.lock_on_client.camera.openCameraPositionEditor"))
                .desc(Component.translatable("focus.lock_on_client.camera.openCameraPositionEditor.desc"))
                .build(LockOnCameraEditorScreen::openFromCurrentScreen);
        public ValidatedString selectedCameraProfile = new ValidatedString("");
        public ConfigAction cycleCameraProfile = new ConfigAction.Builder()
                .title(() -> Component.translatable("focus.lock_on_client.camera.cycleCameraProfile"))
                .desc(Component.translatable("focus.lock_on_client.camera.cycleCameraProfile.desc"))
                .active(() -> !cameraProfileNames().isEmpty())
                .build(FocusClientConfig::cycleSelectedCameraProfile);
        public ConfigAction saveSelectedCameraProfile = new ConfigAction.Builder()
                .title(() -> Component.translatable("focus.lock_on_client.camera.saveSelectedCameraProfile"))
                .desc(Component.translatable("focus.lock_on_client.camera.saveSelectedCameraProfile.desc"))
                .active(() -> !selectedCameraProfileName().isEmpty())
                .build(FocusClientConfig::saveSelectedCameraProfileFromConfigScreen);
        public ConfigAction loadSelectedCameraProfile = new ConfigAction.Builder()
                .title(() -> Component.translatable("focus.lock_on_client.camera.loadSelectedCameraProfile"))
                .desc(Component.translatable("focus.lock_on_client.camera.loadSelectedCameraProfile.desc"))
                .active(() -> !resolveCameraProfileName(selectedCameraProfileName()).isEmpty())
                .build(FocusClientConfig::loadSelectedCameraProfileFromConfigScreen);
        public ConfigAction deleteSelectedCameraProfile = new ConfigAction.Builder()
                .title(() -> Component.translatable("focus.lock_on_client.camera.deleteSelectedCameraProfile"))
                .desc(Component.translatable("focus.lock_on_client.camera.deleteSelectedCameraProfile.desc"))
                .active(() -> !resolveCameraProfileName(selectedCameraProfileName()).isEmpty())
                .build(FocusClientConfig::deleteSelectedCameraProfileFromConfigScreen);
    }

    public static class TargetSwapSection extends ConfigSection {
        public ValidatedDouble targetSwapMouseDeadzone = new ValidatedDouble(
                DEFAULT_TARGET_SWAP_MOUSE_DEADZONE, MAX_TARGET_SWAP_MOUSE_DEADZONE, MIN_TARGET_SWAP_MOUSE_DEADZONE);
        public ValidatedDouble targetSwapMouseActivation = new ValidatedDouble(
                DEFAULT_TARGET_SWAP_MOUSE_ACTIVATION, MAX_TARGET_SWAP_MOUSE_ACTIVATION, MIN_TARGET_SWAP_MOUSE_ACTIVATION);
        public ValidatedDouble targetSwapDirectionThreshold = new ValidatedDouble(
                DEFAULT_TARGET_SWAP_DIRECTION_THRESHOLD, MAX_TARGET_SWAP_DIRECTION_THRESHOLD, MIN_TARGET_SWAP_DIRECTION_THRESHOLD);
        public ValidatedDouble targetSwapMinScreenSeparation = new ValidatedDouble(
                DEFAULT_TARGET_SWAP_MIN_SCREEN_SEPARATION, MAX_TARGET_SWAP_MIN_SCREEN_SEPARATION, MIN_TARGET_SWAP_MIN_SCREEN_SEPARATION);
        public ValidatedDouble targetSwapInputDecay = new ValidatedDouble(
                DEFAULT_TARGET_SWAP_INPUT_DECAY, MAX_TARGET_SWAP_INPUT_DECAY, MIN_TARGET_SWAP_INPUT_DECAY);
        public ValidatedDouble targetSwapCooldownTicks = new ValidatedDouble(
                DEFAULT_TARGET_SWAP_COOLDOWN_TICKS, MAX_TARGET_SWAP_COOLDOWN_TICKS, MIN_TARGET_SWAP_COOLDOWN_TICKS);
        public ValidatedDouble targetSwapSmoothTicks = new ValidatedDouble(
                DEFAULT_TARGET_SWAP_SMOOTH_TICKS, MAX_TARGET_SWAP_SMOOTH_TICKS, MIN_TARGET_SWAP_SMOOTH_TICKS);
        public ValidatedDouble targetSwapLookYawResponsiveness = new ValidatedDouble(
                DEFAULT_TARGET_SWAP_LOOK_RESPONSIVENESS_YAW,
                MAX_TARGET_SWAP_LOOK_RESPONSIVENESS_YAW,
                MIN_TARGET_SWAP_LOOK_RESPONSIVENESS_YAW);
        public ValidatedDouble targetSwapLookPitchResponsiveness = new ValidatedDouble(
                DEFAULT_TARGET_SWAP_LOOK_RESPONSIVENESS_PITCH,
                MAX_TARGET_SWAP_LOOK_RESPONSIVENESS_PITCH,
                MIN_TARGET_SWAP_LOOK_RESPONSIVENESS_PITCH);
        public ValidatedDouble targetSwapLookMaxYawStepPerTick = new ValidatedDouble(
                DEFAULT_TARGET_SWAP_LOOK_MAX_YAW_STEP_PER_TICK,
                MAX_TARGET_SWAP_LOOK_MAX_YAW_STEP_PER_TICK,
                MIN_TARGET_SWAP_LOOK_MAX_YAW_STEP_PER_TICK);
        public ValidatedDouble targetSwapLookMaxPitchStepPerTick = new ValidatedDouble(
                DEFAULT_TARGET_SWAP_LOOK_MAX_PITCH_STEP_PER_TICK,
                MAX_TARGET_SWAP_LOOK_MAX_PITCH_STEP_PER_TICK,
                MIN_TARGET_SWAP_LOOK_MAX_PITCH_STEP_PER_TICK);
        public ValidatedDouble targetSwapTargetPointResponsiveness = new ValidatedDouble(
                DEFAULT_TARGET_SWAP_TARGET_POINT_RESPONSIVENESS,
                MAX_TARGET_SWAP_TARGET_POINT_RESPONSIVENESS,
                MIN_TARGET_SWAP_TARGET_POINT_RESPONSIVENESS);
        public ValidatedDouble targetSwapPlayerLookFollow = new ValidatedDouble(
                DEFAULT_TARGET_SWAP_PLAYER_LOOK_FOLLOW,
                MAX_TARGET_SWAP_PLAYER_LOOK_FOLLOW,
                MIN_TARGET_SWAP_PLAYER_LOOK_FOLLOW);
    }

    public static class TargetFilterSection extends ConfigSection {
        public ValidatedBoolean enableTargetFilters = new ValidatedBoolean(DEFAULT_TARGET_FILTERS_ENABLED);
        public ValidatedEnum<TargetFilterMode> targetFilterMode =
                new ValidatedEnum<>(TargetFilterMode.EXCLUDE, ValidatedEnum.WidgetType.CYCLING);
        public ValidatedBoolean filterPlayers = new ValidatedBoolean(DEFAULT_FILTER_PLAYERS);
        public ValidatedBoolean filterPassiveMobs = new ValidatedBoolean(DEFAULT_FILTER_PASSIVE_MOBS);
        public ValidatedBoolean filterNeutralMobs = new ValidatedBoolean(DEFAULT_FILTER_NEUTRAL_MOBS);
        public ValidatedBoolean filterHostileMobs = new ValidatedBoolean(DEFAULT_FILTER_HOSTILE_MOBS);
        public ValidatedList<String> targetFilterEntityIds = ValidatedList.ofString(DEFAULT_TARGET_FILTER_ENTITY_IDS);
    }

    public static void init() {
        if (INSTANCE == null) {
            INSTANCE = ConfigApiJava.registerAndLoadConfig(FocusClientConfig::new, RegisterType.CLIENT);
            loadCameraPresets();
            if (!INSTANCE.camera.useCustomSwappedShoulderValues.get()) {
                mirrorOppositeShoulderFrom(Shoulder.LEFT);
                saveCameraPresets();
            }
            INSTANCE.targetFilters.targetFilterEntityIds.validateAndSet(
                    sanitizeTargetFilterEntityIds(INSTANCE.targetFilters.targetFilterEntityIds.get()));
            INSTANCE.camera.presetTools.selectedCameraProfile.validateAndSet(
                    sanitizeCameraProfileName(INSTANCE.camera.presetTools.selectedCameraProfile.get()));
        }
    }

    public static boolean autoSwitchToThirdPerson() {
        return config().general.autoSwitchToThirdPerson.get();
    }

    public static boolean allowFirstPersonWhileTargeting() {
        return config().general.allowFirstPersonWhileTargeting.get();
    }

    public static boolean allowFrontFacingThirdPersonWhileTargeting() {
        return config().general.allowFrontFacingThirdPersonWhileTargeting.get();
    }

    public static boolean showLockOnDebugText() {
        return config().general.showLockOnDebugText.get();
    }

    public static LockOnIndicatorStyle lockOnIndicatorStyle() {
        return config().general.lockOnIndicatorStyle.get();
    }

    public static boolean useCustomSwappedShoulderValues() {
        return config().camera.useCustomSwappedShoulderValues.get();
    }

    public static CameraMode cameraMode() {
        return config().camera.cameraMode.get();
    }

    public static double cameraFloatiness() {
        return config().camera.cameraFloatiness.get();
    }

    public static double cameraDrag() {
        return config().camera.cameraDrag.get();
    }

    public static double cameraSwapSpeed() {
        return config().camera.cameraSwapSpeed.get();
    }

    public static double cameraSwapSmoothness() {
        return config().camera.cameraSwapSmoothness.get();
    }

    public static double dynamicCameraSwapSpeed() {
        return config().camera.dynamicCameraSwapSpeed.get();
    }

    public static double dynamicCameraSwapSmoothness() {
        return config().camera.dynamicCameraSwapSmoothness.get();
    }

    public static double targetSwapMouseDeadzone() {
        return config().targetSwap.targetSwapMouseDeadzone.get();
    }

    public static double targetSwapMouseActivation() {
        return Math.max(targetSwapMouseDeadzone(), config().targetSwap.targetSwapMouseActivation.get());
    }

    public static double targetSwapDirectionThreshold() {
        return config().targetSwap.targetSwapDirectionThreshold.get();
    }

    public static double targetSwapMinScreenSeparation() {
        return config().targetSwap.targetSwapMinScreenSeparation.get();
    }

    public static double targetSwapInputDecay() {
        return config().targetSwap.targetSwapInputDecay.get();
    }

    public static int targetSwapCooldownTicks() {
        return (int) Math.round(config().targetSwap.targetSwapCooldownTicks.get());
    }

    public static int targetSwapSmoothTicks() {
        return (int) Math.round(config().targetSwap.targetSwapSmoothTicks.get());
    }

    public static float targetSwapLookYawResponsiveness() {
        return config().targetSwap.targetSwapLookYawResponsiveness.get().floatValue();
    }

    public static float targetSwapLookPitchResponsiveness() {
        return config().targetSwap.targetSwapLookPitchResponsiveness.get().floatValue();
    }

    public static float targetSwapLookMaxYawStepPerTick() {
        return config().targetSwap.targetSwapLookMaxYawStepPerTick.get().floatValue();
    }

    public static float targetSwapLookMaxPitchStepPerTick() {
        return config().targetSwap.targetSwapLookMaxPitchStepPerTick.get().floatValue();
    }

    public static float targetSwapTargetPointResponsiveness() {
        return config().targetSwap.targetSwapTargetPointResponsiveness.get().floatValue();
    }

    public static float targetSwapPlayerLookFollow() {
        return config().targetSwap.targetSwapPlayerLookFollow.get().floatValue();
    }

    public static boolean enableTargetFilters() {
        return config().targetFilters.enableTargetFilters.get();
    }

    public static TargetFilterMode targetFilterMode() {
        return config().targetFilters.targetFilterMode.get();
    }

    public static boolean filterPlayers() {
        return config().targetFilters.filterPlayers.get();
    }

    public static boolean filterPassiveMobs() {
        return config().targetFilters.filterPassiveMobs.get();
    }

    public static boolean filterNeutralMobs() {
        return config().targetFilters.filterNeutralMobs.get();
    }

    public static boolean filterHostileMobs() {
        return config().targetFilters.filterHostileMobs.get();
    }

    public static List<String> targetFilterEntityIds() {
        return sanitizeTargetFilterEntityIds(config().targetFilters.targetFilterEntityIds.get());
    }

    public static String selectedCameraProfileName() {
        return sanitizeCameraProfileName(config().camera.presetTools.selectedCameraProfile.get());
    }

    public static void setSelectedCameraProfileName(String profileName) {
        config().camera.presetTools.selectedCameraProfile.validateAndSet(sanitizeCameraProfileName(profileName));
    }

    public static void setUseCustomSwappedShoulderValues(boolean useCustomSwappedShoulderValues, Shoulder sourceShoulder) {
        config().camera.useCustomSwappedShoulderValues.validateAndSet(useCustomSwappedShoulderValues);
        if (!useCustomSwappedShoulderValues) {
            mirrorOppositeShoulderFrom(sourceShoulder);
        }
    }

    public static double cameraOffsetX() {
        return cameraOffsetX(Shoulder.LEFT);
    }

    public static double cameraOffsetX(Shoulder shoulder) {
        return presetFor(shoulder).offsetX();
    }

    public static double cameraOffsetY() {
        return cameraOffsetY(Shoulder.LEFT);
    }

    public static double cameraOffsetY(Shoulder shoulder) {
        return presetFor(shoulder).offsetY();
    }

    public static double cameraOffsetZ() {
        return cameraOffsetZ(Shoulder.LEFT);
    }

    public static double cameraOffsetZ(Shoulder shoulder) {
        return presetFor(shoulder).offsetZ();
    }

    public static double cameraRotation() {
        return cameraRotation(Shoulder.LEFT);
    }

    public static double cameraRotation(Shoulder shoulder) {
        return presetFor(shoulder).rotation();
    }

    public static void setCameraOffsetX(double value) {
        setCameraOffsetX(Shoulder.LEFT, value);
    }

    public static void setCameraOffsetX(Shoulder shoulder, double value) {
        setRawCameraOffsetX(shoulder, value);
        syncMirroredShoulderIfNeeded(shoulder);
    }

    public static void setCameraOffsetY(double value) {
        setCameraOffsetY(Shoulder.LEFT, value);
    }

    public static void setCameraOffsetY(Shoulder shoulder, double value) {
        setRawCameraOffsetY(shoulder, value);
        syncMirroredShoulderIfNeeded(shoulder);
    }

    public static void setCameraOffsetZ(double value) {
        setCameraOffsetZ(Shoulder.LEFT, value);
    }

    public static void setCameraOffsetZ(Shoulder shoulder, double value) {
        setRawCameraOffsetZ(shoulder, value);
        syncMirroredShoulderIfNeeded(shoulder);
    }

    public static void setCameraRotation(double value) {
        setCameraRotation(Shoulder.LEFT, value);
    }

    public static void setCameraRotation(Shoulder shoulder, double value) {
        setRawCameraRotation(shoulder, value);
        syncMirroredShoulderIfNeeded(shoulder);
    }

    public static void resetCameraOffsetsToDefaults() {
        resetCameraOffsetsToDefaults(Shoulder.LEFT);
    }

    public static void resetCameraOffsetsToDefaults(Shoulder sourceShoulder) {
        setRawPreset(Shoulder.LEFT, defaultLeftPreset());
        setRawPreset(Shoulder.RIGHT, defaultLeftPreset().mirroredForOppositeShoulder());
        if (!useCustomSwappedShoulderValues()) {
            mirrorOppositeShoulderFrom(sourceShoulder);
        }
    }

    public static PerspectivePreset currentPreset() {
        return currentPreset(Shoulder.LEFT);
    }

    public static PerspectivePreset currentPreset(Shoulder shoulder) {
        return presetFor(shoulder);
    }

    public static void applyPreset(PerspectivePreset preset) {
        applyPreset(Shoulder.LEFT, preset);
    }

    public static void applyPreset(Shoulder shoulder, PerspectivePreset preset) {
        setRawPreset(shoulder, preset);
        syncMirroredShoulderIfNeeded(shoulder);
    }

    public static CameraSetupPreset currentCameraSetupPreset() {
        return new CameraSetupPreset(
                autoSwitchToThirdPerson(),
                allowFirstPersonWhileTargeting(),
                allowFrontFacingThirdPersonWhileTargeting(),
                showLockOnDebugText(),
                lockOnIndicatorStyle(),
                cameraMode(),
                cameraFloatiness(),
                cameraDrag(),
                cameraSwapSpeed(),
                cameraSwapSmoothness(),
                dynamicCameraSwapSpeed(),
                dynamicCameraSwapSmoothness(),
                config().targetSwap.targetSwapMouseDeadzone.get(),
                config().targetSwap.targetSwapMouseActivation.get(),
                config().targetSwap.targetSwapDirectionThreshold.get(),
                config().targetSwap.targetSwapMinScreenSeparation.get(),
                config().targetSwap.targetSwapInputDecay.get(),
                config().targetSwap.targetSwapCooldownTicks.get(),
                config().targetSwap.targetSwapSmoothTicks.get(),
                config().targetSwap.targetSwapLookYawResponsiveness.get(),
                config().targetSwap.targetSwapLookPitchResponsiveness.get(),
                config().targetSwap.targetSwapLookMaxYawStepPerTick.get(),
                config().targetSwap.targetSwapLookMaxPitchStepPerTick.get(),
                config().targetSwap.targetSwapTargetPointResponsiveness.get(),
                config().targetSwap.targetSwapPlayerLookFollow.get(),
                enableTargetFilters(),
                targetFilterMode(),
                config().targetFilters.filterPlayers.get(),
                config().targetFilters.filterPassiveMobs.get(),
                config().targetFilters.filterNeutralMobs.get(),
                config().targetFilters.filterHostileMobs.get(),
                targetFilterEntityIds(),
                currentPreset(Shoulder.LEFT),
                currentPreset(Shoulder.RIGHT),
                useCustomSwappedShoulderValues());
    }

    public static void applyCameraSetupPreset(CameraSetupPreset setup) {
        config().general.autoSwitchToThirdPerson.validateAndSet(setup.autoSwitchToThirdPerson());
        config().general.allowFirstPersonWhileTargeting.validateAndSet(setup.allowFirstPersonWhileTargeting());
        config().general.allowFrontFacingThirdPersonWhileTargeting.validateAndSet(setup.allowFrontFacingThirdPersonWhileTargeting());
        config().general.showLockOnDebugText.validateAndSet(setup.showLockOnDebugText());
        config().general.lockOnIndicatorStyle.validateAndSet(setup.lockOnIndicatorStyle());
        config().camera.cameraMode.validateAndSet(setup.cameraMode());
        config().camera.cameraFloatiness.validateAndSet(setup.cameraFloatiness());
        config().camera.cameraDrag.validateAndSet(setup.cameraDrag());
        config().camera.cameraSwapSpeed.validateAndSet(setup.cameraSwapSpeed());
        config().camera.cameraSwapSmoothness.validateAndSet(setup.cameraSwapSmoothness());
        config().camera.dynamicCameraSwapSpeed.validateAndSet(setup.dynamicCameraSwapSpeed());
        config().camera.dynamicCameraSwapSmoothness.validateAndSet(setup.dynamicCameraSwapSmoothness());
        config().targetSwap.targetSwapMouseDeadzone.validateAndSet(setup.targetSwapMouseDeadzone());
        config().targetSwap.targetSwapMouseActivation.validateAndSet(setup.targetSwapMouseActivation());
        config().targetSwap.targetSwapDirectionThreshold.validateAndSet(setup.targetSwapDirectionThreshold());
        config().targetSwap.targetSwapMinScreenSeparation.validateAndSet(setup.targetSwapMinScreenSeparation());
        config().targetSwap.targetSwapInputDecay.validateAndSet(setup.targetSwapInputDecay());
        config().targetSwap.targetSwapCooldownTicks.validateAndSet(setup.targetSwapCooldownTicks());
        config().targetSwap.targetSwapSmoothTicks.validateAndSet(setup.targetSwapSmoothTicks());
        config().targetSwap.targetSwapLookYawResponsiveness.validateAndSet(setup.targetSwapLookYawResponsiveness());
        config().targetSwap.targetSwapLookPitchResponsiveness.validateAndSet(setup.targetSwapLookPitchResponsiveness());
        config().targetSwap.targetSwapLookMaxYawStepPerTick.validateAndSet(setup.targetSwapLookMaxYawStepPerTick());
        config().targetSwap.targetSwapLookMaxPitchStepPerTick.validateAndSet(setup.targetSwapLookMaxPitchStepPerTick());
        config().targetSwap.targetSwapTargetPointResponsiveness.validateAndSet(setup.targetSwapTargetPointResponsiveness());
        config().targetSwap.targetSwapPlayerLookFollow.validateAndSet(setup.targetSwapPlayerLookFollow());
        config().targetFilters.enableTargetFilters.validateAndSet(setup.enableTargetFilters());
        config().targetFilters.targetFilterMode.validateAndSet(setup.targetFilterMode());
        config().targetFilters.filterPlayers.validateAndSet(setup.filterPlayers());
        config().targetFilters.filterPassiveMobs.validateAndSet(setup.filterPassiveMobs());
        config().targetFilters.filterNeutralMobs.validateAndSet(setup.filterNeutralMobs());
        config().targetFilters.filterHostileMobs.validateAndSet(setup.filterHostileMobs());
        config().targetFilters.targetFilterEntityIds.validateAndSet(sanitizeTargetFilterEntityIds(setup.targetFilterEntityIds()));
        if (setup.useCustomSwappedShoulderValues()) {
            config().camera.useCustomSwappedShoulderValues.validateAndSet(true);
            setRawPreset(Shoulder.LEFT, setup.leftShoulder());
            setRawPreset(Shoulder.RIGHT, setup.rightShoulder());
        } else {
            config().camera.useCustomSwappedShoulderValues.validateAndSet(false);
            setRawPreset(Shoulder.LEFT, setup.leftShoulder());
            mirrorOppositeShoulderFrom(Shoulder.LEFT);
        }
    }

    public static List<String> cameraProfileNames() {
        config();
        return List.copyOf(CAMERA_SETUP_PROFILES.keySet());
    }

    public static String resolveCameraProfileName(String profileName) {
        config();
        String sanitized = sanitizeCameraProfileName(profileName);
        if (sanitized.isEmpty()) {
            return "";
        }
        String existing = findExistingProfileName(sanitized);
        return existing == null ? "" : existing;
    }

    public static boolean saveCurrentSetupAsProfile(String profileName) {
        config();
        String sanitized = sanitizeCameraProfileName(profileName);
        if (sanitized.isEmpty()) {
            return false;
        }

        String existing = findExistingProfileName(sanitized);
        if (existing != null && !existing.equals(sanitized)) {
            CAMERA_SETUP_PROFILES.remove(existing);
        }
        CAMERA_SETUP_PROFILES.put(sanitized, currentCameraSetupPreset());
        setSelectedCameraProfileName(sanitized);
        saveCameraPresets();
        return true;
    }

    public static boolean loadCameraProfile(String profileName) {
        config();
        String existing = findExistingProfileName(sanitizeCameraProfileName(profileName));
        if (existing == null) {
            return false;
        }

        CameraSetupPreset preset = CAMERA_SETUP_PROFILES.get(existing);
        if (preset == null) {
            return false;
        }

        applyCameraSetupPreset(preset);
        setSelectedCameraProfileName(existing);
        return true;
    }

    public static boolean deleteCameraProfile(String profileName) {
        config();
        String existing = findExistingProfileName(sanitizeCameraProfileName(profileName));
        if (existing == null) {
            return false;
        }

        CAMERA_SETUP_PROFILES.remove(existing);
        if (existing.equalsIgnoreCase(selectedCameraProfileName())) {
            String nextName = CAMERA_SETUP_PROFILES.keySet().stream().findFirst().orElse("");
            setSelectedCameraProfileName(nextName);
        }
        saveCameraPresets();
        return true;
    }

    public static String serializePreset(PerspectivePreset preset) {
        return presetToJson(sanitizePreset(preset)).toString();
    }

    public static String serializeCameraSetup(CameraSetupPreset setup) {
        return cameraSetupToJson(setup).toString();
    }

    public static PerspectivePreset deserializePreset(String serialized) {
        JsonElement element;
        try {
            element = JsonParser.parseString(serialized);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid preset JSON.", e);
        }

        if (!element.isJsonObject()) {
            throw new IllegalArgumentException("Preset must be a JSON object.");
        }

        return readPreset(element.getAsJsonObject());
    }

    public static CameraSetupPreset deserializeCameraSetup(String serialized) {
        JsonElement element;
        try {
            element = JsonParser.parseString(serialized);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid camera setup JSON.", e);
        }

        if (!element.isJsonObject()) {
            throw new IllegalArgumentException("Camera setup must be a JSON object.");
        }

        return readCameraSetup(element.getAsJsonObject());
    }

    public static void saveConfig() {
        config().save();
        saveCameraPresets();
    }

    private static void cycleSelectedCameraProfile() {
        config();
        List<String> profiles = cameraProfileNames();
        if (profiles.isEmpty()) {
            setSelectedCameraProfileName("");
            saveConfig();
            return;
        }

        String current = resolveCameraProfileName(selectedCameraProfileName());
        int currentIndex = profiles.indexOf(current);
        int nextIndex = currentIndex < 0 ? 0 : (currentIndex + 1) % profiles.size();
        setSelectedCameraProfileName(profiles.get(nextIndex));
        saveConfig();
    }

    private static void saveSelectedCameraProfileFromConfigScreen() {
        if (saveCurrentSetupAsProfile(selectedCameraProfileName())) {
            saveConfig();
        }
    }

    private static void loadSelectedCameraProfileFromConfigScreen() {
        if (loadCameraProfile(selectedCameraProfileName())) {
            saveConfig();
        }
    }

    private static void deleteSelectedCameraProfileFromConfigScreen() {
        if (deleteCameraProfile(selectedCameraProfileName())) {
            saveConfig();
        }
    }

    public static String sanitizeCameraProfileName(String profileName) {
        if (profileName == null) {
            return "";
        }

        String sanitized = profileName.trim().replaceAll("\\s+", " ");
        if (sanitized.length() > MAX_CAMERA_PROFILE_NAME_LENGTH) {
            sanitized = sanitized.substring(0, MAX_CAMERA_PROFILE_NAME_LENGTH);
        }
        return sanitized;
    }

    private static FocusClientConfig config() {
        if (INSTANCE == null) {
            init();
        }
        return INSTANCE;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static List<String> sanitizeTargetFilterEntityIds(List<? extends String> targetFilterEntityIds) {
        if (targetFilterEntityIds == null || targetFilterEntityIds.isEmpty()) {
            return List.of();
        }

        Set<String> sanitizedIds = new LinkedHashSet<>();
        for (String rawId : targetFilterEntityIds) {
            if (rawId == null) {
                continue;
            }
            String trimmed = rawId.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            ResourceLocation parsedId = ResourceLocation.tryParse(trimmed);
            if (parsedId != null) {
                sanitizedIds.add(parsedId.toString());
            }
        }
        return List.copyOf(sanitizedIds);
    }

    private static String findExistingProfileName(String requestedName) {
        if (requestedName == null || requestedName.isEmpty()) {
            return null;
        }

        for (String existingName : CAMERA_SETUP_PROFILES.keySet()) {
            if (existingName.equalsIgnoreCase(requestedName)) {
                return existingName;
            }
        }
        return null;
    }

    private static void syncMirroredShoulderIfNeeded(Shoulder sourceShoulder) {
        if (useCustomSwappedShoulderValues()) {
            return;
        }
        mirrorOppositeShoulderFrom(sourceShoulder);
    }

    private static void mirrorOppositeShoulderFrom(Shoulder sourceShoulder) {
        setRawPreset(sourceShoulder.opposite(), currentPreset(sourceShoulder).mirroredForOppositeShoulder());
    }

    private static void setRawPreset(Shoulder shoulder, PerspectivePreset preset) {
        setPresetFor(shoulder, sanitizePreset(preset));
    }

    private static PerspectivePreset sanitizePreset(PerspectivePreset preset) {
        return new PerspectivePreset(
                clamp(roundForCamera(preset.offsetX()), MIN_CAMERA_OFFSET_X, MAX_CAMERA_OFFSET_X),
                clamp(roundForCamera(preset.offsetY()), MIN_CAMERA_OFFSET_Y, MAX_CAMERA_OFFSET_Y),
                clamp(roundForCamera(preset.offsetZ()), MIN_CAMERA_OFFSET_Z, MAX_CAMERA_OFFSET_Z),
                clamp(roundForCamera(preset.rotation()), MIN_CAMERA_ROTATION, MAX_CAMERA_ROTATION));
    }

    private static void setRawCameraOffsetX(Shoulder shoulder, double value) {
        PerspectivePreset current = currentPreset(shoulder);
        setPresetFor(shoulder, new PerspectivePreset(
                clamp(roundForCamera(value), MIN_CAMERA_OFFSET_X, MAX_CAMERA_OFFSET_X),
                current.offsetY(),
                current.offsetZ(),
                current.rotation()));
    }

    private static void setRawCameraOffsetY(Shoulder shoulder, double value) {
        PerspectivePreset current = currentPreset(shoulder);
        setPresetFor(shoulder, new PerspectivePreset(
                current.offsetX(),
                clamp(roundForCamera(value), MIN_CAMERA_OFFSET_Y, MAX_CAMERA_OFFSET_Y),
                current.offsetZ(),
                current.rotation()));
    }

    private static void setRawCameraOffsetZ(Shoulder shoulder, double value) {
        PerspectivePreset current = currentPreset(shoulder);
        setPresetFor(shoulder, new PerspectivePreset(
                current.offsetX(),
                current.offsetY(),
                clamp(roundForCamera(value), MIN_CAMERA_OFFSET_Z, MAX_CAMERA_OFFSET_Z),
                current.rotation()));
    }

    private static void setRawCameraRotation(Shoulder shoulder, double value) {
        PerspectivePreset current = currentPreset(shoulder);
        setPresetFor(shoulder, new PerspectivePreset(
                current.offsetX(),
                current.offsetY(),
                current.offsetZ(),
                clamp(roundForCamera(value), MIN_CAMERA_ROTATION, MAX_CAMERA_ROTATION)));
    }

    private static PerspectivePreset presetFor(Shoulder shoulder) {
        return shoulder == Shoulder.LEFT ? leftShoulderPreset : rightShoulderPreset;
    }

    private static void setPresetFor(Shoulder shoulder, PerspectivePreset preset) {
        if (shoulder == Shoulder.LEFT) {
            leftShoulderPreset = sanitizePreset(preset);
            return;
        }
        rightShoulderPreset = sanitizePreset(preset);
    }

    private static PerspectivePreset defaultLeftPreset() {
        return new PerspectivePreset(
                DEFAULT_CAMERA_OFFSET_X,
                DEFAULT_CAMERA_OFFSET_Y,
                DEFAULT_CAMERA_OFFSET_Z,
                DEFAULT_CAMERA_ROTATION);
    }

    private static void loadCameraPresets() {
        CAMERA_SETUP_PROFILES.clear();
        if (!Files.isRegularFile(CAMERA_PRESET_PATH)) {
            leftShoulderPreset = defaultLeftPreset();
            rightShoulderPreset = leftShoulderPreset.mirroredForOppositeShoulder();
            return;
        }

        try {
            String serialized = Files.readString(CAMERA_PRESET_PATH, StandardCharsets.UTF_8);
            JsonElement element = JsonParser.parseString(serialized);
            if (!element.isJsonObject()) {
                throw new IllegalArgumentException("Camera preset file must contain a JSON object.");
            }

            JsonObject object = element.getAsJsonObject();
            JsonElement leftElement = object.get("leftShoulder");
            JsonElement rightElement = object.get("rightShoulder");
            if (leftElement == null || !leftElement.isJsonObject() || rightElement == null || !rightElement.isJsonObject()) {
                throw new IllegalArgumentException("Camera preset file must contain leftShoulder and rightShoulder.");
            }

            leftShoulderPreset = readPreset(leftElement.getAsJsonObject());
            rightShoulderPreset = readPreset(rightElement.getAsJsonObject());

            JsonElement profilesElement = object.get("profiles");
            if (profilesElement != null) {
                if (!profilesElement.isJsonArray()) {
                    throw new IllegalArgumentException("Camera preset file profiles field must be an array.");
                }

                for (JsonElement profileElement : profilesElement.getAsJsonArray()) {
                    if (!profileElement.isJsonObject()) {
                        Focus.LOGGER.warn("Skipping invalid camera profile entry; expected JSON object.");
                        continue;
                    }

                    JsonObject profileObject = profileElement.getAsJsonObject();
                    JsonElement nameElement = profileObject.get("name");
                    if (nameElement == null || !nameElement.isJsonPrimitive() || !nameElement.getAsJsonPrimitive().isString()) {
                        Focus.LOGGER.warn("Skipping camera profile entry without a valid name.");
                        continue;
                    }

                    String profileName = sanitizeCameraProfileName(nameElement.getAsString());
                    if (profileName.isEmpty()) {
                        Focus.LOGGER.warn("Skipping camera profile entry with empty name.");
                        continue;
                    }

                    JsonElement setupElement = profileObject.get("setup");
                    JsonObject setupObject;
                    if (setupElement != null) {
                        if (!setupElement.isJsonObject()) {
                            Focus.LOGGER.warn("Skipping camera profile '{}' with non-object setup.", profileName);
                            continue;
                        }
                        setupObject = setupElement.getAsJsonObject();
                    } else {
                        setupObject = profileObject;
                    }

                    try {
                        String existing = findExistingProfileName(profileName);
                        if (existing != null && !existing.equals(profileName)) {
                            CAMERA_SETUP_PROFILES.remove(existing);
                        }
                        CAMERA_SETUP_PROFILES.put(profileName, readCameraSetup(setupObject));
                    } catch (IllegalArgumentException e) {
                        Focus.LOGGER.warn("Skipping invalid camera profile '{}'", profileName, e);
                    }
                }
            }
        } catch (Exception e) {
            Focus.LOGGER.warn("Failed to load camera preset file {}, using defaults", CAMERA_PRESET_PATH, e);
            leftShoulderPreset = defaultLeftPreset();
            rightShoulderPreset = leftShoulderPreset.mirroredForOppositeShoulder();
            CAMERA_SETUP_PROFILES.clear();
        }
    }

    private static void saveCameraPresets() {
        try {
            Files.createDirectories(CAMERA_PRESET_PATH.getParent());
            JsonObject object = new JsonObject();
            object.addProperty("format", "focus_camera_presets_v3");
            object.add("leftShoulder", presetToJson(leftShoulderPreset));
            object.add("rightShoulder", presetToJson(rightShoulderPreset));
            JsonArray profiles = new JsonArray();
            for (Map.Entry<String, CameraSetupPreset> entry : CAMERA_SETUP_PROFILES.entrySet()) {
                JsonObject profileObject = new JsonObject();
                profileObject.addProperty("name", entry.getKey());
                profileObject.add("setup", cameraSetupToJson(entry.getValue()));
                profiles.add(profileObject);
            }
            object.add("profiles", profiles);
            Files.writeString(CAMERA_PRESET_PATH, object.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            Focus.LOGGER.warn("Failed to save camera preset file {}", CAMERA_PRESET_PATH, e);
        }
    }

    private static JsonObject cameraSetupToJson(CameraSetupPreset setup) {
        JsonObject object = new JsonObject();
        object.addProperty("autoSwitchToThirdPerson", setup.autoSwitchToThirdPerson());
        object.addProperty("allowFirstPersonWhileTargeting", setup.allowFirstPersonWhileTargeting());
        object.addProperty("allowFrontFacingThirdPersonWhileTargeting", setup.allowFrontFacingThirdPersonWhileTargeting());
        object.addProperty("showLockOnDebugText", setup.showLockOnDebugText());
        object.addProperty("lockOnIndicatorStyle", setup.lockOnIndicatorStyle().name());
        object.addProperty("cameraMode", setup.cameraMode().name());
        object.addProperty("cameraFloatiness", setup.cameraFloatiness());
        object.addProperty("cameraDrag", setup.cameraDrag());
        object.addProperty("cameraSwapSpeed", setup.cameraSwapSpeed());
        object.addProperty("cameraSwapSmoothness", setup.cameraSwapSmoothness());
        object.addProperty("dynamicCameraSwapSpeed", setup.dynamicCameraSwapSpeed());
        object.addProperty("dynamicCameraSwapSmoothness", setup.dynamicCameraSwapSmoothness());
        object.addProperty("targetSwapMouseDeadzone", setup.targetSwapMouseDeadzone());
        object.addProperty("targetSwapMouseActivation", setup.targetSwapMouseActivation());
        object.addProperty("targetSwapDirectionThreshold", setup.targetSwapDirectionThreshold());
        object.addProperty("targetSwapMinScreenSeparation", setup.targetSwapMinScreenSeparation());
        object.addProperty("targetSwapInputDecay", setup.targetSwapInputDecay());
        object.addProperty("targetSwapCooldownTicks", setup.targetSwapCooldownTicks());
        object.addProperty("targetSwapSmoothTicks", setup.targetSwapSmoothTicks());
        object.addProperty("targetSwapLookYawResponsiveness", setup.targetSwapLookYawResponsiveness());
        object.addProperty("targetSwapLookPitchResponsiveness", setup.targetSwapLookPitchResponsiveness());
        object.addProperty("targetSwapLookMaxYawStepPerTick", setup.targetSwapLookMaxYawStepPerTick());
        object.addProperty("targetSwapLookMaxPitchStepPerTick", setup.targetSwapLookMaxPitchStepPerTick());
        object.addProperty("targetSwapTargetPointResponsiveness", setup.targetSwapTargetPointResponsiveness());
        object.addProperty("targetSwapPlayerLookFollow", setup.targetSwapPlayerLookFollow());
        object.addProperty("enableTargetFilters", setup.enableTargetFilters());
        object.addProperty("targetFilterMode", setup.targetFilterMode().name());
        object.addProperty("filterPlayers", setup.filterPlayers());
        object.addProperty("filterPassiveMobs", setup.filterPassiveMobs());
        object.addProperty("filterNeutralMobs", setup.filterNeutralMobs());
        object.addProperty("filterHostileMobs", setup.filterHostileMobs());
        JsonArray targetFilterEntityIds = new JsonArray();
        for (String id : setup.targetFilterEntityIds()) {
            targetFilterEntityIds.add(id);
        }
        object.add("targetFilterEntityIds", targetFilterEntityIds);
        object.addProperty("useCustomSwappedShoulderValues", setup.useCustomSwappedShoulderValues());
        object.add("leftShoulder", presetToJson(setup.leftShoulder()));
        object.add("rightShoulder", presetToJson(setup.rightShoulder()));
        return object;
    }

    private static CameraSetupPreset readCameraSetup(JsonObject object) {
        JsonElement leftElement = object.get("leftShoulder");
        JsonElement rightElement = object.get("rightShoulder");
        if (leftElement == null || !leftElement.isJsonObject() || rightElement == null || !rightElement.isJsonObject()) {
            throw new IllegalArgumentException("Camera setup must contain leftShoulder and rightShoulder objects.");
        }

        PerspectivePreset left = readPreset(leftElement.getAsJsonObject());
        PerspectivePreset right = readPreset(rightElement.getAsJsonObject());
        boolean autoSwitchToThirdPerson = readRequiredBoolean(object, "autoSwitchToThirdPerson");
        boolean allowFirstPersonWhileTargeting = readRequiredBoolean(object, "allowFirstPersonWhileTargeting");
        boolean allowFrontFacingThirdPersonWhileTargeting = readRequiredBoolean(object, "allowFrontFacingThirdPersonWhileTargeting");
        boolean showLockOnDebugText = readRequiredBoolean(object, "showLockOnDebugText");
        LockOnIndicatorStyle lockOnIndicatorStyle = readRequiredLockOnIndicatorStyle(object, "lockOnIndicatorStyle");
        CameraMode cameraMode = readRequiredCameraMode(object, "cameraMode");
        double cameraFloatiness = readRequiredDouble(object, "cameraFloatiness");
        double cameraDrag = readRequiredDouble(object, "cameraDrag");
        double cameraSwapSpeed = readRequiredDouble(object, "cameraSwapSpeed");
        double cameraSwapSmoothness = readRequiredDouble(object, "cameraSwapSmoothness");
        double dynamicCameraSwapSpeed = readRequiredDouble(object, "dynamicCameraSwapSpeed");
        double dynamicCameraSwapSmoothness = readRequiredDouble(object, "dynamicCameraSwapSmoothness");
        double targetSwapMouseDeadzone = readRequiredDouble(object, "targetSwapMouseDeadzone");
        double targetSwapMouseActivation = readRequiredDouble(object, "targetSwapMouseActivation");
        double targetSwapDirectionThreshold = readRequiredDouble(object, "targetSwapDirectionThreshold");
        double targetSwapMinScreenSeparation = readRequiredDouble(object, "targetSwapMinScreenSeparation");
        double targetSwapInputDecay = readRequiredDouble(object, "targetSwapInputDecay");
        double targetSwapCooldownTicks = readRequiredDouble(object, "targetSwapCooldownTicks");
        double targetSwapSmoothTicks = readRequiredDouble(object, "targetSwapSmoothTicks");
        double targetSwapLookYawResponsiveness = readRequiredDouble(object, "targetSwapLookYawResponsiveness");
        double targetSwapLookPitchResponsiveness = readRequiredDouble(object, "targetSwapLookPitchResponsiveness");
        double targetSwapLookMaxYawStepPerTick = readRequiredDouble(object, "targetSwapLookMaxYawStepPerTick");
        double targetSwapLookMaxPitchStepPerTick = readRequiredDouble(object, "targetSwapLookMaxPitchStepPerTick");
        double targetSwapTargetPointResponsiveness = readRequiredDouble(object, "targetSwapTargetPointResponsiveness");
        double targetSwapPlayerLookFollow = readRequiredDouble(object, "targetSwapPlayerLookFollow");
        boolean enableTargetFilters = readRequiredBoolean(object, "enableTargetFilters");
        TargetFilterMode targetFilterMode = readRequiredTargetFilterMode(object, "targetFilterMode");
        boolean filterPlayers = readRequiredBoolean(object, "filterPlayers");
        boolean filterPassiveMobs = readRequiredBoolean(object, "filterPassiveMobs");
        boolean filterNeutralMobs = readRequiredBoolean(object, "filterNeutralMobs");
        boolean filterHostileMobs = readRequiredBoolean(object, "filterHostileMobs");
        List<String> targetFilterEntityIds = readRequiredStringList(object, "targetFilterEntityIds");
        boolean useCustom = readRequiredBoolean(object, "useCustomSwappedShoulderValues");
        return new CameraSetupPreset(
                autoSwitchToThirdPerson,
                allowFirstPersonWhileTargeting,
                allowFrontFacingThirdPersonWhileTargeting,
                showLockOnDebugText,
                lockOnIndicatorStyle,
                cameraMode,
                cameraFloatiness,
                cameraDrag,
                cameraSwapSpeed,
                cameraSwapSmoothness,
                dynamicCameraSwapSpeed,
                dynamicCameraSwapSmoothness,
                targetSwapMouseDeadzone,
                targetSwapMouseActivation,
                targetSwapDirectionThreshold,
                targetSwapMinScreenSeparation,
                targetSwapInputDecay,
                targetSwapCooldownTicks,
                targetSwapSmoothTicks,
                targetSwapLookYawResponsiveness,
                targetSwapLookPitchResponsiveness,
                targetSwapLookMaxYawStepPerTick,
                targetSwapLookMaxPitchStepPerTick,
                targetSwapTargetPointResponsiveness,
                targetSwapPlayerLookFollow,
                enableTargetFilters,
                targetFilterMode,
                filterPlayers,
                filterPassiveMobs,
                filterNeutralMobs,
                filterHostileMobs,
                targetFilterEntityIds,
                left,
                right,
                useCustom);
    }

    private static JsonObject presetToJson(PerspectivePreset preset) {
        JsonObject object = new JsonObject();
        object.addProperty("offsetX", roundForCamera(preset.offsetX()));
        object.addProperty("offsetY", roundForCamera(preset.offsetY()));
        object.addProperty("offsetZ", roundForCamera(preset.offsetZ()));
        object.addProperty("rotation", roundForCamera(preset.rotation()));
        return object;
    }

    private static PerspectivePreset readPreset(JsonObject object) {
        return sanitizePreset(new PerspectivePreset(
                readRequiredDouble(object, "offsetX"),
                readRequiredDouble(object, "offsetY"),
                readRequiredDouble(object, "offsetZ"),
                readRequiredDouble(object, "rotation")));
    }

    private static double readRequiredDouble(JsonObject object, String key) {
        JsonElement element = object.get(key);
        if (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
            throw new IllegalArgumentException("Preset is missing numeric field: " + key);
        }
        return element.getAsDouble();
    }

    private static boolean readRequiredBoolean(JsonObject object, String key) {
        JsonElement element = object.get(key);
        if (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isBoolean()) {
            throw new IllegalArgumentException("Preset is missing boolean field: " + key);
        }
        return element.getAsBoolean();
    }

    private static List<String> readRequiredStringList(JsonObject object, String key) {
        JsonElement element = object.get(key);
        if (element == null || !element.isJsonArray()) {
            throw new IllegalArgumentException("Preset is missing array field: " + key);
        }

        List<String> values = new ArrayList<>();
        for (JsonElement listElement : element.getAsJsonArray()) {
            if (!listElement.isJsonPrimitive() || !listElement.getAsJsonPrimitive().isString()) {
                throw new IllegalArgumentException("Preset has non-string list entry: " + key);
            }
            values.add(listElement.getAsString());
        }
        return sanitizeTargetFilterEntityIds(values);
    }

    private static double readOptionalDouble(JsonObject object, String key, double fallback) {
        JsonElement element = object.get(key);
        if (element == null) {
            return fallback;
        }
        if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
            throw new IllegalArgumentException("Preset has non-numeric field: " + key);
        }
        return element.getAsDouble();
    }

    private static boolean readOptionalBoolean(JsonObject object, String key, boolean fallback) {
        JsonElement element = object.get(key);
        if (element == null) {
            return fallback;
        }
        if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isBoolean()) {
            throw new IllegalArgumentException("Preset has non-boolean field: " + key);
        }
        return element.getAsBoolean();
    }

    private static CameraMode readOptionalCameraMode(JsonObject object, String key, CameraMode fallback) {
        JsonElement element = object.get(key);
        if (element == null) {
            return fallback;
        }
        if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
            throw new IllegalArgumentException("Preset has non-string enum field: " + key);
        }
        try {
            return CameraMode.valueOf(element.getAsString());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Preset has invalid camera mode: " + element.getAsString(), e);
        }
    }

    private static TargetFilterMode readOptionalTargetFilterMode(JsonObject object, String key, TargetFilterMode fallback) {
        JsonElement element = object.get(key);
        if (element == null) {
            return fallback;
        }
        if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
            throw new IllegalArgumentException("Preset has non-string enum field: " + key);
        }
        try {
            return TargetFilterMode.valueOf(element.getAsString());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Preset has invalid target filter mode: " + element.getAsString(), e);
        }
    }

    private static List<String> readOptionalStringList(JsonObject object, String key, List<String> fallback) {
        JsonElement element = object.get(key);
        if (element == null) {
            return fallback;
        }
        if (!element.isJsonArray()) {
            throw new IllegalArgumentException("Preset has non-array field: " + key);
        }
        List<String> values = new ArrayList<>();
        for (JsonElement listElement : element.getAsJsonArray()) {
            if (!listElement.isJsonPrimitive() || !listElement.getAsJsonPrimitive().isString()) {
                throw new IllegalArgumentException("Preset has non-string list entry: " + key);
            }
            values.add(listElement.getAsString());
        }
        return sanitizeTargetFilterEntityIds(values);
    }

    private static CameraMode readRequiredCameraMode(JsonObject object, String key) {
        JsonElement element = object.get(key);
        if (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
            throw new IllegalArgumentException("Preset is missing string enum field: " + key);
        }
        try {
            return CameraMode.valueOf(element.getAsString());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Preset has invalid camera mode: " + element.getAsString(), e);
        }
    }

    private static LockOnIndicatorStyle readRequiredLockOnIndicatorStyle(JsonObject object, String key) {
        JsonElement element = object.get(key);
        if (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
            throw new IllegalArgumentException("Preset is missing string enum field: " + key);
        }
        try {
            return LockOnIndicatorStyle.valueOf(element.getAsString());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Preset has invalid lock-on indicator style: " + element.getAsString(), e);
        }
    }

    private static TargetFilterMode readRequiredTargetFilterMode(JsonObject object, String key) {
        JsonElement element = object.get(key);
        if (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
            throw new IllegalArgumentException("Preset is missing string enum field: " + key);
        }
        try {
            return TargetFilterMode.valueOf(element.getAsString());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Preset has invalid target filter mode: " + element.getAsString(), e);
        }
    }

    private static double roundForCamera(double value) {
        return BigDecimal.valueOf(value).setScale(CAMERA_VALUE_SCALE, RoundingMode.HALF_UP).doubleValue();
    }

    public record PerspectivePreset(double offsetX, double offsetY, double offsetZ, double rotation) {
        public PerspectivePreset mirroredForOppositeShoulder() {
            return new PerspectivePreset(-offsetX, offsetY, offsetZ, -rotation);
        }
    }

    public record CameraSetupPreset(
            boolean autoSwitchToThirdPerson,
            boolean allowFirstPersonWhileTargeting,
            boolean allowFrontFacingThirdPersonWhileTargeting,
            boolean showLockOnDebugText,
            LockOnIndicatorStyle lockOnIndicatorStyle,
            CameraMode cameraMode,
            double cameraFloatiness,
            double cameraDrag,
            double cameraSwapSpeed,
            double cameraSwapSmoothness,
            double dynamicCameraSwapSpeed,
            double dynamicCameraSwapSmoothness,
            double targetSwapMouseDeadzone,
            double targetSwapMouseActivation,
            double targetSwapDirectionThreshold,
            double targetSwapMinScreenSeparation,
            double targetSwapInputDecay,
            double targetSwapCooldownTicks,
            double targetSwapSmoothTicks,
            double targetSwapLookYawResponsiveness,
            double targetSwapLookPitchResponsiveness,
            double targetSwapLookMaxYawStepPerTick,
            double targetSwapLookMaxPitchStepPerTick,
            double targetSwapTargetPointResponsiveness,
            double targetSwapPlayerLookFollow,
            boolean enableTargetFilters,
            TargetFilterMode targetFilterMode,
            boolean filterPlayers,
            boolean filterPassiveMobs,
            boolean filterNeutralMobs,
            boolean filterHostileMobs,
            List<String> targetFilterEntityIds,
            PerspectivePreset leftShoulder,
            PerspectivePreset rightShoulder,
            boolean useCustomSwappedShoulderValues) {
    }
}
