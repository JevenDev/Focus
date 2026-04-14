package com.jvn.focus.client.hud;

import com.jvn.focus.Focus;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;
import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 * Shared screen-space projection utilities used by HUD overlays and the game renderer mixin.
 */
@EventBusSubscriber(modid = Focus.MOD_ID, value = Dist.CLIENT)
public final class FocusScreenProjectionUtil {
    private static final double MIN_DEPTH = 0.01D;

    /**
     * The FOV (in degrees) most recently used by the game renderer for the
     * main world rendering pass.  Cached from {@link ViewportEvent.ComputeFov}
     * so that screen-space projections match the rendered perspective exactly,
     * including dynamic modifiers such as sprinting and item use.
     */
    private static float lastRenderedFovDegrees = 70.0F;

    private FocusScreenProjectionUtil() {}
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onComputeFov(ViewportEvent.ComputeFov event) {
        if (event.usedConfiguredFov()) {
            lastRenderedFovDegrees = (float) event.getFOV();
        }
    }

    /**
     * Projects a world-space position onto GUI screen coordinates.
     *
     * @return the screen-space point, or {@code null} if the position is behind the camera or off-screen
     */
    public static ScreenPoint projectToScreen(Minecraft minecraft, Vec3 worldPos, float partialTick, int guiWidth, int guiHeight) {
        Camera camera = minecraft.gameRenderer.getMainCamera();
        Vec3 relativePos = worldPos.subtract(camera.getPosition());

        Vector3f leftVector = camera.getLeftVector();
        Vector3f upVector = camera.getUpVector();
        Vector3f lookVector = camera.getLookVector();

        double depth = dot(relativePos, lookVector);
        if (depth <= MIN_DEPTH) {
            return null;
        }

        double horizontal = -dot(relativePos, leftVector);
        double vertical = dot(relativePos, upVector);

        // Apply view bobbing and hurt camera effects so the projected point
        // follows the same transforms as the rendered world geometry.
        // The Camera orientation vectors do not include these effects because
        // GameRenderer.renderLevel() applies them to the PoseStack after
        // Camera.setup(), causing the indicator to drift without this correction.
        Vector4f corrected = applyBobTransforms(minecraft, camera, partialTick,
                (float) horizontal, (float) vertical, (float) (-depth));
        float correctedDepth = -corrected.z();
        if (correctedDepth <= (float) MIN_DEPTH) {
            return null;
        }

        // Use the FOV cached from the world rendering pass so the projection
        // includes dynamic modifiers (sprint, item use, etc.) that widen/narrow
        // the perspective.  Using only the options value would mismatch the
        // rendered world and cause the indicator to drift when moving fast.
        double tanHalfFov = Math.tan(Math.toRadians(lastRenderedFovDegrees * 0.5D));
        if (tanHalfFov <= 0.0D) {
            return null;
        }

        double aspectRatio = (double) guiWidth / (double) guiHeight;
        double ndcX = corrected.x() / (correctedDepth * tanHalfFov * aspectRatio);
        double ndcY = corrected.y() / (correctedDepth * tanHalfFov);
        if (Math.abs(ndcX) > 1.0D || Math.abs(ndcY) > 1.0D) {
            return null;
        }

        float screenX = (float) ((ndcX + 1.0D) * 0.5D * guiWidth);
        float screenY = (float) ((1.0D - ndcY) * 0.5D * guiHeight);
        return new ScreenPoint(screenX, screenY);
    }

    /**
     * Replicates the view-bobbing and hurt-camera transforms that
     * {@code GameRenderer.renderLevel()} applies to the PoseStack before
     * world geometry is drawn.  The camera orientation vectors returned by
     * {@link Camera} do not include these effects, so the projection would
     * otherwise drift from the actually-rendered entity positions.
     *
     * @param x view-space X (right)
     * @param y view-space Y (up)
     * @param z view-space Z (negative depth, OpenGL convention)
     */
    private static Vector4f applyBobTransforms(Minecraft minecraft, Camera camera, float partialTick,
                                               float x, float y, float z) {
        PoseStack bobPose = new PoseStack();
        Entity cameraEntity = camera.getEntity();

        // --- bobHurt (mirrors GameRenderer.bobHurt) ---
        if (cameraEntity instanceof LivingEntity living) {
            float hurtTime = (float) living.hurtTime - partialTick;
            if (living.isDeadOrDying()) {
                float deathTime = Math.min((float) living.deathTime + partialTick, 20.0F);
                bobPose.mulPose(Axis.ZP.rotationDegrees(40.0F - 8000.0F / (deathTime + 200.0F)));
            }
            if (hurtTime >= 0.0F) {
                hurtTime /= (float) living.hurtDuration;
                hurtTime = Mth.sin(hurtTime * hurtTime * hurtTime * hurtTime * (float) Math.PI);
                float dir = living.getHurtDir();
                bobPose.mulPose(Axis.YP.rotationDegrees(-dir));
                float scale = -hurtTime * 14.0F * minecraft.options.damageTiltStrength().get().floatValue();
                bobPose.mulPose(Axis.ZP.rotationDegrees(scale));
                bobPose.mulPose(Axis.YP.rotationDegrees(dir));
            }
        }

        // --- bobView (mirrors GameRenderer.bobView) ---
        if (minecraft.options.bobView().get() && cameraEntity instanceof Player player) {
            float walkDelta = player.walkDist - player.walkDistO;
            float bob = -(player.walkDist + walkDelta * partialTick);
            float bobAngle = Mth.lerp(partialTick, player.oBob, player.bob);
            bobPose.translate(
                    Mth.sin(bob * (float) Math.PI) * bobAngle * 0.5F,
                    -Math.abs(Mth.cos(bob * (float) Math.PI) * bobAngle),
                    0.0F);
            bobPose.mulPose(Axis.ZP.rotationDegrees(
                    Mth.sin(bob * (float) Math.PI) * bobAngle * 3.0F));
            bobPose.mulPose(Axis.XP.rotationDegrees(
                    Math.abs(Mth.cos(bob * (float) Math.PI - 0.2F) * bobAngle) * 5.0F));
        }

        Vector4f result = new Vector4f(x, y, z, 1.0F);
        bobPose.last().pose().transform(result);
        return result;
    }

    public static double dot(Vec3 vector, Vector3f other) {
        return vector.x * other.x() + vector.y * other.y() + vector.z * other.z();
    }

    public static Vec3 vectorToVec3(Vector3f vector) {
        return new Vec3(vector.x(), vector.y(), vector.z());
    }

    public record ScreenPoint(float x, float y) {}
}
