package com.withertech.tim_wim_holes.ducks;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.registry.DynamicRegistries;

import java.util.Map;

public interface IEClientPlayNetworkHandler
{
	void setWorld(ClientWorld world);

	Map getPlayerListEntries();

	void setPlayerListEntries(Map value);

	void portal_setRegistryManager(DynamicRegistries arg);
}
