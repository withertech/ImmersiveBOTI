package com.withertech.imm_boti.mixin.common;

import com.withertech.imm_boti.ducks.IEWorldChunk;
import net.minecraft.entity.Entity;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Chunk.class)
public abstract class MixinWorldChunk implements IEWorldChunk {
    @Final
    @Shadow
    private ClassInheritanceMultiMap<Entity>[] entityLists;

    @Override
    public ClassInheritanceMultiMap<Entity>[] portal_getEntitySections() {
        return entityLists;
    }
}
