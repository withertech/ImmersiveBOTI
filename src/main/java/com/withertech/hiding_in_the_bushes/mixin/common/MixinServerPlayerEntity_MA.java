package com.withertech.hiding_in_the_bushes.mixin.common;

import com.withertech.tim_wim_holes.Global;
import com.withertech.tim_wim_holes.ModMain;
import com.withertech.tim_wim_holes.portal.custom_portal_gen.CustomPortalGenManagement;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.ITeleporter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(ServerPlayerEntity.class)
public class MixinServerPlayerEntity_MA {
    
    @Inject(method = "changeDimension", at = @At("HEAD"), remap = false)
    private void onChangeDimensionByVanilla(
        ServerWorld p_changeDimension_1_,
        ITeleporter p_changeDimension_2_,
        CallbackInfoReturnable<Entity> cir
    ) {
        ServerPlayerEntity oldPlayer = (ServerPlayerEntity) (Object) this;
    
        onBeforeTravel(oldPlayer);
        
    }
    
    @Inject(
        method = "Lnet/minecraft/entity/player/ServerPlayerEntity;teleport(Lnet/minecraft/world/server/ServerWorld;DDDFF)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/server/ServerWorld;removePlayer(Lnet/minecraft/entity/player/ServerPlayerEntity;Z)V"
        )//no remap false
    )
    private void onForgeTeleport(
        ServerWorld serverWorld,
        double x,
        double y,
        double z,
        float yaw,
        float pitch,
        CallbackInfo ci
    ) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        
        //fix issue with good nights sleep
        player.clearBedPosition();
        
        onBeforeTravel(player);
    }
    
    private static void onBeforeTravel(ServerPlayerEntity player) {
        CustomPortalGenManagement.onBeforeConventionalDimensionChange(player);
        Global.chunkDataSyncManager.onPlayerRespawn(player);
        
        ModMain.serverTaskList.addTask(() -> {
            CustomPortalGenManagement.onAfterConventionalDimensionChange(player);
            return true;
        });
    }
}
