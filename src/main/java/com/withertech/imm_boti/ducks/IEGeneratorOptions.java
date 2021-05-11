package com.withertech.imm_boti.ducks;

import net.minecraft.util.registry.SimpleRegistry;
import net.minecraft.world.Dimension;

public interface IEGeneratorOptions {
    void setDimOptionRegistry(SimpleRegistry<Dimension> reg);
}
