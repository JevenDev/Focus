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
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

import eu.midnightdust.lib.config.MidnightConfig;
import eu.midnightdust.lib.config.EntryInfo;
import eu.midnightdust.lib.config.MidnightConfigListWidget;
import eu.midnightdust.lib.config.MidnightConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.neoforged.fml.loading.FMLPaths;

public final class FocusClientConfig extends MidnightConfig {
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
    private static final String GENERAL_CATEGORY = "general";
    private static final String CAMERA_CATEGORY = "camera";
    private static final String TARGET_SWAP_CATEGORY = "target_swap";
    private static final String TARGET_FILTER_CATEGORY = "target_filters";
    private static final int CAMERA_VALUE_SCALE = 1;
    private static final Path CAMERA_PRESET_PATH = FMLPaths.CONFIGDIR.get().resolve(Focus.MOD_ID + "_lock_on_camera.json");

    @Entry(category = GENERAL_CATEGORY, name = "focus.lock_on_client.autoSwitchToThirdPerson")
    public static boolean autoSwitchToThirdPerson = true;
    @Entry(category = GENERAL_CATEGORY, name = "focus.lock_on_client.allowFirstPersonWhileTargeting")
    public static boolean allowFirstPersonWhileTargeting = true;
    @Condition(requiredOption = "allowFirstPersonWhileTargeting", visibleButLocked = true)
    @Entry(category = GENERAL_CATEGORY, name = "focus.lock_on_client.allowFrontFacingThirdPersonWhileTargeting")
    public static boolean allowFrontFacingThirdPersonWhileTargeting = false;
    @Entry(category = GENERAL_CATEGORY, name = "focus.lock_on_client.showLockOnDebugText")
    public static boolean showLockOnDebugText = false;
    @Entry(category = GENERAL_CATEGORY, name = "focus.lock_on_client.lockOnIndicatorStyle")
    public static LockOnIndicatorStyle lockOnIndicatorStyle = LockOnIndicatorStyle.OOT_16X;

    @Entry(category = CAMERA_CATEGORY, name = "focus.lock_on_client.useCustomSwappedShoulderValues")
    public static boolean useCustomSwappedShoulderValues = false;
    @Entry(category = CAMERA_CATEGORY, name = "focus.lock_on_client.cameraMode")
    public static CameraMode cameraMode = CameraMode.DYNAMIC;
    @Entry(category = CAMERA_CATEGORY, name = "focus.lock_on_client.cameraFloatiness", min = MIN_CAMERA_FLOATINESS, max = MAX_CAMERA_FLOATINESS, isSlider = true, precision = 100)
    public static double cameraFloatiness = DEFAULT_CAMERA_FLOATINESS;
    @Entry(category = CAMERA_CATEGORY, name = "focus.lock_on_client.cameraDrag", min = MIN_CAMERA_DRAG, max = MAX_CAMERA_DRAG, isSlider = true, precision = 100)
    public static double cameraDrag = DEFAULT_CAMERA_DRAG;
    @Entry(category = CAMERA_CATEGORY, name = "focus.lock_on_client.cameraSwapSpeed", min = MIN_CAMERA_SWAP_SPEED, max = MAX_CAMERA_SWAP_SPEED, isSlider = true, precision = 100)
    public static double cameraSwapSpeed = DEFAULT_CAMERA_SWAP_SPEED;
    @Entry(category = CAMERA_CATEGORY, name = "focus.lock_on_client.cameraSwapSmoothness", min = MIN_CAMERA_SWAP_SMOOTHNESS, max = MAX_CAMERA_SWAP_SMOOTHNESS, isSlider = true, precision = 100)
    public static double cameraSwapSmoothness = DEFAULT_CAMERA_SWAP_SMOOTHNESS;
    @Entry(category = CAMERA_CATEGORY, name = "focus.lock_on_client.dynamicCameraSwapSpeed", min = MIN_DYNAMIC_CAMERA_SWAP_SPEED, max = MAX_DYNAMIC_CAMERA_SWAP_SPEED, isSlider = true, precision = 100)
    public static double dynamicCameraSwapSpeed = DEFAULT_DYNAMIC_CAMERA_SWAP_SPEED;
    @Entry(category = CAMERA_CATEGORY, name = "focus.lock_on_client.dynamicCameraSwapSmoothness", min = MIN_DYNAMIC_CAMERA_SWAP_SMOOTHNESS, max = MAX_DYNAMIC_CAMERA_SWAP_SMOOTHNESS, isSlider = true, precision = 100)
    public static double dynamicCameraSwapSmoothness = DEFAULT_DYNAMIC_CAMERA_SWAP_SMOOTHNESS;
    @Entry(category = CAMERA_CATEGORY, name = "focus.lock_on_client.selectedCameraProfile", width = MAX_CAMERA_PROFILE_NAME_LENGTH)
    public static String selectedCameraProfile = "";

