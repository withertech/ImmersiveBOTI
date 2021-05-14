package com.withertech.tim_wim_holes.mixin.common;

import com.withertech.tim_wim_holes.ducks.IEMetricsData;
import net.minecraft.util.FrameTimer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(FrameTimer.class)
public class MixinMetricsData implements IEMetricsData
{
	@Shadow
	@Final
	private long[] frames;

	@Override
	public long[] getSamplesNonClientOnly()
	{
		return frames;
	}
}
