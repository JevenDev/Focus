package com.jvn.focus.network;

import com.jvn.focus.Focus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class FocusNetworking {
    private static final String NETWORK_VERSION = "1";

    private FocusNetworking() {}

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(NETWORK_VERSION).optional();
        registrar.playToClient(
                FocusServerPolicyPayload.TYPE,
                FocusServerPolicyPayload.STREAM_CODEC,
                FocusNetworking::handleServerPolicy);
    }

    private static void handleServerPolicy(FocusServerPolicyPayload payload, IPayloadContext context) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientHandler.handleServerPolicy(payload);
        } else {
            Focus.LOGGER.debug("Ignoring clientbound Focus server policy on a dedicated server");
        }
    }

    private static final class ClientHandler {
        private ClientHandler() {}

        private static void handleServerPolicy(FocusServerPolicyPayload payload) {
            com.jvn.focus.client.FocusClientNetworking.handleServerPolicy(payload);
        }
    }
}