    @Entry(category = TARGET_SWAP_CATEGORY, name = "focus.lock_on_client.targetSwapMouseDeadzone", min = MIN_TARGET_SWAP_MOUSE_DEADZONE, max = MAX_TARGET_SWAP_MOUSE_DEADZONE, isSlider = true, precision = 100)
    public static double targetSwapMouseDeadzone = DEFAULT_TARGET_SWAP_MOUSE_DEADZONE;
    @Entry(category = TARGET_SWAP_CATEGORY, name = "focus.lock_on_client.targetSwapMouseActivation", min = MIN_TARGET_SWAP_MOUSE_ACTIVATION, max = MAX_TARGET_SWAP_MOUSE_ACTIVATION, isSlider = true, precision = 100)
    public static double targetSwapMouseActivation = DEFAULT_TARGET_SWAP_MOUSE_ACTIVATION;
    @Entry(category = TARGET_SWAP_CATEGORY, name = "focus.lock_on_client.targetSwapDirectionThreshold", min = MIN_TARGET_SWAP_DIRECTION_THRESHOLD, max = MAX_TARGET_SWAP_DIRECTION_THRESHOLD, isSlider = true, precision = 100)
    public static double targetSwapDirectionThreshold = DEFAULT_TARGET_SWAP_DIRECTION_THRESHOLD;
    @Entry(category = TARGET_SWAP_CATEGORY, name = "focus.lock_on_client.targetSwapMinScreenSeparation", min = MIN_TARGET_SWAP_MIN_SCREEN_SEPARATION, max = MAX_TARGET_SWAP_MIN_SCREEN_SEPARATION, isSlider = true, precision = 100)
    public static double targetSwapMinScreenSeparation = DEFAULT_TARGET_SWAP_MIN_SCREEN_SEPARATION;
    @Entry(category = TARGET_SWAP_CATEGORY, name = "focus.lock_on_client.targetSwapInputDecay", min = MIN_TARGET_SWAP_INPUT_DECAY, max = MAX_TARGET_SWAP_INPUT_DECAY, isSlider = true, precision = 100)
    public static double targetSwapInputDecay = DEFAULT_TARGET_SWAP_INPUT_DECAY;
    @Entry(category = TARGET_SWAP_CATEGORY, name = "focus.lock_on_client.targetSwapCooldownTicks", min = MIN_TARGET_SWAP_COOLDOWN_TICKS, max = MAX_TARGET_SWAP_COOLDOWN_TICKS, isSlider = true, precision = 10)
    public static double targetSwapCooldownTicks = DEFAULT_TARGET_SWAP_COOLDOWN_TICKS;
    @Entry(category = TARGET_SWAP_CATEGORY, name = "focus.lock_on_client.targetSwapSmoothTicks", min = MIN_TARGET_SWAP_SMOOTH_TICKS, max = MAX_TARGET_SWAP_SMOOTH_TICKS, isSlider = true, precision = 10)
    public static double targetSwapSmoothTicks = DEFAULT_TARGET_SWAP_SMOOTH_TICKS;
    @Entry(category = TARGET_SWAP_CATEGORY, name = "focus.lock_on_client.targetSwapLookYawResponsiveness", min = MIN_TARGET_SWAP_LOOK_RESPONSIVENESS_YAW, max = MAX_TARGET_SWAP_LOOK_RESPONSIVENESS_YAW, isSlider = true, precision = 100)
    public static double targetSwapLookYawResponsiveness = DEFAULT_TARGET_SWAP_LOOK_RESPONSIVENESS_YAW;
    @Entry(category = TARGET_SWAP_CATEGORY, name = "focus.lock_on_client.targetSwapLookPitchResponsiveness", min = MIN_TARGET_SWAP_LOOK_RESPONSIVENESS_PITCH, max = MAX_TARGET_SWAP_LOOK_RESPONSIVENESS_PITCH, isSlider = true, precision = 100)
    public static double targetSwapLookPitchResponsiveness = DEFAULT_TARGET_SWAP_LOOK_RESPONSIVENESS_PITCH;
    @Entry(category = TARGET_SWAP_CATEGORY, name = "focus.lock_on_client.targetSwapLookMaxYawStepPerTick", min = MIN_TARGET_SWAP_LOOK_MAX_YAW_STEP_PER_TICK, max = MAX_TARGET_SWAP_LOOK_MAX_YAW_STEP_PER_TICK, isSlider = true, precision = 100)
    public static double targetSwapLookMaxYawStepPerTick = DEFAULT_TARGET_SWAP_LOOK_MAX_YAW_STEP_PER_TICK;
    @Entry(category = TARGET_SWAP_CATEGORY, name = "focus.lock_on_client.targetSwapLookMaxPitchStepPerTick", min = MIN_TARGET_SWAP_LOOK_MAX_PITCH_STEP_PER_TICK, max = MAX_TARGET_SWAP_LOOK_MAX_PITCH_STEP_PER_TICK, isSlider = true, precision = 100)
    public static double targetSwapLookMaxPitchStepPerTick = DEFAULT_TARGET_SWAP_LOOK_MAX_PITCH_STEP_PER_TICK;
    @Entry(category = TARGET_SWAP_CATEGORY, name = "focus.lock_on_client.targetSwapTargetPointResponsiveness", min = MIN_TARGET_SWAP_TARGET_POINT_RESPONSIVENESS, max = MAX_TARGET_SWAP_TARGET_POINT_RESPONSIVENESS, isSlider = true, precision = 100)
    public static double targetSwapTargetPointResponsiveness = DEFAULT_TARGET_SWAP_TARGET_POINT_RESPONSIVENESS;
    @Entry(category = TARGET_SWAP_CATEGORY, name = "focus.lock_on_client.targetSwapPlayerLookFollow", min = MIN_TARGET_SWAP_PLAYER_LOOK_FOLLOW, max = MAX_TARGET_SWAP_PLAYER_LOOK_FOLLOW, isSlider = true, precision = 100)
    public static double targetSwapPlayerLookFollow = DEFAULT_TARGET_SWAP_PLAYER_LOOK_FOLLOW;

