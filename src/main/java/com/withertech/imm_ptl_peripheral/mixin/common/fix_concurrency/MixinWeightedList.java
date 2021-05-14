package com.withertech.imm_ptl_peripheral.mixin.common.fix_concurrency;

import net.minecraft.util.WeightedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Random;

@Mixin(WeightedList.class)
public abstract class MixinWeightedList
{
	@Shadow
	public abstract WeightedList randomizeWithWeight(Random random);

	// it's not thread safe
	// dimension stack made this vanilla issue trigger more frequently

	/**
	 * @author qouteall
	 */
	@Overwrite
	public Object getRandomValue(Random random)
	{
		for (; ; )
		{
			try
			{
				return this.randomizeWithWeight(random).getValueStream().findFirst().orElseThrow(RuntimeException::new);
			} catch (Throwable throwable)
			{
				// including ConcurrentModificationException
				throwable.printStackTrace();
			}
		}
	}
}
