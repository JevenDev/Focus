package com.jvn.focus.client.camera;

import net.minecraft.util.StringRepresentable;

public enum FocusCameraMode implements StringRepresentable {
    COUPLED("focus.lock_on_client.camera_ownership_mode.COUPLED"),
    DELAYED_FOLLOW("focus.lock_on_client.camera_ownership_mode.DELAYED_FOLLOW"),
    FREE_LOOK("focus.lock_on_client.camera_ownership_mode.FREE_LOOK");

    private final String translationKey;

    FocusCameraMode(String translationKey) {
        this.translationKey = translationKey;
    }

    @Override
    public String getSerializedName() {
        return translationKey;
    }
}