    @Entry(category = TARGET_FILTER_CATEGORY, name = "focus.lock_on_client.enableTargetFilters")
    public static boolean enableTargetFilters = DEFAULT_TARGET_FILTERS_ENABLED;
    @Entry(category = TARGET_FILTER_CATEGORY, name = "focus.lock_on_client.targetFilterMode")
    public static TargetFilterMode targetFilterMode = TargetFilterMode.EXCLUDE;
    @Entry(category = TARGET_FILTER_CATEGORY, name = "focus.lock_on_client.filterPlayers")
    public static boolean filterPlayers = DEFAULT_FILTER_PLAYERS;
    @Entry(category = TARGET_FILTER_CATEGORY, name = "focus.lock_on_client.filterPassiveMobs")
    public static boolean filterPassiveMobs = DEFAULT_FILTER_PASSIVE_MOBS;
    @Entry(category = TARGET_FILTER_CATEGORY, name = "focus.lock_on_client.filterNeutralMobs")
    public static boolean filterNeutralMobs = DEFAULT_FILTER_NEUTRAL_MOBS;
    @Entry(category = TARGET_FILTER_CATEGORY, name = "focus.lock_on_client.filterHostileMobs")
    public static boolean filterHostileMobs = DEFAULT_FILTER_HOSTILE_MOBS;
    @Entry(category = TARGET_FILTER_CATEGORY, name = "focus.lock_on_client.targetFilterEntityIds")
    public static List<String> targetFilterEntityIds = new ArrayList<>(DEFAULT_TARGET_FILTER_ENTITY_IDS);

    private static final RuntimeConfig INSTANCE = new RuntimeConfig();
    private static boolean initialized;
    private static PerspectivePreset leftShoulderPreset = defaultLeftPreset();
    private static PerspectivePreset rightShoulderPreset = defaultLeftPreset().mirroredForOppositeShoulder();
    private static final Map<String, CameraSetupPreset> CAMERA_SETUP_PROFILES = new LinkedHashMap<>();
    private static final Map<String, CameraSetupPreset> BUILT_IN_CAMERA_SETUP_PROFILES = createBuiltInCameraSetupProfiles();

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

    public enum CameraMode implements StringRepresentable {
        STATIC("focus.lock_on_client.camera_mode.STATIC"),
        DYNAMIC("focus.lock_on_client.camera_mode.DYNAMIC");

        private final String translationKey;

        CameraMode(String translationKey) {
            this.translationKey = translationKey;
        }

        @Override
        public String getSerializedName() {
            return translationKey;
        }
    }

    public enum TargetFilterMode implements StringRepresentable {
        EXCLUDE("focus.lock_on_client.target_filter_mode.EXCLUDE"),
        EXCLUSIVE("focus.lock_on_client.target_filter_mode.EXCLUSIVE");

        private final String translationKey;

        TargetFilterMode(String translationKey) {
            this.translationKey = translationKey;
        }

        @Override
        public String getSerializedName() {
            return translationKey;
        }
    }

