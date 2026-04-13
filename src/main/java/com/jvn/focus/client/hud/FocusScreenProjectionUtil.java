package com.jvn.focus.client.hud;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.ClientHooks;
import org.joml.Vector3f;

/**
 * Shared screen-space projection utilities used by HUD overlays and the game renderer mixin.
 */
public final class FocusScreenProjectionUtil {
    private static final double MIN_DEPTH = 0.01D;

    private FocusScreenProjectionUtil() {}

    /**
     * Projects a world-space position onto GUI screen coordinates.
     *
     * @return the screen-space point, or {@code null} if the position is behind the camera or off-screen
     */
    public static ScreenPoint projectToScreen(Minecraft minecraft, Vec3 worldPos, float partialTick, int guiWidth, int guiHeight) {
        Camera camera = minecraft.gameRenderer.getMainCamera();
        Vec3 relativePos = worldPos.subtract(camera.getPosition());

        Vector3f leftVector = camera.getLeftVector();
        Vector3f upVector = camera.getUpVector();
        Vector3f lookVector = camera.getLookVector();

        double depth = dot(relativePos, lookVector);
        if (depth <= MIN_DEPTH) {
            return null;
        }

        double horizontal = -dot(relativePos, leftVector);
        double vertical = dot(relativePos, upVector);
        double configuredFov = minecraft.options.fov().get();
        @SuppressWarnings("removal")
        double fovDegrees = ClientHooks.getFieldOfView(minecraft.gameRenderer, camera, partialTick, configuredFov, true);
        double tanHalfFov = Math.tan(Math.toRadians(fovDegrees * 0.5D));
        if (tanHalfFov <= 0.0D) {
            return null;
        }

        double aspectRatio = (double) guiWidth / (double) guiHeight;
        double ndcX = horizontal / (depth * tanHalfFov * aspectRatio);
        double ndcY = vertical / (depth * tanHalfFov);
        if (Math.abs(ndcX) > 1.0D || Math.abs(ndcY) > 1.0D) {
            return null;
        }

        float screenX = (float) ((ndcX + 1.0D) * 0.5D * guiWidth);
        float screenY = (float) ((1.0D - ndcY) * 0.5D * guiHeight);
        return new ScreenPoint(screenX, screenY);
    }

    public static double dot(Vec3 vector, Vector3f other) {
        return vector.x * other.x() + vector.y * other.y() + vector.z * other.z();
    }

    public static Vec3 vectorToVec3(Vector3f vector) {
        return new Vec3(vector.x(), vector.y(), vector.z());
    }

    public record ScreenPoint(float x, float y) {}
}
