package com.jvn.focus.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.jvn.focus.Focus;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class FocusConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve("focus-config.json5");

    private boolean autoSwitchToThirdPerson = true;
    private boolean allowFirstPersonWhileTargeting = true;
    private boolean allowFrontFacingThirdPersonWhileTargeting = false;
    private boolean showLockOnStatusMessages = false;
    private boolean showLockOnDebugText = false;
    private boolean experimentalPersistentWalkthroughBackwardsWalking = false;
    private boolean cinematicBarsWhileLockedOn = true;
    private boolean cinematicBarsUnderHud = true;
    private FocusClientConfig.LockOnIndicatorStyle lockOnIndicatorStyle = FocusClientConfig.LockOnIndicatorStyle.OOT_16X;
    private boolean useCustomSwappedShoulderValues = false;
    private FocusClientConfig.CameraMode cameraMode = FocusClientConfig.CameraMode.DYNAMIC;
    private double cameraSwapSmoothness = 0.9D;
    private double dynamicCameraSwapSmoothness = 0.12D;
    private double dynamicShoulderScreenPlacementWeight = 0.3D;
    private String selectedCameraProfile = "";
    private double targetSwapMouseDeadzone = 12.0D;
    private double targetSwapMouseActivation = 21.0D;
    private double targetSwapDirectionThreshold = 0.56D;
    private double targetSwapMinScreenSeparation = 0.06D;
    private double targetSwapInputDecay = 0.82D;
    private double targetSwapCooldownTicks = 8.0D;
    private double targetSwapSmoothTicks = 12.0D;
    private double targetSwapLookYawResponsiveness = 2.8D;
    private double targetSwapLookPitchResponsiveness = 2.4D;
    private double targetSwapLookMaxYawStepPerTick = 3.8D;
    private double targetSwapLookMaxPitchStepPerTick = 2.8D;
    private double targetSwapTargetPointResponsiveness = 4.2D;
    private double targetSwapPlayerLookFollow = 0.3D;
    private boolean enableTargetFilters = false;
    private FocusClientConfig.TargetFilterMode targetFilterMode = FocusClientConfig.TargetFilterMode.EXCLUDE;
    private boolean filterPlayers = true;
    private boolean filterPassiveMobs = true;
    private boolean filterNeutralMobs = false;
    private boolean filterHostileMobs = false;
    private List<String> targetFilterEntityIds = new ArrayList<>();

    public final CameraBehavior cameraBehavior = new CameraBehavior();
    public final CameraFollowing cameraFollowing = new CameraFollowing();
    public final ShoulderSwap shoulderSwap = new ShoulderSwap();
    public final PlayerTransparency playerTransparency = new PlayerTransparency();
    public final Crosshair crosshair = new Crosshair();

    public static FocusConfig createAndLoad() {
        FocusConfig config = new FocusConfig();
        config.load();
        return config;
    }

    public void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(this), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            Focus.LOGGER.warn("Failed to save Focus config to {}", CONFIG_PATH, exception);
        }
    }

    private void load() {
        if (!Files.isRegularFile(CONFIG_PATH)) {
            save();
            return;
        }

        try (Reader reader = Files.newBufferedReader(CONFIG_PATH, StandardCharsets.UTF_8)) {
            JsonReader jsonReader = new JsonReader(reader);
            jsonReader.setLenient(true);
            FocusConfig loaded = GSON.fromJson(jsonReader, FocusConfig.class);
            if (loaded != null) {
                copyFrom(loaded);
            }
        } catch (Exception exception) {
            Focus.LOGGER.warn("Failed to load Focus config from {}, using defaults", CONFIG_PATH, exception);
        }
    }

    private void copyFrom(FocusConfig other) {
        autoSwitchToThirdPerson = other.autoSwitchToThirdPerson;
        allowFirstPersonWhileTargeting = other.allowFirstPersonWhileTargeting;
        allowFrontFacingThirdPersonWhileTargeting = other.allowFrontFacingThirdPersonWhileTargeting;
        showLockOnStatusMessages = other.showLockOnStatusMessages;
        showLockOnDebugText = other.showLockOnDebugText;
        experimentalPersistentWalkthroughBackwardsWalking = other.experimentalPersistentWalkthroughBackwardsWalking;
        cinematicBarsWhileLockedOn = other.cinematicBarsWhileLockedOn;
        cinematicBarsUnderHud = other.cinematicBarsUnderHud;
        lockOnIndicatorStyle = other.lockOnIndicatorStyle == null
                ? FocusClientConfig.LockOnIndicatorStyle.OOT_16X
                : other.lockOnIndicatorStyle;
        useCustomSwappedShoulderValues = other.useCustomSwappedShoulderValues;
        cameraMode = other.cameraMode == null ? FocusClientConfig.CameraMode.DYNAMIC : other.cameraMode;
        cameraSwapSmoothness = other.cameraSwapSmoothness;
        dynamicCameraSwapSmoothness = other.dynamicCameraSwapSmoothness;
        dynamicShoulderScreenPlacementWeight = other.dynamicShoulderScreenPlacementWeight;
        selectedCameraProfile = other.selectedCameraProfile == null ? "" : other.selectedCameraProfile;
        targetSwapMouseDeadzone = other.targetSwapMouseDeadzone;
        targetSwapMouseActivation = other.targetSwapMouseActivation;
        targetSwapDirectionThreshold = other.targetSwapDirectionThreshold;
        targetSwapMinScreenSeparation = other.targetSwapMinScreenSeparation;
        targetSwapInputDecay = other.targetSwapInputDecay;
        targetSwapCooldownTicks = other.targetSwapCooldownTicks;
        targetSwapSmoothTicks = other.targetSwapSmoothTicks;
        targetSwapLookYawResponsiveness = other.targetSwapLookYawResponsiveness;
        targetSwapLookPitchResponsiveness = other.targetSwapLookPitchResponsiveness;
        targetSwapLookMaxYawStepPerTick = other.targetSwapLookMaxYawStepPerTick;
        targetSwapLookMaxPitchStepPerTick = other.targetSwapLookMaxPitchStepPerTick;
        targetSwapTargetPointResponsiveness = other.targetSwapTargetPointResponsiveness;
        targetSwapPlayerLookFollow = other.targetSwapPlayerLookFollow;
        enableTargetFilters = other.enableTargetFilters;
        targetFilterMode = other.targetFilterMode == null ? FocusClientConfig.TargetFilterMode.EXCLUDE : other.targetFilterMode;
        filterPlayers = other.filterPlayers;
        filterPassiveMobs = other.filterPassiveMobs;
        filterNeutralMobs = other.filterNeutralMobs;
        filterHostileMobs = other.filterHostileMobs;
        targetFilterEntityIds = other.targetFilterEntityIds == null
                ? new ArrayList<>()
                : new ArrayList<>(other.targetFilterEntityIds);
        cameraBehavior.copyFrom(other.cameraBehavior);
        cameraFollowing.copyFrom(other.cameraFollowing);
        shoulderSwap.copyFrom(other.shoulderSwap);
        playerTransparency.copyFrom(other.playerTransparency);
        crosshair.copyFrom(other.crosshair);
    }

    public boolean autoSwitchToThirdPerson() {
        return autoSwitchToThirdPerson;
    }

    public void autoSwitchToThirdPerson(boolean value) {
        autoSwitchToThirdPerson = value;
    }

    public boolean allowFirstPersonWhileTargeting() {
        return allowFirstPersonWhileTargeting;
    }

    public void allowFirstPersonWhileTargeting(boolean value) {
        allowFirstPersonWhileTargeting = value;
    }

    public boolean allowFrontFacingThirdPersonWhileTargeting() {
        return allowFrontFacingThirdPersonWhileTargeting;
    }

    public void allowFrontFacingThirdPersonWhileTargeting(boolean value) {
        allowFrontFacingThirdPersonWhileTargeting = value;
    }

    public boolean showLockOnStatusMessages() {
        return showLockOnStatusMessages;
    }

    public void showLockOnStatusMessages(boolean value) {
        showLockOnStatusMessages = value;
    }

    public boolean showLockOnDebugText() {
        return showLockOnDebugText;
    }

    public void showLockOnDebugText(boolean value) {
        showLockOnDebugText = value;
    }

    public boolean experimentalPersistentWalkthroughBackwardsWalking() {
        return experimentalPersistentWalkthroughBackwardsWalking;
    }

    public void experimentalPersistentWalkthroughBackwardsWalking(boolean value) {
        experimentalPersistentWalkthroughBackwardsWalking = value;
    }

    public boolean cinematicBarsWhileLockedOn() {
        return cinematicBarsWhileLockedOn;
    }

    public void cinematicBarsWhileLockedOn(boolean value) {
        cinematicBarsWhileLockedOn = value;
    }

    public boolean cinematicBarsUnderHud() {
        return cinematicBarsUnderHud;
    }

    public void cinematicBarsUnderHud(boolean value) {
        cinematicBarsUnderHud = value;
    }

    public FocusClientConfig.LockOnIndicatorStyle lockOnIndicatorStyle() {
        return lockOnIndicatorStyle;
    }

    public void lockOnIndicatorStyle(FocusClientConfig.LockOnIndicatorStyle value) {
        lockOnIndicatorStyle = value == null ? FocusClientConfig.LockOnIndicatorStyle.OOT_16X : value;
    }

    public boolean useCustomSwappedShoulderValues() {
        return useCustomSwappedShoulderValues;
    }

    public void useCustomSwappedShoulderValues(boolean value) {
        useCustomSwappedShoulderValues = value;
    }

    public FocusClientConfig.CameraMode cameraMode() {
        return cameraMode;
    }

    public void cameraMode(FocusClientConfig.CameraMode value) {
        cameraMode = value == null ? FocusClientConfig.CameraMode.DYNAMIC : value;
    }

    public double cameraSwapSmoothness() {
        return cameraSwapSmoothness;
    }

    public void cameraSwapSmoothness(double value) {
        cameraSwapSmoothness = value;
    }

    public double dynamicCameraSwapSmoothness() {
        return dynamicCameraSwapSmoothness;
    }

    public void dynamicCameraSwapSmoothness(double value) {
        dynamicCameraSwapSmoothness = value;
    }

    public String selectedCameraProfile() {
        return selectedCameraProfile;
    }

    public void selectedCameraProfile(String value) {
        selectedCameraProfile = value == null ? "" : value;
    }

    public double targetSwapMouseDeadzone() {
        return targetSwapMouseDeadzone;
    }

    public void targetSwapMouseDeadzone(double value) {
        targetSwapMouseDeadzone = value;
    }

    public double targetSwapMouseActivation() {
        return targetSwapMouseActivation;
    }

    public void targetSwapMouseActivation(double value) {
        targetSwapMouseActivation = value;
    }

    public double targetSwapDirectionThreshold() {
        return targetSwapDirectionThreshold;
    }

    public void targetSwapDirectionThreshold(double value) {
        targetSwapDirectionThreshold = value;
    }

    public double targetSwapMinScreenSeparation() {
        return targetSwapMinScreenSeparation;
    }

    public void targetSwapMinScreenSeparation(double value) {
        targetSwapMinScreenSeparation = value;
    }

    public double targetSwapInputDecay() {
        return targetSwapInputDecay;
    }

    public void targetSwapInputDecay(double value) {
        targetSwapInputDecay = value;
    }

    public double targetSwapCooldownTicks() {
        return targetSwapCooldownTicks;
    }

    public void targetSwapCooldownTicks(double value) {
        targetSwapCooldownTicks = value;
    }

    public double targetSwapSmoothTicks() {
        return targetSwapSmoothTicks;
    }

    public void targetSwapSmoothTicks(double value) {
        targetSwapSmoothTicks = value;
    }

    public double targetSwapLookYawResponsiveness() {
        return targetSwapLookYawResponsiveness;
    }

    public void targetSwapLookYawResponsiveness(double value) {
        targetSwapLookYawResponsiveness = value;
    }

    public double targetSwapLookPitchResponsiveness() {
        return targetSwapLookPitchResponsiveness;
    }

    public void targetSwapLookPitchResponsiveness(double value) {
        targetSwapLookPitchResponsiveness = value;
    }

    public double targetSwapLookMaxYawStepPerTick() {
        return targetSwapLookMaxYawStepPerTick;
    }

    public void targetSwapLookMaxYawStepPerTick(double value) {
        targetSwapLookMaxYawStepPerTick = value;
    }

    public double targetSwapLookMaxPitchStepPerTick() {
        return targetSwapLookMaxPitchStepPerTick;
    }

    public void targetSwapLookMaxPitchStepPerTick(double value) {
        targetSwapLookMaxPitchStepPerTick = value;
    }

    public double targetSwapTargetPointResponsiveness() {
        return targetSwapTargetPointResponsiveness;
    }

    public void targetSwapTargetPointResponsiveness(double value) {
        targetSwapTargetPointResponsiveness = value;
    }

    public double targetSwapPlayerLookFollow() {
        return targetSwapPlayerLookFollow;
    }

    public void targetSwapPlayerLookFollow(double value) {
        targetSwapPlayerLookFollow = value;
    }

    public boolean enableTargetFilters() {
        return enableTargetFilters;
    }

    public void enableTargetFilters(boolean value) {
        enableTargetFilters = value;
    }

    public FocusClientConfig.TargetFilterMode targetFilterMode() {
        return targetFilterMode;
    }

    public void targetFilterMode(FocusClientConfig.TargetFilterMode value) {
        targetFilterMode = value == null ? FocusClientConfig.TargetFilterMode.EXCLUDE : value;
    }

    public boolean filterPlayers() {
        return filterPlayers;
    }

    public void filterPlayers(boolean value) {
        filterPlayers = value;
    }

    public boolean filterPassiveMobs() {
        return filterPassiveMobs;
    }

    public void filterPassiveMobs(boolean value) {
        filterPassiveMobs = value;
    }

    public boolean filterNeutralMobs() {
        return filterNeutralMobs;
    }

    public void filterNeutralMobs(boolean value) {
        filterNeutralMobs = value;
    }

    public boolean filterHostileMobs() {
        return filterHostileMobs;
    }

    public void filterHostileMobs(boolean value) {
        filterHostileMobs = value;
    }

    public List<String> targetFilterEntityIds() {
        return new ArrayList<>(targetFilterEntityIds);
    }

    public void targetFilterEntityIds(List<String> value) {
        targetFilterEntityIds = value == null ? new ArrayList<>() : new ArrayList<>(value);
    }

    public static final class CameraBehavior {
        private double cameraFloatiness = 0.32D;
        private double cameraDrag = 0.88D;
        private double cameraStepSize = 0.025D;
        private boolean dynamicallyAdjustOffsets = true;
        private boolean smoothCameraTransition = true;
        private double cameraTransitionSpeed = 0.12D;

        private void copyFrom(CameraBehavior other) {
            if (other == null) {
                return;
            }
            cameraFloatiness = other.cameraFloatiness;
            cameraDrag = other.cameraDrag;
            cameraStepSize = other.cameraStepSize;
            dynamicallyAdjustOffsets = other.dynamicallyAdjustOffsets;
            smoothCameraTransition = other.smoothCameraTransition;
            cameraTransitionSpeed = other.cameraTransitionSpeed;
        }

        public double cameraFloatiness() {
            return cameraFloatiness;
        }

        public void cameraFloatiness(double value) {
            cameraFloatiness = value;
        }

        public double cameraDrag() {
            return cameraDrag;
        }

        public void cameraDrag(double value) {
            cameraDrag = value;
        }

        public double cameraStepSize() {
            return cameraStepSize;
        }

        public void cameraStepSize(double value) {
            cameraStepSize = value;
        }

        public boolean dynamicallyAdjustOffsets() {
            return dynamicallyAdjustOffsets;
        }

        public void dynamicallyAdjustOffsets(boolean value) {
            dynamicallyAdjustOffsets = value;
        }

        public boolean smoothCameraTransition() {
            return smoothCameraTransition;
        }

        public void smoothCameraTransition(boolean value) {
            smoothCameraTransition = value;
        }

        public double cameraTransitionSpeed() {
            return cameraTransitionSpeed;
        }

        public void cameraTransitionSpeed(double value) {
            cameraTransitionSpeed = value;
        }
    }

    public static final class CameraFollowing {
        private boolean followPlayerRotations = true;
        private double followPlayerRotationsDelay = 0.0D;
        private double cameraHeadFollowResponsiveness = 0.45D;
        private double cameraBodyFollowResponsiveness = 0.25D;
        private boolean fullBodyFollowEnabled = true;

        private void copyFrom(CameraFollowing other) {
            if (other == null) {
                return;
            }
            followPlayerRotations = other.followPlayerRotations;
            followPlayerRotationsDelay = other.followPlayerRotationsDelay;
            cameraHeadFollowResponsiveness = other.cameraHeadFollowResponsiveness;
            cameraBodyFollowResponsiveness = other.cameraBodyFollowResponsiveness;
            fullBodyFollowEnabled = other.fullBodyFollowEnabled;
        }

        public boolean followPlayerRotations() {
            return followPlayerRotations;
        }

        public void followPlayerRotations(boolean value) {
            followPlayerRotations = value;
        }

        public double followPlayerRotationsDelay() {
            return followPlayerRotationsDelay;
        }

        public void followPlayerRotationsDelay(double value) {
            followPlayerRotationsDelay = value;
        }

        public double cameraHeadFollowResponsiveness() {
            return cameraHeadFollowResponsiveness;
        }

        public void cameraHeadFollowResponsiveness(double value) {
            cameraHeadFollowResponsiveness = value;
        }

        public double cameraBodyFollowResponsiveness() {
            return cameraBodyFollowResponsiveness;
        }

        public void cameraBodyFollowResponsiveness(double value) {
            cameraBodyFollowResponsiveness = value;
        }

        public boolean fullBodyFollowEnabled() {
            return fullBodyFollowEnabled;
        }

        public void fullBodyFollowEnabled(boolean value) {
            fullBodyFollowEnabled = value;
        }
    }

    public static final class ShoulderSwap {
        private double cameraSwapSpeed = 0.16D;
        private double dynamicCameraSwapSpeed = 0.12D;
        private boolean dynamicShoulderAutoSwapEnabled = true;
        private double dynamicShoulderSwitchThreshold = 0.08D;
        private double dynamicShoulderVisibilityWeight = 0.7D;
        private double dynamicShoulderManualOverrideCooldownTicks = 30.0D;

        private void copyFrom(ShoulderSwap other) {
            if (other == null) {
                return;
            }
            cameraSwapSpeed = other.cameraSwapSpeed;
            dynamicCameraSwapSpeed = other.dynamicCameraSwapSpeed;
            dynamicShoulderAutoSwapEnabled = other.dynamicShoulderAutoSwapEnabled;
            dynamicShoulderSwitchThreshold = other.dynamicShoulderSwitchThreshold;
            dynamicShoulderVisibilityWeight = other.dynamicShoulderVisibilityWeight;
            dynamicShoulderManualOverrideCooldownTicks = other.dynamicShoulderManualOverrideCooldownTicks;
        }

        public double cameraSwapSpeed() {
            return cameraSwapSpeed;
        }

        public void cameraSwapSpeed(double value) {
            cameraSwapSpeed = value;
        }

        public double dynamicCameraSwapSpeed() {
            return dynamicCameraSwapSpeed;
        }

        public void dynamicCameraSwapSpeed(double value) {
            dynamicCameraSwapSpeed = value;
        }

        public boolean dynamicShoulderAutoSwapEnabled() {
            return dynamicShoulderAutoSwapEnabled;
        }

        public void dynamicShoulderAutoSwapEnabled(boolean value) {
            dynamicShoulderAutoSwapEnabled = value;
        }

        public double dynamicShoulderSwitchThreshold() {
            return dynamicShoulderSwitchThreshold;
        }

        public void dynamicShoulderSwitchThreshold(double value) {
            dynamicShoulderSwitchThreshold = value;
        }

        public double dynamicShoulderVisibilityWeight() {
            return dynamicShoulderVisibilityWeight;
        }

        public void dynamicShoulderVisibilityWeight(double value) {
            dynamicShoulderVisibilityWeight = value;
        }

        public double dynamicShoulderManualOverrideCooldownTicks() {
            return dynamicShoulderManualOverrideCooldownTicks;
        }

        public void dynamicShoulderManualOverrideCooldownTicks(double value) {
            dynamicShoulderManualOverrideCooldownTicks = value;
        }
    }

    public static final class PlayerTransparency {
        private boolean adjustPlayerTransparency = true;
        private double playerTransparencyMinAlpha = 0.25D;
        private double playerTransparencyFadeSpeed = 0.2D;
        private boolean playerTransparencyWhenTargetObscuredOnly = true;
        private boolean playerTransparencyInPreview = false;

        private void copyFrom(PlayerTransparency other) {
            if (other == null) {
                return;
            }
            adjustPlayerTransparency = other.adjustPlayerTransparency;
            playerTransparencyMinAlpha = other.playerTransparencyMinAlpha;
            playerTransparencyFadeSpeed = other.playerTransparencyFadeSpeed;
            playerTransparencyWhenTargetObscuredOnly = other.playerTransparencyWhenTargetObscuredOnly;
            playerTransparencyInPreview = other.playerTransparencyInPreview;
        }

        public boolean adjustPlayerTransparency() {
            return adjustPlayerTransparency;
        }

        public void adjustPlayerTransparency(boolean value) {
            adjustPlayerTransparency = value;
        }

        public double playerTransparencyMinAlpha() {
            return playerTransparencyMinAlpha;
        }

        public void playerTransparencyMinAlpha(double value) {
            playerTransparencyMinAlpha = value;
        }

        public double playerTransparencyFadeSpeed() {
            return playerTransparencyFadeSpeed;
        }

        public void playerTransparencyFadeSpeed(double value) {
            playerTransparencyFadeSpeed = value;
        }

        public boolean playerTransparencyWhenTargetObscuredOnly() {
            return playerTransparencyWhenTargetObscuredOnly;
        }

        public void playerTransparencyWhenTargetObscuredOnly(boolean value) {
            playerTransparencyWhenTargetObscuredOnly = value;
        }

        public boolean playerTransparencyInPreview() {
            return playerTransparencyInPreview;
        }

        public void playerTransparencyInPreview(boolean value) {
            playerTransparencyInPreview = value;
        }
    }

    public static final class Crosshair {
        private FocusClientConfig.CrosshairCorrectionMode crosshairCorrectionMode = FocusClientConfig.CrosshairCorrectionMode.HYBRID;
        private boolean renderCorrectedCrosshair = false;
        private boolean correctBlockPlacementRay = true;
        private boolean correctEntityHitRay = true;
        private boolean correctCrosshairOnlyWhileLockedOn = false;
        private boolean hideVanillaCrosshair = true;
        private boolean hideVanillaCrosshairOutOfRange = false;

        private void copyFrom(Crosshair other) {
            if (other == null) {
                return;
            }
            crosshairCorrectionMode = other.crosshairCorrectionMode == null
                    ? FocusClientConfig.CrosshairCorrectionMode.HYBRID
                    : other.crosshairCorrectionMode;
            renderCorrectedCrosshair = other.renderCorrectedCrosshair;
            correctBlockPlacementRay = other.correctBlockPlacementRay;
            correctEntityHitRay = other.correctEntityHitRay;
            correctCrosshairOnlyWhileLockedOn = other.correctCrosshairOnlyWhileLockedOn;
            hideVanillaCrosshair = other.hideVanillaCrosshair;
            hideVanillaCrosshairOutOfRange = other.hideVanillaCrosshairOutOfRange;
        }

        public FocusClientConfig.CrosshairCorrectionMode crosshairCorrectionMode() {
            return crosshairCorrectionMode;
        }

        public void crosshairCorrectionMode(FocusClientConfig.CrosshairCorrectionMode value) {
            crosshairCorrectionMode = value == null ? FocusClientConfig.CrosshairCorrectionMode.HYBRID : value;
        }

        public boolean renderCorrectedCrosshair() {
            return renderCorrectedCrosshair;
        }

        public void renderCorrectedCrosshair(boolean value) {
            renderCorrectedCrosshair = value;
        }

        public boolean correctBlockPlacementRay() {
            return correctBlockPlacementRay;
        }

        public void correctBlockPlacementRay(boolean value) {
            correctBlockPlacementRay = value;
        }

        public boolean correctEntityHitRay() {
            return correctEntityHitRay;
        }

        public void correctEntityHitRay(boolean value) {
            correctEntityHitRay = value;
        }

        public boolean correctCrosshairOnlyWhileLockedOn() {
            return correctCrosshairOnlyWhileLockedOn;
        }

        public void correctCrosshairOnlyWhileLockedOn(boolean value) {
            correctCrosshairOnlyWhileLockedOn = value;
        }

        public boolean hideVanillaCrosshair() {
            return hideVanillaCrosshair;
        }

        public void hideVanillaCrosshair(boolean value) {
            hideVanillaCrosshair = value;
        }

        public boolean hideVanillaCrosshairOutOfRange() {
            return hideVanillaCrosshairOutOfRange;
        }

        public void hideVanillaCrosshairOutOfRange(boolean value) {
            hideVanillaCrosshairOutOfRange = value;
        }
    }
}
