package com.jvn.focus.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jvn.focus.Focus;
import com.jvn.focus.client.FocusConfig;
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

import net.minecraft.client.gui.screens.Screen;
import io.wispforest.owo.config.ui.ConfigScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.neoforged.fml.loading.FMLPaths;

public final class FocusClientConfig {

    public static final double DEFAULT_CAMERA_OFFSET_X = -1.2D;
    public static final double DEFAULT_CAMERA_OFFSET_Y = 0.5D;
    public static final double DEFAULT_CAMERA_OFFSET_Z = 1.0D;
    public static final double DEFAULT_CAMERA_ROTATION = -2.0D;
    public static final double DEFAULT_CAMERA_FLOATINESS = 0.32D;
    public static final double DEFAULT_CAMERA_DRAG = 0.88D;
    public static final double DEFAULT_CAMERA_SWAP_SPEED = 0.16D;
    public static final double DEFAULT_CAMERA_SWAP_SMOOTHNESS = 0.9D;
    public static final double DEFAULT_DYNAMIC_CAMERA_SWAP_SPEED = 0.12D;
    public static final double DEFAULT_DYNAMIC_CAMERA_SWAP_SMOOTHNESS = 0.12D;
    public static final double DEFAULT_CAMERA_STEP_SIZE = 0.025D;
    public static final boolean DEFAULT_DYNAMICALLY_ADJUST_OFFSETS = true;
    public static final boolean DEFAULT_FOLLOW_PLAYER_ROTATIONS = true;
    public static final double DEFAULT_FOLLOW_PLAYER_ROTATIONS_DELAY = 0.0D;
    public static final boolean DEFAULT_SHOW_LOCK_ON_STATUS_MESSAGES = true;
    public static final double DEFAULT_CAMERA_HEAD_FOLLOW_RESPONSIVENESS = 0.45D;
    public static final double DEFAULT_CAMERA_BODY_FOLLOW_RESPONSIVENESS = 0.25D;
    public static final boolean DEFAULT_FULL_BODY_FOLLOW_ENABLED = true;
    public static final boolean DEFAULT_DYNAMIC_SHOULDER_AUTO_SWAP_ENABLED = true;
    public static final double DEFAULT_DYNAMIC_SHOULDER_SWITCH_THRESHOLD = 0.08D;
    public static final double DEFAULT_DYNAMIC_SHOULDER_VISIBILITY_WEIGHT = 0.7D;
    public static final double DEFAULT_DYNAMIC_SHOULDER_SCREEN_PLACEMENT_WEIGHT = 0.3D;
    public static final double DEFAULT_DYNAMIC_SHOULDER_MANUAL_OVERRIDE_COOLDOWN_TICKS = 30.0D;
    public static final boolean DEFAULT_ADJUST_PLAYER_TRANSPARENCY = true;
    public static final double DEFAULT_PLAYER_TRANSPARENCY_MIN_ALPHA = 0.25D;
    public static final double DEFAULT_PLAYER_TRANSPARENCY_FADE_SPEED = 0.2D;
    public static final boolean DEFAULT_PLAYER_TRANSPARENCY_WHEN_TARGET_OBSCURED_ONLY = true;
    public static final boolean DEFAULT_PLAYER_TRANSPARENCY_IN_PREVIEW = false;
    public static final boolean DEFAULT_CINEMATIC_BARS_WHILE_LOCKED_ON = false;
    public static final boolean DEFAULT_CINEMATIC_BARS_UNDER_HUD = false;
    public static final CrosshairCorrectionMode DEFAULT_CROSSHAIR_CORRECTION_MODE = CrosshairCorrectionMode.HYBRID;
    public static final boolean DEFAULT_RENDER_CORRECTED_CROSSHAIR = true;
    public static final boolean DEFAULT_CORRECT_BLOCK_PLACEMENT_RAY = true;
    public static final boolean DEFAULT_CORRECT_ENTITY_HIT_RAY = true;
    public static final boolean DEFAULT_CORRECT_CROSSHAIR_ONLY_WHILE_LOCKED_ON = false;
    public static final boolean DEFAULT_HIDE_VANILLA_CROSSHAIR = true;
    public static final boolean DEFAULT_HIDE_VANILLA_CROSSHAIR_OUT_OF_RANGE = false;
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
    public static final double MIN_CAMERA_STEP_SIZE = 0.001D;
    public static final double MAX_CAMERA_STEP_SIZE = 1.0D;
    public static final double MIN_FOLLOW_PLAYER_ROTATIONS_DELAY = 0.0D;
    public static final double MAX_FOLLOW_PLAYER_ROTATIONS_DELAY = 40.0D;
    public static final double MIN_CAMERA_HEAD_FOLLOW_RESPONSIVENESS = 0.01D;
    public static final double MAX_CAMERA_HEAD_FOLLOW_RESPONSIVENESS = 1.0D;
    public static final double MIN_CAMERA_BODY_FOLLOW_RESPONSIVENESS = 0.01D;
    public static final double MAX_CAMERA_BODY_FOLLOW_RESPONSIVENESS = 1.0D;
    public static final double MIN_DYNAMIC_SHOULDER_SWITCH_THRESHOLD = 0.0D;
    public static final double MAX_DYNAMIC_SHOULDER_SWITCH_THRESHOLD = 1.0D;
    public static final double MIN_DYNAMIC_SHOULDER_VISIBILITY_WEIGHT = 0.0D;
    public static final double MAX_DYNAMIC_SHOULDER_VISIBILITY_WEIGHT = 1.0D;
    public static final double MIN_DYNAMIC_SHOULDER_SCREEN_PLACEMENT_WEIGHT = 0.0D;
    public static final double MAX_DYNAMIC_SHOULDER_SCREEN_PLACEMENT_WEIGHT = 1.0D;
    public static final double MIN_DYNAMIC_SHOULDER_MANUAL_OVERRIDE_COOLDOWN_TICKS = 0.0D;
    public static final double MAX_DYNAMIC_SHOULDER_MANUAL_OVERRIDE_COOLDOWN_TICKS = 200.0D;
    public static final double MIN_PLAYER_TRANSPARENCY_MIN_ALPHA = 0.0D;
    public static final double MAX_PLAYER_TRANSPARENCY_MIN_ALPHA = 1.0D;
    public static final double MIN_PLAYER_TRANSPARENCY_FADE_SPEED = 0.01D;
    public static final double MAX_PLAYER_TRANSPARENCY_FADE_SPEED = 1.0D;
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
    public static final double CAMERA_SLIDER_INCREMENT = DEFAULT_CAMERA_STEP_SIZE;
    public static final double ROTATION_SLIDER_INCREMENT = 1.0D;
    public static final int MAX_CAMERA_PROFILE_NAME_LENGTH = 40;

    private static final int CAMERA_VALUE_SCALE = 3;
    private static final Path CAMERA_PRESET_PATH = FMLPaths.CONFIGDIR.get().resolve(Focus.MOD_ID + "_lock_on_camera.json");

    private static FocusConfig CONFIG;
    private static boolean initialized;
    private static PerspectivePreset leftShoulderPreset = defaultLeftPreset();
    private static PerspectivePreset rightShoulderPreset = defaultLeftPreset().mirroredForOppositeShoulder();
    private static final Map<String, CameraSetupPreset> CAMERA_SETUP_PROFILES = new LinkedHashMap<>();
    private static final Map<String, CameraSetupPreset> BUILT_IN_CAMERA_SETUP_PROFILES = createBuiltInCameraSetupProfiles();

