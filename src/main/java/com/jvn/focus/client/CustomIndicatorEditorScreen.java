package com.jvn.focus.client;

import java.awt.Color;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import com.jvn.focus.client.hud.LockOnIndicatorAnimationUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

public final class CustomIndicatorEditorScreen extends Screen {
    private static final int GRID_SIZE = CustomIndicatorPixelTexture.SIZE;
    private static final int ACCENT = 0xFFFFD45A;
    private static final int ROW_HEIGHT = 20;
    private static final int HISTORY_LIMIT = 64;

    private final Screen parent;
    private final Deque<int[]> undoHistory = new ArrayDeque<>();
    private final Deque<int[]> redoHistory = new ArrayDeque<>();
    private int[] pixels;
    private Tool tool = Tool.PENCIL;
    private float hue = 0.12F;
    private float saturation = 0.78F;
    private float brightness = 1.0F;
    private float alpha = 1.0F;
    private boolean mirrorX;
    private boolean mirrorY;
    private FocusClientConfig.CustomIndicatorAnimation animation;
    private int iconSize;
    private int offsetX;
    private int offsetY;
    private double targetHeight;
    private int orbitRadius;
    private int markerCount;
    private double orbitSpeed;
    private boolean rotateMarkers;

    private int canvasX;
    private int canvasY;
    private int cellSize;
    private int canvasSize;
    private int canvasPanelX;
    private int canvasPanelY;
    private int canvasPanelWidth;
    private int canvasPanelHeight;
    private int colorPanelX;
    private int colorPanelY;
    private int colorPanelWidth;
    private int colorPanelHeight;
    private int wheelX;
    private int wheelY;
    private int wheelSize;
    private int controlsPanelX;
    private int controlsPanelY;
    private int controlsPanelWidth;
    private int controlsPanelHeight;
    private int controlsX;
    private int controlsWidth;
    private int controlsTop;
    private int animationControlsTop;
    private int previewX;
    private int previewY;
    private int previewWidth;
    private int previewHeight;
    private int footerY;
    private boolean compactLayout;
    private boolean drawing;
    private boolean draggingWheel;
    private boolean draggingBrightness;
    private boolean draggingAlpha;
    private int lastPaintIndex = -1;

    private final List<AbstractWidget> canvasWidgets = new ArrayList<>();
    private final List<AbstractWidget> settingsWidgets = new ArrayList<>();
    private EditorTab selectedTab = EditorTab.DRAW;

    private Button pencilButton;
    private Button eraserButton;
    private Button eyedropperButton;
    private Button undoButton;
    private Button redoButton;
    private Button mirrorXButton;
    private Button mirrorYButton;
    private Button animationButton;
    private Button rotateButton;
    private Button drawTabButton;
    private Button settingsTabButton;
    private EditorSlider orbitRadiusSlider;
    private EditorSlider markerCountSlider;
    private EditorSlider orbitSpeedSlider;

    public CustomIndicatorEditorScreen(Screen parent) {
        super(Component.translatable("screen.focus.indicator_editor.title"));
        this.parent = parent;
        this.pixels = CustomIndicatorPixelTexture.decode(FocusClientConfig.customIndicatorPixelArt());
        this.animation = FocusClientConfig.customIndicatorAnimation();
        this.iconSize = FocusClientConfig.customIndicatorSize();
        this.offsetX = FocusClientConfig.customIndicatorOffsetX();
        this.offsetY = FocusClientConfig.customIndicatorOffsetY();
        this.targetHeight = FocusClientConfig.customIndicatorTargetHeight();
        this.orbitRadius = FocusClientConfig.customIndicatorOrbitRadius();
        this.markerCount = FocusClientConfig.customIndicatorOrbitMarkerCount();
        this.orbitSpeed = FocusClientConfig.customIndicatorOrbitSpeed();
        this.rotateMarkers = FocusClientConfig.customIndicatorRotateOrbitMarkers();
    }

    public static void openFromCurrentScreen() {
        Minecraft minecraft = Minecraft.getInstance();
        if (!(minecraft.screen instanceof CustomIndicatorEditorScreen)) {
            minecraft.setScreen(new CustomIndicatorEditorScreen(minecraft.screen));
        }
    }

    @Override
    protected void init() {
        super.init();
        canvasWidgets.clear();
        settingsWidgets.clear();
        footerY = this.height - 27;
        compactLayout = this.width < 720 || this.height < 430;
        if (compactLayout) {
            initCompactLayout();
        } else {
            initWideLayout();
        }

        createTabButtons();
        createCanvasButtons();
        createControlWidgets();
        createFooterButtons();
        refreshButtons();
        updateWidgetVisibility();
        CustomIndicatorPixelTexture.preview(pixels);
    }

