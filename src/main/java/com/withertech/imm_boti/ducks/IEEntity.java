package com.withertech.imm_boti.ducks;

import com.withertech.imm_boti.portal.Portal;
import net.minecraft.entity.Entity;

public interface IEEntity {
    void notifyCollidingWithPortal(Entity portal);
    
    Portal getCollidingPortal();
    
    void tickCollidingPortal(float tickDelta);
    
    boolean isRecentlyCollidingWithPortal();
    
    void portal_requestUpdateChunkPos();
}
