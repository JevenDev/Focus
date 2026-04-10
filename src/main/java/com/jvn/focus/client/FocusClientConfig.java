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
    public static final double DEFAULT_CAMERA_SWAP_SPEED = 0.25D;
    public static final double DEFAULT_CAMERA_SWAP_SMOOTHNESS = 0.7D;
    public static final double DEFAULT_DYNAMIC_CAMERA_SWAP_SPEED = 0.25D;
    public static final double DEFAULT_DYNAMIC_CAMERA_SWAP_SMOOTHNESS = 0.7D;
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

    public ValidatedBoolean autoSwitchToThirdPerson = new ValidatedBoolean(true);
    public ValidatedBoolean allowFirstPersonWhileTargeting = new ValidatedBoolean(true);
    public ValidatedCondition<Boolean> allowFrontFacingThirdPersonWhileTargeting =
            new ValidatedBoolean(false).toCondition(
                    allowFirstPersonWhileTargeting,
                    Component.translatable("focus.lock_on_client.allowFrontFacingThirdPersonWhileTargeting.condition"),
                    () -> false);
    public ValidatedBoolean showLockOnDebugText = new ValidatedBoolean(false);
    public ValidatedBoolean useCustomSwappedShoulderValues = new ValidatedBoolean(false);
    public ValidatedEnum<CameraMode> cameraMode = new ValidatedEnum<>(CameraMode.STATIC, ValidatedEnum.WidgetType.CYCLING);
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
                currentPreset(Shoulder.LEFT),
                currentPreset(Shoulder.RIGHT),
                useCustomSwappedShoulderValues());
    }

    public static void applyCameraSetupPreset(CameraSetupPreset setup) {
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
        boolean useCustom = readRequiredBoolean(object, "useCustomSwappedShoulderValues");
        return new CameraSetupPreset(left, right, useCustom);
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

    private static double roundForCamera(double value) {
        return BigDecimal.valueOf(value).setScale(CAMERA_VALUE_SCALE, RoundingMode.HALF_UP).doubleValue();
    }

    public record PerspectivePreset(double offsetX, double offsetY, double offsetZ, double rotation) {
        public PerspectivePreset mirroredForOppositeShoulder() {
            return new PerspectivePreset(-offsetX, offsetY, offsetZ, -rotation);
        }
    }

    public record CameraSetupPreset(
            PerspectivePreset leftShoulder,
            PerspectivePreset rightShoulder,
            boolean useCustomSwappedShoulderValues) {
    }
}
