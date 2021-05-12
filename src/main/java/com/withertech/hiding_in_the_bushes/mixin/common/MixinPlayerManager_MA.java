package com.withertech.hiding_in_the_bushes.mixin.common;

import com.withertech.tim_wim_holes.Global;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.management.PlayerList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(PlayerList.class)
public class MixinPlayerManager_MA {
    @Shadow
    @Final
    private List<ServerPlayerEntity> players;
    
    @Inject(
        method = "func_232644_a_",
        at = @At("HEAD")
    )
    private void onPlayerRespawn(
        ServerPlayerEntity p_232644_1_, boolean p_232644_2_,
        CallbackInfoReturnable<ServerPlayerEntity> cir
    ) {
        Global.chunkDataSyncManager.onPlayerRespawn(p_232644_1_);
    }
}
