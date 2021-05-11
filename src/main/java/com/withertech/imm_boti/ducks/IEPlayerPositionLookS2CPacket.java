package com.withertech.imm_boti.ducks;

import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;

public interface IEPlayerPositionLookS2CPacket {
    RegistryKey<World> getPlayerDimension();
    
    void setPlayerDimension(RegistryKey<World> dimension);
}
