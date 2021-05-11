package com.withertech.imm_boti.ducks;

import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;

public interface IEPlayerMoveC2SPacket {
    RegistryKey<World> getPlayerDimension();
    
    void setPlayerDimension(RegistryKey<World> dim);
}
