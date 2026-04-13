package com.jvn.focus.mixin;

import com.jvn.focus.client.EntityIdAutocompleteEditBox;
import eu.midnightdust.lib.config.ButtonEntry;
import eu.midnightdust.lib.config.EntryInfo;
import eu.midnightdust.lib.config.MidnightConfigScreen;
import eu.midnightdust.lib.config.MidnightConfigListWidget;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MidnightConfigScreen.class)
public abstract class MidnightConfigScreenMixin {
    @Unique
    private static final String FOCUS_TARGET_FILTER_ENTITY_IDS_FIELD = "targetFilterEntityIds";

    @Shadow
    public MidnightConfigListWidget list;

    @Inject(method = "updateList", at = @At("TAIL"))
    private void focus$upgradeTargetFilterEntryToAutocomplete(CallbackInfo ci) {
        if (this.list == null) {
            return;
        }

        for (ButtonEntry entry : (List<ButtonEntry>) this.list.children()) {
            if (!focus$isTargetFilterEntityIdEntry(entry) || entry.buttons == null || entry.buttons.isEmpty()) {
                continue;
            }

            Object firstWidget = entry.buttons.get(0);
            if (!(firstWidget instanceof EditBox originalEditBox) || firstWidget instanceof EntityIdAutocompleteEditBox) {
                continue;
            }
            EntityIdAutocompleteEditBox autocompleteEditBox = new EntityIdAutocompleteEditBox(
                    Minecraft.getInstance().font,
                    originalEditBox.getX(),
                    originalEditBox.getY(),
                    originalEditBox.getWidth(),
                    originalEditBox.getHeight(),
                    originalEditBox.getMessage(),
                    entry.info);

            EntryInfo info = entry.info;
            if (info.entry != null) {
                autocompleteEditBox.setMaxLength(info.entry.width());
            }
            autocompleteEditBox.setValue(originalEditBox.getValue());
            Consumer<String> responder = ((EditBoxAccessor) (Object) originalEditBox).focus$getResponder();
            if (responder != null) {
                autocompleteEditBox.setResponder(responder);
            }
            autocompleteEditBox.setTooltip(info.getTooltip(true));
            autocompleteEditBox.active = originalEditBox.active;
            if (originalEditBox.isFocused()) {
                autocompleteEditBox.setFocused(true);
            }

            entry.buttons.set(0, autocompleteEditBox);
        }
    }

    @Unique
    private boolean focus$isTargetFilterEntityIdEntry(ButtonEntry entry) {
        if (entry == null || entry.info == null || entry.info.field == null) {
            return false;
        }
        return FOCUS_TARGET_FILTER_ENTITY_IDS_FIELD.equals(entry.info.fieldName);
    }
}
