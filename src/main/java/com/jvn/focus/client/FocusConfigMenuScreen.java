package com.jvn.focus.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class FocusConfigMenuScreen extends Screen {
    private static final int BUTTON_WIDTH = 240;
    private static final int BUTTON_HEIGHT = 20;
    private static final int ROW_GAP = 6;
    private final Screen parent;

    public FocusConfigMenuScreen(Screen parent) {
        super(Component.translatable("screen.focus.config_menu.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int x = (this.width - BUTTON_WIDTH) / 2;
        int y = this.height / 2 - 36;
        addRenderableWidget(Button.builder(
                Component.translatable("screen.focus.config_menu.settings"),
                button -> Minecraft.getInstance().setScreen(FocusClientConfig.createGeneratedConfigScreen(this)))
                .bounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());
        addRenderableWidget(Button.builder(
                Component.translatable("screen.focus.config_menu.indicator_editor"),
                button -> Minecraft.getInstance().setScreen(new CustomIndicatorEditorScreen(this)))
                .bounds(x, y + BUTTON_HEIGHT + ROW_GAP, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(x, y + (BUTTON_HEIGHT + ROW_GAP) * 2 + 10, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 68, 0xFFFFFFFF);
        graphics.drawCenteredString(
                this.font,
                Component.translatable("screen.focus.config_menu.hint"),
                this.width / 2,
                this.height / 2 - 54,
                0xFFA0A0A0);
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderTransparentBackground(graphics);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }
}