    public enum LockOnIndicatorStyle implements StringRepresentable {
        OOT_16X("focus.lock_on_client.lock_on_indicator_style.OOT_16X", "textures/ui/hud/lock_on_indicators/oot_lock_on_16x.png", 8, IndicatorType.OOT_TRIANGLES),
        OOT_32X("focus.lock_on_client.lock_on_indicator_style.OOT_32X", "textures/ui/hud/lock_on_indicators/oot_lock_on_32x.png", 8, IndicatorType.OOT_TRIANGLES),
        DS2_16X("focus.lock_on_client.lock_on_indicator_style.DS2_16X", "textures/ui/hud/lock_on_indicators/ds2_lock_on_16x.png", 16, IndicatorType.STATIC_CENTERED),
        DS2_32X("focus.lock_on_client.lock_on_indicator_style.DS2_32X", "textures/ui/hud/lock_on_indicators/ds2_lock_on_32x.png", 8, IndicatorType.STATIC_CENTERED);

        private final String translationKey;
        private final ResourceLocation texture;
        private final int drawSize;
        private final IndicatorType indicatorType;

        LockOnIndicatorStyle(String translationKey, String texturePath, int drawSize, IndicatorType indicatorType) {
            this.translationKey = translationKey;
            this.texture = ResourceLocation.fromNamespaceAndPath(Focus.MOD_ID, texturePath);
            this.drawSize = drawSize;
            this.indicatorType = indicatorType;
        }

