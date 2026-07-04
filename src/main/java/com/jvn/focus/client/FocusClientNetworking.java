package com.jvn.focus.client;

import com.jvn.focus.network.FocusServerPolicyPayload;

public final class FocusClientNetworking {
    private FocusClientNetworking() {}

    public static void handleServerPolicy(FocusServerPolicyPayload payload) {
        FocusClientConfig.applyServerPolicy(payload);
    }
}
