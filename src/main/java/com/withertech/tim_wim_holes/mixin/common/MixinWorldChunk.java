package com.withertech.tim_wim_holes.mixin.common;

import com.withertech.tim_wim_holes.ducks.IEWorldChunk;
import net.minecraft.entity.Entity;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Chunk.class)
public abstract class MixinWorldChunk implements IEWorldChunk
{
	@Final
	@Shadow
	private ClassInheritanceMultiMap<Entity>[] entityLists;

	@Override
	public ClassInheritanceMultiMap<Entity>[] portal_getEntitySections()
	{
		return entityLists;
	}
}