    private void initWideLayout() {
        int margin = 10;
        int gap = 8;
        int panelTop = 52;
        int panelBottom = footerY - 7;
        int availableWidth = Math.min(this.width - margin * 2, 780);
        colorPanelWidth = Mth.clamp(availableWidth / 4, 128, 160);
        int maxCanvasByWidth = availableWidth - colorPanelWidth - gap - 12;
        int maxCanvasByHeight = panelBottom - panelTop - 84;
        cellSize = Mth.clamp(Math.min(maxCanvasByWidth / GRID_SIZE, maxCanvasByHeight / GRID_SIZE), 6, 18);
        canvasSize = cellSize * GRID_SIZE;
        canvasPanelWidth = canvasSize + 12;
        int groupWidth = canvasPanelWidth + colorPanelWidth + gap;
        canvasPanelX = (this.width - groupWidth) / 2;
        canvasPanelY = panelTop;
        canvasPanelHeight = panelBottom - panelTop;
        canvasX = canvasPanelX + 6;
        canvasY = panelTop + 32;

        colorPanelX = canvasPanelX + canvasPanelWidth + gap;
        colorPanelY = panelTop;
        colorPanelHeight = canvasPanelHeight;
        wheelSize = Math.min(128, colorPanelWidth - 16);
        wheelX = colorPanelX + (colorPanelWidth - wheelSize) / 2;
        wheelY = panelTop + 28;

        controlsPanelX = canvasPanelX;
        controlsPanelY = panelTop;
        controlsPanelWidth = canvasPanelWidth + gap + colorPanelWidth;
        controlsPanelHeight = canvasPanelHeight;
        previewX = canvasX;
        previewY = canvasY;
        previewWidth = canvasSize;
        previewHeight = canvasSize;
        controlsX = colorPanelX;
        controlsWidth = colorPanelWidth;
        controlsTop = previewY;
        animationControlsTop = controlsTop + ROW_HEIGHT * 4 + 20;
    }
    private void initCompactLayout() {
        int margin = 8;
        int gap = 7;
        int panelTop = 50;
        int panelBottom = footerY - 6;
        int toolRailWidth = 84;
        colorPanelWidth = Mth.clamp(this.width / 3, 92, 112);
        int maxCanvasByWidth = this.width - margin * 2 - gap - colorPanelWidth - toolRailWidth - 12;
        int maxCanvasByHeight = panelBottom - (panelTop + 8) - 32;
        cellSize = Mth.clamp(Math.min(maxCanvasByWidth / GRID_SIZE, maxCanvasByHeight / GRID_SIZE), 5, 12);
        canvasSize = cellSize * GRID_SIZE;
        canvasPanelWidth = canvasSize + toolRailWidth + 16;
        int drawGroupWidth = canvasPanelWidth + gap + colorPanelWidth;
        canvasPanelX = (this.width - drawGroupWidth) / 2;
        canvasPanelY = panelTop;
        canvasPanelHeight = panelBottom - panelTop;
        canvasX = canvasPanelX + 6;
        canvasY = panelTop + 8;

        colorPanelX = canvasPanelX + canvasPanelWidth + gap;
        colorPanelY = panelTop;
        colorPanelHeight = canvasPanelHeight;
        wheelSize = Mth.clamp(Math.min(colorPanelWidth - 16, colorPanelHeight - 94), 48, 90);
        wheelX = colorPanelX + (colorPanelWidth - wheelSize) / 2;
        wheelY = panelTop + 22;

        controlsPanelX = canvasPanelX;
        controlsPanelY = panelTop;
        controlsPanelWidth = canvasPanelWidth + gap + colorPanelWidth;
        controlsPanelHeight = panelBottom - panelTop;
        previewX = canvasX;
        previewY = canvasY;
        previewWidth = canvasSize;
        previewHeight = canvasSize;
        controlsX = canvasX + canvasSize + 6;
        controlsWidth = canvasPanelX + canvasPanelWidth - 6 - controlsX;
        controlsTop = previewY;
        animationControlsTop = controlsTop + ROW_HEIGHT * 2;
    }

    private void createTabButtons() {
        int tabWidth = 88;
        int tabGap = 4;
        int x = (this.width - tabWidth * 2 - tabGap) / 2;
        drawTabButton = addRenderableWidget(Button.builder(Component.empty(), button -> setTab(EditorTab.DRAW))
                .bounds(x, 27, tabWidth, 20).build());
        settingsTabButton = addRenderableWidget(Button.builder(Component.empty(), button -> setTab(EditorTab.SETTINGS))
                .bounds(x + tabWidth + tabGap, 27, tabWidth, 20).build());
    }
    private void createCanvasButtons() {
        int gap = 3;
        if (compactLayout) {
            createCompactCanvasButtons(gap);
            return;
        }
        int toolWidth = (canvasSize - gap * 2) / 3;
        int thirdWidth = canvasSize - toolWidth * 2 - gap * 2;
        int toolY = canvasY - 25;
        pencilButton = addCanvasButton(canvasX, toolY, toolWidth, 20, "screen.focus.indicator_editor.pencil_tip", button -> setTool(Tool.PENCIL));
        eraserButton = addCanvasButton(canvasX + toolWidth + gap, toolY, toolWidth, 20, "screen.focus.indicator_editor.eraser_tip", button -> setTool(Tool.ERASER));
        eyedropperButton = addCanvasButton(canvasX + (toolWidth + gap) * 2, toolY, thirdWidth, 20, "screen.focus.indicator_editor.eyedropper_tip", button -> setTool(Tool.EYEDROPPER));

        int actionY = canvasY + canvasSize + 6;
        int actionWidth = (canvasSize - gap * 3) / 4;
        int actionLastWidth = canvasSize - actionWidth * 3 - gap * 3;
        undoButton = addCanvasButton(canvasX, actionY, actionWidth, 20, null, button -> undo());
        undoButton.setMessage(Component.translatable("screen.focus.indicator_editor.undo"));
        redoButton = addCanvasButton(canvasX + actionWidth + gap, actionY, actionWidth, 20, null, button -> redo());
        redoButton.setMessage(Component.translatable("screen.focus.indicator_editor.redo"));
        Button clearButton = addCanvasButton(canvasX + (actionWidth + gap) * 2, actionY, actionWidth, 20, null, button -> clearCanvas());
        clearButton.setMessage(Component.translatable("screen.focus.indicator_editor.clear"));
        Button fillButton = addCanvasButton(canvasX + (actionWidth + gap) * 3, actionY, actionLastWidth, 20, null, button -> fillCanvas());
        fillButton.setMessage(Component.translatable("screen.focus.indicator_editor.fill"));

        int toggleY = actionY + 23;
        int toggleWidth = (canvasSize - gap) / 2;
        int toggleLastWidth = canvasSize - toggleWidth - gap;
        mirrorXButton = addCanvasButton(canvasX, toggleY, toggleWidth, 20, "screen.focus.indicator_editor.mirror_x_tip", button -> {
            mirrorX = !mirrorX;
            refreshButtons();
        });
        mirrorYButton = addCanvasButton(canvasX + toggleWidth + gap, toggleY, toggleLastWidth, 20, "screen.focus.indicator_editor.mirror_y_tip", button -> {
            mirrorY = !mirrorY;
            refreshButtons();
        });
    }

