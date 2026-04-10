package com.jvn.focus.client.hud;

import com.mojang.math.Axis;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

final class LockOnIndicatorAnimationUtil {
    private static final int OOT_MARKER_COUNT = 4;
    private static final float OOT_ORBIT_RADIUS = 14.0F;
    private static final float OOT_SPIN_SPEED_DEGREES_PER_TICK = 6.0F;

    private LockOnIndicatorAnimationUtil() {}

    static void renderOotTriangleOrbit(
            GuiGraphics guiGraphics,
            float centerX,
            float centerY,
            ResourceLocation texture,
            int drawSize,
            float animationTicks,
            int textureSize) {
        float halfSize = drawSize * 0.5F;
        float baseAngle = animationTicks * OOT_SPIN_SPEED_DEGREES_PER_TICK;

        for (int markerIndex = 0; markerIndex < OOT_MARKER_COUNT; markerIndex++) {
            float markerAngleDegrees = baseAngle + markerIndex * (360.0F / OOT_MARKER_COUNT) - 90.0F;
            double markerAngleRadians = Math.toRadians(markerAngleDegrees);
            float markerX = centerX + (float) (Math.cos(markerAngleRadians) * OOT_ORBIT_RADIUS) - halfSize;
            float markerY = centerY + (float) (Math.sin(markerAngleRadians) * OOT_ORBIT_RADIUS) - halfSize;

            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(markerX + halfSize, markerY + halfSize, 0.0F);
            guiGraphics.pose().mulPose(Axis.ZP.rotationDegrees(markerAngleDegrees + 90.0F));
            guiGraphics.pose().translate(-halfSize, -halfSize, 0.0F);
            guiGraphics.blit(texture, 0, 0, drawSize, drawSize, 0.0F, 0.0F, textureSize, textureSize, textureSize, textureSize);
            guiGraphics.pose().popPose();
        }
    }
}
