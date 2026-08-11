package com.jvn.focus.client;

import com.jvn.focus.Focus;
import com.jvn.focus.client.camera.FocusCameraController;
import com.jvn.focus.client.camera.FocusCameraPose;
import com.jvn.focus.client.compat.FocusShoulderSurfingCompat;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.CalculateDetachedCameraDistanceEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

@EventBusSubscriber(modid = Focus.MOD_ID, value = Dist.CLIENT)
public final class LockOnHandler {
    private static final int OCCLUDED_GRACE_TICKS = 15;
    private static final int OUT_OF_RANGE_GRACE_TICKS = 10;
    private static final double CONTROLIFY_TARGET_SWAP_INPUT_SCALE = 3.0D;
    private static final double MIDNIGHT_CONTROLS_TARGET_SWAP_INPUT_SCALE = 8.0D;

    private static final FocusCameraController CAMERA_CONTROLLER = FocusCameraController.getInstance();
    private static final FocusTargetSwapInput TARGET_SWAP_INPUT = new FocusTargetSwapInput();

    private static LivingEntity lockedTarget;
    private static CameraType previousCameraType;
    private static int occlusionGraceTicks;
    private static int outOfRangeGraceTicks;
    private static CameraType lockOnPreferredCameraType;
    private static CameraType lastEnforcedCameraType;