    private FocusClientConfig() {}

    // â”€â”€ Enums â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

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

    public enum CrosshairCorrectionMode implements StringRepresentable {
        VANILLA("focus.lock_on_client.crosshair_correction_mode.VANILLA"),
        CAMERA_PROJECTED("focus.lock_on_client.crosshair_correction_mode.CAMERA_PROJECTED"),
        TARGET_PROJECTED("focus.lock_on_client.crosshair_correction_mode.TARGET_PROJECTED"),
        HYBRID("focus.lock_on_client.crosshair_correction_mode.HYBRID");

        private final String translationKey;

        CrosshairCorrectionMode(String translationKey) {
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

        @Override
        public String getSerializedName() {
            return translationKey;
        }

        public enum IndicatorType {
            OOT_TRIANGLES,
            STATIC_CENTERED
        }
    }

    // â”€â”€ Initialization â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    public static void init() {
        if (initialized) {
            return;
        }
        CONFIG = FocusConfig.createAndLoad();
        initialized = true;

        loadCameraPresets();

        if (!useCustomSwappedShoulderValues()) {
            mirrorOppositeShoulderFrom(Shoulder.LEFT);
        }

        List<String> sanitized = sanitizeTargetFilterEntityIds(CONFIG.targetFilterEntityIds());
        CONFIG.targetFilterEntityIds(sanitized);

        String profileName = sanitizeCameraProfileName(CONFIG.selectedCameraProfile());
        CONFIG.selectedCameraProfile(profileName);
    }

    public static FocusConfig configInstance() {
        config();
        return CONFIG;
    }

    public static Screen createConfigScreen(Screen parent) {
        config();
        return ConfigScreen.create(CONFIG, parent);
    }

