package com.jvn.focus.mixin.compat.midnightcontrols;

import com.jvn.focus.client.LockOnHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "eu.midnightdust.midnightcontrols.client.MidnightInput")
public abstract class MidnightInputMixin {
    @Inject(method = "updateCamera", at = @At("HEAD"), cancellable = true, remap = false)
    private void focus$suppressMidnightControlsCamera(CallbackInfo ci) {
        if (LockOnHandler.shouldSuppressVanillaMouseTurn()) {
            ci.cancel();
        }
    }
}
