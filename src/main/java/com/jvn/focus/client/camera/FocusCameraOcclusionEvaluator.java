package com.jvn.focus.client.camera;

import com.jvn.focus.client.FocusClientConfig;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class FocusCameraOcclusionEvaluator {
    private static final double EPSILON = 1.0E-6D;

    public FocusCameraOcclusionResult evaluate(
            LocalPlayer player,
            float partialTick,
            Vec3 targetPoint,
            FocusCameraPose leftPose,
            FocusCameraPose rightPose,
            FocusClientConfig.Shoulder referenceShoulder) {
        Vec3 pivot = player.getEyePosition(partialTick);
        Vec3 leftCamera = computeCameraPosition(pivot, targetPoint, leftPose);
        Vec3 rightCamera = computeCameraPosition(pivot, targetPoint, rightPose);

        double leftVisibility = visibilityScore(player, leftCamera, targetPoint);
        double rightVisibility = visibilityScore(player, rightCamera, targetPoint);
        double leftDistance = safeDistanceToCamera(player, pivot, leftCamera);
        double rightDistance = safeDistanceToCamera(player, pivot, rightCamera);

        Vec3 activeCamera = referenceShoulder == FocusClientConfig.Shoulder.LEFT ? leftCamera : rightCamera;
        boolean playerBlocksRay = playerBlocksSegment(player, activeCamera, targetPoint);
        boolean targetVisibilityCompromised = Math.max(leftVisibility, rightVisibility) < 0.35D;
        double safeDistance = referenceShoulder == FocusClientConfig.Shoulder.LEFT ? leftDistance : rightDistance;
        return new FocusCameraOcclusionResult(safeDistance, leftVisibility, rightVisibility, playerBlocksRay, targetVisibilityCompromised);
    }

    public static Vec3 computeCameraPosition(Vec3 pivot, Vec3 targetPoint, FocusCameraPose pose) {
        FocusCameraBasisUtil.Rotation pivotRotation = FocusCameraBasisUtil.rotationToTarget(pivot, targetPoint, 0.0F, 0.0F);
        float orbitYaw = pivotRotation.yaw() + pose.rotationDegrees();
        float orbitPitch = pivotRotation.pitch();
        FocusCameraBasisUtil.CameraBasis basis = FocusCameraBasisUtil.basisFromRotation(orbitYaw, orbitPitch);
        Vec3 worldOffset = basis.up().scale(pose.offsetY())
                .add(basis.right().scale(pose.offsetX()))
                .add(basis.look().scale(-pose.offsetZ()));
        return pivot.add(worldOffset);
    }

    private static double visibilityScore(LocalPlayer player, Vec3 cameraPosition, Vec3 targetPoint) {
        Vec3 ray = targetPoint.subtract(cameraPosition);
        double totalDistance = ray.length();
        if (totalDistance <= EPSILON) {
            return 1.0D;
        }

        BlockHitResult hitResult = player.level().clip(
                new ClipContext(cameraPosition, targetPoint, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        if (hitResult.getType() == HitResult.Type.MISS) {
            return 1.0D;
        }

        double visibleDistance = hitResult.getLocation().distanceTo(cameraPosition);
        return Mth.clamp(visibleDistance / totalDistance, 0.0D, 1.0D);
    }

    private static double safeDistanceToCamera(LocalPlayer player, Vec3 pivot, Vec3 cameraPosition) {
        double desiredDistance = cameraPosition.distanceTo(pivot);
        if (desiredDistance <= EPSILON) {
            return 0.0D;
        }

        BlockHitResult hitResult = player.level().clip(
                new ClipContext(pivot, cameraPosition, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, player));
        if (hitResult.getType() == HitResult.Type.MISS) {
            return desiredDistance;
        }
        return Math.min(desiredDistance, hitResult.getLocation().distanceTo(pivot));
    }

    private static boolean playerBlocksSegment(LocalPlayer player, Vec3 from, Vec3 to) {
        Vec3 center = player.position().add(0.0D, player.getBbHeight() * 0.6D, 0.0D);
        double radius = Math.max(0.15D, player.getBbWidth() * 0.7D);
        double distance = pointToSegmentDistance(center, from, to);
        return distance < radius;
    }

    private static double pointToSegmentDistance(Vec3 point, Vec3 segmentStart, Vec3 segmentEnd) {
        Vec3 segment = segmentEnd.subtract(segmentStart);
        double segmentLengthSqr = segment.lengthSqr();
        if (segmentLengthSqr <= EPSILON) {
            return point.distanceTo(segmentStart);
        }

        double t = point.subtract(segmentStart).dot(segment) / segmentLengthSqr;
        t = Mth.clamp(t, 0.0D, 1.0D);
        Vec3 closest = segmentStart.add(segment.scale(t));
        return point.distanceTo(closest);
    }
}
