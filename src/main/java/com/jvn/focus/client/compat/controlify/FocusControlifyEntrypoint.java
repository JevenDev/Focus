package com.jvn.focus.client.compat.controlify;

import com.jvn.focus.Focus;
import com.jvn.focus.client.FocusKeyMappings;
import com.jvn.focus.client.LockOnHandler;
import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.api.bind.ControlifyBindApi;
import dev.isxander.controlify.api.bind.InputBinding;
import dev.isxander.controlify.api.bind.InputBindingSupplier;
import dev.isxander.controlify.api.entrypoint.ControlifyEntrypoint;
import dev.isxander.controlify.api.entrypoint.InitContext;
import dev.isxander.controlify.api.entrypoint.PreInitContext;
import dev.isxander.controlify.api.event.ControlifyEvents;
import dev.isxander.controlify.api.ingameinput.LookInputModifier;
import dev.isxander.controlify.bindings.BindContext;
import dev.isxander.controlify.controller.ControllerEntity;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import org.joml.Vector2f;

public final class FocusControlifyEntrypoint implements ControlifyEntrypoint {
    private static final Component FOCUS_CATEGORY = Component.translatable(FocusKeyMappings.CATEGORY);

    private static InputBindingSupplier lockOnBinding;
    private static InputBindingSupplier swapShoulderBinding;
    private static InputBindingSupplier openCameraEditorBinding;

    @Override
    public void onControlifyPreInit(PreInitContext context) {
        ControlifyBindApi bindings = context.bindings();
        lockOnBinding = registerInGameBinding(bindings, "lock_on", FocusKeyMappings.LOCK_ON);
        swapShoulderBinding = registerInGameBinding(bindings, "swap_shoulder", FocusKeyMappings.SWAP_SHOULDER);
        openCameraEditorBinding = registerInGameBinding(
                bindings,
                "open_camera_editor",
                FocusKeyMappings.OPEN_CAMERA_EDITOR);

        ControlifyEvents.ACTIVE_CONTROLLER_TICKED.register(FocusControlifyEntrypoint::handleControllerTick);
        ControlifyEvents.LOOK_INPUT_MODIFIER.register(FocusControlifyEntrypoint::handleLookInput);
    }

    @Override
    public void onControlifyInit(InitContext context) {}

    @Override
    public void onControllersDiscovered(ControlifyApi controlify) {}

    private static InputBindingSupplier registerInGameBinding(ControlifyBindApi api, String path, KeyMapping keyMapping) {
        return api.registerBinding(builder -> builder
                .id(Focus.MOD_ID, path)
                .category(FOCUS_CATEGORY)
                .allowedContexts(BindContext.IN_GAME)
                .addKeyCorrelation(keyMapping));
    }

    private static void handleControllerTick(ControlifyEvents.ControllerStateUpdate event) {
        ControllerEntity controller = event.controller();
        if (consumePress(lockOnBinding, controller)) {
            LockOnHandler.onControlifyLockOnPressed();
        }
        if (consumePress(swapShoulderBinding, controller)) {
            LockOnHandler.onControlifySwapShoulderPressed();
        }
        if (consumePress(openCameraEditorBinding, controller)) {
            LockOnHandler.onControlifyOpenCameraEditorPressed();
        }
    }

    private static boolean consumePress(InputBindingSupplier supplier, ControllerEntity controller) {
        if (supplier == null) {
            return false;
        }
        InputBinding binding = supplier.onOrNull(controller);
        return binding != null && binding.justPressed();
    }

    private static void handleLookInput(LookInputModifier event) {
        if (!LockOnHandler.shouldSuppressVanillaMouseTurn()) {
            return;
        }

        Vector2f lookInput = event.lookInput();
        LockOnHandler.onControlifyLookInput(lookInput.x, lookInput.y);
        lookInput.zero();
    }
}
