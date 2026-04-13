package com.jvn.focus.mixin;

import com.jvn.focus.client.EntityIdAutocompleteEditBox;
import eu.midnightdust.lib.config.ButtonEntry;
import eu.midnightdust.lib.config.EntryInfo;
import eu.midnightdust.lib.config.MidnightConfigListWidget;
import net.minecraft.client.gui.components.AbstractSelectionList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractSelectionList.class)
public abstract class AbstractSelectionListMixin {
    private static final String FOCUS_TARGET_FILTER_ENTITY_IDS_FIELD = "targetFilterEntityIds";

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void focus$routeAutocompleteDropdownClicks(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (!((Object) this instanceof MidnightConfigListWidget listWidget)) {
            return;
        }

        for (ButtonEntry entry : listWidget.children()) {
            EntityIdAutocompleteEditBox autocompleteEditBox = focus$getAutocompleteEditBox(entry);
            if (autocompleteEditBox != null && autocompleteEditBox.handleDropdownClick(mouseX, mouseY, button)) {
                cir.setReturnValue(true);
                return;
            }
        }
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void focus$routeAutocompleteDropdownScroll(
            double mouseX,
            double mouseY,
            double scrollX,
            double scrollY,
            CallbackInfoReturnable<Boolean> cir) {
        if (!((Object) this instanceof MidnightConfigListWidget listWidget)) {
            return;
        }

        for (ButtonEntry entry : listWidget.children()) {
            EntityIdAutocompleteEditBox autocompleteEditBox = focus$getAutocompleteEditBox(entry);
            if (autocompleteEditBox != null && autocompleteEditBox.handleDropdownScroll(mouseX, mouseY, scrollY)) {
                cir.setReturnValue(true);
                return;
            }
        }
    }

    private static EntityIdAutocompleteEditBox focus$getAutocompleteEditBox(ButtonEntry entry) {
        if (entry == null || entry.info == null || !focus$isTargetEntityIdEntry(entry.info) || entry.buttons == null || entry.buttons.isEmpty()) {
            return null;
        }

        Object firstWidget = entry.buttons.get(0);
        if (firstWidget instanceof EntityIdAutocompleteEditBox autocompleteEditBox) {
            return autocompleteEditBox;
        }
        return null;
    }

    private static boolean focus$isTargetEntityIdEntry(EntryInfo info) {
        return info != null && FOCUS_TARGET_FILTER_ENTITY_IDS_FIELD.equals(info.fieldName);
    }
}
