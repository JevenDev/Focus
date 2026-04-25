package com.jvn.focus;

import com.jvn.focus.client.FocusClientBootstrap;
import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(Focus.MOD_ID)
public final class Focus {
    public static final String MOD_ID = "focus";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Focus() {
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> FocusClientBootstrap::init);
        LOGGER.debug("Initializing {}", MOD_ID);
    }
}
