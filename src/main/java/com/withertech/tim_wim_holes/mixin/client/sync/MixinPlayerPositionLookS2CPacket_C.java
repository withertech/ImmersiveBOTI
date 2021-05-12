package com.withertech.tim_wim_holes.mixin.client.sync;

import com.withertech.tim_wim_holes.dimension_sync.DimId;
import com.withertech.tim_wim_holes.ducks.IEPlayerPositionLookS2CPacket;
import com.withertech.tim_wim_holes.network.NetworkAdapt;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SPlayerPositionLookPacket.class)
public class MixinPlayerPositionLookS2CPacket_C {
    @Inject(method = "Lnet/minecraft/network/play/server/SPlayerPositionLookPacket;readPacketData(Lnet/minecraft/network/PacketBuffer;)V", at = @At("RETURN"))
    private void onRead(PacketBuffer buf, CallbackInfo ci) {
        if (buf.isReadable()) {
            RegistryKey<World> playerDimension = DimId.readWorldId(buf, true);
            ((IEPlayerPositionLookS2CPacket) this).setPlayerDimension(playerDimension);
            NetworkAdapt.setServerHasIP(true);
        }
        else {
            NetworkAdapt.setServerHasIP(false);
        }
    }
}