    private void createCompactCanvasButtons(int gap) {
        int railX = canvasX + canvasSize + 6;
        int buttonWidth = canvasPanelX + canvasPanelWidth - 6 - railX;
        int buttonHeight = 20;
        int rowGap = 2;
        pencilButton = addCanvasButton(railX, canvasY, buttonWidth, buttonHeight,
                "screen.focus.indicator_editor.pencil_tip", button -> setTool(Tool.PENCIL));
        eraserButton = addCanvasButton(railX, canvasY + buttonHeight + rowGap, buttonWidth, buttonHeight,
                "screen.focus.indicator_editor.eraser_tip", button -> setTool(Tool.ERASER));
        eyedropperButton = addCanvasButton(railX, canvasY + (buttonHeight + rowGap) * 2, buttonWidth, buttonHeight,
                "screen.focus.indicator_editor.eyedropper_tip", button -> setTool(Tool.EYEDROPPER));

        int togglesY = canvasY + (buttonHeight + rowGap) * 3 + 5;
        mirrorXButton = addCanvasButton(railX, togglesY, buttonWidth, buttonHeight,
                "screen.focus.indicator_editor.mirror_x_tip", button -> {
            mirrorX = !mirrorX;
            refreshButtons();
        });
        mirrorYButton = addCanvasButton(railX, togglesY + buttonHeight + rowGap, buttonWidth, buttonHeight,
                "screen.focus.indicator_editor.mirror_y_tip", button -> {
            mirrorY = !mirrorY;
            refreshButtons();
        });

        int actionY = canvasY + canvasSize + 4;
        int actionWidth = (canvasSize - gap * 3) / 4;
        int actionLastWidth = canvasSize - actionWidth * 3 - gap * 3;
        undoButton = addCanvasButton(canvasX, actionY, actionWidth, 20,
                "screen.focus.indicator_editor.undo_tip", button -> undo());
        undoButton.setMessage(Component.translatable("screen.focus.indicator_editor.undo"));
        redoButton = addCanvasButton(canvasX + actionWidth + gap, actionY, actionWidth, 20,
                "screen.focus.indicator_editor.redo_tip", button -> redo());
        redoButton.setMessage(Component.translatable("screen.focus.indicator_editor.redo"));
        Button clearButton = addCanvasButton(canvasX + (actionWidth + gap) * 2, actionY, actionWidth, 20,
                "screen.focus.indicator_editor.clear", button -> clearCanvas());
        clearButton.setMessage(Component.translatable("screen.focus.indicator_editor.clear"));
        Button fillButton = addCanvasButton(canvasX + (actionWidth + gap) * 3, actionY, actionLastWidth, 20,
                "screen.focus.indicator_editor.fill", button -> fillCanvas());
        fillButton.setMessage(Component.translatable("screen.focus.indicator_editor.fill"));
    }
    private Button addCanvasButton(int x, int y, int width, int height, String tooltipKey, Button.OnPress onPress) {
        Button.Builder builder = Button.builder(Component.empty(), onPress).bounds(x, y, width, height);
        if (tooltipKey != null) {
            builder.tooltip(Tooltip.create(Component.translatable(tooltipKey)));
        }
        Button button = builder.build();
        canvasWidgets.add(button);
        return addRenderableWidget(button);
    }

    private void createControlWidgets() {
        if (compactLayout) {
            createCompactControlWidgets();
        } else {
            createWideControlWidgets();
        }
    }

    private void createWideControlWidgets() {
        int y = controlsTop;
        addSettingSlider(controlsX, y, controlsWidth, "screen.focus.indicator_editor.size",
                1, 128, () -> iconSize, value -> iconSize = (int) Math.round(value), 0);
        y += ROW_HEIGHT;
        addSettingSlider(controlsX, y, controlsWidth, "screen.focus.indicator_editor.offset_x",
                -128, 128, () -> offsetX, value -> offsetX = (int) Math.round(value), 0);
        y += ROW_HEIGHT;
        addSettingSlider(controlsX, y, controlsWidth, "screen.focus.indicator_editor.offset_y",
                -128, 128, () -> offsetY, value -> offsetY = (int) Math.round(value), 0);
        y += ROW_HEIGHT;
        addSettingSlider(controlsX, y, controlsWidth, "screen.focus.indicator_editor.target_height",
                0, 1.5, () -> targetHeight, value -> targetHeight = value, 2);

        y = animationControlsTop;
        animationButton = addSettingButton(controlsX, y, controlsWidth, null, button -> toggleAnimation());
        y += ROW_HEIGHT;
        orbitRadiusSlider = addSettingSlider(controlsX, y, controlsWidth, "screen.focus.indicator_editor.orbit_radius",
                0, 128, () -> orbitRadius, value -> orbitRadius = (int) Math.round(value), 0);
        y += ROW_HEIGHT;
        markerCountSlider = addSettingSlider(controlsX, y, controlsWidth, "screen.focus.indicator_editor.marker_count",
                1, 12, () -> markerCount, value -> markerCount = (int) Math.round(value), 0);
        y += ROW_HEIGHT;
        orbitSpeedSlider = addSettingSlider(controlsX, y, controlsWidth, "screen.focus.indicator_editor.orbit_speed",
                -30, 30, () -> orbitSpeed, value -> orbitSpeed = value, 1);
        y += ROW_HEIGHT;
        rotateButton = addSettingButton(controlsX, y, controlsWidth, "screen.focus.indicator_editor.rotate_markers_tip", button -> toggleMarkerRotation());
    }

