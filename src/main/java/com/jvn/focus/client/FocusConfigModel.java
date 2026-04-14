package com.jvn.focus.client;

import io.wispforest.owo.config.annotation.*;
import io.wispforest.owo.config.annotation.RangeConstraint;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
@Config(name = "focus-config", wrapperName = "FocusConfig")
public class FocusConfigModel {

    // ── General ──────────────────────────────────────────────────────────────

    @SectionHeader("general")
    public boolean autoSwitchToThirdPerson = true;
    public boolean allowFirstPersonWhileTargeting = true;
    public boolean allowFrontFacingThirdPersonWhileTargeting = false;
    public boolean showLockOnDebugText = false;
    public boolean cinematicBarsWhileLockedOn = false;
    public boolean cinematicBarsUnderHud = false;
    public FocusClientConfig.LockOnIndicatorStyle lockOnIndicatorStyle = FocusClientConfig.LockOnIndicatorStyle.OOT_16X;

    // ── Camera ───────────────────────────────────────────────────────────────

    @SectionHeader("camera")
    public boolean useCustomSwappedShoulderValues = false;
    public FocusClientConfig.CameraMode cameraMode = FocusClientConfig.CameraMode.DYNAMIC;

    @Nest
    public CameraBehavior cameraBehavior = new CameraBehavior();

    @Nest
    public CameraFollowing cameraFollowing = new CameraFollowing();

    @Nest
    public ShoulderSwap shoulderSwap = new ShoulderSwap();

    @Nest
    public PlayerTransparency playerTransparency = new PlayerTransparency();

    @Nest
    public Crosshair crosshair = new Crosshair();

    // Hidden camera fields — persisted for preset compatibility, not shown in screen
    @ExcludeFromScreen
    public double cameraSwapSmoothness = 0.9;
    @ExcludeFromScreen
    public double dynamicCameraSwapSmoothness = 0.12;
    @ExcludeFromScreen
    public double dynamicShoulderScreenPlacementWeight = 0.3;
    @ExcludeFromScreen
    public String selectedCameraProfile = "";

    // ── Target Swap ──────────────────────────────────────────────────────────

    @SectionHeader("targetSwap")
    @RangeConstraint(min = 0.0, max = 60.0)
    public double targetSwapMouseDeadzone = 12.0;

    @RangeConstraint(min = 0.0, max = 80.0)
    public double targetSwapMouseActivation = 21.0;

    @RangeConstraint(min = 0.0, max = 1.0)
    public double targetSwapDirectionThreshold = 0.56;

    @RangeConstraint(min = 0.0, max = 0.5)
    public double targetSwapMinScreenSeparation = 0.06;

    @RangeConstraint(min = 0.0, max = 40.0)
    public double targetSwapCooldownTicks = 8.0;

    @RangeConstraint(min = 0.0, max = 40.0)
    public double targetSwapSmoothTicks = 12.0;

    // Hidden target swap fields
    @ExcludeFromScreen
    public double targetSwapInputDecay = 0.82;
    @ExcludeFromScreen
    public double targetSwapLookYawResponsiveness = 2.8;
    @ExcludeFromScreen
    public double targetSwapLookPitchResponsiveness = 2.4;
    @ExcludeFromScreen
    public double targetSwapLookMaxYawStepPerTick = 3.8;
    @ExcludeFromScreen
    public double targetSwapLookMaxPitchStepPerTick = 2.8;
    @ExcludeFromScreen
    public double targetSwapTargetPointResponsiveness = 4.2;
    @ExcludeFromScreen
    public double targetSwapPlayerLookFollow = 0.3;

    // ── Target Filters ───────────────────────────────────────────────────────

    @SectionHeader("targetFilters")
    public boolean enableTargetFilters = false;
    public FocusClientConfig.TargetFilterMode targetFilterMode = FocusClientConfig.TargetFilterMode.EXCLUDE;
    public boolean filterPlayers = true;
    public boolean filterPassiveMobs = true;
    public boolean filterNeutralMobs = false;
    public boolean filterHostileMobs = false;
    public List<String> targetFilterEntityIds = new ArrayList<>();

    // ── Nested config classes ────────────────────────────────────────────────

    public static class CameraBehavior {
        @RangeConstraint(min = 0.01, max = 1.0)
        public double cameraFloatiness = 0.32;

        @RangeConstraint(min = 0.0, max = 0.95)
        public double cameraDrag = 0.88;

        @RangeConstraint(min = 0.001, max = 1.0)
        public double cameraStepSize = 0.025;

        public boolean dynamicallyAdjustOffsets = true;
    }

    public static class CameraFollowing {
        public boolean followPlayerRotations = true;

        @RangeConstraint(min = 0.0, max = 40.0)
        public double followPlayerRotationsDelay = 0.0;

        @RangeConstraint(min = 0.01, max = 1.0)
        public double cameraHeadFollowResponsiveness = 0.45;

        @RangeConstraint(min = 0.01, max = 1.0)
        public double cameraBodyFollowResponsiveness = 0.25;

        public boolean fullBodyFollowEnabled = true;
    }

    public static class ShoulderSwap {
        @RangeConstraint(min = 0.01, max = 1.0)
        public double cameraSwapSpeed = 0.16;

        @RangeConstraint(min = 0.01, max = 1.0)
        public double dynamicCameraSwapSpeed = 0.12;

        public boolean dynamicShoulderAutoSwapEnabled = true;

        @RangeConstraint(min = 0.0, max = 1.0)
        public double dynamicShoulderSwitchThreshold = 0.08;

        @RangeConstraint(min = 0.0, max = 1.0)
        public double dynamicShoulderVisibilityWeight = 0.7;

        @RangeConstraint(min = 0.0, max = 200.0)
        public double dynamicShoulderManualOverrideCooldownTicks = 30.0;
    }

    public static class PlayerTransparency {
        public boolean adjustPlayerTransparency = true;

        @RangeConstraint(min = 0.0, max = 1.0)
        public double playerTransparencyMinAlpha = 0.25;

        @RangeConstraint(min = 0.01, max = 1.0)
        public double playerTransparencyFadeSpeed = 0.2;

        public boolean playerTransparencyWhenTargetObscuredOnly = true;
        public boolean playerTransparencyInPreview = false;
    }

    public static class Crosshair {
        public FocusClientConfig.CrosshairCorrectionMode crosshairCorrectionMode = FocusClientConfig.CrosshairCorrectionMode.HYBRID;
        public boolean renderCorrectedCrosshair = true;
        public boolean correctBlockPlacementRay = true;
        public boolean correctEntityHitRay = true;
        public boolean correctCrosshairOnlyWhileLockedOn = false;
        public boolean hideVanillaCrosshair = true;
        public boolean hideVanillaCrosshairOutOfRange = false;
    }
}