    private LockOnHandler() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.level == null) {
            resetForWorldUnload();
            return;
        }

        if (!FocusClientConfig.lockOnAllowed() && lockedTarget != null) {
            endLock(minecraft);
            showLockOnBlockedByServerMessage(player);
        }
        while (FocusKeyMappings.LOCK_ON.consumeClick()) {
            handleLockOnInput(minecraft, player);
        }
        while (FocusKeyMappings.SWAP_SHOULDER.consumeClick()) {
            handleSwapShoulderInput(minecraft, player);
        }
        handleOpenCameraEditorInput(minecraft);
        handleOpenIndicatorEditorInput();
        boolean previewOrbitActive = lockedTarget == null && isCameraEditorPreviewActive();
        CAMERA_CONTROLLER.setPreviewOrbitActive(previewOrbitActive);
        handleCameraAdjustmentInput(player);

        updateLockOnState(player, minecraft);
        CAMERA_CONTROLLER.updatePlayerVisibility(player, lockedTarget, 1.0F);
        updateTargetSwapInput();
        CAMERA_CONTROLLER.onClientTick(lockedTarget != null);
        if (lockedTarget != null && !previewOrbitActive) {
            tryDirectionalTargetSwap(player);
        } else {
            resetTargetSwapInput();
        }
        enforceCameraType(minecraft);
    }

    private static void updateLockOnState(LocalPlayer player, Minecraft minecraft) {
        if (lockedTarget == null) {
            return;
        }

        if (!lockedTarget.isAlive()) {
            LivingEntity replacementTarget = FocusTargetSelector.findReplacementTarget(player, lockedTarget);
            if (replacementTarget != null) {
                resetTargetSwapInput();
                switchTarget(player, replacementTarget);
                occlusionGraceTicks = 0;
                outOfRangeGraceTicks = 0;
            } else {
                unlockWithMessage(player, minecraft, "message.focus.lock_on.lost");
            }
            return;
        }

        if (FocusTargetSelector.isLockOnHiddenFromPlayer(player, lockedTarget) || !FocusTargetSelector.isTargetAllowed(lockedTarget)) {
            unlockWithMessage(player, minecraft, "message.focus.lock_on.lost");
            return;
        }

        updateGraceTimers(player, minecraft);
    }

    private static void updateGraceTimers(LocalPlayer player, Minecraft minecraft) {
        boolean obstructed = !FocusTargetSelector.hasTargetingSight(player, lockedTarget);
        double distanceSqr = player.distanceToSqr(lockedTarget);
        double maxDistanceSqr = obstructed
                ? FocusTargetSelector.OCCLUDED_LOCK_DISTANCE_SQR
                : FocusTargetSelector.MAX_LOCK_DISTANCE_SQR;

        outOfRangeGraceTicks = distanceSqr > maxDistanceSqr ? outOfRangeGraceTicks + 1 : 0;
        occlusionGraceTicks = obstructed ? occlusionGraceTicks + 1 : 0;

        if (outOfRangeGraceTicks > OUT_OF_RANGE_GRACE_TICKS) {
            unlockWithMessage(player, minecraft, obstructed ? "message.focus.lock_on.obstructed" : "message.focus.lock_on.lost");
        } else if (occlusionGraceTicks > OCCLUDED_GRACE_TICKS) {
            unlockWithMessage(player, minecraft, "message.focus.lock_on.obstructed");
        }
    }

    private static void unlockWithMessage(LocalPlayer player, Minecraft minecraft, String messageKey) {
        endLock(minecraft);
        showLockOnStatusMessage(player, Component.translatable(messageKey));
    }

    private static void updateTargetSwapInput() {
        TARGET_SWAP_INPUT.tick(FocusClientConfig.targetSwapMouseDeadzone());
    }

    private static void enforceCameraType(Minecraft minecraft) {
        if (lockedTarget != null) {
            CameraType cameraType = minecraft.options.getCameraType();
            boolean userChangedPerspective = lastEnforcedCameraType != null && cameraType != lastEnforcedCameraType;
            boolean allowFirstPerson = FocusClientConfig.allowFirstPersonWhileTargeting();
            boolean allowFrontFacing = FocusClientConfig.allowFrontFacingThirdPersonWhileTargeting();
            if (cameraType.isFirstPerson() && !allowFirstPerson) {
                minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK);
            } else if (cameraType == CameraType.THIRD_PERSON_FRONT && !allowFrontFacing) {
                minecraft.options.setCameraType(allowFirstPerson ? CameraType.FIRST_PERSON : CameraType.THIRD_PERSON_BACK);
            }
            CameraType enforcedType = minecraft.options.getCameraType();
            if (userChangedPerspective) {
                lockOnPreferredCameraType = enforcedType;
            }
            lastEnforcedCameraType = enforcedType;
        } else if (isCameraEditorPreviewActive() && minecraft.options.getCameraType() != CameraType.THIRD_PERSON_BACK) {
            minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK);
        }
    }

    @SubscribeEvent
    public static void onRenderFrame(RenderFrameEvent.Pre event) {
        float deltaTicks = Math.max(0.01F, event.getPartialTick().getRealtimeDeltaTicks());
        // Always store frame timing for FPS-independent smoothing, even when not locked on.
        CAMERA_CONTROLLER.storeRenderDeltaTicks(deltaTicks);

        if (lockedTarget == null) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.level == null) {
            return;
        }

        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(true);
        CAMERA_CONTROLLER.onRenderFrame(player, lockedTarget, partialTick, deltaTicks);
    }

    @SubscribeEvent
    public static void onMovementInput(MovementInputUpdateEvent event) {
        if (lockedTarget == null) {
            return;
        }

        net.minecraft.client.player.Input input = event.getInput();
        float rawForward = input.forwardImpulse;

        // First-person movement should remain conventional: W follows the view/target.
        // The preserved walkthrough heading is specifically a back-camera combat aid.
        if (Minecraft.getInstance().options.getCameraType() != CameraType.THIRD_PERSON_BACK) {
            CAMERA_CONTROLLER.updateCloseRangeHeadingLock(false, event.getEntity().getYRot());
            return;
        }

        // Update heading lock state using RAW input (before rotation).
        // This must happen here, not in the render-frame rotation policy,
        // because our rotation below modifies forwardImpulse — if the
        // rotation policy reads the modified value it clears the lock
        // prematurely once the yaw difference exceeds 90°.
        CAMERA_CONTROLLER.updateCloseRangeHeadingLock(
                rawForward > 0.0F,
                event.getEntity().getYRot());

        if (!CAMERA_CONTROLLER.isCloseRangeHeadingLocked()) {
            return;
        }

        // Rotate the movement input vector so that "forward" moves in the locked heading
        // direction instead of toward the target. This lets setYRot face the target (so
        // strafe is correct) while W still walks straight through.
        float currentYaw = event.getEntity().getYRot();
        float lockedYaw = CAMERA_CONTROLLER.getCloseRangeLockedHeadingYaw();
        FocusMovementInputRemapper.MovementInput remapped = FocusMovementInputRemapper.rotateToHeading(
                input.forwardImpulse,
                input.leftImpulse,
                lockedYaw - currentYaw);
        input.forwardImpulse = remapped.forward();
        input.leftImpulse = remapped.left();
    }

    @SubscribeEvent
    public static void onDetachedCameraDistance(CalculateDetachedCameraDistanceEvent event) {
        if (lockedTarget != null || isCameraEditorPreviewActive()) {
            event.setDistance((float) Math.max(CAMERA_CONTROLLER.currentDetachedCameraDistance(lockedTarget != null), 4.0D));
        }
    }

    @SubscribeEvent
    public static void onClientLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        FocusClientConfig.clearServerPolicy();
    }

    private static void toggleLockOn(Minecraft minecraft, LocalPlayer player) {
        if (lockedTarget != null) {
            endLock(minecraft);
            showLockOnStatusMessage(player, Component.translatable("message.focus.lock_on.disabled"));
            return;
        }

        LivingEntity nextTarget = FocusTargetSelector.findTarget(player);
        if (nextTarget == null) {
            showLockOnStatusMessage(player, Component.translatable("message.focus.lock_on.no_target"));
            return;
        }

        lockedTarget = nextTarget;
        FocusShoulderSurfingCompat.resetDebugCounters();
        previousCameraType = minecraft.options.getCameraType();
        if (FocusClientConfig.autoSwitchToThirdPerson()) {
            CameraType currentType = previousCameraType;
            boolean allowFirstPerson = FocusClientConfig.allowFirstPersonWhileTargeting();
            boolean allowFrontFacing = FocusClientConfig.allowFrontFacingThirdPersonWhileTargeting();

            if (currentType == CameraType.THIRD_PERSON_BACK) {
                // Already in back third-person; keep it.
            } else if (currentType == CameraType.THIRD_PERSON_FRONT && allowFrontFacing) {
                // Front-facing is allowed; keep it.
            } else if (currentType == CameraType.THIRD_PERSON_FRONT) {
                // Front-facing not allowed; switch to back.
                minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK);
            } else {
                // First-person or unknown: switch to a third-person mode.
                CameraType preferred = lockOnPreferredCameraType;
                if (preferred == null || preferred.isFirstPerson()
                        || (preferred == CameraType.THIRD_PERSON_FRONT && !allowFrontFacing)) {
                    preferred = CameraType.THIRD_PERSON_BACK;
                }
                minecraft.options.setCameraType(preferred);
            }
        }
        lastEnforcedCameraType = minecraft.options.getCameraType();
        occlusionGraceTicks = 0;
        outOfRangeGraceTicks = 0;
        CAMERA_CONTROLLER.onLockStarted(player, nextTarget);
        resetTargetSwapInput();
        showLockOnStatusMessage(player, Component.translatable("message.focus.lock_on.enabled", nextTarget.getDisplayName()));
    }

    private static void endLock(Minecraft minecraft) {
        lockedTarget = null;
        if (previousCameraType != null) {
            minecraft.options.setCameraType(previousCameraType);
        }
        previousCameraType = null;
        lastEnforcedCameraType = null;
        occlusionGraceTicks = 0;
        outOfRangeGraceTicks = 0;
        CAMERA_CONTROLLER.onLockEnded();
        resetTargetSwapInput();
        FocusShoulderSurfingCompat.syncCameraToPlayer(minecraft.player);
    }

    private static void resetForWorldUnload() {
        lockedTarget = null;
        previousCameraType = null;
        lastEnforcedCameraType = null;
        lockOnPreferredCameraType = null;
        occlusionGraceTicks = 0;
        outOfRangeGraceTicks = 0;
        CAMERA_CONTROLLER.resetForWorldUnload();
        resetTargetSwapInput();
    }

    private static void switchTarget(LocalPlayer player, LivingEntity nextTarget) {
        lockedTarget = nextTarget;
        CAMERA_CONTROLLER.onTargetSwitched(player, nextTarget);
    }

    public static void onRawMouseInput(double deltaX, double deltaY) {
        boolean previewOrbit = lockedTarget == null && isCameraEditorPreviewActive();
        if (lockedTarget == null && !previewOrbit) {
            return;
        }

        if (previewOrbit) {
            float sensitivity = 0.12F;
            CAMERA_CONTROLLER.addPreviewOrbitDelta((float) deltaX * sensitivity, (float) -deltaY * sensitivity);
            return;
        }

        TARGET_SWAP_INPUT.record(deltaX, deltaY);
    }

    public static void onControlifyLookInput(float deltaX, float deltaY) {
        // Controlify reports degrees per tick (roughly 0..10), while mouse input
        // arrives as raw cursor pixels. Normalize it to the existing flick threshold.
        onRawMouseInput(
                deltaX * CONTROLIFY_TARGET_SWAP_INPUT_SCALE,
                deltaY * CONTROLIFY_TARGET_SWAP_INPUT_SCALE);
    }

    public static void onMidnightControlsLookInput(double deltaX, double deltaY) {
        // MidnightControls exposes a smaller pre-rotation impulse than either the
        // mouse or Controlify, so it needs its own normalization.
        onRawMouseInput(
                deltaX * MIDNIGHT_CONTROLS_TARGET_SWAP_INPUT_SCALE,
                deltaY * MIDNIGHT_CONTROLS_TARGET_SWAP_INPUT_SCALE);
    }

    public static void onControlifyLockOnPressed() {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.level == null) {
            return;
        }
        handleLockOnInput(minecraft, player);
    }

    public static void onControlifySwapShoulderPressed() {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.level == null) {
            return;
        }
        handleSwapShoulderInput(minecraft, player);
    }

    public static void onControlifyOpenCameraEditorPressed() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            return;
        }
        openCameraEditorScreen(minecraft);
    }

    public static LivingEntity getLockedTarget() {
        return lockedTarget;
    }

    public static boolean isLockOnActive() {
        return lockedTarget != null;
    }

    public static FocusCameraPose getActiveCameraData(LocalPlayer player, float partialTick) {
        if (player == null) {
            return null;
        }

        return CAMERA_CONTROLLER.getActiveCameraPose(player, lockedTarget, partialTick);
    }

    public static float getTargetSwapBlendToNormal() {
        return CAMERA_CONTROLLER.getTargetSwapBlendToNormal();
    }

    public static float getTargetSwapCameraPositionFactor() {
        return CAMERA_CONTROLLER.getTargetSwapCameraPositionFactor();
    }

    public static float getTargetSwapCameraRotationFactor() {
        return CAMERA_CONTROLLER.getTargetSwapCameraRotationFactor();
    }

    public static boolean isInitialLockCameraSnapActive() {
        return CAMERA_CONTROLLER.isInitialLockCameraSnapActive();
    }

    public static void startCameraEditorPreview() {
        CAMERA_CONTROLLER.startCameraEditorPreview();
    }

    public static void stopCameraEditorPreview(CameraType previousCameraType) {
        Minecraft minecraft = Minecraft.getInstance();
        CAMERA_CONTROLLER.stopCameraEditorPreview();
        if (lockedTarget == null && previousCameraType != null) {
            minecraft.options.setCameraType(previousCameraType);
        }
        FocusShoulderSurfingCompat.syncCameraToPlayer(minecraft.player);
    }

    public static boolean isCameraEditorPreviewActive() {
        return CAMERA_CONTROLLER.isCameraEditorPreviewActive();
    }

    public static boolean hasLineOfSightToLockedTarget(LocalPlayer player) {
        return lockedTarget != null && player != null && FocusTargetSelector.hasDirectSight(player, lockedTarget);
    }

    public static boolean isLockedTargetWithinHitRange(LocalPlayer player) {
        return lockedTarget != null && player != null && player.canInteractWithEntity(lockedTarget, 0.0D);
    }

    public static boolean canHitLockedTarget(LocalPlayer player) {
        return hasLineOfSightToLockedTarget(player) && isLockedTargetWithinHitRange(player);
    }

    public static FocusClientConfig.Shoulder getActiveShoulder() {
        return CAMERA_CONTROLLER.getActiveShoulder();
    }

    public static FocusClientConfig.Shoulder getDisplayedShoulder() {
        return CAMERA_CONTROLLER.getDisplayedShoulder(lockedTarget != null);
    }

    public static double getDynamicAutoCurrentBlend() {
        return CAMERA_CONTROLLER.getDynamicAutoCurrentBlend();
    }

    public static double getStaticSwapBlend() {
        return CAMERA_CONTROLLER.getStaticSwapBlend();
    }

    public static void setActiveShoulder(FocusClientConfig.Shoulder shoulder) {
        CAMERA_CONTROLLER.setActiveShoulder(shoulder);
    }

    public static FocusClientConfig.Shoulder swapShoulder(LocalPlayer player, boolean showMessage) {
        return CAMERA_CONTROLLER.swapShoulder(player, showMessage, lockedTarget != null);
    }

    public static float playerTransparencyAlpha() {
        return CAMERA_CONTROLLER.playerTransparencyAlpha();
    }

    public static boolean shouldSuppressVanillaMouseTurn() {
        return lockedTarget != null
                || isCameraEditorPreviewActive();
    }

    private static void tryDirectionalTargetSwap(LocalPlayer player) {
        FocusTargetSwapInput.Direction direction = TARGET_SWAP_INPUT.pollDirection(
                FocusClientConfig.targetSwapMouseDeadzone(),
                FocusClientConfig.targetSwapMouseActivation(),
                FocusClientConfig.targetSwapInputDecay());
        if (direction == null) {
            return;
        }

        Vec2 mouseDirection = new Vec2((float) direction.x(), (float) -direction.y());
        LivingEntity swappedTarget = FocusTargetSelector.findDirectionalTarget(
                player,
                lockedTarget,
                mouseDirection,
                Minecraft.getInstance().gameRenderer.getMainCamera());
        if (swappedTarget == null) {
            TARGET_SWAP_INPUT.dampen(0.45D);
            return;
        }

        TARGET_SWAP_INPUT.markSwapped(FocusClientConfig.targetSwapCooldownTicks());
        switchTarget(player, swappedTarget);
    }

    private static void resetTargetSwapInput() {
        TARGET_SWAP_INPUT.reset();
    }

    private static void handleOpenCameraEditorInput(Minecraft minecraft) {
        while (FocusKeyMappings.OPEN_CAMERA_EDITOR.consumeClick()) {
            openCameraEditorScreen(minecraft);
        }
    }

    private static void handleOpenIndicatorEditorInput() {
        while (FocusKeyMappings.OPEN_INDICATOR_EDITOR.consumeClick()) {
            CustomIndicatorEditorScreen.openFromCurrentScreen();
        }
    }

    private static void handleLockOnInput(Minecraft minecraft, LocalPlayer player) {
        if (!FocusClientConfig.lockOnAllowed()) {
            showLockOnBlockedByServerMessage(player);
            return;
        }
        toggleLockOn(minecraft, player);
    }

    private static void handleSwapShoulderInput(Minecraft minecraft, LocalPlayer player) {
        boolean showMessage = lockedTarget != null
                && minecraft.options.getCameraType() == CameraType.THIRD_PERSON_BACK
                && FocusClientConfig.showLockOnStatusMessages();
        swapShoulder(player, showMessage);
    }

    private static void showLockOnStatusMessage(LocalPlayer player, Component message) {
        if (player != null && FocusClientConfig.showLockOnStatusMessages()) {
            player.displayClientMessage(message, true);
        }
    }

    private static void showLockOnBlockedByServerMessage(LocalPlayer player) {
        if (player != null) {
            player.displayClientMessage(Component.translatable("message.focus.lock_on.disabled_by_server"), true);
        }
    }

    private static void openCameraEditorScreen(Minecraft minecraft) {
        if (minecraft.screen instanceof LockOnCameraEditorScreen) {
            return;
        }
        LockOnCameraEditorScreen.openFromCurrentScreen();
    }

    private static void handleCameraAdjustmentInput(LocalPlayer player) {
        if (lockedTarget == null && !isCameraEditorPreviewActive()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen instanceof LockOnCameraEditorScreen) {
            return;
        }
        if (minecraft.screen != null) {
            return;
        }

        FocusClientConfig.Shoulder shoulder = getActiveShoulder();
        boolean changed = false;
        while (FocusKeyMappings.CAMERA_LEFT.consumeClick()) {
            FocusClientConfig.adjustCameraLeft(shoulder);
            changed = true;
        }
        while (FocusKeyMappings.CAMERA_RIGHT.consumeClick()) {
            FocusClientConfig.adjustCameraRight(shoulder);
            changed = true;
        }
        while (FocusKeyMappings.CAMERA_IN.consumeClick()) {
            FocusClientConfig.adjustCameraIn(shoulder);
            changed = true;
        }
        while (FocusKeyMappings.CAMERA_OUT.consumeClick()) {
            FocusClientConfig.adjustCameraOut(shoulder);
            changed = true;
        }
        while (FocusKeyMappings.CAMERA_UP.consumeClick()) {
            FocusClientConfig.adjustCameraUp(shoulder);
            changed = true;
        }
        while (FocusKeyMappings.CAMERA_DOWN.consumeClick()) {
            FocusClientConfig.adjustCameraDown(shoulder);
            changed = true;
        }
        if (changed) {
            FocusClientConfig.saveConfig();
            if (player != null && isCameraEditorPreviewActive()) {
                player.displayClientMessage(Component.translatable("screen.focus.camera_editor.adjusted"), true);
            }
        }
    }

}
