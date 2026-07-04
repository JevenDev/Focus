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
    private static final String SSR_IMPL_CLASS = "com.github.exopandora.shouldersurfing.client.ShoulderSurfingImpl";
    private static final String SSR_CLASS = "com.github.exopandora.shouldersurfing.client.ShoulderSurfing";
    @Nullable
    private static final Bridge BRIDGE = createBridge();
    private static int cameraDecoupledSuppressions;
    private static int movementInputBypasses;

    private FocusShoulderSurfingCompat() {}

    public static boolean isControllingShoulderSurfing() {
        if (!LockOnHandler.isLockOnActive()) {
            return false;
        }
        if (BRIDGE != null) {
            Boolean active = BRIDGE.isShoulderSurfingActive();
            if (active != null) {
                return active;
            }
        }
        return ModList.get().isLoaded(SSR_MOD_ID);
    }

    public static void recordCameraDecoupledSuppression() {
        cameraDecoupledSuppressions++;
    }

    public static void recordMovementInputBypass() {
        movementInputBypasses++;
    }

    public static void resetDebugCounters() {
        cameraDecoupledSuppressions = 0;
        movementInputBypasses = 0;
    }

    public static int cameraDecoupledSuppressions() {
        return cameraDecoupledSuppressions;
    }

    public static int movementInputBypasses() {
        return movementInputBypasses;
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
            Class<?> implClass = findClass(SSR_IMPL_CLASS, SSR_CLASS);
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

    private static Class<?> findClass(String... classNames) throws ClassNotFoundException {
        ClassNotFoundException lastException = null;
        for (String className : classNames) {
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException exception) {
                lastException = exception;
            }
        }
        throw lastException != null ? lastException : new ClassNotFoundException("No class names supplied");
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

        @Nullable
        private Boolean isShoulderSurfingActive() {
            if (failed) {
                return null;
            }

            try {
                Object instance = getInstance.invoke(null);
                return (boolean) isShoulderSurfing.invoke(instance);
            } catch (IllegalAccessException | InvocationTargetException exception) {
                failed = true;
                Focus.LOGGER.warn("Disabling Shoulder Surfing Reloaded compatibility after reflection failure", exception);
                return null;
            }
        }
    }
}
