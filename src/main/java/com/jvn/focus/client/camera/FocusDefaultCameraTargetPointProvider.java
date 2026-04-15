package com.jvn.focus.client.camera;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

final class FocusDefaultCameraTargetPointProvider implements FocusCameraTargetPointProvider {
    @Override
    public Vec3 resolveTargetPoint(LivingEntity target, float partialTick) {
        return target.getPosition(partialTick).add(0.0D, target.getBbHeight() * 0.75D, 0.0D);
    }
}
