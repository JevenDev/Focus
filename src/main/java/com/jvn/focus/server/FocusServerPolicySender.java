package com.jvn.focus.server;

import com.jvn.focus.Focus;
import com.jvn.focus.network.FocusServerPolicyPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.extensions.ICommonPacketListener;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

@EventBusSubscriber(modid = Focus.MOD_ID)
public final class FocusServerPolicySender {
    private FocusServerPolicySender() {}

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            sendTo(player);
        }
    }

    public static void onServerConfigReloaded(ModConfigEvent.Reloading event) {
        if (event.getConfig().getModId().equals(Focus.MOD_ID)
                && event.getConfig().getType() == ModConfig.Type.SERVER) {
            sendToAllPlayers();
        }
    }

    public static void sendToAllPlayers() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            sendTo(player);
        }
    }

    public static void sendTo(ServerPlayer player) {
        if (!canReceivePolicy(player)) {
            return;
        }
        PacketDistributor.sendToPlayer(player, FocusServerConfig.createPolicyPayload());
    }

    private static boolean canReceivePolicy(ServerPlayer player) {
        return ((ICommonPacketListener) player.connection).hasChannel(FocusServerPolicyPayload.TYPE);
    }
}
