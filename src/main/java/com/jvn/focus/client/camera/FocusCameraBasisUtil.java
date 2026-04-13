package com.jvn.focus.client.camera;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * Shared camera basis and rotation-to-target utilities used by the shoulder-surfing
 * camera system and the occlusion evaluator.
 */
public final class FocusCameraBasisUtil {
    private static final double EPSILON = 1.0E-6D;

    private FocusCameraBasisUtil() {}

    /**
     * Computes an orthonormal basis (look, right, up) from yaw and pitch angles.
     */
    public static CameraBasis basisFromRotation(float yaw, float pitch) {
        Vec3 look = Vec3.directionFromRotation(pitch, yaw);
        if (look.lengthSqr() < EPSILON) {
            look = new Vec3(0.0D, 0.0D, 1.0D);
        } else {
            look = look.normalize();
        }

        Vec3 right = new Vec3(0.0D, 1.0D, 0.0D).cross(look);
        if (right.lengthSqr() < EPSILON) {
            right = new Vec3(1.0D, 0.0D, 0.0D);
        } else {
            right = right.normalize();
        }

        Vec3 up = look.cross(right);
        if (up.lengthSqr() < EPSILON) {
            up = new Vec3(0.0D, 1.0D, 0.0D);
        } else {
            up = up.normalize();
        }

        return new CameraBasis(look, right, up);
    }

    /**
     * Computes yaw and pitch angles from a source position to a target position.
     * Falls back to the provided angles when the positions are nearly coincident.
     */
    public static Rotation rotationToTarget(Vec3 from, Vec3 to, float fallbackYaw, float fallbackPitch) {
        Vec3 lookVector = to.subtract(from);
        if (lookVector.lengthSqr() < EPSILON) {
            return new Rotation(fallbackYaw, fallbackPitch);
        }

        double horizontal = Math.sqrt(lookVector.x * lookVector.x + lookVector.z * lookVector.z);
        float yaw = (float) (Mth.atan2(lookVector.z, lookVector.x) * (180.0D / Math.PI)) - 90.0F;
        float pitch = (float) -(Mth.atan2(lookVector.y, horizontal) * (180.0D / Math.PI));
        return new Rotation(yaw, pitch);
    }

    public record CameraBasis(Vec3 look, Vec3 right, Vec3 up) {}

    public record Rotation(float yaw, float pitch) {}
}
