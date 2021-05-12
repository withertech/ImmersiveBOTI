package com.withertech.tim_wim_holes.ducks;

import net.minecraft.entity.Entity;
import net.minecraft.util.ClassInheritanceMultiMap;


public interface IEWorldChunk {
    ClassInheritanceMultiMap<Entity>[] portal_getEntitySections();
}
