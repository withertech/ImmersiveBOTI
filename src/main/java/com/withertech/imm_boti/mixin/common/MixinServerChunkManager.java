package com.withertech.imm_boti.mixin.common;

import com.withertech.imm_boti.ducks.IEServerChunkManager;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.TicketManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerChunkProvider.class)
public abstract class MixinServerChunkManager implements IEServerChunkManager {
    @Shadow
    @Final
    private TicketManager ticketManager;
    
    @Override
    public TicketManager getTicketManager() {
        return ticketManager;
    }
}
