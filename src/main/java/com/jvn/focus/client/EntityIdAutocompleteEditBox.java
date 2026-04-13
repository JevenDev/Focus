package com.jvn.focus.client;

import com.jvn.focus.mixin.EntryInfoAccessor;
import eu.midnightdust.lib.config.EntryInfo;
import eu.midnightdust.lib.config.ButtonEntry;
import eu.midnightdust.lib.config.MidnightConfigScreen;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

public final class EntityIdAutocompleteEditBox extends EditBox {
    private static final int ROW_HEIGHT = 12;
    private static final int VISIBLE_ROWS = 3;
    private static final int MAX_TOTAL_MATCHES = 300;
    private static final int BORDER_COLOR = 0xFF5E5E5E;
    private static final int BACKGROUND_COLOR = 0xEE101010;
    private static final int HIGHLIGHT_COLOR = 0xFF2A4B6E;
    private static final int TEXT_COLOR = 0xFFECECEC;

    private final Font font;
    private final EntryInfo info;
    private final List<String> matches = new ArrayList<>();
    private String lastQuery = "";
    private int selectedIndex = -1;
    private int scrollOffset;

    public EntityIdAutocompleteEditBox(Font font, int x, int y, int width, int height, Component message, EntryInfo info) {
        super(font, x, y, width, height, message);
        this.font = font;
        this.info = info;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!isFocused()) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        refreshMatches();
        if (hasVisibleDropdown()) {
            if (keyCode == GLFW.GLFW_KEY_DOWN) {
                moveSelection(1);
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_UP) {
                moveSelection(-1);
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_TAB || keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                addSelectionOrTypedValue();
                return true;
            }
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            addSelectionOrTypedValue();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (handleDropdownClick(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (handleDropdownScroll(mouseX, mouseY, scrollY)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        refreshMatches();
        if (!hasVisibleDropdown()) {
            return;
        }

        int left = getX();
        int right = getX() + getWidth();
        int top = getY() + getHeight() + 1;
        int rows = visibleRowCount();
        int bottom = top + (rows * ROW_HEIGHT);
        guiGraphics.fill(left - 1, top - 1, right + 1, bottom + 1, BORDER_COLOR);
        guiGraphics.fill(left, top, right, bottom, BACKGROUND_COLOR);

        for (int row = 0; row < rows; row++) {
            int index = this.scrollOffset + row;
            if (index >= this.matches.size()) {
                break;
            }

            int rowTop = top + (row * ROW_HEIGHT);
            int rowBottom = rowTop + ROW_HEIGHT;
            boolean hovered = mouseX >= left && mouseX <= right && mouseY >= rowTop && mouseY < rowBottom;
            if (index == this.selectedIndex || hovered) {
                guiGraphics.fill(left + 1, rowTop, right - 1, rowBottom, HIGHLIGHT_COLOR);
            }

            String candidate = this.matches.get(index);
            String clipped = this.font.plainSubstrByWidth(candidate, Math.max(12, getWidth() - 8));
            guiGraphics.drawString(this.font, clipped, left + 4, rowTop + 2, TEXT_COLOR, false);
        }
    }

    private void refreshMatches() {
        String query = getValue().trim().toLowerCase(Locale.ROOT);
        this.lastQuery = query;
        this.matches.clear();

        List<String> selected = selectedIds();
        for (String entityId : FocusClientConfig.availableMobEntityTypeIds()) {
            if (selected.contains(entityId)) {
                continue;
            }
            if (!query.isEmpty() && !entityId.contains(query)) {
                continue;
            }
            this.matches.add(entityId);
            if (this.matches.size() >= MAX_TOTAL_MATCHES) {
                break;
            }
        }

        if (this.matches.isEmpty()) {
            this.selectedIndex = -1;
            this.scrollOffset = 0;
            return;
        }

        if (this.selectedIndex < 0 || this.selectedIndex >= this.matches.size()) {
            this.selectedIndex = 0;
        }
        this.scrollOffset = Mth.clamp(this.scrollOffset, 0, maxScroll());
        clampSelectionIntoWindow();
    }

    private boolean hasVisibleDropdown() {
        return isFocused() && !this.matches.isEmpty();
    }

    private int visibleRowCount() {
        return Math.max(1, Math.min(VISIBLE_ROWS, this.matches.size()));
    }

    private int maxScroll() {
        return Math.max(0, this.matches.size() - visibleRowCount());
    }

    private void moveSelection(int delta) {
        if (this.matches.isEmpty()) {
            return;
        }
        this.selectedIndex = Mth.clamp(this.selectedIndex + delta, 0, this.matches.size() - 1);
        if (this.selectedIndex < this.scrollOffset) {
            this.scrollOffset = this.selectedIndex;
        } else {
            int maxVisibleIndex = this.scrollOffset + visibleRowCount() - 1;
            if (this.selectedIndex > maxVisibleIndex) {
                this.scrollOffset = this.selectedIndex - visibleRowCount() + 1;
            }
        }
        this.scrollOffset = Mth.clamp(this.scrollOffset, 0, maxScroll());
    }

    private void addSelectionOrTypedValue() {
        String candidate = this.selectedIndex >= 0 && this.selectedIndex < this.matches.size()
                ? this.matches.get(this.selectedIndex)
                : getValue().trim();
        if (candidate.isEmpty()) {
            return;
        }

        ResourceLocation parsed = ResourceLocation.tryParse(candidate);
        if (parsed == null) {
            return;
        }
        String normalized = parsed.toString();

        List<String> selected = selectedIds();
        if (selected.contains(normalized)) {
            return;
        }

        int nextListIndex = focus$appendToConfiguredList(normalized);
        focus$focusListEntry(nextListIndex);
        this.lastQuery = "";
        refreshMatches();
    }

    public boolean handleDropdownClick(double mouseX, double mouseY, int button) {
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT || !hasVisibleDropdown() || !isWithinDropdown(mouseX, mouseY)) {
            return false;
        }

        int index = rowIndexAt(mouseY);
        if (index >= 0 && index < this.matches.size()) {
            this.selectedIndex = index;
            addSelectionOrTypedValue();
        }
        return true;
    }

    public boolean handleDropdownScroll(double mouseX, double mouseY, double scrollY) {
        if (!hasVisibleDropdown() || !isWithinDropdown(mouseX, mouseY)) {
            return false;
        }

        int maxScroll = maxScroll();
        if (maxScroll > 0) {
            this.scrollOffset = Mth.clamp(this.scrollOffset + (scrollY < 0.0D ? 1 : -1), 0, maxScroll);
            this.selectedIndex = Mth.clamp(this.selectedIndex, this.scrollOffset, this.scrollOffset + visibleRowCount() - 1);
        }
        return true;
    }

    private List<String> selectedIds() {
        Object value = ((EntryInfoAccessor) (Object) this.info).focus$getValue();
        if (!(value instanceof List<?> raw)) {
            return List.of();
        }
        List<String> ids = new ArrayList<>();
        for (Object element : raw) {
            if (element instanceof String id && !id.isBlank()) {
                ids.add(id);
            }
        }
        return ids;
    }

    private void clampSelectionIntoWindow() {
        if (this.selectedIndex < this.scrollOffset) {
            this.selectedIndex = this.scrollOffset;
        }
        int maxVisibleIndex = this.scrollOffset + visibleRowCount() - 1;
        if (this.selectedIndex > maxVisibleIndex) {
            this.selectedIndex = maxVisibleIndex;
        }
    }

    private boolean isWithinDropdown(double mouseX, double mouseY) {
        int left = getX();
        int right = getX() + getWidth();
        int top = getY() + getHeight() + 1;
        int bottom = top + (visibleRowCount() * ROW_HEIGHT);
        return mouseX >= left && mouseX <= right && mouseY >= top && mouseY < bottom;
    }

    private int rowIndexAt(double mouseY) {
        int top = getY() + getHeight() + 1;
        int row = ((int) mouseY - top) / ROW_HEIGHT;
        if (row < 0 || row >= visibleRowCount()) {
            return -1;
        }
        return this.scrollOffset + row;
    }

    private int focus$appendToConfiguredList(String normalized) {
        EntryInfoAccessor accessor = (EntryInfoAccessor) (Object) this.info;
        List<String> updated = new ArrayList<>(selectedIds());
        updated.add(normalized);
        accessor.focus$setValue(updated);
        int nextListIndex = updated.size();
        accessor.focus$setListIndex(nextListIndex);
        accessor.focus$setTempValue("");
        this.info.updateFieldValue();
        return nextListIndex;
    }

    @SuppressWarnings("unchecked")
    private void focus$focusListEntry(int nextListIndex) {
        if (nextListIndex < 0) {
            return;
        }

        if (!(Minecraft.getInstance().screen instanceof MidnightConfigScreen configScreen) || configScreen.list == null) {
            return;
        }

        configScreen.updateList();
        EntityIdAutocompleteEditBox fallback = null;
        for (ButtonEntry entry : (List<ButtonEntry>) configScreen.list.children()) {
            if (entry == null || entry.info == null || entry.buttons == null || entry.buttons.isEmpty()
                    || !"targetFilterEntityIds".equals(entry.info.fieldName)) {
                continue;
            }

            Object firstWidget = entry.buttons.get(0);
            if (firstWidget instanceof EntityIdAutocompleteEditBox autocompleteEditBox) {
                if (fallback == null) {
                    fallback = autocompleteEditBox;
                }
                int entryListIndex = ((EntryInfoAccessor) (Object) entry.info).focus$getListIndex();
                if (entryListIndex == nextListIndex) {
                    configScreen.setFocused(autocompleteEditBox);
                    autocompleteEditBox.setFocused(true);
                    return;
                }
            }
        }
        if (fallback != null) {
            configScreen.setFocused(fallback);
            fallback.setFocused(true);
        }
    }
}