        @Override
        public String getSerializedName() {
            return translationKey;
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

    private static final class RuntimeConfig {
        private final GeneralSection general = new GeneralSection();
        private final CameraSection camera = new CameraSection();
        private final TargetSwapSection targetSwap = new TargetSwapSection();
        private final TargetFilterSection targetFilters = new TargetFilterSection();

        private void save() {
            MidnightConfig.write(Focus.MOD_ID);
        }
    }

    public static class GeneralSection {
        public final BooleanValue autoSwitchToThirdPerson = new BooleanValue(
                () -> FocusClientConfig.autoSwitchToThirdPerson,
                value -> FocusClientConfig.autoSwitchToThirdPerson = value);
        public final BooleanValue allowFirstPersonWhileTargeting = new BooleanValue(
                () -> FocusClientConfig.allowFirstPersonWhileTargeting,
                value -> FocusClientConfig.allowFirstPersonWhileTargeting = value);
        public final BooleanValue allowFrontFacingThirdPersonWhileTargeting = new BooleanValue(
                () -> FocusClientConfig.allowFirstPersonWhileTargeting && FocusClientConfig.allowFrontFacingThirdPersonWhileTargeting,
                value -> FocusClientConfig.allowFrontFacingThirdPersonWhileTargeting = value);
        public final BooleanValue showLockOnDebugText = new BooleanValue(
                () -> FocusClientConfig.showLockOnDebugText,
                value -> FocusClientConfig.showLockOnDebugText = value);
        public final EnumValue<LockOnIndicatorStyle> lockOnIndicatorStyle = new EnumValue<>(
                () -> FocusClientConfig.lockOnIndicatorStyle,
                value -> FocusClientConfig.lockOnIndicatorStyle = value);
    }

    public static class CameraSection {
        public final BooleanValue useCustomSwappedShoulderValues = new BooleanValue(
                () -> FocusClientConfig.useCustomSwappedShoulderValues,
                value -> FocusClientConfig.useCustomSwappedShoulderValues = value);
        public final EnumValue<CameraMode> cameraMode = new EnumValue<>(
                () -> FocusClientConfig.cameraMode,
                value -> FocusClientConfig.cameraMode = value);
        public final DoubleValue cameraFloatiness = new DoubleValue(
                () -> FocusClientConfig.cameraFloatiness,
                value -> FocusClientConfig.cameraFloatiness = value,
                MIN_CAMERA_FLOATINESS,
                MAX_CAMERA_FLOATINESS);
        public final DoubleValue cameraDrag = new DoubleValue(
                () -> FocusClientConfig.cameraDrag,
                value -> FocusClientConfig.cameraDrag = value,
                MIN_CAMERA_DRAG,
                MAX_CAMERA_DRAG);
        public final DoubleValue cameraSwapSpeed = new DoubleValue(
                () -> FocusClientConfig.cameraSwapSpeed,
                value -> FocusClientConfig.cameraSwapSpeed = value,
                MIN_CAMERA_SWAP_SPEED,
                MAX_CAMERA_SWAP_SPEED);
        public final DoubleValue cameraSwapSmoothness = new DoubleValue(
                () -> FocusClientConfig.cameraSwapSmoothness,
                value -> FocusClientConfig.cameraSwapSmoothness = value,
                MIN_CAMERA_SWAP_SMOOTHNESS,
                MAX_CAMERA_SWAP_SMOOTHNESS);
        public final DoubleValue dynamicCameraSwapSpeed = new DoubleValue(
                () -> FocusClientConfig.dynamicCameraSwapSpeed,
                value -> FocusClientConfig.dynamicCameraSwapSpeed = value,
                MIN_DYNAMIC_CAMERA_SWAP_SPEED,
                MAX_DYNAMIC_CAMERA_SWAP_SPEED);
        public final DoubleValue dynamicCameraSwapSmoothness = new DoubleValue(
                () -> FocusClientConfig.dynamicCameraSwapSmoothness,
                value -> FocusClientConfig.dynamicCameraSwapSmoothness = value,
                MIN_DYNAMIC_CAMERA_SWAP_SMOOTHNESS,
                MAX_DYNAMIC_CAMERA_SWAP_SMOOTHNESS);
        public final CameraPresetToolsSection presetTools = new CameraPresetToolsSection();
    }

    public static class CameraPresetToolsSection {
        public final StringValue selectedCameraProfile = new StringValue(
                () -> FocusClientConfig.selectedCameraProfile,
                value -> FocusClientConfig.selectedCameraProfile = value);
    }

    public static class TargetSwapSection {
        public final DoubleValue targetSwapMouseDeadzone = new DoubleValue(
                () -> FocusClientConfig.targetSwapMouseDeadzone,
                value -> FocusClientConfig.targetSwapMouseDeadzone = value,
                MIN_TARGET_SWAP_MOUSE_DEADZONE,
                MAX_TARGET_SWAP_MOUSE_DEADZONE);
        public final DoubleValue targetSwapMouseActivation = new DoubleValue(
                () -> FocusClientConfig.targetSwapMouseActivation,
                value -> FocusClientConfig.targetSwapMouseActivation = value,
                MIN_TARGET_SWAP_MOUSE_ACTIVATION,
                MAX_TARGET_SWAP_MOUSE_ACTIVATION);
        public final DoubleValue targetSwapDirectionThreshold = new DoubleValue(
                () -> FocusClientConfig.targetSwapDirectionThreshold,
                value -> FocusClientConfig.targetSwapDirectionThreshold = value,
                MIN_TARGET_SWAP_DIRECTION_THRESHOLD,
                MAX_TARGET_SWAP_DIRECTION_THRESHOLD);
        public final DoubleValue targetSwapMinScreenSeparation = new DoubleValue(
                () -> FocusClientConfig.targetSwapMinScreenSeparation,
                value -> FocusClientConfig.targetSwapMinScreenSeparation = value,
                MIN_TARGET_SWAP_MIN_SCREEN_SEPARATION,
                MAX_TARGET_SWAP_MIN_SCREEN_SEPARATION);
        public final DoubleValue targetSwapInputDecay = new DoubleValue(
                () -> FocusClientConfig.targetSwapInputDecay,
                value -> FocusClientConfig.targetSwapInputDecay = value,
                MIN_TARGET_SWAP_INPUT_DECAY,
                MAX_TARGET_SWAP_INPUT_DECAY);
        public final DoubleValue targetSwapCooldownTicks = new DoubleValue(
                () -> FocusClientConfig.targetSwapCooldownTicks,
                value -> FocusClientConfig.targetSwapCooldownTicks = value,
                MIN_TARGET_SWAP_COOLDOWN_TICKS,
                MAX_TARGET_SWAP_COOLDOWN_TICKS);
        public final DoubleValue targetSwapSmoothTicks = new DoubleValue(
                () -> FocusClientConfig.targetSwapSmoothTicks,
                value -> FocusClientConfig.targetSwapSmoothTicks = value,
                MIN_TARGET_SWAP_SMOOTH_TICKS,
                MAX_TARGET_SWAP_SMOOTH_TICKS);
        public final DoubleValue targetSwapLookYawResponsiveness = new DoubleValue(
                () -> FocusClientConfig.targetSwapLookYawResponsiveness,
                value -> FocusClientConfig.targetSwapLookYawResponsiveness = value,
                MIN_TARGET_SWAP_LOOK_RESPONSIVENESS_YAW,
                MAX_TARGET_SWAP_LOOK_RESPONSIVENESS_YAW);
        public final DoubleValue targetSwapLookPitchResponsiveness = new DoubleValue(
                () -> FocusClientConfig.targetSwapLookPitchResponsiveness,
                value -> FocusClientConfig.targetSwapLookPitchResponsiveness = value,
                MIN_TARGET_SWAP_LOOK_RESPONSIVENESS_PITCH,
                MAX_TARGET_SWAP_LOOK_RESPONSIVENESS_PITCH);
        public final DoubleValue targetSwapLookMaxYawStepPerTick = new DoubleValue(
                () -> FocusClientConfig.targetSwapLookMaxYawStepPerTick,
                value -> FocusClientConfig.targetSwapLookMaxYawStepPerTick = value,
                MIN_TARGET_SWAP_LOOK_MAX_YAW_STEP_PER_TICK,
                MAX_TARGET_SWAP_LOOK_MAX_YAW_STEP_PER_TICK);
        public final DoubleValue targetSwapLookMaxPitchStepPerTick = new DoubleValue(
                () -> FocusClientConfig.targetSwapLookMaxPitchStepPerTick,
                value -> FocusClientConfig.targetSwapLookMaxPitchStepPerTick = value,
                MIN_TARGET_SWAP_LOOK_MAX_PITCH_STEP_PER_TICK,
                MAX_TARGET_SWAP_LOOK_MAX_PITCH_STEP_PER_TICK);
        public final DoubleValue targetSwapTargetPointResponsiveness = new DoubleValue(
                () -> FocusClientConfig.targetSwapTargetPointResponsiveness,
                value -> FocusClientConfig.targetSwapTargetPointResponsiveness = value,
                MIN_TARGET_SWAP_TARGET_POINT_RESPONSIVENESS,
                MAX_TARGET_SWAP_TARGET_POINT_RESPONSIVENESS);
        public final DoubleValue targetSwapPlayerLookFollow = new DoubleValue(
                () -> FocusClientConfig.targetSwapPlayerLookFollow,
                value -> FocusClientConfig.targetSwapPlayerLookFollow = value,
                MIN_TARGET_SWAP_PLAYER_LOOK_FOLLOW,
                MAX_TARGET_SWAP_PLAYER_LOOK_FOLLOW);
    }

    public static class TargetFilterSection {
        public final BooleanValue enableTargetFilters = new BooleanValue(
                () -> FocusClientConfig.enableTargetFilters,
                value -> FocusClientConfig.enableTargetFilters = value);
        public final EnumValue<TargetFilterMode> targetFilterMode = new EnumValue<>(
                () -> FocusClientConfig.targetFilterMode,
                value -> FocusClientConfig.targetFilterMode = value);
        public final BooleanValue filterPlayers = new BooleanValue(
                () -> FocusClientConfig.filterPlayers,
                value -> FocusClientConfig.filterPlayers = value);
        public final BooleanValue filterPassiveMobs = new BooleanValue(
                () -> FocusClientConfig.filterPassiveMobs,
                value -> FocusClientConfig.filterPassiveMobs = value);
        public final BooleanValue filterNeutralMobs = new BooleanValue(
                () -> FocusClientConfig.filterNeutralMobs,
                value -> FocusClientConfig.filterNeutralMobs = value);
        public final BooleanValue filterHostileMobs = new BooleanValue(
                () -> FocusClientConfig.filterHostileMobs,
                value -> FocusClientConfig.filterHostileMobs = value);
        public final StringListValue targetFilterEntityIds = new StringListValue(
                () -> FocusClientConfig.targetFilterEntityIds,
                value -> FocusClientConfig.targetFilterEntityIds = value);
    }

    private static final class BooleanValue {
        private final BooleanSupplier getter;
        private final Consumer<Boolean> setter;

        private BooleanValue(BooleanSupplier getter, Consumer<Boolean> setter) {
            this.getter = getter;
            this.setter = setter;
        }

        private Boolean get() {
            return getter.getAsBoolean();
        }

        private void validateAndSet(boolean value) {
            setter.accept(value);
        }
    }

    private static final class DoubleValue {
        private final DoubleSupplier getter;
        private final DoubleConsumer setter;
        private final double min;
        private final double max;

        private DoubleValue(DoubleSupplier getter, DoubleConsumer setter, double min, double max) {
            this.getter = getter;
            this.setter = setter;
            this.min = min;
            this.max = max;
        }

        private Double get() {
            return getter.getAsDouble();
        }

        private void validateAndSet(double value) {
            setter.accept(clamp(value, min, max));
        }
    }

    private static final class EnumValue<T extends Enum<T>> {
        private final Supplier<T> getter;
        private final Consumer<T> setter;

        private EnumValue(Supplier<T> getter, Consumer<T> setter) {
            this.getter = getter;
            this.setter = setter;
        }

        private T get() {
            return getter.get();
        }

        private void validateAndSet(T value) {
            if (value != null) {
                setter.accept(value);
            }
        }
    }

    private static final class StringValue {
        private final Supplier<String> getter;
        private final Consumer<String> setter;

        private StringValue(Supplier<String> getter, Consumer<String> setter) {
            this.getter = getter;
            this.setter = setter;
        }

        private String get() {
            return getter.get();
        }

        private void validateAndSet(String value) {
            setter.accept(value == null ? "" : value);
        }
    }

    private static final class StringListValue {
        private final Supplier<List<String>> getter;
        private final Consumer<List<String>> setter;

        private StringListValue(Supplier<List<String>> getter, Consumer<List<String>> setter) {
            this.getter = getter;
            this.setter = setter;
        }

        private List<String> get() {
            return getter.get();
        }

        private void validateAndSet(List<? extends String> value) {
            setter.accept(new ArrayList<>(sanitizeTargetFilterEntityIds(value)));
        }
    }

    public static void init() {
        if (initialized) {
            return;
        }

        MidnightConfig.init(Focus.MOD_ID, FocusClientConfig.class);
        initialized = true;
        loadCameraPresets();

        if (!INSTANCE.camera.useCustomSwappedShoulderValues.get()) {
            mirrorOppositeShoulderFrom(Shoulder.LEFT);
            saveCameraPresets();
        }

        INSTANCE.targetFilters.targetFilterEntityIds.validateAndSet(
                sanitizeTargetFilterEntityIds(INSTANCE.targetFilters.targetFilterEntityIds.get()));
        String selectedProfile = sanitizeCameraProfileName(INSTANCE.camera.presetTools.selectedCameraProfile.get());
        INSTANCE.camera.presetTools.selectedCameraProfile.validateAndSet(selectedProfile);
        if (findExistingProfileName(selectedProfile) == null && !CAMERA_SETUP_PROFILES.isEmpty()) {
            INSTANCE.camera.presetTools.selectedCameraProfile.validateAndSet(
                    CAMERA_SETUP_PROFILES.keySet().iterator().next());
        }
    }

    @Override
    public void onTabInit(String tabName, MidnightConfigListWidget list, MidnightConfigScreen screen) {
        if (!CAMERA_CATEGORY.equals(tabName)) {
            return;
        }

        addCameraAction(list, "focus.lock_on_client.openCameraPositionEditor", "Open", true, LockOnCameraEditorScreen::openFromCurrentScreen);
        addCameraAction(list, "focus.lock_on_client.cycleCameraProfile", "Cycle", !cameraProfileNames().isEmpty(), () -> {
            cycleSelectedCameraProfile();
            screen.updateList();
        });
        addCameraAction(list, "focus.lock_on_client.saveSelectedCameraProfile", "Save", !selectedCameraProfileName().isEmpty(), () -> {
            saveSelectedCameraProfileFromConfigScreen();
            screen.updateList();
        });
        addCameraAction(list, "focus.lock_on_client.loadSelectedCameraProfile", "Load", !resolveCameraProfileName(selectedCameraProfileName()).isEmpty(), () -> {
            loadSelectedCameraProfileFromConfigScreen();
            screen.updateList();
        });
        addCameraAction(list, "focus.lock_on_client.deleteSelectedCameraProfile", "Delete", !resolveCameraProfileName(selectedCameraProfileName()).isEmpty(), () -> {
            deleteSelectedCameraProfileFromConfigScreen();
            screen.updateList();
        });
    }

    private void addCameraAction(MidnightConfigListWidget list, String labelKey, String buttonText, boolean active, Runnable action) {
        int buttonX = Minecraft.getInstance().getWindow().getGuiScaledWidth() - 185;
        Button button = Button.builder(Component.literal(buttonText), pressed -> action.run())
                .bounds(buttonX, 0, 150, 20)
                .build();
        button.active = active;
        // MidnightLib 1.9.2 requires non-null EntryInfo in ButtonEntry#render.
        list.addButton(List.of(button), Component.translatable(labelKey), new EntryInfo(null, Focus.MOD_ID));
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

    private static RuntimeConfig config() {
        if (!initialized) {
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

    private static Map<String, CameraSetupPreset> createBuiltInCameraSetupProfiles() {
        Map<String, CameraSetupPreset> profiles = new LinkedHashMap<>();
        profiles.put("Close Combat", createBuiltInCameraSetupPreset(
                new PerspectivePreset(-1.2D, 0.5D, 1.0D, -2.0D),
                CameraMode.DYNAMIC,
                0.32D,
                0.88D,
                0.16D,
                0.9D,
                0.12D,
                0.12D));
        profiles.put("Wide Awareness", createBuiltInCameraSetupPreset(
                new PerspectivePreset(-2.9D, 0.6D, 2.8D, 2.0D),
                CameraMode.DYNAMIC,
                0.22D,
                0.95D,
                0.1D,
                1.0D,
                0.08D,
                0.12D));
        profiles.put("Centered Duel", createBuiltInCameraSetupPreset(
                new PerspectivePreset(-0.8D, 0.35D, 1.7D, 0.0D),
                CameraMode.STATIC,
                0.18D,
                0.95D,
                0.1D,
                1.0D,
                0.01D,
                0.0D));
        profiles.put("High Angle", createBuiltInCameraSetupPreset(
                new PerspectivePreset(-1.9D, 1.0D, 2.4D, -1.0D),
                CameraMode.STATIC,
                0.2D,
                0.95D,
                0.1D,
                1.0D,
                0.01D,
                0.0D));
        return profiles;
    }

    private static CameraSetupPreset createBuiltInCameraSetupPreset(
            PerspectivePreset leftShoulder,
            CameraMode cameraMode,
            double cameraFloatiness,
            double cameraDrag,
            double cameraSwapSpeed,
            double cameraSwapSmoothness,
            double dynamicCameraSwapSpeed,
            double dynamicCameraSwapSmoothness) {
        PerspectivePreset sanitizedLeftShoulder = sanitizePreset(leftShoulder);
        return new CameraSetupPreset(
                true,
                true,
                false,
                false,
                LockOnIndicatorStyle.OOT_16X,
                cameraMode,
                clamp(cameraFloatiness, MIN_CAMERA_FLOATINESS, MAX_CAMERA_FLOATINESS),
                clamp(cameraDrag, MIN_CAMERA_DRAG, MAX_CAMERA_DRAG),
                clamp(cameraSwapSpeed, MIN_CAMERA_SWAP_SPEED, MAX_CAMERA_SWAP_SPEED),
                clamp(cameraSwapSmoothness, MIN_CAMERA_SWAP_SMOOTHNESS, MAX_CAMERA_SWAP_SMOOTHNESS),
                clamp(dynamicCameraSwapSpeed, MIN_DYNAMIC_CAMERA_SWAP_SPEED, MAX_DYNAMIC_CAMERA_SWAP_SPEED),
                clamp(dynamicCameraSwapSmoothness, MIN_DYNAMIC_CAMERA_SWAP_SMOOTHNESS, MAX_DYNAMIC_CAMERA_SWAP_SMOOTHNESS),
                DEFAULT_TARGET_SWAP_MOUSE_DEADZONE,
                DEFAULT_TARGET_SWAP_MOUSE_ACTIVATION,
                DEFAULT_TARGET_SWAP_DIRECTION_THRESHOLD,
                DEFAULT_TARGET_SWAP_MIN_SCREEN_SEPARATION,
                DEFAULT_TARGET_SWAP_INPUT_DECAY,
                DEFAULT_TARGET_SWAP_COOLDOWN_TICKS,
                DEFAULT_TARGET_SWAP_SMOOTH_TICKS,
                DEFAULT_TARGET_SWAP_LOOK_RESPONSIVENESS_YAW,
                DEFAULT_TARGET_SWAP_LOOK_RESPONSIVENESS_PITCH,
                DEFAULT_TARGET_SWAP_LOOK_MAX_YAW_STEP_PER_TICK,
                DEFAULT_TARGET_SWAP_LOOK_MAX_PITCH_STEP_PER_TICK,
                DEFAULT_TARGET_SWAP_TARGET_POINT_RESPONSIVENESS,
                DEFAULT_TARGET_SWAP_PLAYER_LOOK_FOLLOW,
                DEFAULT_TARGET_FILTERS_ENABLED,
                TargetFilterMode.EXCLUDE,
                DEFAULT_FILTER_PLAYERS,
                DEFAULT_FILTER_PASSIVE_MOBS,
                DEFAULT_FILTER_NEUTRAL_MOBS,
                DEFAULT_FILTER_HOSTILE_MOBS,
                DEFAULT_TARGET_FILTER_ENTITY_IDS,
                sanitizedLeftShoulder,
                sanitizedLeftShoulder.mirroredForOppositeShoulder(),
                false);
    }

    private static void seedBuiltInCameraSetupProfiles() {
        for (Map.Entry<String, CameraSetupPreset> entry : BUILT_IN_CAMERA_SETUP_PROFILES.entrySet()) {
            if (findExistingProfileName(entry.getKey()) == null) {
                CAMERA_SETUP_PROFILES.put(entry.getKey(), entry.getValue());
            }
        }
    }

    private static void loadCameraPresets() {
        CAMERA_SETUP_PROFILES.clear();
        if (!Files.isRegularFile(CAMERA_PRESET_PATH)) {
            leftShoulderPreset = defaultLeftPreset();
            rightShoulderPreset = leftShoulderPreset.mirroredForOppositeShoulder();
            seedBuiltInCameraSetupProfiles();
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
            seedBuiltInCameraSetupProfiles();
        } catch (Exception e) {
            Focus.LOGGER.warn("Failed to load camera preset file {}, using defaults", CAMERA_PRESET_PATH, e);
            leftShoulderPreset = defaultLeftPreset();
            rightShoulderPreset = leftShoulderPreset.mirroredForOppositeShoulder();
            CAMERA_SETUP_PROFILES.clear();
            seedBuiltInCameraSetupProfiles();
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
