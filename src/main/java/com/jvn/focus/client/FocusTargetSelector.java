package com.jvn.focus.client;

import com.jvn.focus.client.camera.FocusCameraController;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

/**
 * Target selection, filtering, and directional-swap logic for the lock-on system.
 * Extracted from {@link LockOnHandler} to separate targeting concerns from input/state handling.
 */
final class FocusTargetSelector {
    private static final double MAX_LOCK_DISTANCE = 20.0D;
    static final double MAX_LOCK_DISTANCE_SQR = MAX_LOCK_DISTANCE * MAX_LOCK_DISTANCE;
    private static final double OCCLUDED_LOCK_DISTANCE = 10.0D;
    static final double OCCLUDED_LOCK_DISTANCE_SQR = OCCLUDED_LOCK_DISTANCE * OCCLUDED_LOCK_DISTANCE;
    private static final double LOCK_ON_FOV_THRESHOLD = 0.35D;

    private static final FocusCameraController CAMERA_CONTROLLER = FocusCameraController.getInstance();

    private FocusTargetSelector() {}

    static LivingEntity findTarget(LocalPlayer player) {
        Vec3 eyePosition = player.getEyePosition();
        Vec3 lookDirection = player.getLookAngle().normalize();
        TargetFilterSettings filterSettings = readTargetFilterSettings();
        LivingEntity bestTarget = null;
        double bestAlignment = LOCK_ON_FOV_THRESHOLD;
        double bestDistanceSqr = Double.MAX_VALUE;

        for (LivingEntity entity : player.level().getEntitiesOfClass(
                LivingEntity.class,
                player.getBoundingBox().inflate(MAX_LOCK_DISTANCE),
                candidate -> candidate != player && candidate.isAlive() && isTargetAllowed(candidate, filterSettings))) {
            if (isLockOnHiddenFromPlayer(player, entity) || !isWithinAllowedLockDistance(player, entity)) {
                continue;
            }

            double alignment = getTargetAlignment(eyePosition, lookDirection, entity);
            if (alignment < LOCK_ON_FOV_THRESHOLD) {
                continue;
            }

            double distanceSqr = player.distanceToSqr(entity);
            if (alignment > bestAlignment + 1.0E-4D
                    || (Math.abs(alignment - bestAlignment) <= 1.0E-4D && distanceSqr < bestDistanceSqr)) {
                bestAlignment = alignment;
                bestDistanceSqr = distanceSqr;
                bestTarget = entity;
            }
        }

        return bestTarget;
    }

    static LivingEntity findClosestTarget(LocalPlayer player, LivingEntity excludedTarget) {
        TargetFilterSettings filterSettings = readTargetFilterSettings();
        LivingEntity bestTarget = null;
        double bestDistanceSqr = Double.MAX_VALUE;

        for (LivingEntity entity : player.level().getEntitiesOfClass(
                LivingEntity.class,
                player.getBoundingBox().inflate(MAX_LOCK_DISTANCE),
                candidate -> candidate != player
                        && candidate.isAlive()
                        && candidate != excludedTarget
                        && isTargetAllowed(candidate, filterSettings))) {
            if (isLockOnHiddenFromPlayer(player, entity) || !isWithinAllowedLockDistance(player, entity)) {
                continue;
            }

            double distanceSqr = player.distanceToSqr(entity);
            if (distanceSqr < bestDistanceSqr) {
                bestDistanceSqr = distanceSqr;
                bestTarget = entity;
            }
        }

        return bestTarget;
    }

