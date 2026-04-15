package com.jvn.focus.client.camera;

import javax.annotation.Nullable;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public record FocusCameraTargetContext(
        LocalPlayer player,
        @Nullable LivingEntity lockedTarget,
        Vec3 targetPoint,
        float partialTick,
        float deltaTicks,
        boolean lockOnActive,
        boolean editorPreviewActive,
        boolean targetSwapActive) {}
