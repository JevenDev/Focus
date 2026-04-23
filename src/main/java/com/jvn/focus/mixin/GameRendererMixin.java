package com.jvn.focus.mixin;

import com.jvn.focus.client.FocusClientConfig;
import com.jvn.focus.client.LockOnHandler;
import com.jvn.focus.client.hud.FocusScreenProjectionUtil;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow
    @Final
    Minecraft minecraft;

    @Inject(method = "pick(F)V", at = @At("TAIL"))
    private void focus$applyCameraAwarePick(float partialTick, CallbackInfo ci) {
        if (minecraft.player == null || minecraft.level == null || minecraft.hitResult == null) {
            return;
        }
        if (minecraft.options.getCameraType().isFirstPerson()) {
            return;
        }
        if (FocusClientConfig.correctCrosshairOnlyWhileLockedOn() && LockOnHandler.getLockedTarget() == null) {
            return;
        }
        if (!FocusClientConfig.correctBlockPlacementRay() && !FocusClientConfig.correctEntityHitRay()) {
            return;
        }

        LivingEntity lockedTarget = LockOnHandler.getLockedTarget();
        FocusClientConfig.CrosshairCorrectionMode mode = FocusClientConfig.crosshairCorrectionMode();
        boolean targetProjected = mode == FocusClientConfig.CrosshairCorrectionMode.TARGET_PROJECTED
                || (mode == FocusClientConfig.CrosshairCorrectionMode.HYBRID && lockedTarget != null);

        Camera camera = minecraft.gameRenderer.getMainCamera();
        // Use the player's eye position as the ray origin so that reach distance and attack
        // range match first-person behaviour.  The camera position is behind/above the player
        // and would shorten the effective interaction distance.
        Vec3 from = minecraft.player.getEyePosition(partialTick);
        // 1.20.1 Forge does not expose the 1.21 interaction-range accessors on LocalPlayer.
        // Keep vanilla-equivalent behavior by reading pick range from game mode and using
        // the standard entity reach values (3.0 survival / 6.0 creative-style far reach).
        double blockReach = minecraft.gameMode != null ? minecraft.gameMode.getPickRange() : 4.5D;
        double entityReach = minecraft.gameMode != null && minecraft.gameMode.hasFarPickRange() ? 6.0D : 3.0D;
        Vec3 lookDirection = FocusScreenProjectionUtil.vectorToVec3(camera.getLookVector()).normalize();

        if (targetProjected && lockedTarget != null && lockedTarget.isAlive()) {
            // Aim from the player's eye toward the target so the pick angle matches the attack
            // origin, not the camera origin.
            Vec3 targetPoint = lockedTarget.getPosition(partialTick).add(0.0D, lockedTarget.getBbHeight() * 0.75D, 0.0D);
            Vec3 toTarget = targetPoint.subtract(from);
            double toTargetLength = toTarget.length();
            if (toTargetLength > 1.0E-6D) {
                lookDirection = toTarget.scale(1.0D / toTargetLength);
            }
        }
        Vec3 blockRayEnd = from.add(lookDirection.scale(blockReach));
        Vec3 entityRayEnd = from.add(lookDirection.scale(entityReach));

        HitResult corrected = minecraft.hitResult;
        if (FocusClientConfig.correctBlockPlacementRay()) {
            BlockHitResult blockHit = minecraft.level.clip(
                    new ClipContext(
                            from,
                            blockRayEnd,
                            ClipContext.Block.OUTLINE,
                            ClipContext.Fluid.NONE,
                            minecraft.player));
            corrected = blockHit;
        }

        if (FocusClientConfig.correctEntityHitRay()) {
            AABB aabb = new AABB(from, entityRayEnd).inflate(1.0D);
            EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                    minecraft.player,
                    from,
                    entityRayEnd,
                    aabb,
                    entity -> !entity.isSpectator() && entity.isPickable(),
                    entityReach * entityReach);
            if (entityHit != null) {
                if (corrected.getType() == HitResult.Type.MISS) {
                    corrected = entityHit;
                } else {
                    double entityDistance = from.distanceToSqr(entityHit.getLocation());
                    double blockDistance = from.distanceToSqr(corrected.getLocation());
                    if (entityDistance < blockDistance) {
                        corrected = entityHit;
                    }
                }
            }
        }

        minecraft.hitResult = corrected;
        minecraft.crosshairPickEntity = null;
        if (corrected instanceof EntityHitResult entityHitResult) {
            Entity entity = entityHitResult.getEntity();
            if (entity != null) {
                minecraft.crosshairPickEntity = entity;
            }
        }
    }
}
