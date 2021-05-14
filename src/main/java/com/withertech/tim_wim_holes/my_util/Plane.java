package com.withertech.tim_wim_holes.my_util;

import net.minecraft.util.math.vector.Vector3d;

import java.util.Objects;

public class Plane
{
	public final Vector3d pos;
	public final Vector3d normal;

	public Plane(Vector3d pos, Vector3d normal)
	{
		this.pos = pos;
		this.normal = normal;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Plane plane = (Plane) o;
		return pos.equals(plane.pos) &&
				normal.equals(plane.normal);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(pos, normal);
	}

	@Override
	public String toString()
	{
		return "Plane{" +
				"pos=" + pos +
				", normal=" + normal +
				'}';
	}
}
