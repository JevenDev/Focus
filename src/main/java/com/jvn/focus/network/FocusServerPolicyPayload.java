package com.jvn.focus.network;

import com.jvn.focus.Focus;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record FocusServerPolicyPayload(
        int forcedBooleanMask,
        int booleanValueMask,
        String targetFilterModeOverride,
        boolean overrideTargetFilterEntityIds,
        List<String> targetFilterEntityIds,
        String crosshairCorrectionModeOverride) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<FocusServerPolicyPayload> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(Focus.MOD_ID, "server_policy"));
    public static final StreamCodec<RegistryFriendlyByteBuf, FocusServerPolicyPayload> STREAM_CODEC =
            StreamCodec.ofMember(FocusServerPolicyPayload::write, FocusServerPolicyPayload::read);
    public static final String NO_MODE_OVERRIDE = "";

    private static final FocusServerPolicyPayload EMPTY = new FocusServerPolicyPayload(
            0,
            0,
            NO_MODE_OVERRIDE,
            false,
            List.of(),
            NO_MODE_OVERRIDE);

    public FocusServerPolicyPayload {
        targetFilterModeOverride = targetFilterModeOverride == null ? NO_MODE_OVERRIDE : targetFilterModeOverride;
        crosshairCorrectionModeOverride = crosshairCorrectionModeOverride == null ? NO_MODE_OVERRIDE : crosshairCorrectionModeOverride;
        targetFilterEntityIds = targetFilterEntityIds == null ? List.of() : List.copyOf(targetFilterEntityIds);
    }

    public static FocusServerPolicyPayload empty() {
        return EMPTY;
    }

    public boolean hasBooleanOverride(BooleanSetting setting) {
        return (forcedBooleanMask & setting.mask()) != 0;
    }

    public boolean booleanValue(BooleanSetting setting, boolean fallback) {
        if (!hasBooleanOverride(setting)) {
            return fallback;
        }
        return (booleanValueMask & setting.mask()) != 0;
    }

    public boolean lockOnAllowed() {
        return booleanValue(BooleanSetting.LOCK_ON_ALLOWED, true);
    }

    public boolean hasTargetFilterModeOverride() {
        return !targetFilterModeOverride.isBlank();
    }

    public boolean hasCrosshairCorrectionModeOverride() {
        return !crosshairCorrectionModeOverride.isBlank();
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeVarInt(forcedBooleanMask);
        buffer.writeVarInt(booleanValueMask);
        buffer.writeUtf(targetFilterModeOverride);
        buffer.writeBoolean(overrideTargetFilterEntityIds);
        buffer.writeCollection(targetFilterEntityIds, (buf, value) -> buf.writeUtf(value));
        buffer.writeUtf(crosshairCorrectionModeOverride);
    }

    private static FocusServerPolicyPayload read(RegistryFriendlyByteBuf buffer) {
        int forcedBooleanMask = buffer.readVarInt();
        int booleanValueMask = buffer.readVarInt();
        String targetFilterModeOverride = buffer.readUtf();
        boolean overrideTargetFilterEntityIds = buffer.readBoolean();
        List<String> targetFilterEntityIds = buffer.readList(buf -> buf.readUtf(256));
        String crosshairCorrectionModeOverride = buffer.readUtf();
        return new FocusServerPolicyPayload(
                forcedBooleanMask,
                booleanValueMask,
                targetFilterModeOverride,
                overrideTargetFilterEntityIds,
                targetFilterEntityIds,
                crosshairCorrectionModeOverride);
    }

    public enum BooleanSetting {
        LOCK_ON_ALLOWED,
        AUTO_SWITCH_TO_THIRD_PERSON,
        ALLOW_FIRST_PERSON_WHILE_TARGETING,
        ALLOW_FRONT_FACING_THIRD_PERSON_WHILE_TARGETING,
        ENABLE_TARGET_FILTERS,
        FILTER_PLAYERS,
        FILTER_PASSIVE_MOBS,
        FILTER_NEUTRAL_MOBS,
        FILTER_HOSTILE_MOBS,
        RENDER_CORRECTED_CROSSHAIR,
        CORRECT_BLOCK_PLACEMENT_RAY,
        CORRECT_ENTITY_HIT_RAY,
        CORRECT_CROSSHAIR_ONLY_WHILE_LOCKED_ON,
        HIDE_VANILLA_CROSSHAIR,
        HIDE_VANILLA_CROSSHAIR_OUT_OF_RANGE;

        public int mask() {
            return 1 << ordinal();
        }
    }
}
