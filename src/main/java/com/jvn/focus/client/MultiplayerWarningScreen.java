package com.jvn.focus.client;

import java.util.Locale;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

public final class MultiplayerWarningScreen extends Screen {
    private static final long ACKNOWLEDGE_DELAY_MILLIS = 3_000L;
    private static final int WARNING_RED = 0xFF5555;

    private final Screen parent;
    private final Screen destination;
    private Button acknowledgeButton;
    private final long acknowledgeUnlockAt;

    public MultiplayerWarningScreen(Screen parent, Screen destination) {
        super(Component.translatable("screen.focus.multiplayer_warning.title"));
        this.parent = parent;
        this.destination = destination;
        this.acknowledgeUnlockAt = Util.getMillis() + ACKNOWLEDGE_DELAY_MILLIS;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int buttonY = this.height / 4 + 108;
        this.acknowledgeButton = this.addRenderableWidget(
                Button.builder(CommonComponents.EMPTY, button -> acknowledgeAndContinue())
                        .bounds(centerX - 100, buttonY, 200, 20)
                        .build());
        this.addRenderableWidget(
                Button.builder(CommonComponents.GUI_BACK, button -> onClose())
                        .bounds(centerX - 100, buttonY + 24, 200, 20)
                        .build());
        updateAcknowledgeButtonState(Util.getMillis());
    }

    @Override
    public void tick() {
        updateAcknowledgeButtonState(Util.getMillis());
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        int y = this.height / 4 - 28;
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, y, 0xFFFFFF);
        y += this.font.lineHeight + 12;
        y = drawCenteredWrappedLine(guiGraphics, Component.translatable("screen.focus.multiplayer_warning.line1"), y, 0xE0E0E0);
        y = drawCenteredWrappedLine(guiGraphics, Component.translatable("screen.focus.multiplayer_warning.line2"), y + 4, 0xE0E0E0);
        drawCenteredWrappedLine(guiGraphics, Component.translatable("screen.focus.multiplayer_warning.line3"), y + 4, 0xE0E0E0);

        if (this.acknowledgeButton != null && !this.acknowledgeButton.active) {
            String remaining = String.format(Locale.ROOT, "%.1fs", remainingSeconds(Util.getMillis()));
            Component timerText = Component.translatable("screen.focus.multiplayer_warning.timer", remaining);
            int x = this.width / 2 - this.font.width(timerText) / 2;
            int textY = this.acknowledgeButton.getY() + (this.acknowledgeButton.getHeight() - this.font.lineHeight) / 2;
            guiGraphics.drawString(this.font, timerText, x, textY, WARNING_RED, false);
        }
    }

    private int drawCenteredWrappedLine(GuiGraphics guiGraphics, Component text, int y, int color) {
        for (FormattedCharSequence line : this.font.split(text, this.width - 48)) {
            guiGraphics.drawCenteredString(this.font, line, this.width / 2, y, color);
            y += this.font.lineHeight + 2;
        }
        return y;
    }

    private void updateAcknowledgeButtonState(long now) {
        if (this.acknowledgeButton == null) {
            return;
        }
        if (now < this.acknowledgeUnlockAt) {
            this.acknowledgeButton.active = false;
            this.acknowledgeButton.setMessage(CommonComponents.EMPTY);
            return;
        }
        this.acknowledgeButton.active = true;
        this.acknowledgeButton.setMessage(Component.translatable("screen.focus.multiplayer_warning.acknowledge"));
    }

    private double remainingSeconds(long now) {
        if (now >= this.acknowledgeUnlockAt) {
            return 0.0D;
        }
        return (this.acknowledgeUnlockAt - now) / 1000.0D;
    }

    private void acknowledgeAndContinue() {
        MultiplayerWarningState.acknowledge();
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.destination);
        }
    }
}
