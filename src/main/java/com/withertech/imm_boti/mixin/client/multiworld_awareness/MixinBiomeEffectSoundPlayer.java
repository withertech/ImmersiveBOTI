package com.withertech.imm_boti.mixin.client.multiworld_awareness;

import net.minecraft.client.audio.BiomeSoundHandler;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.world.biome.BiomeManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BiomeSoundHandler.class)
public class MixinBiomeEffectSoundPlayer {
    @Mutable
    @Shadow
    @Final
    private BiomeManager biomeManager;
    
    @Shadow
    @Final
    private ClientPlayerEntity player;
    
    // change the biomeAccess field when player dimension changes
    @Inject(method = "Lnet/minecraft/client/audio/BiomeSoundHandler;tick()V", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        biomeManager = player.world.getBiomeManager();
    }
}
