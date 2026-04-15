package com.jvn.focus.client.camera;

import net.minecraft.world.phys.Vec3;

public record FocusCameraPose(Vec3 targetPoint, double offsetX, double offsetY, double offsetZ, float rotationDegrees) {}