    // â”€â”€ Getters â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    public static boolean autoSwitchToThirdPerson() {
        return config().autoSwitchToThirdPerson();
    }

    public static boolean allowFirstPersonWhileTargeting() {
        return config().allowFirstPersonWhileTargeting();
    }

    public static boolean allowFrontFacingThirdPersonWhileTargeting() {
        return config().allowFrontFacingThirdPersonWhileTargeting();
    }

    public static boolean showLockOnStatusMessages() {
        return config().showLockOnStatusMessages();
    }

    public static boolean showLockOnDebugText() {
        return config().showLockOnDebugText();
    }

    public static boolean cinematicBarsWhileLockedOn() {
        return config().cinematicBarsWhileLockedOn();
    }

    public static boolean cinematicBarsUnderHud() {
        return config().cinematicBarsUnderHud();
    }

    public static LockOnIndicatorStyle lockOnIndicatorStyle() {
        return config().lockOnIndicatorStyle();
    }

    public static boolean useCustomSwappedShoulderValues() {
        return config().useCustomSwappedShoulderValues();
    }

    public static CameraMode cameraMode() {
        return config().cameraMode();
    }

    public static double cameraFloatiness() {
        return config().cameraBehavior.cameraFloatiness();
    }

    public static double cameraDrag() {
        return config().cameraBehavior.cameraDrag();
    }

    public static double cameraSwapSpeed() {
        return config().shoulderSwap.cameraSwapSpeed();
    }

    public static double cameraSwapSmoothness() {
        return config().cameraSwapSmoothness();
    }

    public static double dynamicCameraSwapSpeed() {
        return config().shoulderSwap.dynamicCameraSwapSpeed();
    }

    public static double dynamicCameraSwapSmoothness() {
        return config().dynamicCameraSwapSmoothness();
    }

    public static double cameraStepSize() {
        return config().cameraBehavior.cameraStepSize();
    }

    public static boolean dynamicallyAdjustOffsets() {
        return config().cameraBehavior.dynamicallyAdjustOffsets();
    }

    public static boolean followPlayerRotations() {
        return config().cameraFollowing.followPlayerRotations();
    }

    public static int followPlayerRotationsDelay() {
        return (int) Math.round(config().cameraFollowing.followPlayerRotationsDelay());
    }

    public static float cameraHeadFollowResponsiveness() {
        return (float) config().cameraFollowing.cameraHeadFollowResponsiveness();
    }

    public static float cameraBodyFollowResponsiveness() {
        return (float) config().cameraFollowing.cameraBodyFollowResponsiveness();
    }

    public static boolean fullBodyFollowEnabled() {
        return config().cameraFollowing.fullBodyFollowEnabled();
    }

    public static boolean dynamicShoulderAutoSwapEnabled() {
        return config().shoulderSwap.dynamicShoulderAutoSwapEnabled();
    }

    public static double dynamicShoulderSwitchThreshold() {
        return config().shoulderSwap.dynamicShoulderSwitchThreshold();
    }

    public static double dynamicShoulderVisibilityWeight() {
        return config().shoulderSwap.dynamicShoulderVisibilityWeight();
    }

    public static double dynamicShoulderScreenPlacementWeight() {
        return 1.0D - dynamicShoulderVisibilityWeight();
    }

    public static int dynamicShoulderManualOverrideCooldownTicks() {
        return (int) Math.round(config().shoulderSwap.dynamicShoulderManualOverrideCooldownTicks());
    }

    public static boolean adjustPlayerTransparency() {
        return config().playerTransparency.adjustPlayerTransparency();
    }

    public static float playerTransparencyMinAlpha() {
        return (float) config().playerTransparency.playerTransparencyMinAlpha();
    }

    public static float playerTransparencyFadeSpeed() {
        return (float) config().playerTransparency.playerTransparencyFadeSpeed();
    }

    public static boolean playerTransparencyWhenTargetObscuredOnly() {
        return config().playerTransparency.playerTransparencyWhenTargetObscuredOnly();
    }

    public static boolean playerTransparencyInPreview() {
        return config().playerTransparency.playerTransparencyInPreview();
    }

    public static CrosshairCorrectionMode crosshairCorrectionMode() {
        return config().crosshair.crosshairCorrectionMode();
    }

    public static boolean renderCorrectedCrosshair() {
        return config().crosshair.renderCorrectedCrosshair();
    }

    public static boolean correctBlockPlacementRay() {
        return config().crosshair.correctBlockPlacementRay();
    }

    public static boolean correctEntityHitRay() {
        return config().crosshair.correctEntityHitRay();
    }

    public static boolean correctCrosshairOnlyWhileLockedOn() {
        return config().crosshair.correctCrosshairOnlyWhileLockedOn();
    }

    public static boolean hideVanillaCrosshair() {
        return config().crosshair.hideVanillaCrosshair();
    }

    public static boolean hideVanillaCrosshairOutOfRange() {
        return config().crosshair.hideVanillaCrosshairOutOfRange();
    }

    public static double targetSwapMouseDeadzone() {
        return config().targetSwapMouseDeadzone();
    }

    public static double targetSwapMouseActivation() {
        return Math.max(targetSwapMouseDeadzone(), config().targetSwapMouseActivation());
    }

    public static double targetSwapDirectionThreshold() {
        return config().targetSwapDirectionThreshold();
    }

    public static double targetSwapMinScreenSeparation() {
        return config().targetSwapMinScreenSeparation();
    }

    public static double targetSwapInputDecay() {
        return config().targetSwapInputDecay();
    }

    public static int targetSwapCooldownTicks() {
        return (int) Math.round(config().targetSwapCooldownTicks());
    }

    public static int targetSwapSmoothTicks() {
        return (int) Math.round(config().targetSwapSmoothTicks());
    }

    public static float targetSwapLookYawResponsiveness() {
        return (float) config().targetSwapLookYawResponsiveness();
    }

    public static float targetSwapLookPitchResponsiveness() {
        return (float) config().targetSwapLookPitchResponsiveness();
    }

    public static float targetSwapLookMaxYawStepPerTick() {
        return (float) config().targetSwapLookMaxYawStepPerTick();
    }

    public static float targetSwapLookMaxPitchStepPerTick() {
        return (float) config().targetSwapLookMaxPitchStepPerTick();
    }

    public static float targetSwapTargetPointResponsiveness() {
        return (float) config().targetSwapTargetPointResponsiveness();
    }

    public static float targetSwapPlayerLookFollow() {
        return (float) config().targetSwapPlayerLookFollow();
    }

    public static boolean enableTargetFilters() {
        return config().enableTargetFilters();
    }

    public static TargetFilterMode targetFilterMode() {
        return config().targetFilterMode();
    }

    public static boolean filterPlayers() {
        return config().filterPlayers();
    }

    public static boolean filterPassiveMobs() {
        return config().filterPassiveMobs();
    }

    public static boolean filterNeutralMobs() {
        return config().filterNeutralMobs();
    }

    public static boolean filterHostileMobs() {
        return config().filterHostileMobs();
    }

    public static List<String> targetFilterEntityIds() {
        return sanitizeTargetFilterEntityIds(config().targetFilterEntityIds());
    }

    public static String selectedCameraProfileName() {
        return sanitizeCameraProfileName(config().selectedCameraProfile());
    }

    public static void setSelectedCameraProfileName(String profileName) {
        config().selectedCameraProfile(sanitizeCameraProfileName(profileName));
    }


    // â”€â”€ Setters â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    public static void setFollowPlayerRotations(boolean value) {
        config().cameraFollowing.followPlayerRotations(value);
    }

    public static void setUseCustomSwappedShoulderValues(boolean useCustomSwappedShoulderValues, Shoulder sourceShoulder) {
        config().useCustomSwappedShoulderValues(useCustomSwappedShoulderValues);
        if (!useCustomSwappedShoulderValues) {
            mirrorOppositeShoulderFrom(sourceShoulder);
        }
    }

    // â”€â”€ Camera offset accessors â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

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

    // â”€â”€ Camera adjustment methods â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    public static void adjustCameraLeft() {
        adjustCameraLeft(Shoulder.LEFT, cameraStepSize());
    }

    public static void adjustCameraLeft(Shoulder shoulder) {
        adjustCameraLeft(shoulder, cameraStepSize());
    }

    public static void adjustCameraLeft(Shoulder shoulder, double step) {
        setCameraOffsetX(shoulder, cameraOffsetX(shoulder) + Math.abs(step));
    }

    public static void adjustCameraRight() {
        adjustCameraRight(Shoulder.LEFT, cameraStepSize());
    }

    public static void adjustCameraRight(Shoulder shoulder) {
        adjustCameraRight(shoulder, cameraStepSize());
    }

    public static void adjustCameraRight(Shoulder shoulder, double step) {
        setCameraOffsetX(shoulder, cameraOffsetX(shoulder) - Math.abs(step));
    }

    public static void adjustCameraUp() {
        adjustCameraUp(Shoulder.LEFT, cameraStepSize());
    }

    public static void adjustCameraUp(Shoulder shoulder) {
        adjustCameraUp(shoulder, cameraStepSize());
    }

    public static void adjustCameraUp(Shoulder shoulder, double step) {
        setCameraOffsetY(shoulder, cameraOffsetY(shoulder) + Math.abs(step));
    }

    public static void adjustCameraDown() {
        adjustCameraDown(Shoulder.LEFT, cameraStepSize());
    }

    public static void adjustCameraDown(Shoulder shoulder) {
        adjustCameraDown(shoulder, cameraStepSize());
    }

    public static void adjustCameraDown(Shoulder shoulder, double step) {
        setCameraOffsetY(shoulder, cameraOffsetY(shoulder) - Math.abs(step));
    }

    public static void adjustCameraIn() {
        adjustCameraIn(Shoulder.LEFT, cameraStepSize());
    }

    public static void adjustCameraIn(Shoulder shoulder) {
        adjustCameraIn(shoulder, cameraStepSize());
    }

    public static void adjustCameraIn(Shoulder shoulder, double step) {
        setCameraOffsetZ(shoulder, cameraOffsetZ(shoulder) - Math.abs(step));
    }

    public static void adjustCameraOut() {
        adjustCameraOut(Shoulder.LEFT, cameraStepSize());
    }

    public static void adjustCameraOut(Shoulder shoulder) {
        adjustCameraOut(shoulder, cameraStepSize());
    }

    public static void adjustCameraOut(Shoulder shoulder, double step) {
        setCameraOffsetZ(shoulder, cameraOffsetZ(shoulder) + Math.abs(step));
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

    // â”€â”€ Preset accessors â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

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

    // â”€â”€ Camera setup presets â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    public static CameraSetupPreset currentCameraSetupPreset() {
        return new CameraSetupPreset(
                autoSwitchToThirdPerson(),
                allowFirstPersonWhileTargeting(),
                allowFrontFacingThirdPersonWhileTargeting(),
                showLockOnStatusMessages(),
                showLockOnDebugText(),
                cinematicBarsWhileLockedOn(),
                cinematicBarsUnderHud(),
                lockOnIndicatorStyle(),
                cameraMode(),
                cameraFloatiness(),
                cameraDrag(),
                cameraSwapSpeed(),
                cameraSwapSmoothness(),
                dynamicCameraSwapSpeed(),
                dynamicCameraSwapSmoothness(),
                cameraStepSize(),
                dynamicallyAdjustOffsets(),
                followPlayerRotations(),
                config().cameraFollowing.followPlayerRotationsDelay(),
                config().cameraFollowing.cameraHeadFollowResponsiveness(),
                config().cameraFollowing.cameraBodyFollowResponsiveness(),
                fullBodyFollowEnabled(),
                dynamicShoulderAutoSwapEnabled(),
                config().shoulderSwap.dynamicShoulderSwitchThreshold(),
                dynamicShoulderVisibilityWeight(),
                dynamicShoulderScreenPlacementWeight(),
                config().shoulderSwap.dynamicShoulderManualOverrideCooldownTicks(),
                adjustPlayerTransparency(),
                config().playerTransparency.playerTransparencyMinAlpha(),
                config().playerTransparency.playerTransparencyFadeSpeed(),
                playerTransparencyWhenTargetObscuredOnly(),
                playerTransparencyInPreview(),
                crosshairCorrectionMode(),
                renderCorrectedCrosshair(),
                correctBlockPlacementRay(),
                correctEntityHitRay(),
                correctCrosshairOnlyWhileLockedOn(),
                hideVanillaCrosshair(),
                hideVanillaCrosshairOutOfRange(),
                config().targetSwapMouseDeadzone(),
                config().targetSwapMouseActivation(),
                config().targetSwapDirectionThreshold(),
                config().targetSwapMinScreenSeparation(),
                config().targetSwapInputDecay(),
                config().targetSwapCooldownTicks(),
                config().targetSwapSmoothTicks(),
                config().targetSwapLookYawResponsiveness(),
                config().targetSwapLookPitchResponsiveness(),
                config().targetSwapLookMaxYawStepPerTick(),
                config().targetSwapLookMaxPitchStepPerTick(),
                config().targetSwapTargetPointResponsiveness(),
                config().targetSwapPlayerLookFollow(),
                enableTargetFilters(),
                targetFilterMode(),
                config().filterPlayers(),
                config().filterPassiveMobs(),
                config().filterNeutralMobs(),
                config().filterHostileMobs(),
                targetFilterEntityIds(),
                currentPreset(Shoulder.LEFT),
                currentPreset(Shoulder.RIGHT),
                useCustomSwappedShoulderValues());
    }

    public static void applyCameraSetupPreset(CameraSetupPreset setup) {
        config().autoSwitchToThirdPerson(setup.autoSwitchToThirdPerson());
        config().allowFirstPersonWhileTargeting(setup.allowFirstPersonWhileTargeting());
        config().allowFrontFacingThirdPersonWhileTargeting(setup.allowFrontFacingThirdPersonWhileTargeting());
        config().showLockOnStatusMessages(setup.showLockOnStatusMessages());
        config().showLockOnDebugText(setup.showLockOnDebugText());
        config().cinematicBarsWhileLockedOn(setup.cinematicBarsWhileLockedOn());
        config().cinematicBarsUnderHud(setup.cinematicBarsUnderHud());
        config().lockOnIndicatorStyle(setup.lockOnIndicatorStyle());
        config().cameraMode(setup.cameraMode());
        config().cameraBehavior.cameraFloatiness(setup.cameraFloatiness());
        config().cameraBehavior.cameraDrag(setup.cameraDrag());
        config().shoulderSwap.cameraSwapSpeed(setup.cameraSwapSpeed());
        config().cameraSwapSmoothness(setup.cameraSwapSmoothness());
        config().shoulderSwap.dynamicCameraSwapSpeed(setup.dynamicCameraSwapSpeed());
        config().dynamicCameraSwapSmoothness(setup.dynamicCameraSwapSmoothness());
        config().cameraBehavior.cameraStepSize(setup.cameraStepSize());
        config().cameraBehavior.dynamicallyAdjustOffsets(setup.dynamicallyAdjustOffsets());
        config().cameraFollowing.followPlayerRotations(setup.followPlayerRotations());
        config().cameraFollowing.followPlayerRotationsDelay(setup.followPlayerRotationsDelay());
        config().cameraFollowing.cameraHeadFollowResponsiveness(setup.cameraHeadFollowResponsiveness());
        config().cameraFollowing.cameraBodyFollowResponsiveness(setup.cameraBodyFollowResponsiveness());
        config().cameraFollowing.fullBodyFollowEnabled(setup.fullBodyFollowEnabled());
        config().shoulderSwap.dynamicShoulderAutoSwapEnabled(setup.dynamicShoulderAutoSwapEnabled());
        config().shoulderSwap.dynamicShoulderSwitchThreshold(setup.dynamicShoulderSwitchThreshold());
        config().shoulderSwap.dynamicShoulderVisibilityWeight(
            normalizeDynamicShoulderVisibilityWeight(
                setup.dynamicShoulderVisibilityWeight(),
                setup.dynamicShoulderScreenPlacementWeight()));
        config().shoulderSwap.dynamicShoulderManualOverrideCooldownTicks(setup.dynamicShoulderManualOverrideCooldownTicks());
        config().playerTransparency.adjustPlayerTransparency(setup.adjustPlayerTransparency());
        config().playerTransparency.playerTransparencyMinAlpha(setup.playerTransparencyMinAlpha());
        config().playerTransparency.playerTransparencyFadeSpeed(setup.playerTransparencyFadeSpeed());
        config().playerTransparency.playerTransparencyWhenTargetObscuredOnly(setup.playerTransparencyWhenTargetObscuredOnly());
        config().playerTransparency.playerTransparencyInPreview(setup.playerTransparencyInPreview());
        config().crosshair.crosshairCorrectionMode(setup.crosshairCorrectionMode());
        config().crosshair.renderCorrectedCrosshair(setup.renderCorrectedCrosshair());
        config().crosshair.correctBlockPlacementRay(setup.correctBlockPlacementRay());
        config().crosshair.correctEntityHitRay(setup.correctEntityHitRay());
        config().crosshair.correctCrosshairOnlyWhileLockedOn(setup.correctCrosshairOnlyWhileLockedOn());
        config().crosshair.hideVanillaCrosshair(setup.hideVanillaCrosshair());
        config().crosshair.hideVanillaCrosshairOutOfRange(setup.hideVanillaCrosshairOutOfRange());
        config().targetSwapMouseDeadzone(setup.targetSwapMouseDeadzone());
        config().targetSwapMouseActivation(setup.targetSwapMouseActivation());
        config().targetSwapDirectionThreshold(setup.targetSwapDirectionThreshold());
        config().targetSwapMinScreenSeparation(setup.targetSwapMinScreenSeparation());
        config().targetSwapInputDecay(setup.targetSwapInputDecay());
        config().targetSwapCooldownTicks(setup.targetSwapCooldownTicks());
        config().targetSwapSmoothTicks(setup.targetSwapSmoothTicks());
        config().targetSwapLookYawResponsiveness(setup.targetSwapLookYawResponsiveness());
        config().targetSwapLookPitchResponsiveness(setup.targetSwapLookPitchResponsiveness());
        config().targetSwapLookMaxYawStepPerTick(setup.targetSwapLookMaxYawStepPerTick());
        config().targetSwapLookMaxPitchStepPerTick(setup.targetSwapLookMaxPitchStepPerTick());
        config().targetSwapTargetPointResponsiveness(setup.targetSwapTargetPointResponsiveness());
        config().targetSwapPlayerLookFollow(setup.targetSwapPlayerLookFollow());
        config().enableTargetFilters(setup.enableTargetFilters());
        config().targetFilterMode(setup.targetFilterMode());
        config().filterPlayers(setup.filterPlayers());
        config().filterPassiveMobs(setup.filterPassiveMobs());
        config().filterNeutralMobs(setup.filterNeutralMobs());
        config().filterHostileMobs(setup.filterHostileMobs());
        config().targetFilterEntityIds(sanitizeTargetFilterEntityIds(setup.targetFilterEntityIds()));
        if (setup.useCustomSwappedShoulderValues()) {
            config().useCustomSwappedShoulderValues(true);
            setRawPreset(Shoulder.LEFT, setup.leftShoulder());
            setRawPreset(Shoulder.RIGHT, setup.rightShoulder());
        } else {
            config().useCustomSwappedShoulderValues(false);
            setRawPreset(Shoulder.LEFT, setup.leftShoulder());
            mirrorOppositeShoulderFrom(Shoulder.LEFT);
        }
    }

    // â”€â”€ Profile management â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

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

    // â”€â”€ Serialization â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

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

    // â”€â”€ Utility â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

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

    // â”€â”€ Private helpers â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    private static FocusConfig config() {
        if (!initialized) {
            init();
        }
        return CONFIG;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double normalizeDynamicShoulderVisibilityWeight(double visibilityWeight, double screenPlacementWeight) {
        double clampedVisibility = clamp(
                visibilityWeight,
                MIN_DYNAMIC_SHOULDER_VISIBILITY_WEIGHT,
                MAX_DYNAMIC_SHOULDER_VISIBILITY_WEIGHT);
        double clampedScreenPlacement = clamp(
                screenPlacementWeight,
                MIN_DYNAMIC_SHOULDER_SCREEN_PLACEMENT_WEIGHT,
                MAX_DYNAMIC_SHOULDER_SCREEN_PLACEMENT_WEIGHT);
        double total = clampedVisibility + clampedScreenPlacement;
        if (total <= 0.000001D) {
            return DEFAULT_DYNAMIC_SHOULDER_VISIBILITY_WEIGHT;
        }
        return clamp(
                clampedVisibility / total,
                MIN_DYNAMIC_SHOULDER_VISIBILITY_WEIGHT,
                MAX_DYNAMIC_SHOULDER_VISIBILITY_WEIGHT);
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

    // â”€â”€ Built-in camera setup profiles â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

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
                DEFAULT_SHOW_LOCK_ON_STATUS_MESSAGES,
                false,
                DEFAULT_CINEMATIC_BARS_WHILE_LOCKED_ON,
                DEFAULT_CINEMATIC_BARS_UNDER_HUD,
                LockOnIndicatorStyle.OOT_16X,
                cameraMode,
                clamp(cameraFloatiness, MIN_CAMERA_FLOATINESS, MAX_CAMERA_FLOATINESS),
                clamp(cameraDrag, MIN_CAMERA_DRAG, MAX_CAMERA_DRAG),
                clamp(cameraSwapSpeed, MIN_CAMERA_SWAP_SPEED, MAX_CAMERA_SWAP_SPEED),
                clamp(cameraSwapSmoothness, MIN_CAMERA_SWAP_SMOOTHNESS, MAX_CAMERA_SWAP_SMOOTHNESS),
                clamp(dynamicCameraSwapSpeed, MIN_DYNAMIC_CAMERA_SWAP_SPEED, MAX_DYNAMIC_CAMERA_SWAP_SPEED),
                clamp(dynamicCameraSwapSmoothness, MIN_DYNAMIC_CAMERA_SWAP_SMOOTHNESS, MAX_DYNAMIC_CAMERA_SWAP_SMOOTHNESS),
                DEFAULT_CAMERA_STEP_SIZE,
                DEFAULT_DYNAMICALLY_ADJUST_OFFSETS,
                DEFAULT_FOLLOW_PLAYER_ROTATIONS,
                DEFAULT_FOLLOW_PLAYER_ROTATIONS_DELAY,
                DEFAULT_CAMERA_HEAD_FOLLOW_RESPONSIVENESS,
                DEFAULT_CAMERA_BODY_FOLLOW_RESPONSIVENESS,
                DEFAULT_FULL_BODY_FOLLOW_ENABLED,
                DEFAULT_DYNAMIC_SHOULDER_AUTO_SWAP_ENABLED,
                DEFAULT_DYNAMIC_SHOULDER_SWITCH_THRESHOLD,
                DEFAULT_DYNAMIC_SHOULDER_VISIBILITY_WEIGHT,
                DEFAULT_DYNAMIC_SHOULDER_SCREEN_PLACEMENT_WEIGHT,
                DEFAULT_DYNAMIC_SHOULDER_MANUAL_OVERRIDE_COOLDOWN_TICKS,
                DEFAULT_ADJUST_PLAYER_TRANSPARENCY,
                DEFAULT_PLAYER_TRANSPARENCY_MIN_ALPHA,
                DEFAULT_PLAYER_TRANSPARENCY_FADE_SPEED,
                DEFAULT_PLAYER_TRANSPARENCY_WHEN_TARGET_OBSCURED_ONLY,
                DEFAULT_PLAYER_TRANSPARENCY_IN_PREVIEW,
                DEFAULT_CROSSHAIR_CORRECTION_MODE,
                DEFAULT_RENDER_CORRECTED_CROSSHAIR,
                DEFAULT_CORRECT_BLOCK_PLACEMENT_RAY,
                DEFAULT_CORRECT_ENTITY_HIT_RAY,
                DEFAULT_CORRECT_CROSSHAIR_ONLY_WHILE_LOCKED_ON,
                DEFAULT_HIDE_VANILLA_CROSSHAIR,
                DEFAULT_HIDE_VANILLA_CROSSHAIR_OUT_OF_RANGE,
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

    // â”€â”€ Camera preset persistence â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

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
            object.addProperty("format", "focus_camera_presets_v5");
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

    // â”€â”€ JSON serialization helpers â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    private static JsonObject cameraSetupToJson(CameraSetupPreset setup) {
        JsonObject object = new JsonObject();
        object.addProperty("autoSwitchToThirdPerson", setup.autoSwitchToThirdPerson());
        object.addProperty("allowFirstPersonWhileTargeting", setup.allowFirstPersonWhileTargeting());
        object.addProperty("allowFrontFacingThirdPersonWhileTargeting", setup.allowFrontFacingThirdPersonWhileTargeting());
        object.addProperty("showLockOnStatusMessages", setup.showLockOnStatusMessages());
        object.addProperty("showLockOnDebugText", setup.showLockOnDebugText());
        object.addProperty("cinematicBarsWhileLockedOn", setup.cinematicBarsWhileLockedOn());
        object.addProperty("cinematicBarsUnderHud", setup.cinematicBarsUnderHud());
        object.addProperty("lockOnIndicatorStyle", setup.lockOnIndicatorStyle().name());
        object.addProperty("cameraMode", setup.cameraMode().name());
        object.addProperty("cameraFloatiness", setup.cameraFloatiness());
        object.addProperty("cameraDrag", setup.cameraDrag());
        object.addProperty("cameraSwapSpeed", setup.cameraSwapSpeed());
        object.addProperty("cameraSwapSmoothness", setup.cameraSwapSmoothness());
        object.addProperty("dynamicCameraSwapSpeed", setup.dynamicCameraSwapSpeed());
        object.addProperty("dynamicCameraSwapSmoothness", setup.dynamicCameraSwapSmoothness());
        object.addProperty("cameraStepSize", setup.cameraStepSize());
        object.addProperty("dynamicallyAdjustOffsets", setup.dynamicallyAdjustOffsets());
        object.addProperty("followPlayerRotations", setup.followPlayerRotations());
        object.addProperty("followPlayerRotationsDelay", setup.followPlayerRotationsDelay());
        object.addProperty("cameraHeadFollowResponsiveness", setup.cameraHeadFollowResponsiveness());
        object.addProperty("cameraBodyFollowResponsiveness", setup.cameraBodyFollowResponsiveness());
        object.addProperty("fullBodyFollowEnabled", setup.fullBodyFollowEnabled());
        object.addProperty("dynamicShoulderAutoSwapEnabled", setup.dynamicShoulderAutoSwapEnabled());
        object.addProperty("dynamicShoulderSwitchThreshold", setup.dynamicShoulderSwitchThreshold());
        object.addProperty("dynamicShoulderVisibilityWeight", setup.dynamicShoulderVisibilityWeight());
        object.addProperty("dynamicShoulderScreenPlacementWeight", setup.dynamicShoulderScreenPlacementWeight());
        object.addProperty("dynamicShoulderManualOverrideCooldownTicks", setup.dynamicShoulderManualOverrideCooldownTicks());
        object.addProperty("adjustPlayerTransparency", setup.adjustPlayerTransparency());
        object.addProperty("playerTransparencyMinAlpha", setup.playerTransparencyMinAlpha());
        object.addProperty("playerTransparencyFadeSpeed", setup.playerTransparencyFadeSpeed());
        object.addProperty("playerTransparencyWhenTargetObscuredOnly", setup.playerTransparencyWhenTargetObscuredOnly());
        object.addProperty("playerTransparencyInPreview", setup.playerTransparencyInPreview());
        object.addProperty("crosshairCorrectionMode", setup.crosshairCorrectionMode().name());
        object.addProperty("renderCorrectedCrosshair", setup.renderCorrectedCrosshair());
        object.addProperty("correctBlockPlacementRay", setup.correctBlockPlacementRay());
        object.addProperty("correctEntityHitRay", setup.correctEntityHitRay());
        object.addProperty("correctCrosshairOnlyWhileLockedOn", setup.correctCrosshairOnlyWhileLockedOn());
        object.addProperty("hideVanillaCrosshair", setup.hideVanillaCrosshair());
        object.addProperty("hideVanillaCrosshairOutOfRange", setup.hideVanillaCrosshairOutOfRange());
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
        boolean autoSwitchToThirdPerson = readOptionalBoolean(object, "autoSwitchToThirdPerson", true);
        boolean allowFirstPersonWhileTargeting = readOptionalBoolean(object, "allowFirstPersonWhileTargeting", true);
        boolean allowFrontFacingThirdPersonWhileTargeting = readOptionalBoolean(object, "allowFrontFacingThirdPersonWhileTargeting", false);
        boolean showLockOnStatusMessages = readOptionalBoolean(object, "showLockOnStatusMessages", DEFAULT_SHOW_LOCK_ON_STATUS_MESSAGES);
        boolean showLockOnDebugText = readOptionalBoolean(object, "showLockOnDebugText", false);
        boolean cinematicBarsWhileLockedOn = readOptionalBoolean(
                object,
                "cinematicBarsWhileLockedOn",
                DEFAULT_CINEMATIC_BARS_WHILE_LOCKED_ON);
        boolean cinematicBarsUnderHud = readOptionalBoolean(
                object,
                "cinematicBarsUnderHud",
                DEFAULT_CINEMATIC_BARS_UNDER_HUD);
        LockOnIndicatorStyle lockOnIndicatorStyle = readOptionalLockOnIndicatorStyle(object, "lockOnIndicatorStyle", LockOnIndicatorStyle.OOT_16X);
        CameraMode cameraMode = readOptionalCameraMode(object, "cameraMode", CameraMode.DYNAMIC);
        double cameraFloatiness = clamp(
                readOptionalDouble(object, "cameraFloatiness", DEFAULT_CAMERA_FLOATINESS),
                MIN_CAMERA_FLOATINESS, MAX_CAMERA_FLOATINESS);
        double cameraDrag = clamp(
                readOptionalDouble(object, "cameraDrag", DEFAULT_CAMERA_DRAG),
                MIN_CAMERA_DRAG, MAX_CAMERA_DRAG);
        double cameraSwapSpeed = clamp(
                readOptionalDouble(object, "cameraSwapSpeed", DEFAULT_CAMERA_SWAP_SPEED),
                MIN_CAMERA_SWAP_SPEED, MAX_CAMERA_SWAP_SPEED);
        double cameraSwapSmoothness = clamp(
                readOptionalDouble(object, "cameraSwapSmoothness", DEFAULT_CAMERA_SWAP_SMOOTHNESS),
                MIN_CAMERA_SWAP_SMOOTHNESS, MAX_CAMERA_SWAP_SMOOTHNESS);
        double dynamicCameraSwapSpeed = clamp(
                readOptionalDouble(object, "dynamicCameraSwapSpeed", DEFAULT_DYNAMIC_CAMERA_SWAP_SPEED),
                MIN_DYNAMIC_CAMERA_SWAP_SPEED, MAX_DYNAMIC_CAMERA_SWAP_SPEED);
        double dynamicCameraSwapSmoothness = clamp(
                readOptionalDouble(object, "dynamicCameraSwapSmoothness", DEFAULT_DYNAMIC_CAMERA_SWAP_SMOOTHNESS),
                MIN_DYNAMIC_CAMERA_SWAP_SMOOTHNESS, MAX_DYNAMIC_CAMERA_SWAP_SMOOTHNESS);
        double cameraStepSize = clamp(
                readOptionalDouble(object, "cameraStepSize", DEFAULT_CAMERA_STEP_SIZE),
                MIN_CAMERA_STEP_SIZE,
                MAX_CAMERA_STEP_SIZE);
        boolean dynamicallyAdjustOffsets = readOptionalBoolean(object, "dynamicallyAdjustOffsets", DEFAULT_DYNAMICALLY_ADJUST_OFFSETS);
        boolean followPlayerRotations = readOptionalBoolean(object, "followPlayerRotations", DEFAULT_FOLLOW_PLAYER_ROTATIONS);
        double followPlayerRotationsDelay = clamp(
                readOptionalDouble(object, "followPlayerRotationsDelay", DEFAULT_FOLLOW_PLAYER_ROTATIONS_DELAY),
                MIN_FOLLOW_PLAYER_ROTATIONS_DELAY,
                MAX_FOLLOW_PLAYER_ROTATIONS_DELAY);
        double cameraHeadFollowResponsiveness = clamp(
                readOptionalDouble(object, "cameraHeadFollowResponsiveness", DEFAULT_CAMERA_HEAD_FOLLOW_RESPONSIVENESS),
                MIN_CAMERA_HEAD_FOLLOW_RESPONSIVENESS,
                MAX_CAMERA_HEAD_FOLLOW_RESPONSIVENESS);
        double cameraBodyFollowResponsiveness = clamp(
                readOptionalDouble(object, "cameraBodyFollowResponsiveness", DEFAULT_CAMERA_BODY_FOLLOW_RESPONSIVENESS),
                MIN_CAMERA_BODY_FOLLOW_RESPONSIVENESS,
                MAX_CAMERA_BODY_FOLLOW_RESPONSIVENESS);
        boolean fullBodyFollowEnabled = readOptionalBoolean(object, "fullBodyFollowEnabled", DEFAULT_FULL_BODY_FOLLOW_ENABLED);
        boolean dynamicShoulderAutoSwapEnabled = readOptionalBoolean(object, "dynamicShoulderAutoSwapEnabled", DEFAULT_DYNAMIC_SHOULDER_AUTO_SWAP_ENABLED);
        double dynamicShoulderSwitchThreshold = clamp(
                readOptionalDouble(object, "dynamicShoulderSwitchThreshold", DEFAULT_DYNAMIC_SHOULDER_SWITCH_THRESHOLD),
                MIN_DYNAMIC_SHOULDER_SWITCH_THRESHOLD,
                MAX_DYNAMIC_SHOULDER_SWITCH_THRESHOLD);
        double dynamicShoulderVisibilityWeight = clamp(
                readOptionalDouble(object, "dynamicShoulderVisibilityWeight", DEFAULT_DYNAMIC_SHOULDER_VISIBILITY_WEIGHT),
                MIN_DYNAMIC_SHOULDER_VISIBILITY_WEIGHT,
                MAX_DYNAMIC_SHOULDER_VISIBILITY_WEIGHT);
        double dynamicShoulderScreenPlacementWeight = clamp(
                readOptionalDouble(object, "dynamicShoulderScreenPlacementWeight", DEFAULT_DYNAMIC_SHOULDER_SCREEN_PLACEMENT_WEIGHT),
                MIN_DYNAMIC_SHOULDER_SCREEN_PLACEMENT_WEIGHT,
                MAX_DYNAMIC_SHOULDER_SCREEN_PLACEMENT_WEIGHT);
        double dynamicShoulderManualOverrideCooldownTicks = clamp(
                readOptionalDouble(object, "dynamicShoulderManualOverrideCooldownTicks", DEFAULT_DYNAMIC_SHOULDER_MANUAL_OVERRIDE_COOLDOWN_TICKS),
                MIN_DYNAMIC_SHOULDER_MANUAL_OVERRIDE_COOLDOWN_TICKS,
                MAX_DYNAMIC_SHOULDER_MANUAL_OVERRIDE_COOLDOWN_TICKS);
        boolean adjustPlayerTransparency = readOptionalBoolean(object, "adjustPlayerTransparency", DEFAULT_ADJUST_PLAYER_TRANSPARENCY);
        double playerTransparencyMinAlpha = clamp(
                readOptionalDouble(object, "playerTransparencyMinAlpha", DEFAULT_PLAYER_TRANSPARENCY_MIN_ALPHA),
                MIN_PLAYER_TRANSPARENCY_MIN_ALPHA,
                MAX_PLAYER_TRANSPARENCY_MIN_ALPHA);
        double playerTransparencyFadeSpeed = clamp(
                readOptionalDouble(object, "playerTransparencyFadeSpeed", DEFAULT_PLAYER_TRANSPARENCY_FADE_SPEED),
                MIN_PLAYER_TRANSPARENCY_FADE_SPEED,
                MAX_PLAYER_TRANSPARENCY_FADE_SPEED);
        boolean playerTransparencyWhenTargetObscuredOnly = readOptionalBoolean(
                object,
                "playerTransparencyWhenTargetObscuredOnly",
                DEFAULT_PLAYER_TRANSPARENCY_WHEN_TARGET_OBSCURED_ONLY);
        boolean playerTransparencyInPreview = readOptionalBoolean(object, "playerTransparencyInPreview", DEFAULT_PLAYER_TRANSPARENCY_IN_PREVIEW);
        CrosshairCorrectionMode crosshairCorrectionMode = readOptionalCrosshairCorrectionMode(
                object,
                "crosshairCorrectionMode",
                DEFAULT_CROSSHAIR_CORRECTION_MODE);
        boolean renderCorrectedCrosshair = readOptionalBoolean(object, "renderCorrectedCrosshair", DEFAULT_RENDER_CORRECTED_CROSSHAIR);
        boolean correctBlockPlacementRay = readOptionalBoolean(object, "correctBlockPlacementRay", DEFAULT_CORRECT_BLOCK_PLACEMENT_RAY);
        boolean correctEntityHitRay = readOptionalBoolean(object, "correctEntityHitRay", DEFAULT_CORRECT_ENTITY_HIT_RAY);
        boolean correctCrosshairOnlyWhileLockedOn = readOptionalBoolean(
                object,
                "correctCrosshairOnlyWhileLockedOn",
                DEFAULT_CORRECT_CROSSHAIR_ONLY_WHILE_LOCKED_ON);
        boolean hideVanillaCrosshair = readOptionalBoolean(object, "hideVanillaCrosshair", DEFAULT_HIDE_VANILLA_CROSSHAIR);
        boolean hideVanillaCrosshairOutOfRange = readOptionalBoolean(object, "hideVanillaCrosshairOutOfRange", DEFAULT_HIDE_VANILLA_CROSSHAIR_OUT_OF_RANGE);
        double targetSwapMouseDeadzone = clamp(
                readOptionalDouble(object, "targetSwapMouseDeadzone", DEFAULT_TARGET_SWAP_MOUSE_DEADZONE),
                MIN_TARGET_SWAP_MOUSE_DEADZONE, MAX_TARGET_SWAP_MOUSE_DEADZONE);
        double targetSwapMouseActivation = clamp(
                readOptionalDouble(object, "targetSwapMouseActivation", DEFAULT_TARGET_SWAP_MOUSE_ACTIVATION),
                MIN_TARGET_SWAP_MOUSE_ACTIVATION, MAX_TARGET_SWAP_MOUSE_ACTIVATION);
        double targetSwapDirectionThreshold = clamp(
                readOptionalDouble(object, "targetSwapDirectionThreshold", DEFAULT_TARGET_SWAP_DIRECTION_THRESHOLD),
                MIN_TARGET_SWAP_DIRECTION_THRESHOLD, MAX_TARGET_SWAP_DIRECTION_THRESHOLD);
        double targetSwapMinScreenSeparation = clamp(
                readOptionalDouble(object, "targetSwapMinScreenSeparation", DEFAULT_TARGET_SWAP_MIN_SCREEN_SEPARATION),
                MIN_TARGET_SWAP_MIN_SCREEN_SEPARATION, MAX_TARGET_SWAP_MIN_SCREEN_SEPARATION);
        double targetSwapInputDecay = clamp(
                readOptionalDouble(object, "targetSwapInputDecay", DEFAULT_TARGET_SWAP_INPUT_DECAY),
                MIN_TARGET_SWAP_INPUT_DECAY, MAX_TARGET_SWAP_INPUT_DECAY);
        double targetSwapCooldownTicks = clamp(
                readOptionalDouble(object, "targetSwapCooldownTicks", DEFAULT_TARGET_SWAP_COOLDOWN_TICKS),
                MIN_TARGET_SWAP_COOLDOWN_TICKS, MAX_TARGET_SWAP_COOLDOWN_TICKS);
        double targetSwapSmoothTicks = clamp(
                readOptionalDouble(object, "targetSwapSmoothTicks", DEFAULT_TARGET_SWAP_SMOOTH_TICKS),
                MIN_TARGET_SWAP_SMOOTH_TICKS, MAX_TARGET_SWAP_SMOOTH_TICKS);
        double targetSwapLookYawResponsiveness = clamp(
                readOptionalDouble(object, "targetSwapLookYawResponsiveness", DEFAULT_TARGET_SWAP_LOOK_RESPONSIVENESS_YAW),
                MIN_TARGET_SWAP_LOOK_RESPONSIVENESS_YAW, MAX_TARGET_SWAP_LOOK_RESPONSIVENESS_YAW);
        double targetSwapLookPitchResponsiveness = clamp(
                readOptionalDouble(object, "targetSwapLookPitchResponsiveness", DEFAULT_TARGET_SWAP_LOOK_RESPONSIVENESS_PITCH),
                MIN_TARGET_SWAP_LOOK_RESPONSIVENESS_PITCH, MAX_TARGET_SWAP_LOOK_RESPONSIVENESS_PITCH);
        double targetSwapLookMaxYawStepPerTick = clamp(
                readOptionalDouble(object, "targetSwapLookMaxYawStepPerTick", DEFAULT_TARGET_SWAP_LOOK_MAX_YAW_STEP_PER_TICK),
                MIN_TARGET_SWAP_LOOK_MAX_YAW_STEP_PER_TICK, MAX_TARGET_SWAP_LOOK_MAX_YAW_STEP_PER_TICK);
        double targetSwapLookMaxPitchStepPerTick = clamp(
                readOptionalDouble(object, "targetSwapLookMaxPitchStepPerTick", DEFAULT_TARGET_SWAP_LOOK_MAX_PITCH_STEP_PER_TICK),
                MIN_TARGET_SWAP_LOOK_MAX_PITCH_STEP_PER_TICK, MAX_TARGET_SWAP_LOOK_MAX_PITCH_STEP_PER_TICK);
        double targetSwapTargetPointResponsiveness = clamp(
                readOptionalDouble(object, "targetSwapTargetPointResponsiveness", DEFAULT_TARGET_SWAP_TARGET_POINT_RESPONSIVENESS),
                MIN_TARGET_SWAP_TARGET_POINT_RESPONSIVENESS, MAX_TARGET_SWAP_TARGET_POINT_RESPONSIVENESS);
        double targetSwapPlayerLookFollow = clamp(
                readOptionalDouble(object, "targetSwapPlayerLookFollow", DEFAULT_TARGET_SWAP_PLAYER_LOOK_FOLLOW),
                MIN_TARGET_SWAP_PLAYER_LOOK_FOLLOW, MAX_TARGET_SWAP_PLAYER_LOOK_FOLLOW);
        boolean enableTargetFilters = readOptionalBoolean(object, "enableTargetFilters", DEFAULT_TARGET_FILTERS_ENABLED);
        TargetFilterMode targetFilterMode = readOptionalTargetFilterMode(object, "targetFilterMode", TargetFilterMode.EXCLUDE);
        boolean filterPlayers = readOptionalBoolean(object, "filterPlayers", DEFAULT_FILTER_PLAYERS);
        boolean filterPassiveMobs = readOptionalBoolean(object, "filterPassiveMobs", DEFAULT_FILTER_PASSIVE_MOBS);
        boolean filterNeutralMobs = readOptionalBoolean(object, "filterNeutralMobs", DEFAULT_FILTER_NEUTRAL_MOBS);
        boolean filterHostileMobs = readOptionalBoolean(object, "filterHostileMobs", DEFAULT_FILTER_HOSTILE_MOBS);
        List<String> targetFilterEntityIdsValue = readOptionalStringList(object, "targetFilterEntityIds", DEFAULT_TARGET_FILTER_ENTITY_IDS);
        boolean useCustom = readOptionalBoolean(object, "useCustomSwappedShoulderValues", false);
        return new CameraSetupPreset(
                autoSwitchToThirdPerson,
                allowFirstPersonWhileTargeting,
                allowFrontFacingThirdPersonWhileTargeting,
                showLockOnStatusMessages,
                showLockOnDebugText,
                cinematicBarsWhileLockedOn,
                cinematicBarsUnderHud,
                lockOnIndicatorStyle,
                cameraMode,
                cameraFloatiness,
                cameraDrag,
                cameraSwapSpeed,
                cameraSwapSmoothness,
                dynamicCameraSwapSpeed,
                dynamicCameraSwapSmoothness,
                cameraStepSize,
                dynamicallyAdjustOffsets,
                followPlayerRotations,
                followPlayerRotationsDelay,
                cameraHeadFollowResponsiveness,
                cameraBodyFollowResponsiveness,
                fullBodyFollowEnabled,
                dynamicShoulderAutoSwapEnabled,
                dynamicShoulderSwitchThreshold,
                dynamicShoulderVisibilityWeight,
                dynamicShoulderScreenPlacementWeight,
                dynamicShoulderManualOverrideCooldownTicks,
                adjustPlayerTransparency,
                playerTransparencyMinAlpha,
                playerTransparencyFadeSpeed,
                playerTransparencyWhenTargetObscuredOnly,
                playerTransparencyInPreview,
                crosshairCorrectionMode,
                renderCorrectedCrosshair,
                correctBlockPlacementRay,
                correctEntityHitRay,
                correctCrosshairOnlyWhileLockedOn,
                hideVanillaCrosshair,
                hideVanillaCrosshairOutOfRange,
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
                targetFilterEntityIdsValue,
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

    // â”€â”€ JSON read helpers â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    private static double readRequiredDouble(JsonObject object, String key) {
        JsonElement element = object.get(key);
        if (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
            throw new IllegalArgumentException("Preset is missing numeric field: " + key);
        }
        return element.getAsDouble();
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

    private static CrosshairCorrectionMode readOptionalCrosshairCorrectionMode(
            JsonObject object,
            String key,
            CrosshairCorrectionMode fallback) {
        JsonElement element = object.get(key);
        if (element == null) {
            return fallback;
        }
        if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
            throw new IllegalArgumentException("Preset has non-string enum field: " + key);
        }
        try {
            return CrosshairCorrectionMode.valueOf(element.getAsString());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Preset has invalid crosshair correction mode: " + element.getAsString(), e);
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

    private static LockOnIndicatorStyle readOptionalLockOnIndicatorStyle(JsonObject object, String key, LockOnIndicatorStyle fallback) {
        JsonElement element = object.get(key);
        if (element == null) {
            return fallback;
        }
        if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
            throw new IllegalArgumentException("Preset has non-string enum field: " + key);
        }
        try {
            return LockOnIndicatorStyle.valueOf(element.getAsString());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Preset has invalid lock-on indicator style: " + element.getAsString(), e);
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

    private static double roundForCamera(double value) {
        return BigDecimal.valueOf(value).setScale(CAMERA_VALUE_SCALE, RoundingMode.HALF_UP).doubleValue();
    }

    // â”€â”€ Records â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    public record PerspectivePreset(double offsetX, double offsetY, double offsetZ, double rotation) {
        public PerspectivePreset mirroredForOppositeShoulder() {
            return new PerspectivePreset(-offsetX, offsetY, offsetZ, -rotation);
        }
    }

    public record CameraSetupPreset(
            boolean autoSwitchToThirdPerson,
            boolean allowFirstPersonWhileTargeting,
            boolean allowFrontFacingThirdPersonWhileTargeting,
            boolean showLockOnStatusMessages,
            boolean showLockOnDebugText,
            boolean cinematicBarsWhileLockedOn,
            boolean cinematicBarsUnderHud,
            LockOnIndicatorStyle lockOnIndicatorStyle,
            CameraMode cameraMode,
            double cameraFloatiness,
            double cameraDrag,
            double cameraSwapSpeed,
            double cameraSwapSmoothness,
            double dynamicCameraSwapSpeed,
            double dynamicCameraSwapSmoothness,
            double cameraStepSize,
            boolean dynamicallyAdjustOffsets,
            boolean followPlayerRotations,
            double followPlayerRotationsDelay,
            double cameraHeadFollowResponsiveness,
            double cameraBodyFollowResponsiveness,
            boolean fullBodyFollowEnabled,
            boolean dynamicShoulderAutoSwapEnabled,
            double dynamicShoulderSwitchThreshold,
            double dynamicShoulderVisibilityWeight,
            double dynamicShoulderScreenPlacementWeight,
            double dynamicShoulderManualOverrideCooldownTicks,
            boolean adjustPlayerTransparency,
            double playerTransparencyMinAlpha,
            double playerTransparencyFadeSpeed,
            boolean playerTransparencyWhenTargetObscuredOnly,
            boolean playerTransparencyInPreview,
            CrosshairCorrectionMode crosshairCorrectionMode,
            boolean renderCorrectedCrosshair,
            boolean correctBlockPlacementRay,
            boolean correctEntityHitRay,
            boolean correctCrosshairOnlyWhileLockedOn,
            boolean hideVanillaCrosshair,
            boolean hideVanillaCrosshairOutOfRange,
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
