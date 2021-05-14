package com.withertech.tim_wim_holes.ducks;

public interface IEFrustum
{
	boolean canDetermineInvisible(double minX, double minY, double minZ, double maxX, double maxY, double maxZ);
}
