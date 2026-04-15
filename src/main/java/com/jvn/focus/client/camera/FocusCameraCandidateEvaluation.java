package com.jvn.focus.client.camera;

import com.jvn.focus.client.FocusClientConfig;
import net.minecraft.world.phys.Vec3;

public record FocusCameraCandidateEvaluation(
        FocusClientConfig.Shoulder shoulder,
        Vec3 cameraPosition,
        double visibilityScore,
        double screenPlacementScore,
        double continuityPenalty,
        double totalScore) {}
