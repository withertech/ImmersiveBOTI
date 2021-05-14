package com.withertech.tim_wim_holes.ducks;

import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;

public interface IEPlayerMoveC2SPacket
{
	RegistryKey<World> getPlayerDimension();

	void setPlayerDimension(RegistryKey<World> dim);
}
