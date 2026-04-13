package com.jvn.focus.mixin;

import eu.midnightdust.lib.config.EntryInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntryInfo.class)
public interface EntryInfoAccessor {
    @Accessor("value")
    Object focus$getValue();

    @Accessor("value")
    void focus$setValue(Object value);

    @Accessor("listIndex")
    int focus$getListIndex();

    @Accessor("listIndex")
    void focus$setListIndex(int listIndex);

    @Accessor("tempValue")
    String focus$getTempValue();

    @Accessor("tempValue")
    void focus$setTempValue(String tempValue);
}
