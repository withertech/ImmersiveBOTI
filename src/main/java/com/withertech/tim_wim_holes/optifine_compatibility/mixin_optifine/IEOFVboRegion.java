package com.withertech.tim_wim_holes.optifine_compatibility.mixin_optifine;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Invoker;

@Pseudo
@Mixin(targets = "net.optifine.render.VboRegion", remap = false)
public interface IEOFVboRegion
{
	@Invoker(value = "deleteGlBuffers", remap = false)
	void ip_deleteGlBuffers();
}
