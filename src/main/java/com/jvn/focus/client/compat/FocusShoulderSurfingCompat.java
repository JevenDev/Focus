package com.jvn.focus.client.compat;

import com.jvn.focus.Focus;
import com.jvn.focus.client.LockOnHandler;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.fml.ModList;

/**
 * Optional compatibility bridge for Shoulder Surfing Reloaded.
 *
 * Focus owns camera control while locked on or previewing camera changes. During that
 * window SSR's own movement remapping and camera-decoupling logic can fight Focus and
 * produce inverted or rotated controls, so this bridge neutralizes those SSR hooks and
 * keeps SSR's internal camera rotation aligned with Focus's final pose.
 */
public final class FocusShoulderSurfingCompat {
    private static final String SSR_MOD_ID = "shouldersurfing";
    @Nullable
    private static final Bridge BRIDGE = createBridge();

    private FocusShoulderSurfingCompat() {}

    public static void initialize() {
        if (BRIDGE != null) {
            BRIDGE.installHooks();
        }
    }

    public static boolean isShoulderSurfingActive() {
        return BRIDGE != null && BRIDGE.isShoulderSurfingActive();
    }

    public static void syncCameraRotation(float yaw, float pitch) {
        if (BRIDGE != null) {
            BRIDGE.installHooks();
            BRIDGE.syncCameraRotation(yaw, pitch);
        }
    }

    public static void syncCameraToPlayer(@Nullable LocalPlayer player) {
        if (player != null) {
            syncCameraRotation(player.getYRot(), player.getXRot());
        }
    }

    public static boolean shouldNeutralizeShoulderSurfing() {
        return LockOnHandler.shouldSuppressVanillaMouseTurn();
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

            Class<?> registrarClass = Class.forName("com.github.exopandora.shouldersurfing.plugin.ShoulderSurfingRegistrar");
            Method getRegistrarInstance = registrarClass.getMethod("getInstance");
            Field playerInputCallbacks = registrarClass.getDeclaredField("playerInputCallbacks");
            Field cameraCouplingCallbacks = registrarClass.getDeclaredField("cameraCouplingCallbacks");
            playerInputCallbacks.setAccessible(true);
            cameraCouplingCallbacks.setAccessible(true);

            Class<?> playerInputCallbackClass = Class.forName("com.github.exopandora.shouldersurfing.api.callback.IPlayerInputCallback");
            Class<?> cameraCouplingCallbackClass = Class.forName("com.github.exopandora.shouldersurfing.api.callback.ICameraCouplingCallback");

            return new Bridge(
                    getInstance,
                    isShoulderSurfing,
                    getCamera,
                    setXRot,
                    setYRot,
                    getRegistrarInstance,
                    playerInputCallbacks,
                    cameraCouplingCallbacks,
                    playerInputCallbackClass,
                    cameraCouplingCallbackClass);
        } catch (ClassNotFoundException | NoSuchMethodException | NoSuchFieldException exception) {
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
        private final Method getRegistrarInstance;
        private final Field playerInputCallbacks;
        private final Field cameraCouplingCallbacks;
        private final Class<?> playerInputCallbackClass;
        private final Class<?> cameraCouplingCallbackClass;
        private final Object playerInputCallbackProxy;
        private final Object cameraCouplingCallbackProxy;
        private boolean hooksInstalled;
        private boolean failed;

        private Bridge(
                Method getInstance,
                Method isShoulderSurfing,
                Method getCamera,
                Method setXRot,
                Method setYRot,
                Method getRegistrarInstance,
                Field playerInputCallbacks,
                Field cameraCouplingCallbacks,
                Class<?> playerInputCallbackClass,
                Class<?> cameraCouplingCallbackClass) {
            this.getInstance = getInstance;
            this.isShoulderSurfing = isShoulderSurfing;
            this.getCamera = getCamera;
            this.setXRot = setXRot;
            this.setYRot = setYRot;
            this.getRegistrarInstance = getRegistrarInstance;
            this.playerInputCallbacks = playerInputCallbacks;
            this.cameraCouplingCallbacks = cameraCouplingCallbacks;
            this.playerInputCallbackClass = playerInputCallbackClass;
            this.cameraCouplingCallbackClass = cameraCouplingCallbackClass;
            this.playerInputCallbackProxy = createBooleanCallbackProxy(playerInputCallbackClass, "isForcingVanillaMovementInput");
            this.cameraCouplingCallbackProxy = createBooleanCallbackProxy(cameraCouplingCallbackClass, "isForcingCameraCoupling");
        }

        private void installHooks() {
            if (hooksInstalled || failed) {
                return;
            }

            try {
                Object registrar = getRegistrarInstance.invoke(null);
                addCallback(registrar, playerInputCallbacks, playerInputCallbackProxy);
                addCallback(registrar, cameraCouplingCallbacks, cameraCouplingCallbackProxy);
                hooksInstalled = true;
            } catch (IllegalAccessException | InvocationTargetException exception) {
                fail("Disabling Shoulder Surfing Reloaded compatibility after reflection failure", exception);
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
                fail("Disabling Shoulder Surfing Reloaded compatibility after reflection failure", exception);
                return false;
            }
        }

        private void syncCameraRotation(float yaw, float pitch) {
            if (failed || !isShoulderSurfingActive()) {
                return;
            }

            try {
                Object instance = getInstance.invoke(null);
                Object camera = getCamera.invoke(instance);
                setYRot.invoke(camera, yaw);
                setXRot.invoke(camera, pitch);
            } catch (IllegalAccessException | InvocationTargetException exception) {
                fail("Disabling Shoulder Surfing Reloaded compatibility after reflection failure", exception);
            }
        }

        @SuppressWarnings("unchecked")
        private static void addCallback(Object registrar, Field field, Object callback) throws IllegalAccessException {
            List<Object> callbacks = (List<Object>) field.get(registrar);
            if (!callbacks.contains(callback)) {
                callbacks.add(callback);
            }
        }

        private static Object createBooleanCallbackProxy(Class<?> callbackClass, String methodName) {
            return Proxy.newProxyInstance(
                    callbackClass.getClassLoader(),
                    new Class<?>[] {callbackClass},
                    (proxy, method, args) -> {
                        String name = method.getName();
                        if (name.equals(methodName)) {
                            return FocusShoulderSurfingCompat.shouldNeutralizeShoulderSurfing();
                        }
                        if (name.equals("toString")) {
                            return "FocusShoulderSurfingCompat[" + methodName + "]";
                        }
                        if (name.equals("hashCode")) {
                            return System.identityHashCode(proxy);
                        }
                        if (name.equals("equals")) {
                            return proxy == args[0];
                        }
                        return null;
                    });
        }

        private void fail(String message, ReflectiveOperationException exception) {
            failed = true;
            Focus.LOGGER.warn(message, exception);
        }
    }
}