    static LivingEntity findDirectionalTarget(LocalPlayer player, LivingEntity currentTarget, Vec2 mouseDirection, Vec3 cameraLookDirection) {
        if (currentTarget == null) {
            return null;
        }

        float mouseMagnitude = Mth.sqrt(
                mouseDirection.x * mouseDirection.x + mouseDirection.y * mouseDirection.y);
        if (mouseMagnitude < 1.0E-4F) {
            return null;
        }

        float mouseDirX = mouseDirection.x / mouseMagnitude;
        float mouseDirY = mouseDirection.y / mouseMagnitude;
        Vec3 eyePosition = player.getEyePosition();
        Vec3 lookDirection = cameraLookDirection.lengthSqr() > 1.0E-6D
                ? cameraLookDirection.normalize()
                : player.getLookAngle().normalize();
        Vec3 right = lookDirection.cross(new Vec3(0.0D, 1.0D, 0.0D));
        if (right.lengthSqr() < 1.0E-6D) {
            right = new Vec3(1.0D, 0.0D, 0.0D);
        } else {
            right = right.normalize();
        }
        Vec3 up = right.cross(lookDirection).normalize();

        Vec2 currentTargetScreen = projectToScreenSpace(eyePosition, lookDirection, right, up, currentTarget);
        if (currentTargetScreen == null) {
            return null;
        }

        double directionThreshold = FocusClientConfig.targetSwapDirectionThreshold();
        double minScreenSeparation = FocusClientConfig.targetSwapMinScreenSeparation();
        TargetFilterSettings filterSettings = readTargetFilterSettings();
        LivingEntity bestTarget = null;
        float bestScreenDistance = Float.MAX_VALUE;
        float bestAlignment = (float) directionThreshold;
        double bestDistanceSqr = Double.MAX_VALUE;

        for (LivingEntity entity : player.level().getEntitiesOfClass(
                LivingEntity.class,
                player.getBoundingBox().inflate(MAX_LOCK_DISTANCE),
                candidate -> candidate != player
                        && candidate.isAlive()
                        && candidate != currentTarget
                        && isTargetAllowed(candidate, filterSettings))) {
            if (isLockOnHiddenFromPlayer(player, entity) || !isWithinAllowedLockDistance(player, entity)) {
                continue;
            }

            Vec2 candidateScreen = projectToScreenSpace(eyePosition, lookDirection, right, up, entity);
            if (candidateScreen == null) {
                continue;
            }

            float deltaX = candidateScreen.x - currentTargetScreen.x;
            float deltaY = candidateScreen.y - currentTargetScreen.y;
            float candidateScreenDistance = Mth.sqrt(deltaX * deltaX + deltaY * deltaY);
            if (candidateScreenDistance < minScreenSeparation) {
                continue;
            }

            float candidateDirX = deltaX / candidateScreenDistance;
            float candidateDirY = deltaY / candidateScreenDistance;
            float directionalAlignment = candidateDirX * mouseDirX + candidateDirY * mouseDirY;
            if (directionalAlignment < directionThreshold) {
                continue;
            }

            double distanceSqr = player.distanceToSqr(entity);
            if (candidateScreenDistance < bestScreenDistance - 1.0E-4F
                    || (Math.abs(candidateScreenDistance - bestScreenDistance) <= 1.0E-4F
                            && directionalAlignment > bestAlignment + 1.0E-4F)
                    || (Math.abs(candidateScreenDistance - bestScreenDistance) <= 1.0E-4F
                            && Math.abs(directionalAlignment - bestAlignment) <= 1.0E-4F
                            && distanceSqr < bestDistanceSqr)) {
                bestScreenDistance = candidateScreenDistance;
                bestAlignment = directionalAlignment;
                bestDistanceSqr = distanceSqr;
                bestTarget = entity;
            }
        }

        return bestTarget;
    }

    static boolean hasDirectSight(LocalPlayer player, LivingEntity target) {
        Vec3 from = player.getEyePosition();
        Vec3 to = getTargetAimPoint(target, 1.0F);
        BlockHitResult hitResult = player.level().clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        return hitResult.getType() != HitResult.Type.BLOCK;
    }

    static boolean isLockOnHiddenFromPlayer(LocalPlayer player, LivingEntity target) {
        return target.isInvisible() || target.isInvisibleTo(player);
    }

    static boolean isTargetAllowed(LivingEntity target) {
        return isTargetAllowed(target, readTargetFilterSettings());
    }

    // --- Private helpers ---

    private static Vec3 getTargetAimPoint(LivingEntity target, float partialTick) {
        return CAMERA_CONTROLLER.targetPointFor(target, partialTick);
    }

    private static double getTargetAlignment(Vec3 eyePosition, Vec3 lookDirection, LivingEntity target) {
        Vec3 toTarget = getTargetAimPoint(target, 1.0F).subtract(eyePosition);
        double lengthSqr = toTarget.lengthSqr();
        if (lengthSqr <= 1.0E-6D) {
            return -1.0D;
        }

        return toTarget.scale(1.0D / Math.sqrt(lengthSqr)).dot(lookDirection);
    }

