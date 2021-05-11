package com.withertech.imm_boti.ducks;

import net.minecraft.util.FrameTimer;

public interface IEMinecraftServer {
    public FrameTimer getMetricsDataNonClientOnly();
    
    boolean portal_getAreAllWorldsLoaded();
}
