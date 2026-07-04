package com.jvn.focus.server;

import com.jvn.focus.network.FocusServerPolicyPayload;
import com.jvn.focus.network.FocusServerPolicyPayload.BooleanSetting;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class FocusServerConfig {
    public static final ModConfigSpec SPEC;

    private static final ModConfigSpec.EnumValue<BooleanPolicy> LOCK_ON_ALLOWED;
    private static final ModConfigSpec.EnumValue<BooleanPolicy> AUTO_SWITCH_TO_THIRD_PERSON;
    private static final ModConfigSpec.EnumValue<BooleanPolicy> ALLOW_FIRST_PERSON_WHILE_TARGETING;
    private static final ModConfigSpec.EnumValue<BooleanPolicy> ALLOW_FRONT_FACING_THIRD_PERSON_WHILE_TARGETING;

    private static final ModConfigSpec.EnumValue<BooleanPolicy> ENABLE_TARGET_FILTERS;
    private static final ModConfigSpec.EnumValue<FilterModePolicy> TARGET_FILTER_MODE;
    private static final ModConfigSpec.EnumValue<BooleanPolicy> FILTER_PLAYERS;
    private static final ModConfigSpec.EnumValue<BooleanPolicy> FILTER_PASSIVE_MOBS;
    private static final ModConfigSpec.EnumValue<BooleanPolicy> FILTER_NEUTRAL_MOBS;
    private static final ModConfigSpec.EnumValue<BooleanPolicy> FILTER_HOSTILE_MOBS;
    private static final ModConfigSpec.BooleanValue OVERRIDE_TARGET_FILTER_ENTITY_IDS;
    private static final ModConfigSpec.ConfigValue<List<String>> TARGET_FILTER_ENTITY_IDS;

    private static final ModConfigSpec.EnumValue<CrosshairCorrectionModePolicy> CROSSHAIR_CORRECTION_MODE;
    private static final ModConfigSpec.EnumValue<BooleanPolicy> RENDER_CORRECTED_CROSSHAIR;
    private static final ModConfigSpec.EnumValue<BooleanPolicy> CORRECT_BLOCK_PLACEMENT_RAY;
    private static final ModConfigSpec.EnumValue<BooleanPolicy> CORRECT_ENTITY_HIT_RAY;
    private static final ModConfigSpec.EnumValue<BooleanPolicy> CORRECT_CROSSHAIR_ONLY_WHILE_LOCKED_ON;
    private static final ModConfigSpec.EnumValue<BooleanPolicy> HIDE_VANILLA_CROSSHAIR;
    private static final ModConfigSpec.EnumValue<BooleanPolicy> HIDE_VANILLA_CROSSHAIR_OUT_OF_RANGE;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment(
                "Server-side Focus policy.",
                "CLIENT leaves each player's local Focus config alone.",
                "FORCE_ON and FORCE_OFF override that setting while the player is connected to this server.")
                .push("general");

        LOCK_ON_ALLOWED = builder.comment("FORCE_OFF prevents Focus clients from starting or keeping lock-on.")
                .defineEnum("lockOnAllowed", BooleanPolicy.CLIENT);
        AUTO_SWITCH_TO_THIRD_PERSON = builder.comment("Controls automatic switch to rear third-person when lock-on starts.")
                .defineEnum("autoSwitchToThirdPerson", BooleanPolicy.CLIENT);
        ALLOW_FIRST_PERSON_WHILE_TARGETING = builder.comment("Set FORCE_OFF to block first-person while locked on.")
                .defineEnum("allowFirstPersonWhileTargeting", BooleanPolicy.CLIENT);
        ALLOW_FRONT_FACING_THIRD_PERSON_WHILE_TARGETING = builder.comment("Set FORCE_OFF to block front-facing third-person while locked on.")
                .defineEnum("allowFrontFacingThirdPersonWhileTargeting", BooleanPolicy.CLIENT);

        builder.pop();

        builder.comment(
                "Target filter policy.",
                "To disable locking onto players, set targetFiltersEnabled=FORCE_ON, targetFilterMode=EXCLUDE, and filterPlayers=FORCE_ON.")
                .push("targetFilters");

        ENABLE_TARGET_FILTERS = builder.comment("Controls whether Focus target filters are active.")
                .defineEnum("targetFiltersEnabled", BooleanPolicy.CLIENT);
        TARGET_FILTER_MODE = builder.comment("CLIENT leaves local mode alone. EXCLUDE ignores matching targets. EXCLUSIVE allows only matching targets.")
                .defineEnum("targetFilterMode", FilterModePolicy.CLIENT);
        FILTER_PLAYERS = builder.comment("Includes players in the target filter rules.")
                .defineEnum("filterPlayers", BooleanPolicy.CLIENT);
        FILTER_PASSIVE_MOBS = builder.comment("Includes passive mobs in the target filter rules.")
                .defineEnum("filterPassiveMobs", BooleanPolicy.CLIENT);
        FILTER_NEUTRAL_MOBS = builder.comment("Includes neutral mobs in the target filter rules.")
                .defineEnum("filterNeutralMobs", BooleanPolicy.CLIENT);
        FILTER_HOSTILE_MOBS = builder.comment("Includes hostile mobs in the target filter rules.")
                .defineEnum("filterHostileMobs", BooleanPolicy.CLIENT);
        OVERRIDE_TARGET_FILTER_ENTITY_IDS = builder.comment("When true, the entity id list below replaces each client's local entity id filter list.")
                .define("overrideTargetFilterEntityIds", false);
        TARGET_FILTER_ENTITY_IDS = builder.comment("Entity type ids used by the target filter rules, for example minecraft:creeper.")
                .define("targetFilterEntityIds", List.<String>of(), FocusServerConfig::isValidEntityIdList);

        builder.pop();

        builder.comment("Crosshair and hit-ray correction policy.")
                .push("crosshair");

        CROSSHAIR_CORRECTION_MODE = builder.comment("CLIENT leaves local mode alone. Other values force the matching Focus crosshair correction mode.")
                .defineEnum("crosshairCorrectionMode", CrosshairCorrectionModePolicy.CLIENT);
        RENDER_CORRECTED_CROSSHAIR = builder.comment("Controls rendering of Focus's corrected crosshair overlay.")
                .defineEnum("renderCorrectedCrosshair", BooleanPolicy.CLIENT);
        CORRECT_BLOCK_PLACEMENT_RAY = builder.comment("Controls camera-aware block placement ray correction.")
                .defineEnum("correctBlockPlacementRay", BooleanPolicy.CLIENT);
        CORRECT_ENTITY_HIT_RAY = builder.comment("Controls camera-aware entity hit ray correction.")
                .defineEnum("correctEntityHitRay", BooleanPolicy.CLIENT);
        CORRECT_CROSSHAIR_ONLY_WHILE_LOCKED_ON = builder.comment("Controls whether corrected crosshair rendering is limited to active lock-on.")
                .defineEnum("correctCrosshairOnlyWhileLockedOn", BooleanPolicy.CLIENT);
        HIDE_VANILLA_CROSSHAIR = builder.comment("Controls vanilla crosshair suppression.")
                .defineEnum("hideVanillaCrosshair", BooleanPolicy.CLIENT);
        HIDE_VANILLA_CROSSHAIR_OUT_OF_RANGE = builder.comment("Controls crosshair suppression while locked on to an out-of-range target.")
                .defineEnum("hideVanillaCrosshairOutOfRange", BooleanPolicy.CLIENT);

        builder.pop();

        SPEC = builder.build();
    }

    private FocusServerConfig() {}

    public static FocusServerPolicyPayload createPolicyPayload() {
        int[] masks = new int[2];
        applyBooleanPolicy(masks, BooleanSetting.LOCK_ON_ALLOWED, LOCK_ON_ALLOWED.get());
        applyBooleanPolicy(masks, BooleanSetting.AUTO_SWITCH_TO_THIRD_PERSON, AUTO_SWITCH_TO_THIRD_PERSON.get());
        applyBooleanPolicy(masks, BooleanSetting.ALLOW_FIRST_PERSON_WHILE_TARGETING, ALLOW_FIRST_PERSON_WHILE_TARGETING.get());
        applyBooleanPolicy(masks, BooleanSetting.ALLOW_FRONT_FACING_THIRD_PERSON_WHILE_TARGETING, ALLOW_FRONT_FACING_THIRD_PERSON_WHILE_TARGETING.get());
        applyBooleanPolicy(masks, BooleanSetting.ENABLE_TARGET_FILTERS, ENABLE_TARGET_FILTERS.get());
        applyBooleanPolicy(masks, BooleanSetting.FILTER_PLAYERS, FILTER_PLAYERS.get());
        applyBooleanPolicy(masks, BooleanSetting.FILTER_PASSIVE_MOBS, FILTER_PASSIVE_MOBS.get());
        applyBooleanPolicy(masks, BooleanSetting.FILTER_NEUTRAL_MOBS, FILTER_NEUTRAL_MOBS.get());
        applyBooleanPolicy(masks, BooleanSetting.FILTER_HOSTILE_MOBS, FILTER_HOSTILE_MOBS.get());
        applyBooleanPolicy(masks, BooleanSetting.RENDER_CORRECTED_CROSSHAIR, RENDER_CORRECTED_CROSSHAIR.get());
        applyBooleanPolicy(masks, BooleanSetting.CORRECT_BLOCK_PLACEMENT_RAY, CORRECT_BLOCK_PLACEMENT_RAY.get());
        applyBooleanPolicy(masks, BooleanSetting.CORRECT_ENTITY_HIT_RAY, CORRECT_ENTITY_HIT_RAY.get());
        applyBooleanPolicy(masks, BooleanSetting.CORRECT_CROSSHAIR_ONLY_WHILE_LOCKED_ON, CORRECT_CROSSHAIR_ONLY_WHILE_LOCKED_ON.get());
        applyBooleanPolicy(masks, BooleanSetting.HIDE_VANILLA_CROSSHAIR, HIDE_VANILLA_CROSSHAIR.get());
        applyBooleanPolicy(masks, BooleanSetting.HIDE_VANILLA_CROSSHAIR_OUT_OF_RANGE, HIDE_VANILLA_CROSSHAIR_OUT_OF_RANGE.get());

        return new FocusServerPolicyPayload(
                masks[0],
                masks[1],
                TARGET_FILTER_MODE.get().serializedOverride(),
                OVERRIDE_TARGET_FILTER_ENTITY_IDS.getAsBoolean(),
                targetFilterEntityIds(),
                CROSSHAIR_CORRECTION_MODE.get().serializedOverride());
    }

    private static void applyBooleanPolicy(int[] masks, BooleanSetting setting, BooleanPolicy policy) {
        if (policy == BooleanPolicy.CLIENT) {
            return;
        }
        masks[0] |= setting.mask();
        if (policy == BooleanPolicy.FORCE_ON) {
            masks[1] |= setting.mask();
        }
    }

    private static List<String> targetFilterEntityIds() {
        List<String> configuredIds = new ArrayList<>();
        for (String id : TARGET_FILTER_ENTITY_IDS.get()) {
            if (ResourceLocation.tryParse(id) != null && !configuredIds.contains(id)) {
                configuredIds.add(id);
            }
        }
        return List.copyOf(configuredIds);
    }

    private static boolean isValidEntityTypeId(Object value) {
        return value instanceof String id && ResourceLocation.tryParse(id) != null;
    }

    private static boolean isValidEntityIdList(Object value) {
        return value instanceof List<?> ids && ids.stream().allMatch(FocusServerConfig::isValidEntityTypeId);
    }

    public enum BooleanPolicy {
        CLIENT,
        FORCE_ON,
        FORCE_OFF
    }

    public enum FilterModePolicy {
        CLIENT(FocusServerPolicyPayload.NO_MODE_OVERRIDE),
        EXCLUDE("EXCLUDE"),
        EXCLUSIVE("EXCLUSIVE");

        private final String serializedOverride;

        FilterModePolicy(String serializedOverride) {
            this.serializedOverride = serializedOverride;
        }

        public String serializedOverride() {
            return serializedOverride;
        }
    }

    public enum CrosshairCorrectionModePolicy {
        CLIENT(FocusServerPolicyPayload.NO_MODE_OVERRIDE),
        VANILLA("VANILLA"),
        CAMERA_PROJECTED("CAMERA_PROJECTED"),
        TARGET_PROJECTED("TARGET_PROJECTED"),
        HYBRID("HYBRID");

        private final String serializedOverride;

        CrosshairCorrectionModePolicy(String serializedOverride) {
            this.serializedOverride = serializedOverride;
        }

        public String serializedOverride() {
            return serializedOverride;
        }
    }
}
