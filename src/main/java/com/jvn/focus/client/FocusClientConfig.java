package com.jvn.focus.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jvn.focus.Focus;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import me.fzzyhmstrs.fzzy_config.api.ConfigApiJava;
import me.fzzyhmstrs.fzzy_config.api.RegisterType;
import me.fzzyhmstrs.fzzy_config.config.ConfigAction;
import me.fzzyhmstrs.fzzy_config.config.Config;
import me.fzzyhmstrs.fzzy_config.util.EnumTranslatable;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedBoolean;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedCondition;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedEnum;
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
    private static final int CAMERA_VALUE_SCALE = 1;
    private static final Path CAMERA_PRESET_PATH = FMLPaths.CONFIGDIR.get().resolve(Focus.MOD_ID + "_lock_on_camera.json");

    private static FocusClientConfig INSTANCE;
    private static PerspectivePreset leftShoulderPreset = defaultLeftPreset();
    private static PerspectivePreset rightShoulderPreset = defaultLeftPreset().mirroredForOppositeShoulder();

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

    public ValidatedBoolean autoSwitchToThirdPerson = new ValidatedBoolean(true);
    public ValidatedBoolean allowFirstPersonWhileTargeting = new ValidatedBoolean(true);
    public ValidatedCondition<Boolean> allowFrontFacingThirdPersonWhileTargeting =
            new ValidatedBoolean(false).toCondition(
                    allowFirstPersonWhileTargeting,
                    Component.translatable("focus.lock_on_client.allowFrontFacingThirdPersonWhileTargeting.condition"),
                    () -> false);
    public ValidatedBoolean showLockOnDebugText = new ValidatedBoolean(false);
    public ValidatedEnum<LockOnIndicatorStyle> lockOnIndicatorStyle =
            new ValidatedEnum<>(LockOnIndicatorStyle.OOT_16X, ValidatedEnum.WidgetType.CYCLING);
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
    public ConfigAction openCameraPositionEditor = new ConfigAction.Builder()
            .title(() -> Component.translatable("focus.lock_on_client.openCameraPositionEditor"))
            .desc(Component.translatable("focus.lock_on_client.openCameraPositionEditor.desc"))
            .build(LockOnCameraEditorScreen::openFromCurrentScreen);

    public FocusClientConfig() {
        super(ResourceLocation.fromNamespaceAndPath(Focus.MOD_ID, "lock_on_client"));
    }

    public static void init() {
        if (INSTANCE == null) {
            INSTANCE = ConfigApiJava.registerAndLoadConfig(FocusClientConfig::new, RegisterType.CLIENT);
            loadCameraPresets();
            if (!INSTANCE.useCustomSwappedShoulderValues.get()) {
                mirrorOppositeShoulderFrom(Shoulder.LEFT);
                saveCameraPresets();
            }
        }
    }

    public static boolean autoSwitchToThirdPerson() {
        return config().autoSwitchToThirdPerson.get();
    }

    public static boolean allowFirstPersonWhileTargeting() {
        return config().allowFirstPersonWhileTargeting.get();
    }

    public static boolean allowFrontFacingThirdPersonWhileTargeting() {
        return config().allowFrontFacingThirdPersonWhileTargeting.get();
    }

    public static boolean showLockOnDebugText() {
        return config().showLockOnDebugText.get();
    }

    public static LockOnIndicatorStyle lockOnIndicatorStyle() {
        return config().lockOnIndicatorStyle.get();
    }

    public static boolean useCustomSwappedShoulderValues() {
        return config().useCustomSwappedShoulderValues.get();
    }

    public static CameraMode cameraMode() {
        return config().cameraMode.get();
    }

    public static double cameraFloatiness() {
        return config().cameraFloatiness.get();
    }

    public static double cameraDrag() {
        return config().cameraDrag.get();
    }

    public static double cameraSwapSpeed() {
        return config().cameraSwapSpeed.get();
    }

    public static double cameraSwapSmoothness() {
        return config().cameraSwapSmoothness.get();
    }

    public static double dynamicCameraSwapSpeed() {
        return config().dynamicCameraSwapSpeed.get();
    }

    public static double dynamicCameraSwapSmoothness() {
        return config().dynamicCameraSwapSmoothness.get();
    }

    public static double targetSwapMouseDeadzone() {
        return config().targetSwapMouseDeadzone.get();
    }

    public static double targetSwapMouseActivation() {
        return Math.max(targetSwapMouseDeadzone(), config().targetSwapMouseActivation.get());
    }

    public static double targetSwapDirectionThreshold() {
        return config().targetSwapDirectionThreshold.get();
    }

    public static double targetSwapMinScreenSeparation() {
        return config().targetSwapMinScreenSeparation.get();
    }

    public static double targetSwapInputDecay() {
        return config().targetSwapInputDecay.get();
    }

    public static int targetSwapCooldownTicks() {
        return (int) Math.round(config().targetSwapCooldownTicks.get());
    }

    public static int targetSwapSmoothTicks() {
        return (int) Math.round(config().targetSwapSmoothTicks.get());
    }

    public static float targetSwapLookYawResponsiveness() {
        return config().targetSwapLookYawResponsiveness.get().floatValue();
    }

    public static float targetSwapLookPitchResponsiveness() {
        return config().targetSwapLookPitchResponsiveness.get().floatValue();
    }

    public static float targetSwapLookMaxYawStepPerTick() {
        return config().targetSwapLookMaxYawStepPerTick.get().floatValue();
    }

    public static float targetSwapLookMaxPitchStepPerTick() {
        return config().targetSwapLookMaxPitchStepPerTick.get().floatValue();
    }

    public static float targetSwapTargetPointResponsiveness() {
        return config().targetSwapTargetPointResponsiveness.get().floatValue();
    }

    public static float targetSwapPlayerLookFollow() {
        return config().targetSwapPlayerLookFollow.get().floatValue();
    }

    public static void setUseCustomSwappedShoulderValues(boolean useCustomSwappedShoulderValues, Shoulder sourceShoulder) {
        config().useCustomSwappedShoulderValues.validateAndSet(useCustomSwappedShoulderValues);
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
                cameraMode(),
                cameraFloatiness(),
                cameraDrag(),
                cameraSwapSpeed(),
                cameraSwapSmoothness(),
                dynamicCameraSwapSpeed(),
                dynamicCameraSwapSmoothness(),
                config().targetSwapMouseDeadzone.get(),
                config().targetSwapMouseActivation.get(),
                config().targetSwapDirectionThreshold.get(),
                config().targetSwapMinScreenSeparation.get(),
                config().targetSwapInputDecay.get(),
                config().targetSwapCooldownTicks.get(),
                config().targetSwapSmoothTicks.get(),
                config().targetSwapLookYawResponsiveness.get(),
                config().targetSwapLookPitchResponsiveness.get(),
                config().targetSwapLookMaxYawStepPerTick.get(),
                config().targetSwapLookMaxPitchStepPerTick.get(),
                config().targetSwapTargetPointResponsiveness.get(),
                config().targetSwapPlayerLookFollow.get(),
                currentPreset(Shoulder.LEFT),
                currentPreset(Shoulder.RIGHT),
                useCustomSwappedShoulderValues());
    }

    public static void applyCameraSetupPreset(CameraSetupPreset setup) {
        config().cameraMode.validateAndSet(setup.cameraMode());
        config().cameraFloatiness.validateAndSet(setup.cameraFloatiness());
        config().cameraDrag.validateAndSet(setup.cameraDrag());
        config().cameraSwapSpeed.validateAndSet(setup.cameraSwapSpeed());
        config().cameraSwapSmoothness.validateAndSet(setup.cameraSwapSmoothness());
        config().dynamicCameraSwapSpeed.validateAndSet(setup.dynamicCameraSwapSpeed());
        config().dynamicCameraSwapSmoothness.validateAndSet(setup.dynamicCameraSwapSmoothness());
        config().targetSwapMouseDeadzone.validateAndSet(setup.targetSwapMouseDeadzone());
        config().targetSwapMouseActivation.validateAndSet(setup.targetSwapMouseActivation());
        config().targetSwapDirectionThreshold.validateAndSet(setup.targetSwapDirectionThreshold());
        config().targetSwapMinScreenSeparation.validateAndSet(setup.targetSwapMinScreenSeparation());
        config().targetSwapInputDecay.validateAndSet(setup.targetSwapInputDecay());
        config().targetSwapCooldownTicks.validateAndSet(setup.targetSwapCooldownTicks());
        config().targetSwapSmoothTicks.validateAndSet(setup.targetSwapSmoothTicks());
        config().targetSwapLookYawResponsiveness.validateAndSet(setup.targetSwapLookYawResponsiveness());
        config().targetSwapLookPitchResponsiveness.validateAndSet(setup.targetSwapLookPitchResponsiveness());
        config().targetSwapLookMaxYawStepPerTick.validateAndSet(setup.targetSwapLookMaxYawStepPerTick());
        config().targetSwapLookMaxPitchStepPerTick.validateAndSet(setup.targetSwapLookMaxPitchStepPerTick());
        config().targetSwapTargetPointResponsiveness.validateAndSet(setup.targetSwapTargetPointResponsiveness());
        config().targetSwapPlayerLookFollow.validateAndSet(setup.targetSwapPlayerLookFollow());
        if (setup.useCustomSwappedShoulderValues()) {
            config().useCustomSwappedShoulderValues.validateAndSet(true);
            setRawPreset(Shoulder.LEFT, setup.leftShoulder());
            setRawPreset(Shoulder.RIGHT, setup.rightShoulder());
        } else {
            config().useCustomSwappedShoulderValues.validateAndSet(false);
            setRawPreset(Shoulder.LEFT, setup.leftShoulder());
            mirrorOppositeShoulderFrom(Shoulder.LEFT);
        }
    }

    public static String serializePreset(PerspectivePreset preset) {
        return presetToJson(sanitizePreset(preset)).toString();
    }

    public static String serializeCameraSetup(CameraSetupPreset setup) {
        JsonObject object = new JsonObject();
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
        object.addProperty("useCustomSwappedShoulderValues", setup.useCustomSwappedShoulderValues());
        object.add("leftShoulder", presetToJson(setup.leftShoulder()));
        object.add("rightShoulder", presetToJson(setup.rightShoulder()));
        return object.toString();
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

        JsonObject object = element.getAsJsonObject();
        JsonElement leftElement = object.get("leftShoulder");
        JsonElement rightElement = object.get("rightShoulder");
        if (leftElement == null || !leftElement.isJsonObject() || rightElement == null || !rightElement.isJsonObject()) {
            throw new IllegalArgumentException("Camera setup must contain leftShoulder and rightShoulder objects.");
        }

        PerspectivePreset left = readPreset(leftElement.getAsJsonObject());
        PerspectivePreset right = readPreset(rightElement.getAsJsonObject());
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
        boolean useCustom = readRequiredBoolean(object, "useCustomSwappedShoulderValues");
        return new CameraSetupPreset(
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
                left,
                right,
                useCustom);
    }

    public static void saveConfig() {
        config().save();
        saveCameraPresets();
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
        } catch (Exception e) {
            Focus.LOGGER.warn("Failed to load camera preset file {}, using defaults", CAMERA_PRESET_PATH, e);
            leftShoulderPreset = defaultLeftPreset();
            rightShoulderPreset = leftShoulderPreset.mirroredForOppositeShoulder();
        }
    }

    private static void saveCameraPresets() {
        try {
            Files.createDirectories(CAMERA_PRESET_PATH.getParent());
            JsonObject object = new JsonObject();
            object.addProperty("format", "focus_camera_presets_v1");
            object.add("leftShoulder", presetToJson(leftShoulderPreset));
            object.add("rightShoulder", presetToJson(rightShoulderPreset));
            Files.writeString(CAMERA_PRESET_PATH, object.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            Focus.LOGGER.warn("Failed to save camera preset file {}", CAMERA_PRESET_PATH, e);
        }
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

    private static double roundForCamera(double value) {
        return BigDecimal.valueOf(value).setScale(CAMERA_VALUE_SCALE, RoundingMode.HALF_UP).doubleValue();
    }

    public record PerspectivePreset(double offsetX, double offsetY, double offsetZ, double rotation) {
        public PerspectivePreset mirroredForOppositeShoulder() {
            return new PerspectivePreset(-offsetX, offsetY, offsetZ, -rotation);
        }
    }

    public record CameraSetupPreset(
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
            PerspectivePreset leftShoulder,
            PerspectivePreset rightShoulder,
            boolean useCustomSwappedShoulderValues) {
    }
}
