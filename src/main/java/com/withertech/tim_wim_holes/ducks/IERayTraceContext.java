package com.withertech.tim_wim_holes.ducks;

import net.minecraft.util.math.vector.Vector3d;

public interface IERayTraceContext
{
	IERayTraceContext setStart(Vector3d newStart);

	IERayTraceContext setEnd(Vector3d newEnd);
}
