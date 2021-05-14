package com.withertech.tim_wim_holes.ducks;

import net.minecraft.world.storage.ISpawnWorldInfo;

public interface IEWorld
{

	ISpawnWorldInfo myGetProperties();

	void portal_setWeather(float rainGradPrev, float rainGrad, float thunderGradPrev, float thunderGrad);
}
