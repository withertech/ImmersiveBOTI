package com.withertech.tim_wim_holes.ducks;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;

public interface IEServerPlayerEntity
{
	void setEnteredNetherPos(Vector3d pos);

	void updateDimensionTravelAdvancements(ServerWorld fromWorld);

	void setIsInTeleportationState(boolean arg);

	void stopRidingWithoutTeleportRequest();

	void startRidingWithoutTeleportRequest(Entity newVehicle);
}
