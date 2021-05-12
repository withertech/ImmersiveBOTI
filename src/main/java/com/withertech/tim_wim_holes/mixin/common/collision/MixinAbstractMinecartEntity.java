package com.withertech.tim_wim_holes.mixin.common.collision;

import com.withertech.tim_wim_holes.ducks.IEEntity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractMinecartEntity.class)
public class MixinAbstractMinecartEntity {
    @Inject(
        method = "Lnet/minecraft/entity/item/minecart/AbstractMinecartEntity;tick()V",
        at = @At("HEAD")
    )
    private void onTick(CallbackInfo ci) {
        ((IEEntity) this).tickCollidingPortal(1);
    }
}
