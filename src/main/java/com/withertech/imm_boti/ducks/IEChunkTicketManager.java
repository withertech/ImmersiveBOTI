package com.withertech.imm_boti.ducks;

import net.minecraft.util.SortedArraySet;
import net.minecraft.world.server.Ticket;

public interface IEChunkTicketManager {
    void mySetWatchDistance(int newWatchDistance);
    
    SortedArraySet<Ticket<?>> portal_getTicketSet(long chunkPos);
}
