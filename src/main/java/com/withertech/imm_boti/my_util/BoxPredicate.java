package com.withertech.imm_boti.my_util;

public interface BoxPredicate {
    public static BoxPredicate nonePredicate =
        (double minX, double minY, double minZ, double maxX, double maxY, double maxZ) -> false;
    
    boolean test(double minX, double minY, double minZ, double maxX, double maxY, double maxZ);
}