    private void createCompactControlWidgets() {
        int y = controlsTop;
        addSettingSlider(controlsX, y, controlsWidth, "screen.focus.indicator_editor.size_compact",
                1, 128, () -> iconSize, value -> iconSize = (int) Math.round(value), 0);
        y += ROW_HEIGHT;
        addSettingSlider(controlsX, y, controlsWidth, "screen.focus.indicator_editor.target_height_compact",
                0, 1.5, () -> targetHeight, value -> targetHeight = value, 2);
        y += ROW_HEIGHT;
        addSettingSlider(controlsX, y, controlsWidth, "screen.focus.indicator_editor.offset_x_compact",
                -128, 128, () -> offsetX, value -> offsetX = (int) Math.round(value), 0);
        y += ROW_HEIGHT;
        addSettingSlider(controlsX, y, controlsWidth, "screen.focus.indicator_editor.offset_y_compact",
                -128, 128, () -> offsetY, value -> offsetY = (int) Math.round(value), 0);
        y += ROW_HEIGHT;
        animationButton = addSettingButton(controlsX, y, controlsWidth, null, button -> toggleAnimation());
        y += ROW_HEIGHT;
        orbitRadiusSlider = addSettingSlider(controlsX, y, controlsWidth, "screen.focus.indicator_editor.orbit_radius_compact",
                0, 128, () -> orbitRadius, value -> orbitRadius = (int) Math.round(value), 0);
        y += ROW_HEIGHT;
        markerCountSlider = addSettingSlider(controlsX, y, controlsWidth, "screen.focus.indicator_editor.marker_count_compact",
                1, 12, () -> markerCount, value -> markerCount = (int) Math.round(value), 0);
        y += ROW_HEIGHT;
        orbitSpeedSlider = addSettingSlider(controlsX, y, controlsWidth, "screen.focus.indicator_editor.orbit_speed_compact",
                -30, 30, () -> orbitSpeed, value -> orbitSpeed = value, 1);
        y += ROW_HEIGHT;
        rotateButton = addSettingButton(controlsX, y, controlsWidth,
                "screen.focus.indicator_editor.rotate_markers_tip", button -> toggleMarkerRotation());
    }

    private EditorSlider addSettingSlider(int x, int y, int width, String labelKey, double min, double max,
            DoubleSupplier getter, DoubleConsumer setter, int precision) {
        EditorSlider slider = new EditorSlider(x, y, width, labelKey, min, max, getter, setter, precision);
        settingsWidgets.add(slider);
        return addRenderableWidget(slider);
    }

    private Button addSettingButton(int x, int y, int width, String tooltipKey, Button.OnPress onPress) {
        Button.Builder builder = Button.builder(Component.empty(), onPress).bounds(x, y, width, 20);
        if (tooltipKey != null) {
            builder.tooltip(Tooltip.create(Component.translatable(tooltipKey)));
        }
        Button button = builder.build();
        settingsWidgets.add(button);
        return addRenderableWidget(button);
    }

    private void createFooterButtons() {
        int width = Math.min(this.width - 16, 480);
        int x = (this.width - width) / 2;
        int gap = 4;
        int third = (width - gap * 2) / 3;
        addRenderableWidget(Button.builder(Component.translatable("screen.focus.indicator_editor.reset"), button -> resetCanvas())
                .tooltip(Tooltip.create(Component.translatable("screen.focus.indicator_editor.reset_tip")))
                .bounds(x, footerY, third, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), button -> cancel())
                .bounds(x + third + gap, footerY, third, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.focus.indicator_editor.save"), button -> applyAndClose())
                .bounds(x + (third + gap) * 2, footerY, width - third * 2 - gap * 2, 20).build());
    }
    private void toggleAnimation() {
        animation = animation == FocusClientConfig.CustomIndicatorAnimation.STATIC
                ? FocusClientConfig.CustomIndicatorAnimation.ORBIT
                : FocusClientConfig.CustomIndicatorAnimation.STATIC;
        refreshButtons();
    }

