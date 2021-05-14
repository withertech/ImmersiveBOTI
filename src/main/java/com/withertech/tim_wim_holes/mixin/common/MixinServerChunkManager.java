package com.withertech.tim_wim_holes.mixin.common;

import com.withertech.tim_wim_holes.ducks.IEServerChunkManager;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.TicketManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerChunkProvider.class)
public abstract class MixinServerChunkManager implements IEServerChunkManager
{
	@Shadow
	@Final
	private TicketManager ticketManager;

	@Override
	public TicketManager getTicketManager()
	{
		return ticketManager;
	}
}