    private static Vec2 projectToScreenSpace(
            Vec3 eyePosition,
            Vec3 lookDirection,
            Vec3 right,
            Vec3 up,
            LivingEntity target) {
        Vec3 toTarget = getTargetAimPoint(target, 1.0F).subtract(eyePosition);
        double forward = toTarget.dot(lookDirection);
        if (forward <= 1.0E-3D) {
            return null;
        }

        double screenX = toTarget.dot(right) / forward;
        double screenY = toTarget.dot(up) / forward;
        return new Vec2((float) screenX, (float) screenY);
    }

    private static boolean isWithinAllowedLockDistance(LocalPlayer player, LivingEntity target) {
        boolean obstructed = !hasDirectSight(player, target);
        double maxDistanceSqr = obstructed ? OCCLUDED_LOCK_DISTANCE_SQR : MAX_LOCK_DISTANCE_SQR;
        return player.distanceToSqr(target) <= maxDistanceSqr;
    }

    private static boolean isTargetAllowed(LivingEntity target, TargetFilterSettings filterSettings) {
        if (!filterSettings.enabled()) {
            return true;
        }

        boolean matchesAnyFilter = matchesAnyFilter(target, filterSettings);
        if (filterSettings.mode() == FocusClientConfig.TargetFilterMode.EXCLUSIVE) {
            return matchesAnyFilter;
        }
        return !matchesAnyFilter;
    }

    private static boolean matchesAnyFilter(LivingEntity target, TargetFilterSettings filterSettings) {
        if (filterSettings.filterPlayers() && target instanceof Player) {
            return true;
        }
        if (filterSettings.filterHostileMobs() && target instanceof Enemy) {
            return true;
        }
        if (filterSettings.filterNeutralMobs() && target instanceof NeutralMob && !(target instanceof Enemy)) {
            return true;
        }
        if (filterSettings.filterPassiveMobs() && isPassiveMob(target)) {
            return true;
        }
        if (!filterSettings.entityTypeIds().isEmpty()) {
            ResourceLocation entityTypeId = BuiltInRegistries.ENTITY_TYPE.getKey(target.getType());
            if (entityTypeId != null && filterSettings.entityTypeIds().contains(entityTypeId)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isPassiveMob(LivingEntity target) {
        return target instanceof Mob && !(target instanceof Enemy) && !(target instanceof NeutralMob);
    }

    private static TargetFilterSettings readTargetFilterSettings() {
        if (!FocusClientConfig.enableTargetFilters()) {
            return new TargetFilterSettings(
                    false,
                    FocusClientConfig.TargetFilterMode.EXCLUDE,
                    false,
                    false,
                    false,
                    false,
                    Set.of());
        }

        return new TargetFilterSettings(
                true,
                FocusClientConfig.targetFilterMode(),
                FocusClientConfig.filterPlayers(),
                FocusClientConfig.filterPassiveMobs(),
                FocusClientConfig.filterNeutralMobs(),
                FocusClientConfig.filterHostileMobs(),
                parseEntityTypeIds(FocusClientConfig.targetFilterEntityIds()));
    }

    private static Set<ResourceLocation> parseEntityTypeIds(List<String> configuredEntityTypeIds) {
        if (configuredEntityTypeIds.isEmpty()) {
            return Set.of();
        }

        Set<ResourceLocation> parsedEntityTypeIds = new HashSet<>();
        for (String configuredEntityTypeId : configuredEntityTypeIds) {
            ResourceLocation parsedId = ResourceLocation.tryParse(configuredEntityTypeId);
            if (parsedId != null) {
                parsedEntityTypeIds.add(parsedId);
            }
        }
        return parsedEntityTypeIds;
    }

    record TargetFilterSettings(
            boolean enabled,
            FocusClientConfig.TargetFilterMode mode,
            boolean filterPlayers,
            boolean filterPassiveMobs,
            boolean filterNeutralMobs,
            boolean filterHostileMobs,
            Set<ResourceLocation> entityTypeIds) {
    }
}
