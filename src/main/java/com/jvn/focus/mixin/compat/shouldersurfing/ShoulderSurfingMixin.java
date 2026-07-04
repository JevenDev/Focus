package com.jvn.focus.mixin.compat.shouldersurfing;

import com.jvn.focus.client.compat.FocusShoulderSurfingCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.github.exopandora.shouldersurfing.client.ShoulderSurfing")
public abstract class ShoulderSurfingMixin {
    @Inject(method = "isCameraDecoupled", at = @At("HEAD"), cancellable = true, remap = false)
    private void focus$forceCoupledCameraWhileFocusControls(CallbackInfoReturnable<Boolean> cir) {
        if (FocusShoulderSurfingCompat.isControllingShoulderSurfing()) {
            // SSR's decoupled camera imports player head-yaw deltas; Focus drives those during lock-on.
            FocusShoulderSurfingCompat.recordCameraDecoupledSuppression();
            cir.setReturnValue(false);
        }
    }
}
