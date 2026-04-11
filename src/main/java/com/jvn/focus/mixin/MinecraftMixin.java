package com.jvn.focus.mixin;

import com.jvn.focus.client.MultiplayerWarningScreen;
import com.jvn.focus.client.MultiplayerWarningState;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.SafetyScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Shadow
    @Nullable
    public Screen screen;

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void focus$showMultiplayerWarningOnce(@Nullable Screen nextScreen, CallbackInfo ci) {
        if (nextScreen == null || nextScreen instanceof MultiplayerWarningScreen || MultiplayerWarningState.isAcknowledged()) {
            return;
        }
        if (!(this.screen instanceof TitleScreen)) {
            return;
        }
        if (!(nextScreen instanceof JoinMultiplayerScreen) && !(nextScreen instanceof SafetyScreen)) {
            return;
        }

        Minecraft minecraft = (Minecraft) (Object) this;
        minecraft.setScreen(new MultiplayerWarningScreen(this.screen, nextScreen));
        ci.cancel();
    }
}
