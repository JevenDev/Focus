package com.jvn.focus.client;

import com.jvn.focus.Focus;

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
    public static final double DEFAULT_CAMERA_OFFSET_X = 1.75D;
    public static final double DEFAULT_CAMERA_OFFSET_Y = 0.45D;
    public static final double DEFAULT_CAMERA_OFFSET_Z = 2.50D;
    public static final double DEFAULT_CAMERA_ROTATION = 0.0D;
    public static final double MIN_CAMERA_OFFSET_X = -4.0D;
    public static final double MAX_CAMERA_OFFSET_X = 4.0D;
    public static final double MIN_CAMERA_OFFSET_Y = -2.0D;
    public static final double MAX_CAMERA_OFFSET_Y = 2.5D;
    public static final double MIN_CAMERA_OFFSET_Z = 0.5D;
    public static final double MAX_CAMERA_OFFSET_Z = 8.0D;
    public static final double MIN_CAMERA_ROTATION = -180.0D;
    public static final double MAX_CAMERA_ROTATION = 180.0D;
    public static final double CAMERA_SLIDER_INCREMENT = 0.05D;
    public static final double ROTATION_SLIDER_INCREMENT = 1.0D;

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
        config().cameraOffsetX.validateAndSet(clamp(value, MIN_CAMERA_OFFSET_X, MAX_CAMERA_OFFSET_X));
    }

    public static void setCameraOffsetY(double value) {
        config().cameraOffsetY.validateAndSet(clamp(value, MIN_CAMERA_OFFSET_Y, MAX_CAMERA_OFFSET_Y));
    }

    public static void setCameraOffsetZ(double value) {
        config().cameraOffsetZ.validateAndSet(clamp(value, MIN_CAMERA_OFFSET_Z, MAX_CAMERA_OFFSET_Z));
    }

    public static void setCameraRotation(double value) {
        config().cameraRotation.validateAndSet(clamp(value, MIN_CAMERA_ROTATION, MAX_CAMERA_ROTATION));
    }

    public static void resetCameraOffsetsToDefaults() {
        FocusClientConfig config = config();
        config.cameraOffsetX.validateAndSet(DEFAULT_CAMERA_OFFSET_X);
        config.cameraOffsetY.validateAndSet(DEFAULT_CAMERA_OFFSET_Y);
        config.cameraOffsetZ.validateAndSet(DEFAULT_CAMERA_OFFSET_Z);
        config.cameraRotation.validateAndSet(DEFAULT_CAMERA_ROTATION);
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
}
