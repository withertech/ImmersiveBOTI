package com.withertech.tim_wim_holes.ducks;

import net.minecraft.util.math.vector.Vector3d;
import net.tardis.mod.tileentities.ConsoleTile;

public interface IEExteriorTile
{
	default void genPortals()
	{

	}

	default void updateFallingPortal(Vector3d pos, ConsoleTile console)
	{

	}

	void updatePortals();

	void clearPortals();

	void clearPortals(Vector3d pos);
}
