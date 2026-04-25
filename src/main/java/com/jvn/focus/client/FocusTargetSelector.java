package com.jvn.focus.client;

import com.jvn.focus.client.camera.FocusCameraController;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Target selection, filtering, and directional-swap logic for the lock-on system.
 * Extracted from {@link LockOnHandler} to separate targeting concerns from input/state handling.
 */
final class FocusTargetSelector {
    private static final double MAX_LOCK_DISTANCE = 15.0D;
    static final double MAX_LOCK_DISTANCE_SQR = MAX_LOCK_DISTANCE * MAX_LOCK_DISTANCE;
    private static final double OCCLUDED_LOCK_DISTANCE = 8.0D;
    static final double OCCLUDED_LOCK_DISTANCE_SQR = OCCLUDED_LOCK_DISTANCE * OCCLUDED_LOCK_DISTANCE;
    private static final double LOCK_ON_FOV_THRESHOLD = 0.35D;
    private static final int MAX_SIGHT_ITERATIONS = 48;

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
            if (!isValidNewLockCandidate(player, entity)) {
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
            if (!isValidNewLockCandidate(player, entity)) {
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
            if (!isValidNewLockCandidate(player, entity)) {
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

    /**
     * Checks direct line-of-sight using standard collision shapes.
     * Used for combat/hit checks where all collision-bearing blocks should count.
     */
    static boolean hasDirectSight(LocalPlayer player, LivingEntity target) {
        Vec3 from = player.getEyePosition();
        Vec3 to = getTargetAimPoint(target, 1.0F);
        BlockHitResult hitResult = player.level().clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        return hitResult.getType() != HitResult.Type.BLOCK;
    }

    /**
     * Checks targeting line-of-sight, ignoring blocks that are not real targeting
     * occluders (glass, fences, panes, iron bars, leaves, etc.).
     * <p>
     * Used for both new target acquisition (must have clear sight) and existing lock
     * persistence (grace timer only counts real occlusion).
     */
    static boolean hasTargetingSight(LocalPlayer player, LivingEntity target) {
        Vec3 from = player.getEyePosition();
        Vec3 to = getTargetAimPoint(target, 1.0F);
        Vec3 ray = to.subtract(from);
        double totalDistSqr = ray.lengthSqr();
        if (totalDistSqr < 1.0E-8) {
            return true;
        }
        Vec3 dir = ray.normalize();

        Level level = player.level();
        Vec3 start = from;
        for (int i = 0; i < MAX_SIGHT_ITERATIONS; i++) {
            BlockHitResult hit = level.clip(
                    new ClipContext(start, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
            if (hit.getType() != HitResult.Type.BLOCK) {
                return true;
            }

            BlockState state = level.getBlockState(hit.getBlockPos());
            if (isTargetingOccluder(state, level, hit.getBlockPos())) {
                return false;
            }

            start = advancePastBlock(hit.getLocation(), dir, hit.getBlockPos());
            if (start.subtract(from).lengthSqr() >= totalDistSqr) {
                return true;
            }
        }
        return false;
    }

    static boolean isLockOnHiddenFromPlayer(LocalPlayer player, LivingEntity target) {
        return target.isInvisible() || target.isInvisibleTo(player);
    }

    static boolean isTargetAllowed(LivingEntity target) {
        return isTargetAllowed(target, readTargetFilterSettings());
    }

    // --- Private helpers: targeting sight ---

    /**
     * Checks if a candidate entity passes all requirements for new lock-on acquisition.
     * Requires the target to be not hidden, within max lock distance, and to have clear
     * targeting sight (no real walls between player and target).
     * <p>
     * Note: {@code isAlive()} and target-filter checks should be done before calling this
     * method (typically in the entity-class filter passed to {@code getEntitiesOfClass}).
     */
    private static boolean isValidNewLockCandidate(LocalPlayer player, LivingEntity candidate) {
        if (isLockOnHiddenFromPlayer(player, candidate)) {
            return false;
        }
        if (player.distanceToSqr(candidate) > MAX_LOCK_DISTANCE_SQR) {
            return false;
        }
        return hasTargetingSight(player, candidate);
    }

    /**
     * Returns {@code true} if the given block state represents a real targeting occluder
     * that should block lock-on line-of-sight.
     * <p>
     * Non-occluding blocks (glass, glass panes, iron bars, doors, trapdoors, leaves,
     * and similar transparent or pass-through blocks) return {@code false} via
     * {@link BlockState#canOcclude()}.
     * <p>
     * Blocks whose collision shape does not cover any full block face (fences, chests,
     * walls, and similar non-full-cube shapes) are also excluded, since the player can
     * always see around or through them.
     */
    private static boolean isTargetingOccluder(BlockState state, BlockGetter level, BlockPos pos) {
        if (state.isAir()) {
            return false;
        }
        if (!state.canOcclude()) {
            return false;
        }
        VoxelShape collisionShape = state.getCollisionShape(level, pos);
        if (collisionShape.isEmpty()) {
            return false;
        }
        // Only count as an occluder if the shape covers at least one full block face.
        // Fences (thin posts) and chests (smaller than full cube) fail this check,
        // while solid blocks and stairs (full bottom face) pass.
        for (Direction direction : Direction.values()) {
            if (Block.isShapeFullBlock(collisionShape.getFaceShape(direction))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Advances a position along the ray direction to just past the unit-cube boundary
     * of the given block position. Used by {@link #hasTargetingSight} to skip past
     * non-occluding blocks during iterative raycasting.
     */
    private static Vec3 advancePastBlock(Vec3 hitLoc, Vec3 dir, BlockPos blockPos) {
        double bx = blockPos.getX();
        double by = blockPos.getY();
        double bz = blockPos.getZ();

        double tX = dir.x > 0 ? (bx + 1.0 - hitLoc.x) / dir.x
                   : dir.x < 0 ? (bx - hitLoc.x) / dir.x
                   : Double.MAX_VALUE;
        double tY = dir.y > 0 ? (by + 1.0 - hitLoc.y) / dir.y
                   : dir.y < 0 ? (by - hitLoc.y) / dir.y
                   : Double.MAX_VALUE;
        double tZ = dir.z > 0 ? (bz + 1.0 - hitLoc.z) / dir.z
                   : dir.z < 0 ? (bz - hitLoc.z) / dir.z
                   : Double.MAX_VALUE;

        double tExit = Double.MAX_VALUE;
        if (tX > 1.0E-6) {
            tExit = Math.min(tExit, tX);
        }
        if (tY > 1.0E-6) {
            tExit = Math.min(tExit, tY);
        }
        if (tZ > 1.0E-6) {
            tExit = Math.min(tExit, tZ);
        }

        double advance = tExit < Double.MAX_VALUE ? tExit + 0.001 : 0.5;
        return hitLoc.add(dir.scale(advance));
    }

    // --- Private helpers: general ---

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
            ResourceLocation entityTypeId = ForgeRegistries.ENTITY_TYPES.getKey(target.getType());
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
