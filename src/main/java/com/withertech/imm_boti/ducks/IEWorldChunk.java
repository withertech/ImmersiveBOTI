package com.withertech.imm_boti.ducks;

import net.minecraft.entity.Entity;
import net.minecraft.util.ClassInheritanceMultiMap;


public interface IEWorldChunk {
    ClassInheritanceMultiMap<Entity>[] portal_getEntitySections();
}
