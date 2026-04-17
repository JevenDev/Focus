package com.jvn.focus.mixin;

import java.util.List;
import java.util.Set;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public final class FocusMixinPlugin implements IMixinConfigPlugin {
    private static final String SSR_INPUT_HANDLER = "com.github.exopandora.shouldersurfing.client.InputHandler";
    private static final String SSR_IMPL = "com.github.exopandora.shouldersurfing.client.ShoulderSurfingImpl";
    private static final String SSR_MIXIN_PACKAGE = "com.jvn.focus.mixin.compat.shouldersurfing.";

    @Override
    public void onLoad(String mixinPackage) {}

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (!mixinClassName.startsWith(SSR_MIXIN_PACKAGE)) {
            return true;
        }

        return switch (mixinClassName.substring(SSR_MIXIN_PACKAGE.length())) {
            case "ShoulderSurfingInputHandlerMixin" -> classExists(SSR_INPUT_HANDLER);
            case "ShoulderSurfingImplMixin" -> classExists(SSR_IMPL);
            default -> true;
        };
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    private static boolean classExists(String className) {
        try {
            Class.forName(className, false, FocusMixinPlugin.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException exception) {
            return false;
        }
    }
}
