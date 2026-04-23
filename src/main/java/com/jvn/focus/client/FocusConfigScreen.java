package com.jvn.focus.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class FocusConfigScreen extends Screen {
    private final Screen parent;
    private final List<Runnable> messageRefreshers = new ArrayList<>();

    public FocusConfigScreen(Screen parent) {
        super(Component.literal("Focus Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        messageRefreshers.clear();
        int buttonWidth = 260;
        int left = this.width / 2 - buttonWidth / 2;
        int y = this.height / 6;

        this.addRenderableWidget(Button.builder(
                        Component.literal("Open Camera Editor"),
                        button -> LockOnCameraEditorScreen.openFromCurrentScreen())
                .bounds(left, y, buttonWidth, 20)
                .build());
        y += 24;

        addToggle(left, y, buttonWidth, "Auto Third Person",
                FocusClientConfig::autoSwitchToThirdPerson,
                value -> FocusClientConfig.configInstance().autoSwitchToThirdPerson(value));
        y += 24;
        addToggle(left, y, buttonWidth, "Allow First Person",
                FocusClientConfig::allowFirstPersonWhileTargeting,
                value -> FocusClientConfig.configInstance().allowFirstPersonWhileTargeting(value));
        y += 24;
        addToggle(left, y, buttonWidth, "Allow Front Third Person",
                FocusClientConfig::allowFrontFacingThirdPersonWhileTargeting,
                value -> FocusClientConfig.configInstance().allowFrontFacingThirdPersonWhileTargeting(value));
        y += 24;
        addToggle(left, y, buttonWidth, "Status Messages",
                FocusClientConfig::showLockOnStatusMessages,
                value -> FocusClientConfig.configInstance().showLockOnStatusMessages(value));
        y += 24;
        addToggle(left, y, buttonWidth, "Debug Overlay",
                FocusClientConfig::showLockOnDebugText,
                value -> FocusClientConfig.configInstance().showLockOnDebugText(value));
        y += 24;
        addToggle(left, y, buttonWidth, "Cinematic Bars",
                FocusClientConfig::cinematicBarsWhileLockedOn,
                value -> FocusClientConfig.configInstance().cinematicBarsWhileLockedOn(value));
        y += 24;
        addToggle(left, y, buttonWidth, "Render Corrected Crosshair",
                FocusClientConfig::renderCorrectedCrosshair,
                value -> FocusClientConfig.configInstance().crosshair.renderCorrectedCrosshair(value));
        y += 24;
        addToggle(left, y, buttonWidth, "Hide Vanilla Crosshair",
                FocusClientConfig::hideVanillaCrosshair,
                value -> FocusClientConfig.configInstance().crosshair.hideVanillaCrosshair(value));
        y += 24;
        addToggle(left, y, buttonWidth, "Enable Target Filters",
                FocusClientConfig::enableTargetFilters,
                value -> FocusClientConfig.configInstance().enableTargetFilters(value));
        y += 24;

        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(left, this.height - 30, buttonWidth, 20)
                .build());
    }

    private void addToggle(int x, int y, int width, String label, Supplier<Boolean> getter, Consumer<Boolean> setter) {
        Button button = Button.builder(Component.empty(), clicked -> {
            setter.accept(!getter.get());
            FocusClientConfig.saveConfig();
            refreshMessages();
        }).bounds(x, y, width, 20).build();
        this.addRenderableWidget(button);
        messageRefreshers.add(() -> button.setMessage(Component.literal(label + ": " + onOff(getter.get()))));
        refreshMessages();
    }

    private void refreshMessages() {
        for (Runnable refresher : messageRefreshers) {
            refresher.run();
        }
    }

    private static String onOff(boolean value) {
        return value ? "ON" : "OFF";
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        guiGraphics.drawCenteredString(this.font, Component.literal("Focus Settings"), this.width / 2, this.height / 6 - 16, 0xFFFFFF);
        guiGraphics.drawCenteredString(this.font,
                Component.literal("Use the camera editor for offset and shoulder tuning"),
                this.width / 2, this.height / 6 - 4, 0xA0A0A0);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(parent);
        }
    }
}
