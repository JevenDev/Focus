package com.jvn.focus.client.compat;

import com.jvn.focus.Focus;
import com.jvn.focus.client.LockOnHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.annotation.Nullable;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.fml.ModList;

/**
 * Shoulder Surfing Reloaded bridge used for state checks and camera alignment while Focus owns the
 * lock-on camera.
 */
public final class FocusShoulderSurfingCompat {
    private static final String SSR_MOD_ID = "shouldersurfing";
    @Nullable
    private static final Bridge BRIDGE = createBridge();

    private FocusShoulderSurfingCompat() {}

    public static boolean isControllingShoulderSurfing() {
        return LockOnHandler.shouldSuppressVanillaMouseTurn()
                && BRIDGE != null
                && BRIDGE.isShoulderSurfingActive();
    }

    public static void syncCameraRotation(float yaw, float pitch) {
        if (BRIDGE != null) {
            BRIDGE.syncCameraRotation(yaw, pitch);
        }
    }

    public static void syncCameraToPlayer(@Nullable LocalPlayer player) {
        if (player != null) {
            syncCameraRotation(player.getYRot(), player.getXRot());
        }
    }

    @Nullable
    private static Bridge createBridge() {
        if (!ModList.get().isLoaded(SSR_MOD_ID)) {
            return null;
        }

        try {
            Class<?> implClass = Class.forName("com.github.exopandora.shouldersurfing.client.ShoulderSurfingImpl");
            Method getInstance = implClass.getMethod("getInstance");
            Method isShoulderSurfing = implClass.getMethod("isShoulderSurfing");
            Method getCamera = implClass.getMethod("getCamera");

            Class<?> cameraClass = Class.forName("com.github.exopandora.shouldersurfing.client.ShoulderSurfingCamera");
            Method setXRot = cameraClass.getMethod("setXRot", float.class);
            Method setYRot = cameraClass.getMethod("setYRot", float.class);

            return new Bridge(getInstance, isShoulderSurfing, getCamera, setXRot, setYRot);
        } catch (ClassNotFoundException | NoSuchMethodException exception) {
            Focus.LOGGER.warn("Failed to initialize Shoulder Surfing Reloaded compatibility bridge", exception);
            return null;
        }
    }

    private static final class Bridge {
        private final Method getInstance;
        private final Method isShoulderSurfing;
        private final Method getCamera;
        private final Method setXRot;
        private final Method setYRot;
        private boolean failed;

        private Bridge(
                Method getInstance,
                Method isShoulderSurfing,
                Method getCamera,
                Method setXRot,
                Method setYRot) {
            this.getInstance = getInstance;
            this.isShoulderSurfing = isShoulderSurfing;
            this.getCamera = getCamera;
            this.setXRot = setXRot;
            this.setYRot = setYRot;
        }

        private void syncCameraRotation(float yaw, float pitch) {
            if (failed) {
                return;
            }

            try {
                Object instance = getInstance.invoke(null);
                if (!((boolean) isShoulderSurfing.invoke(instance))) {
                    return;
                }
                Object camera = getCamera.invoke(instance);
                setYRot.invoke(camera, yaw);
                setXRot.invoke(camera, pitch);
            } catch (IllegalAccessException | InvocationTargetException exception) {
                failed = true;
                Focus.LOGGER.warn("Disabling Shoulder Surfing Reloaded compatibility after reflection failure", exception);
            }
        }

        private boolean isShoulderSurfingActive() {
            if (failed) {
                return false;
            }

            try {
                Object instance = getInstance.invoke(null);
                return (boolean) isShoulderSurfing.invoke(instance);
            } catch (IllegalAccessException | InvocationTargetException exception) {
                failed = true;
                Focus.LOGGER.warn("Disabling Shoulder Surfing Reloaded compatibility after reflection failure", exception);
                return false;
            }
        }
    }
}