    private void toggleMarkerRotation() {
        rotateMarkers = !rotateMarkers;
        refreshButtons();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderTransparentBackground(graphics);

        boolean showCanvas = selectedTab == EditorTab.DRAW;
        boolean showSettings = selectedTab == EditorTab.SETTINGS;
        if (showCanvas) {
            renderCanvas(graphics, mouseX, mouseY);
            renderColorControls(graphics);
        }
        if (showSettings) {
            renderPreview(graphics, partialTick);
        }
        renderSectionLabels(graphics);
        refreshHistoryButtons();
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 9, 0xFFFFFFFF);
    }

    private void renderSectionLabels(GuiGraphics graphics) {
        if (selectedTab == EditorTab.DRAW) {
            if (!compactLayout) {
                drawOutlinedText(graphics, Component.translatable("screen.focus.indicator_editor.canvas"),
                        canvasPanelX + 8, canvasPanelY + 8);
            }
            drawCenteredOutlinedText(graphics, Component.translatable("screen.focus.indicator_editor.color"),
                    colorPanelX + colorPanelWidth / 2, colorPanelY + 8);
            return;
        }

        if (previewHeight > 0) {
            drawOutlinedText(graphics, Component.translatable("screen.focus.indicator_editor.preview"),
                    previewX + 5, previewY + 5);
        }
        if (!compactLayout) {
            drawCenteredOutlinedText(graphics, Component.translatable("screen.focus.indicator_editor.placement"),
                    controlsX + controlsWidth / 2, controlsTop - 12);
            drawCenteredOutlinedText(graphics, Component.translatable("screen.focus.indicator_editor.animation_settings"),
                    controlsX + controlsWidth / 2, animationControlsTop - 12);
        }
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Rendered explicitly before custom content; suppress Screen.render's blurred pass.
    }
    private void renderCanvas(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.fill(canvasX - 2, canvasY - 2, canvasX + canvasSize + 2, canvasY + canvasSize + 2, 0xFF000000);
        renderEditorBackground(graphics, canvasX, canvasY, cellSize);
        for (int y = 0; y < GRID_SIZE; y++) {
            for (int x = 0; x < GRID_SIZE; x++) {
                int left = canvasX + x * cellSize;
                int top = canvasY + y * cellSize;
                int color = pixels[y * GRID_SIZE + x];
                if ((color >>> 24) != 0) {
                    graphics.fill(left, top, left + cellSize, top + cellSize,
                            compositeOver(color, editorBackgroundColor(x, y)));
                }
            }
        }

        if (mirrorX) {
            int guideX = canvasX + canvasSize / 2;
            graphics.fill(guideX, canvasY, guideX + 1, canvasY + canvasSize, 0x88FFD45A);
        }
        if (mirrorY) {
            int guideY = canvasY + canvasSize / 2;
            graphics.fill(canvasX, guideY, canvasX + canvasSize, guideY + 1, 0x88FFD45A);
        }
        if (inside(mouseX, mouseY, canvasX, canvasY, canvasSize, canvasSize)) {
            int x = Mth.clamp((mouseX - canvasX) / cellSize, 0, GRID_SIZE - 1);
            int y = Mth.clamp((mouseY - canvasY) / cellSize, 0, GRID_SIZE - 1);
            int left = canvasX + x * cellSize;
            int top = canvasY + y * cellSize;
            graphics.fill(left, top, left + cellSize, top + 1, ACCENT);
            graphics.fill(left, top + cellSize - 1, left + cellSize, top + cellSize, ACCENT);
            graphics.fill(left, top, left + 1, top + cellSize, ACCENT);
            graphics.fill(left + cellSize - 1, top, left + cellSize, top + cellSize, ACCENT);
        }
    }

    private void renderColorControls(GuiGraphics graphics) {
        float radius = wheelSize * 0.5F;
        float centerX = wheelX + radius;
        float centerY = wheelY + radius;
        for (int y = 0; y < wheelSize; y += 2) {
            for (int x = 0; x < wheelSize; x += 2) {
                float dx = x + 1 - radius;
                float dy = y + 1 - radius;
                float distance = (float) Math.sqrt(dx * dx + dy * dy);
                if (distance <= radius) {
                    float sampleHue = (float) ((Math.atan2(dy, dx) / (Math.PI * 2.0D) + 1.0D) % 1.0D);
                    float sampleSaturation = Mth.clamp(distance / radius, 0.0F, 1.0F);
                    int color = 0xFF000000 | Color.HSBtoRGB(sampleHue, sampleSaturation, brightness) & 0xFFFFFF;
                    graphics.fill(wheelX + x, wheelY + y, wheelX + x + 2, wheelY + y + 2, color);
                }
            }
        }
        double angle = hue * Math.PI * 2.0D;
        int cursorX = Math.round(centerX + (float) Math.cos(angle) * saturation * radius);
        int cursorY = Math.round(centerY + (float) Math.sin(angle) * saturation * radius);
        graphics.fill(cursorX - 3, cursorY - 1, cursorX + 4, cursorY + 2, 0xFF000000);
        graphics.fill(cursorX - 1, cursorY - 3, cursorX + 2, cursorY + 4, 0xFF000000);
        graphics.fill(cursorX - 2, cursorY, cursorX + 3, cursorY + 1, 0xFFFFFFFF);
        graphics.fill(cursorX, cursorY - 2, cursorX + 1, cursorY + 3, 0xFFFFFFFF);

        int sliderX = colorPanelX + 8;
        int sliderWidth = colorPanelWidth - 16;
        int brightnessY = compactLayout ? wheelY + wheelSize + 6 : wheelY + wheelSize + 12;
        drawOutlinedText(graphics, Component.translatable("screen.focus.indicator_editor.brightness"),
                sliderX, brightnessY);
        renderGradient(graphics, sliderX, brightnessY + 10, sliderWidth, false);
        int alphaY = brightnessY + (compactLayout ? 24 : 31);
        drawOutlinedText(graphics, Component.translatable("screen.focus.indicator_editor.alpha"),
                sliderX, alphaY);
        renderGradient(graphics, sliderX, alphaY + 10, sliderWidth, true);

        int swatchY = alphaY + (compactLayout ? 24 : 31);
        int swatchHeight = compactLayout ? 18 : 22;
        for (int x = 0; x < sliderWidth; x += 8) {
            int checker = ((x / 8) & 1) == 0 ? 0xFF35353A : 0xFF27272B;
            graphics.fill(sliderX + x, swatchY, Math.min(sliderX + x + 8, sliderX + sliderWidth),
                    swatchY + swatchHeight, compositeOver(currentColor(), checker));
        }
        drawCenteredOutlinedText(graphics, Component.literal(hexColor()), colorPanelX + colorPanelWidth / 2,
                swatchY + (compactLayout ? 5 : 7));
        if (!compactLayout) {
            drawCenteredOutlinedText(graphics, Component.translatable("screen.focus.indicator_editor.mouse_hint"),
                    colorPanelX + colorPanelWidth / 2, swatchY + 29);
        }
    }

    private void renderGradient(GuiGraphics graphics, int x, int y, int width, boolean alphaGradient) {
        for (int i = 0; i < width; i++) {
            float amount = i / (float) Math.max(1, width - 1);
            int color;
            if (alphaGradient) {
                int argb = (Math.round(amount * 255.0F) << 24) | (currentColor() & 0xFFFFFF);
                color = compositeOver(argb, ((i / 5) & 1) == 0 ? 0xFF3A3A40 : 0xFF25252A);
            } else {
                color = 0xFF000000 | Color.HSBtoRGB(hue, saturation, amount) & 0xFFFFFF;
            }
            graphics.fill(x + i, y, x + i + 1, y + 8, color);
        }
        float value = alphaGradient ? alpha : brightness;
        int markerX = x + Math.round(value * (width - 1));
        graphics.fill(markerX - 1, y - 2, markerX + 2, y + 10, 0xFFFFFFFF);
        graphics.fill(markerX, y - 1, markerX + 1, y + 9, 0xFF111111);
    }

    private void renderPreview(GuiGraphics graphics, float partialTick) {
        if (previewHeight <= 0) {
            return;
        }

        graphics.fill(previewX - 2, previewY - 2, previewX + previewWidth + 2, previewY + previewHeight + 2, 0xFF000000);
        renderEditorBackground(graphics, previewX, previewY, cellSize);
        graphics.enableScissor(previewX, previewY, previewX + previewWidth, previewY + previewHeight);

        float anchorX = previewX + previewWidth * 0.5F;
        float anchorY = previewY + previewHeight * 0.5F;
        renderPreviewCrosshair(graphics, Math.round(anchorX), Math.round(anchorY));
        float centerX = anchorX + offsetX;
        float centerY = anchorY + offsetY;
        ResourceLocation texture = CustomIndicatorPixelTexture.preview(pixels);
        float animationTicks = (System.currentTimeMillis() % 100000L) / 50.0F + partialTick;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(
                FocusClientConfig.customIndicatorRed(),
                FocusClientConfig.customIndicatorGreen(),
                FocusClientConfig.customIndicatorBlue(),
                FocusClientConfig.customIndicatorAlpha());

        if (animation == FocusClientConfig.CustomIndicatorAnimation.ORBIT) {
            LockOnIndicatorAnimationUtil.renderOrbit(
                    graphics,
                    centerX,
                    centerY,
                    texture,
                    iconSize,
                    animationTicks,
                    GRID_SIZE,
                    markerCount,
                    orbitRadius,
                    (float) orbitSpeed,
                    rotateMarkers);
        } else {
            LockOnIndicatorAnimationUtil.renderCentered(
                    graphics,
                    centerX,
                    centerY,
                    texture,
                    iconSize,
                    GRID_SIZE);
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
        graphics.disableScissor();
    }
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean canvasVisible = selectedTab == EditorTab.DRAW;
        if (canvasVisible && inside(mouseX, mouseY, canvasX, canvasY, canvasSize, canvasSize)) {
            beginCanvasAction(mouseX, mouseY, button);
            return true;
        }
        if (canvasVisible && inside(mouseX, mouseY, wheelX, wheelY, wheelSize, wheelSize)) {
            draggingWheel = updateWheel(mouseX, mouseY);
            return draggingWheel;
        }
        int sliderX = colorPanelX + 8;
        int sliderWidth = colorPanelWidth - 16;
        int brightnessY = wheelY + wheelSize + (compactLayout ? 16 : 22);
        if (canvasVisible && inside(mouseX, mouseY, sliderX, brightnessY, sliderWidth, 10)) {
            draggingBrightness = true;
            updateBrightness(mouseX);
            return true;
        }
        int alphaY = brightnessY + (compactLayout ? 24 : 31);
        if (canvasVisible && inside(mouseX, mouseY, sliderX, alphaY, sliderWidth, 10)) {
            draggingAlpha = true;
            updateAlpha(mouseX);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (drawing) {
            paintAt(mouseX, mouseY, tool, false);
            return true;
        }
        if (draggingWheel) {
            updateWheel(mouseX, mouseY);
            return true;
        }
        if (draggingBrightness) {
            updateBrightness(mouseX);
            return true;
        }
        if (draggingAlpha) {
            updateAlpha(mouseX);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        drawing = false;
        draggingWheel = false;
        draggingBrightness = false;
        draggingAlpha = false;
        lastPaintIndex = -1;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (Screen.hasControlDown() && keyCode == GLFW.GLFW_KEY_Z) {
            if (Screen.hasShiftDown()) redo(); else undo();
            return true;
        }
        if (Screen.hasControlDown() && keyCode == GLFW.GLFW_KEY_Y) {
            redo();
            return true;
        }
        if (!Screen.hasControlDown()) {
            if (keyCode == GLFW.GLFW_KEY_B) {
                setTab(EditorTab.DRAW);
                setTool(Tool.PENCIL);
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_E) {
                setTab(EditorTab.DRAW);
                setTool(Tool.ERASER);
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_I) {
                setTab(EditorTab.DRAW);
                setTool(Tool.EYEDROPPER);
                return true;
            }

            if (keyCode == GLFW.GLFW_KEY_X) {
                mirrorX = !mirrorX;
                refreshButtons();
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_Y) {
                mirrorY = !mirrorY;
                refreshButtons();
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void beginCanvasAction(double mouseX, double mouseY, int button) {
        Tool activeTool = button == 1 ? Tool.ERASER : button == 2 ? Tool.EYEDROPPER : tool;
        if (activeTool != Tool.EYEDROPPER) {
            rememberForUndo();
            drawing = true;
        }
        paintAt(mouseX, mouseY, activeTool, true);
    }

    private void paintAt(double mouseX, double mouseY, Tool activeTool, boolean first) {
        if (!inside(mouseX, mouseY, canvasX, canvasY, canvasSize, canvasSize)) {
            return;
        }
        int x = Mth.clamp((int) ((mouseX - canvasX) / cellSize), 0, GRID_SIZE - 1);
        int y = Mth.clamp((int) ((mouseY - canvasY) / cellSize), 0, GRID_SIZE - 1);
        int index = y * GRID_SIZE + x;
        if (!first && index == lastPaintIndex) {
            return;
        }
        lastPaintIndex = index;
        if (activeTool == Tool.EYEDROPPER) {
            sampleColor(pixels[index]);
            setTool(Tool.PENCIL);
            return;
        }
        int color = activeTool == Tool.ERASER ? 0 : currentColor();
        setMirroredPixel(x, y, color);
        CustomIndicatorPixelTexture.preview(pixels);
    }

    private void setMirroredPixel(int x, int y, int color) {
        pixels[y * GRID_SIZE + x] = color;
        if (mirrorX) pixels[y * GRID_SIZE + (GRID_SIZE - 1 - x)] = color;
        if (mirrorY) pixels[(GRID_SIZE - 1 - y) * GRID_SIZE + x] = color;
        if (mirrorX && mirrorY) pixels[(GRID_SIZE - 1 - y) * GRID_SIZE + (GRID_SIZE - 1 - x)] = color;
    }

    private boolean updateWheel(double mouseX, double mouseY) {
        float radius = wheelSize * 0.5F;
        double dx = mouseX - (wheelX + radius);
        double dy = mouseY - (wheelY + radius);
        double distance = Math.sqrt(dx * dx + dy * dy);
        if (distance > radius + 2) return false;
        hue = (float) ((Math.atan2(dy, dx) / (Math.PI * 2.0D) + 1.0D) % 1.0D);
        saturation = Mth.clamp((float) (distance / radius), 0.0F, 1.0F);
        return true;
    }

    private void updateBrightness(double mouseX) {
        int x = colorPanelX + 8;
        int width = colorPanelWidth - 16;
        brightness = Mth.clamp((float) ((mouseX - x) / (width - 1)), 0.0F, 1.0F);
    }

    private void updateAlpha(double mouseX) {
        int x = colorPanelX + 8;
        int width = colorPanelWidth - 16;
        alpha = Mth.clamp((float) ((mouseX - x) / (width - 1)), 0.0F, 1.0F);
    }

    private int currentColor() {
        return (Math.round(alpha * 255.0F) << 24) | (Color.HSBtoRGB(hue, saturation, brightness) & 0xFFFFFF);
    }

    private void sampleColor(int color) {
        if ((color >>> 24) == 0) return;
        float[] hsb = Color.RGBtoHSB(color >>> 16 & 0xFF, color >>> 8 & 0xFF, color & 0xFF, null);
        hue = hsb[0];
        saturation = hsb[1];
        brightness = hsb[2];
        alpha = (color >>> 24) / 255.0F;
    }

    private void rememberForUndo() {
        undoHistory.push(Arrays.copyOf(pixels, pixels.length));
        while (undoHistory.size() > HISTORY_LIMIT) undoHistory.removeLast();
        redoHistory.clear();
    }

    private void undo() {
        if (undoHistory.isEmpty()) return;
        redoHistory.push(Arrays.copyOf(pixels, pixels.length));
        pixels = undoHistory.pop();
        CustomIndicatorPixelTexture.preview(pixels);
    }

    private void redo() {
        if (redoHistory.isEmpty()) return;
        undoHistory.push(Arrays.copyOf(pixels, pixels.length));
        pixels = redoHistory.pop();
        CustomIndicatorPixelTexture.preview(pixels);
    }

    private void clearCanvas() {
        rememberForUndo();
        Arrays.fill(pixels, 0);
        CustomIndicatorPixelTexture.preview(pixels);
    }

    private void fillCanvas() {
        rememberForUndo();
        Arrays.fill(pixels, tool == Tool.ERASER ? 0 : currentColor());
        CustomIndicatorPixelTexture.preview(pixels);
    }

    private void resetCanvas() {
        rememberForUndo();
        pixels = CustomIndicatorPixelTexture.defaultPixels();
        CustomIndicatorPixelTexture.preview(pixels);
    }

    private void setTool(Tool tool) {
        this.tool = tool;
        refreshButtons();
    }

    private void setTab(EditorTab tab) {
        selectedTab = tab;
        refreshButtons();
        updateWidgetVisibility();
    }

    private void updateWidgetVisibility() {
        boolean showCanvasWidgets = selectedTab == EditorTab.DRAW;
        boolean showSettingsWidgets = selectedTab == EditorTab.SETTINGS;
        for (AbstractWidget widget : canvasWidgets) {
            widget.visible = showCanvasWidgets;
        }
        for (AbstractWidget widget : settingsWidgets) {
            widget.visible = showSettingsWidgets;
        }
        if (drawTabButton != null) drawTabButton.visible = true;
        if (settingsTabButton != null) settingsTabButton.visible = true;
        refreshOrbitControlState();
    }

    private void refreshButtons() {
        if (compactLayout) {
            if (pencilButton != null) pencilButton.setMessage(toolLabel("screen.focus.indicator_editor.pencil", "", tool == Tool.PENCIL));
            if (eraserButton != null) eraserButton.setMessage(toolLabel("screen.focus.indicator_editor.eraser", "", tool == Tool.ERASER));
            if (eyedropperButton != null) eyedropperButton.setMessage(toolLabel("screen.focus.indicator_editor.eyedropper", "", tool == Tool.EYEDROPPER));
            if (mirrorXButton != null) mirrorXButton.setMessage(toggleLabel("screen.focus.indicator_editor.mirror_x", mirrorX));
            if (mirrorYButton != null) mirrorYButton.setMessage(toggleLabel("screen.focus.indicator_editor.mirror_y", mirrorY));
        } else {
            if (pencilButton != null) pencilButton.setMessage(toolLabel("screen.focus.indicator_editor.pencil", "B", tool == Tool.PENCIL));
            if (eraserButton != null) eraserButton.setMessage(toolLabel("screen.focus.indicator_editor.eraser", "E", tool == Tool.ERASER));
            if (eyedropperButton != null) eyedropperButton.setMessage(toolLabel("screen.focus.indicator_editor.eyedropper", "I", tool == Tool.EYEDROPPER));
            if (mirrorXButton != null) mirrorXButton.setMessage(toggleLabel("screen.focus.indicator_editor.mirror_x", mirrorX));
            if (mirrorYButton != null) mirrorYButton.setMessage(toggleLabel("screen.focus.indicator_editor.mirror_y", mirrorY));
        }
        if (animationButton != null) animationButton.setMessage(Component.translatable(
                compactLayout ? "screen.focus.indicator_editor.animation_compact" : "screen.focus.indicator_editor.animation",
                Component.translatable(animation.getSerializedName())));
        if (rotateButton != null) rotateButton.setMessage(toggleLabel(
                compactLayout ? "screen.focus.indicator_editor.rotate_markers_compact" : "screen.focus.indicator_editor.rotate_markers",
                rotateMarkers));
        if (drawTabButton != null) drawTabButton.setMessage(tabLabel("screen.focus.indicator_editor.tab_draw", selectedTab == EditorTab.DRAW));
        if (settingsTabButton != null) settingsTabButton.setMessage(tabLabel("screen.focus.indicator_editor.tab_settings", selectedTab == EditorTab.SETTINGS));
        refreshOrbitControlState();
    }

    private void refreshOrbitControlState() {
        boolean orbitActive = animation == FocusClientConfig.CustomIndicatorAnimation.ORBIT;
        if (orbitRadiusSlider != null) orbitRadiusSlider.active = orbitActive;
        if (markerCountSlider != null) markerCountSlider.active = orbitActive;
        if (orbitSpeedSlider != null) orbitSpeedSlider.active = orbitActive;
        if (rotateButton != null) rotateButton.active = orbitActive;
    }

    private Component toolLabel(String key, String shortcut, boolean active) {
        return Component.literal(active ? "> " : "  ")
                .append(Component.translatable(key))
                .append(shortcut.isEmpty() ? Component.empty() : Component.literal(" (" + shortcut + ")"));
    }

    private Component compactToolLabel(String shortcut, boolean active) {
        return Component.literal(active ? ">" + shortcut : shortcut);
    }

    private Component tabLabel(String key, boolean active) {
        return Component.literal(active ? "> " : "").append(Component.translatable(key));
    }

    private void refreshHistoryButtons() {
        if (undoButton != null) undoButton.active = !undoHistory.isEmpty();
        if (redoButton != null) redoButton.active = !redoHistory.isEmpty();
    }

    private Component toggleLabel(String key, boolean active) {
        return Component.translatable(key, Component.translatable(active
                ? "screen.focus.indicator_editor.on"
                : "screen.focus.indicator_editor.off"));
    }

    private void applyAndClose() {
        FocusClientConfig.applyCustomIndicatorEditorState(new FocusClientConfig.CustomIndicatorEditorState(
                CustomIndicatorPixelTexture.encode(pixels), animation, iconSize, offsetX, offsetY,
                targetHeight, orbitRadius, markerCount, orbitSpeed, rotateMarkers));
        Minecraft.getInstance().setScreen(parent);
    }

    private void cancel() {
        CustomIndicatorPixelTexture.invalidate();
        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public void onClose() {
        cancel();
    }

    private String hexColor() {
        return String.format(Locale.ROOT, "#%06X  %d%%", currentColor() & 0xFFFFFF, Math.round(alpha * 100.0F));
    }

    private static void renderEditorBackground(GuiGraphics graphics, int originX, int originY, int tileSize) {
        for (int y = 0; y < GRID_SIZE; y++) {
            for (int x = 0; x < GRID_SIZE; x++) {
                int left = originX + x * tileSize;
                int top = originY + y * tileSize;
                graphics.fill(left, top, left + tileSize, top + tileSize, editorBackgroundColor(x, y));
            }
        }
    }

    private static int editorBackgroundColor(int x, int y) {
        return ((x + y) & 1) == 0 ? 0xFF343439 : 0xFF29292E;
    }
    private static void renderPreviewCrosshair(GuiGraphics graphics, int centerX, int centerY) {
        graphics.fill(centerX - 5, centerY - 1, centerX + 6, centerY + 2, 0xFF000000);
        graphics.fill(centerX - 1, centerY - 5, centerX + 2, centerY + 6, 0xFF000000);
        graphics.fill(centerX - 4, centerY, centerX + 5, centerY + 1, 0xFFFFFFFF);
        graphics.fill(centerX, centerY - 4, centerX + 1, centerY + 5, 0xFFFFFFFF);
    }
    private void drawCenteredOutlinedText(GuiGraphics graphics, Component text, int centerX, int y) {
        drawOutlinedText(graphics, text, centerX - this.font.width(text) / 2, y);
    }

    private void drawOutlinedText(GuiGraphics graphics, Component text, int x, int y) {
        for (int outlineY = -1; outlineY <= 1; outlineY++) {
            for (int outlineX = -1; outlineX <= 1; outlineX++) {
                if (outlineX != 0 || outlineY != 0) {
                    graphics.drawString(this.font, text, x + outlineX, y + outlineY, 0xFF000000, false);
                }
            }
        }
        graphics.drawString(this.font, text, x, y, 0xFFFFFFFF, false);
    }

    private static int compositeOver(int foreground, int background) {
        int alpha = foreground >>> 24;
        if (alpha == 255) return foreground;
        if (alpha == 0) return background;
        int inverse = 255 - alpha;
        int red = ((foreground >>> 16 & 0xFF) * alpha + (background >>> 16 & 0xFF) * inverse) / 255;
        int green = ((foreground >>> 8 & 0xFF) * alpha + (background >>> 8 & 0xFF) * inverse) / 255;
        int blue = ((foreground & 0xFF) * alpha + (background & 0xFF) * inverse) / 255;
        return 0xFF000000 | red << 16 | green << 8 | blue;
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private enum EditorTab { DRAW, SETTINGS }

    private enum Tool { PENCIL, ERASER, EYEDROPPER }

    private static final class EditorSlider extends AbstractSliderButton {
        private final String labelKey;
        private final double min;
        private final double max;
        private final DoubleSupplier getter;
        private final DoubleConsumer setter;
        private final int precision;

        private EditorSlider(int x, int y, int width, String labelKey, double min, double max,
                DoubleSupplier getter, DoubleConsumer setter, int precision) {
            super(x, y, width, 20, Component.empty(), 0.0D);
            this.labelKey = labelKey;
            this.min = min;
            this.max = max;
            this.getter = getter;
            this.setter = setter;
            this.precision = precision;
            this.value = Mth.clamp((getter.getAsDouble() - min) / (max - min), 0.0D, 1.0D);
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            double actual = Mth.clampedLerp(min, max, value);
            setMessage(Component.translatable(labelKey, String.format(Locale.ROOT, "%." + precision + "f", actual)));
        }

        @Override
        protected void applyValue() {
            setter.accept(Mth.clampedLerp(min, max, value));
        }
    }
}
