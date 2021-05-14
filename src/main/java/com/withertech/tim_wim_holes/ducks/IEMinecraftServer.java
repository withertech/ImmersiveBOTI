package com.withertech.tim_wim_holes.ducks;

import net.minecraft.util.FrameTimer;

public interface IEMinecraftServer
{
	FrameTimer getMetricsDataNonClientOnly();

	boolean portal_getAreAllWorldsLoaded();
}
