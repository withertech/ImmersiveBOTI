package com.withertech.tim_wim_holes.ducks;

import com.withertech.tim_wim_holes.portal.Portal;
import net.minecraft.entity.Entity;

public interface IEEntity {
    void notifyCollidingWithPortal(Entity portal);
    
    Portal getCollidingPortal();
    
    void tickCollidingPortal(float tickDelta);
    
    boolean isRecentlyCollidingWithPortal();
    
    void portal_requestUpdateChunkPos();
}
