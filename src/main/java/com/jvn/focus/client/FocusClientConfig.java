package com.jvn.focus.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jvn.focus.Focus;
import java.math.BigDecimal;
import java.math.RoundingMode;

import me.fzzyhmstrs.fzzy_config.api.ConfigApiJava;
import me.fzzyhmstrs.fzzy_config.api.RegisterType;
import me.fzzyhmstrs.fzzy_config.config.ConfigAction;
import me.fzzyhmstrs.fzzy_config.config.Config;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedBoolean;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedCondition;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedDouble;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedNumber;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class FocusClientConfig extends Config {
    public static final double DEFAULT_CAMERA_OFFSET_X = -2.0D;
    public static final double DEFAULT_CAMERA_OFFSET_Y = 0.4D;
    public static final double DEFAULT_CAMERA_OFFSET_Z = 1.5D;
    public static final double DEFAULT_CAMERA_ROTATION = 0.0D;
    public static final double MIN_CAMERA_OFFSET_X = -4.0D;
    public static final double MAX_CAMERA_OFFSET_X = 4.0D;
    public static final double MIN_CAMERA_OFFSET_Y = -2.0D;
    public static final double MAX_CAMERA_OFFSET_Y = 2.5D;
    public static final double MIN_CAMERA_OFFSET_Z = 0.5D;
    public static final double MAX_CAMERA_OFFSET_Z = 8.0D;
    public static final double MIN_CAMERA_ROTATION = -180.0D;
    public static final double MAX_CAMERA_ROTATION = 180.0D;
    public static final double CAMERA_SLIDER_INCREMENT = 0.1D;
    public static final double ROTATION_SLIDER_INCREMENT = 1.0D;
    private static final int CAMERA_VALUE_SCALE = 1;

    private static FocusClientConfig INSTANCE;

    public ValidatedBoolean autoSwitchToThirdPerson = new ValidatedBoolean(true);
    public ValidatedBoolean allowFirstPersonWhileTargeting = new ValidatedBoolean(false);
    public ValidatedCondition<Boolean> allowFrontFacingThirdPersonWhileTargeting =
            new ValidatedBoolean(false).toCondition(
                    allowFirstPersonWhileTargeting,
                    Component.translatable("focus.lock_on_client.allowFrontFacingThirdPersonWhileTargeting.condition"),
                    () -> false);
    public ValidatedBoolean showLockOnDebugText = new ValidatedBoolean(false);
    public ValidatedDouble cameraOffsetX = ValidatedNumber.withIncrement(
            new ValidatedDouble(DEFAULT_CAMERA_OFFSET_X, MAX_CAMERA_OFFSET_X, MIN_CAMERA_OFFSET_X), CAMERA_SLIDER_INCREMENT);
    public ValidatedDouble cameraOffsetY = ValidatedNumber.withIncrement(
            new ValidatedDouble(DEFAULT_CAMERA_OFFSET_Y, MAX_CAMERA_OFFSET_Y, MIN_CAMERA_OFFSET_Y), CAMERA_SLIDER_INCREMENT);
    public ValidatedDouble cameraOffsetZ = ValidatedNumber.withIncrement(
            new ValidatedDouble(DEFAULT_CAMERA_OFFSET_Z, MAX_CAMERA_OFFSET_Z, MIN_CAMERA_OFFSET_Z), CAMERA_SLIDER_INCREMENT);
    public ValidatedDouble cameraRotation = ValidatedNumber.withIncrement(
            new ValidatedDouble(DEFAULT_CAMERA_ROTATION, MAX_CAMERA_ROTATION, MIN_CAMERA_ROTATION), ROTATION_SLIDER_INCREMENT);
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

    public static double cameraOffsetX() {
        return config().cameraOffsetX.get();
    }

    public static double cameraOffsetY() {
        return config().cameraOffsetY.get();
    }

    public static double cameraOffsetZ() {
        return config().cameraOffsetZ.get();
    }

    public static double cameraRotation() {
        return config().cameraRotation.get();
    }

    public static void setCameraOffsetX(double value) {
        config().cameraOffsetX.validateAndSet(clamp(roundForCamera(value), MIN_CAMERA_OFFSET_X, MAX_CAMERA_OFFSET_X));
    }

    public static void setCameraOffsetY(double value) {
        config().cameraOffsetY.validateAndSet(clamp(roundForCamera(value), MIN_CAMERA_OFFSET_Y, MAX_CAMERA_OFFSET_Y));
    }

    public static void setCameraOffsetZ(double value) {
        config().cameraOffsetZ.validateAndSet(clamp(roundForCamera(value), MIN_CAMERA_OFFSET_Z, MAX_CAMERA_OFFSET_Z));
    }

    public static void setCameraRotation(double value) {
        config().cameraRotation.validateAndSet(clamp(roundForCamera(value), MIN_CAMERA_ROTATION, MAX_CAMERA_ROTATION));
    }

    public static void resetCameraOffsetsToDefaults() {
        FocusClientConfig config = config();
        config.cameraOffsetX.validateAndSet(DEFAULT_CAMERA_OFFSET_X);
        config.cameraOffsetY.validateAndSet(DEFAULT_CAMERA_OFFSET_Y);
        config.cameraOffsetZ.validateAndSet(DEFAULT_CAMERA_OFFSET_Z);
        config.cameraRotation.validateAndSet(DEFAULT_CAMERA_ROTATION);
    }

    public static PerspectivePreset currentPreset() {
        return new PerspectivePreset(cameraOffsetX(), cameraOffsetY(), cameraOffsetZ(), cameraRotation());
    }

    public static void applyPreset(PerspectivePreset preset) {
        setCameraOffsetX(preset.offsetX());
        setCameraOffsetY(preset.offsetY());
        setCameraOffsetZ(preset.offsetZ());
        setCameraRotation(preset.rotation());
    }

    public static String serializePreset(PerspectivePreset preset) {
        JsonObject object = new JsonObject();
        object.addProperty("offsetX", roundForPreset(preset.offsetX()));
        object.addProperty("offsetY", roundForPreset(preset.offsetY()));
        object.addProperty("offsetZ", roundForPreset(preset.offsetZ()));
        object.addProperty("rotation", roundForPreset(preset.rotation()));
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

        JsonObject object = element.getAsJsonObject();
        return new PerspectivePreset(
                readRequiredDouble(object, "offsetX"),
                readRequiredDouble(object, "offsetY"),
                readRequiredDouble(object, "offsetZ"),
                readRequiredDouble(object, "rotation"));
    }

    public static void saveConfig() {
        config().save();
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

    private static double readRequiredDouble(JsonObject object, String key) {
        JsonElement element = object.get(key);
        if (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
            throw new IllegalArgumentException("Preset is missing numeric field: " + key);
        }
        return element.getAsDouble();
    }

    private static double roundForPreset(double value) {
        return roundForCamera(value);
    }

    private static double roundForCamera(double value) {
        return BigDecimal.valueOf(value).setScale(CAMERA_VALUE_SCALE, RoundingMode.HALF_UP).doubleValue();
    }

    public record PerspectivePreset(double offsetX, double offsetY, double offsetZ, double rotation) {
    }
}
