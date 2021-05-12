package com.withertech.tim_wim_holes.mixin.client.collisions;

import com.withertech.tim_wim_holes.ducks.IEEntity;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public class MixinClientPlayerEntity {
    @Inject(
        method = "Lnet/minecraft/client/entity/player/ClientPlayerEntity;shouldBlockPushPlayer(Lnet/minecraft/util/math/BlockPos;)Z",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onCannotFitAt(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (((IEEntity) this).getCollidingPortal() != null) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
}
