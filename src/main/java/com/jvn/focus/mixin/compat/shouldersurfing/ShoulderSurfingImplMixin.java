package com.jvn.focus.mixin.compat.shouldersurfing;

import com.jvn.focus.client.compat.FocusShoulderSurfingCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.github.exopandora.shouldersurfing.client.ShoulderSurfingImpl")
public abstract class ShoulderSurfingImplMixin {
    @Inject(method = "shouldEntityFollowCamera", at = @At("HEAD"), cancellable = true, remap = false)
    private void focus$stopShoulderSurfingFollowWhileLockOnActive(CallbackInfoReturnable<Boolean> cir) {
        if (FocusShoulderSurfingCompat.isControllingShoulderSurfing()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "shouldEntityAimAtTarget", at = @At("HEAD"), cancellable = true, remap = false)
    private void focus$stopShoulderSurfingAimAssistWhileLockOnActive(CallbackInfoReturnable<Boolean> cir) {
        if (FocusShoulderSurfingCompat.isControllingShoulderSurfing()) {
            cir.setReturnValue(false);
        }
    }
}
