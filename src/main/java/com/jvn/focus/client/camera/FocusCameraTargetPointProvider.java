package com.jvn.focus.client.camera;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public interface FocusCameraTargetPointProvider {
    Vec3 resolveTargetPoint(LivingEntity target, float partialTick);
}
