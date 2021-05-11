package com.withertech.imm_boti.portal.global_portals;

import com.withertech.imm_boti.portal.Portal;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

public class GlobalTrackedPortal extends Portal {
    public static EntityType<GlobalTrackedPortal> entityType;
    
    public GlobalTrackedPortal(
        EntityType<?> entityType_1,
        World world_1
    ) {
        super(entityType_1, world_1);
    }
    
}
