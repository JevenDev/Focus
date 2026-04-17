package com.jvn.focus.mixin.compat.shouldersurfing;

import com.jvn.focus.client.compat.FocusShoulderSurfingCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "com.github.exopandora.shouldersurfing.client.InputHandler")
public abstract class ShoulderSurfingInputHandlerMixin {
    @Inject(method = "updateMovementInput", at = @At("HEAD"), cancellable = true, remap = false)
    private void focus$bypassShoulderSurfingMovementRemap(CallbackInfo ci) {
        if (FocusShoulderSurfingCompat.isControllingShoulderSurfing()) {
            ci.cancel();
        }
    }
}
