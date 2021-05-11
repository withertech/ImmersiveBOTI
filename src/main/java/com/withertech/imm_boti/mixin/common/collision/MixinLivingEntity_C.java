package com.withertech.imm_boti.mixin.common.collision;

import com.withertech.imm_boti.ducks.IEEntity;
import com.withertech.imm_boti.portal.Portal;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class MixinLivingEntity_C {
    //fix climbing onto ladder cross portal
    @Inject(method = "Lnet/minecraft/entity/LivingEntity;getBlockState()Lnet/minecraft/block/BlockState;", at = @At("HEAD"), cancellable = true)
    private void onGetBlockState(CallbackInfoReturnable<BlockState> cir) {
        Portal collidingPortal = ((IEEntity) this).getCollidingPortal();
        Entity this_ = (Entity) (Object) this;
        if (collidingPortal != null) {
            if (collidingPortal.getNormal().y > 0) {
                BlockPos remoteLandingPos = new BlockPos(
                    collidingPortal.transformPoint(this_.getPositionVec())
                );
                
                World destinationWorld = collidingPortal.getDestinationWorld();
                
                if (destinationWorld.isBlockLoaded(remoteLandingPos)) {
                    BlockState result = destinationWorld.getBlockState(remoteLandingPos);
                    
                    if (!result.isAir()) {
                        cir.setReturnValue(result);
                        cir.cancel();
                    }
                }
            }
        }
    }
}
