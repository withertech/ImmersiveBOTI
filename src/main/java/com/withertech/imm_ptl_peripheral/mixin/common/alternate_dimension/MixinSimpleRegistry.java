package com.withertech.imm_ptl_peripheral.mixin.common.alternate_dimension;

import com.withertech.tim_wim_holes.ducks.IESimpleRegistry;
import net.minecraft.util.registry.SimpleRegistry;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SimpleRegistry.class)
public class MixinSimpleRegistry implements IESimpleRegistry
{

}
