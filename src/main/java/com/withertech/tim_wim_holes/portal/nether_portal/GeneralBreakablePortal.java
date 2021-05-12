package com.withertech.tim_wim_holes.portal.nether_portal;

import com.withertech.tim_wim_holes.portal.PortalPlaceholderBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

public class GeneralBreakablePortal extends BreakablePortalEntity {
    
    public static EntityType<GeneralBreakablePortal> entityType;
    
    public GeneralBreakablePortal(
        EntityType<?> entityType_1,
        World world_1
    ) {
        super(entityType_1, world_1);
    }
    
    @Override
    protected boolean isPortalIntactOnThisSide() {
        return blockPortalShape.area.stream()
            .allMatch(blockPos ->
                world.getBlockState(blockPos).getBlock() == PortalPlaceholderBlock.instance
            ) &&
            blockPortalShape.frameAreaWithoutCorner.stream()
                .allMatch(blockPos -> !world.isAirBlock(blockPos));
    }
    
    @Override
    protected void addSoundAndParticle() {
    
    }
}
