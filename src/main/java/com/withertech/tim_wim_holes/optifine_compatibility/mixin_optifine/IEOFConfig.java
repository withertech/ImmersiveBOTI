package com.withertech.tim_wim_holes.optifine_compatibility.mixin_optifine;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Invoker;

@Pseudo
@Mixin(targets = "net.optifine.Config", remap = false)
public interface IEOFConfig
{
	@Invoker("isVbo")
	static boolean ip_isVbo()
	{
		throw new RuntimeException();
	}

	@Invoker("isRenderRegions")
	static boolean ip_isRenderRegions()
	{
		throw new RuntimeException();
	}
}
