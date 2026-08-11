package com.jvn.focus.client.hud;

import com.mojang.math.Axis;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public final class LockOnIndicatorAnimationUtil {
    private LockOnIndicatorAnimationUtil() {}

    static void renderOotTriangleOrbit(
            GuiGraphics guiGraphics,
            float centerX,
            float centerY,
            ResourceLocation texture,
            int drawSize,
            float animationTicks,
            int textureSize) {
        renderOrbit(guiGraphics, centerX, centerY, texture, drawSize, animationTicks, textureSize, 4, 14.0F, 6.0F, true);
    }

    public static void renderCentered(
            GuiGraphics guiGraphics,
            float centerX,
            float centerY,
            ResourceLocation texture,
            int drawSize,
            int textureSize) {
        float halfSize = drawSize * 0.5F;
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(centerX - halfSize, centerY - halfSize, 0.0F);
        guiGraphics.blit(
                texture,
                0,
                0,
                drawSize,
                drawSize,
                0.0F,
                0.0F,
                textureSize,
                textureSize,
                textureSize,
                textureSize);
        guiGraphics.pose().popPose();
    }
    public static void renderOrbit(
            GuiGraphics guiGraphics,
            float centerX,
            float centerY,
            ResourceLocation texture,
            int drawSize,
            float animationTicks,
            int textureSize,
            int markerCount,
            float orbitRadius,
            float spinSpeed,
            boolean rotateMarkers) {
        float halfSize = drawSize * 0.5F;
        float baseAngle = animationTicks * spinSpeed;

        for (int markerIndex = 0; markerIndex < markerCount; markerIndex++) {
            float markerAngleDegrees = baseAngle + markerIndex * (360.0F / markerCount) - 90.0F;
            double markerAngleRadians = Math.toRadians(markerAngleDegrees);
            float markerX = centerX + (float) (Math.cos(markerAngleRadians) * orbitRadius) - halfSize;
            float markerY = centerY + (float) (Math.sin(markerAngleRadians) * orbitRadius) - halfSize;

            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(markerX + halfSize, markerY + halfSize, 0.0F);
            if (rotateMarkers) {
                guiGraphics.pose().mulPose(Axis.ZP.rotationDegrees(markerAngleDegrees + 90.0F));
            }
            guiGraphics.pose().translate(-halfSize, -halfSize, 0.0F);
            guiGraphics.blit(texture, 0, 0, drawSize, drawSize, 0.0F, 0.0F, textureSize, textureSize, textureSize, textureSize);
            guiGraphics.pose().popPose();
        }
    }
}
