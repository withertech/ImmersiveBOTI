package com.withertech.tim_wim_holes.ducks;

import net.minecraft.util.SortedArraySet;
import net.minecraft.world.server.Ticket;

public interface IEChunkTicketManager {
    void mySetWatchDistance(int newWatchDistance);
    
    SortedArraySet<Ticket<?>> portal_getTicketSet(long chunkPos);
}
