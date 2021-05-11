package com.withertech.imm_boti.ducks;

import net.minecraft.util.math.vector.Vector3d;

public interface IERayTraceContext {
    IERayTraceContext setStart(Vector3d newStart);

    IERayTraceContext setEnd(Vector3d newEnd);
}
