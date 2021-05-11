package com.withertech.imm_boti.mixin.client.sync;

import com.withertech.imm_boti.ducks.IEPlayerMoveC2SPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@OnlyIn(Dist.CLIENT)
@Mixin(CPlayerPacket.RotationPacket.class)
public class MixinPlayerMoveC2SPacketLookOnly {
    @OnlyIn(Dist.CLIENT)
    @Inject(
        method = "<init>(FFZ)V",
        at = @At("RETURN")
    )
    private void onConstruct(float float_1, float float_2, boolean boolean_1, CallbackInfo ci) {
        RegistryKey<World> dimension = Minecraft.getInstance().player.world.getDimensionKey();
        ((IEPlayerMoveC2SPacket) this).setPlayerDimension(dimension);
        assert dimension == Minecraft.getInstance().world.getDimensionKey();
    }
    
    
}
