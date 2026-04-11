package com.jvn.focus.mixin;

import com.jvn.focus.client.LockOnHandler;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
    @Shadow
    private double accumulatedDX;

    @Shadow
    private double accumulatedDY;

    @Inject(method = "turnPlayer", at = @At("HEAD"))
    private void focus$captureMouseMovementForTargetSwap(CallbackInfo ci) {
        LockOnHandler.onRawMouseInput(this.accumulatedDX, this.accumulatedDY);
        if (LockOnHandler.getLockedTarget() != null) {
            // Keep raw delta capture for directional swaps, but block vanilla camera turning while locked on.
            this.accumulatedDX = 0.0D;
            this.accumulatedDY = 0.0D;
        }
    }
}
