package com.jvn.focus.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jvn.focus.Focus;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import net.neoforged.fml.loading.FMLPaths;

public final class MultiplayerWarningState {
    private static final Path STATE_PATH = FMLPaths.CONFIGDIR.get().resolve(Focus.MOD_ID + "_multiplayer_warning.json");
    private static boolean loaded;
    private static boolean acknowledged;

    private MultiplayerWarningState() {}

    public static synchronized boolean isAcknowledged() {
        if (!loaded) {
            load();
        }
        return acknowledged;
    }

    public static synchronized void acknowledge() {
        if (isAcknowledged()) {
            return;
        }
        acknowledged = true;
        save();
    }

    private static void load() {
        loaded = true;
        acknowledged = false;
        if (!Files.isRegularFile(STATE_PATH)) {
            return;
        }

        try {
            String raw = Files.readString(STATE_PATH, StandardCharsets.UTF_8);
            JsonElement element = JsonParser.parseString(raw);
            if (element.isJsonObject()) {
                JsonElement acknowledgedValue = element.getAsJsonObject().get("acknowledged");
                if (acknowledgedValue != null && acknowledgedValue.isJsonPrimitive() && acknowledgedValue.getAsJsonPrimitive().isBoolean()) {
                    acknowledged = acknowledgedValue.getAsBoolean();
                }
            }
        } catch (Exception e) {
            Focus.LOGGER.warn("Failed to load multiplayer warning state from {}", STATE_PATH, e);
        }
    }

    private static void save() {
        try {
            Files.createDirectories(STATE_PATH.getParent());
            JsonObject object = new JsonObject();
            object.addProperty("format", "focus_multiplayer_warning_v1");
            object.addProperty("acknowledged", acknowledged);
            Files.writeString(STATE_PATH, object.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            Focus.LOGGER.warn("Failed to save multiplayer warning state to {}", STATE_PATH, e);
        }
    }
}
