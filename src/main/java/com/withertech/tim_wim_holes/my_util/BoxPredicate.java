package com.withertech.tim_wim_holes.my_util;

public interface BoxPredicate
{
	BoxPredicate nonePredicate =
			(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) -> false;

	boolean test(double minX, double minY, double minZ, double maxX, double maxY, double maxZ);
}
