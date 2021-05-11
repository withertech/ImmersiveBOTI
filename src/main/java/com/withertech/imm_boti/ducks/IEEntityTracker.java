package com.withertech.imm_boti.ducks;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.SectionPos;

public interface IEEntityTracker {
    Entity getEntity_();
    
    void updateEntityTrackingStatus(ServerPlayerEntity player);
    
    void onPlayerRespawn(ServerPlayerEntity oldPlayer);
    
    void resendSpawnPacketToTrackers();
    
    void stopTrackingToAllPlayers_();
    
    void tickEntry();
    
    SectionPos getLastCameraPosition();
    
    void setLastCameraPosition(SectionPos arg);
}
