package com.withertech.tim_wim_holes.mixin.common.entity_sync;

import com.withertech.hiding_in_the_bushes.MyNetwork;
import com.withertech.tim_wim_holes.network.CommonNetwork;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.ServerPlayNetHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ServerPlayNetHandler.class)
public class MixinServerPlayNetworkHandler_E {
    @ModifyVariable(
        method = "Lnet/minecraft/network/play/ServerPlayNetHandler;sendPacket(Lnet/minecraft/network/IPacket;Lio/netty/util/concurrent/GenericFutureListener;)V",
        at = @At("HEAD"),
        argsOnly = true
    )
    private IPacket modifyPacket(IPacket originalPacket) {
        if (CommonNetwork.getForceRedirectDimension() == null) {
            return originalPacket;
        }
        
        return MyNetwork.createRedirectedMessage(CommonNetwork.getForceRedirectDimension(), originalPacket);
    }
}
