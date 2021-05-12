package com.withertech.tim_wim_holes.mixin.common.dimension;

import com.withertech.tim_wim_holes.api.IPDimensionAPI;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.chunk.listener.IChunkStatusListener;
import net.minecraft.world.gen.settings.DimensionGeneratorSettings;
import net.minecraft.world.storage.IServerConfiguration;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer_D {
    @Shadow
    @Final
    protected IServerConfiguration serverConfig;
    
    @Shadow
    public abstract DynamicRegistries getDynamicRegistries();
    
    @Inject(method = "Lnet/minecraft/server/MinecraftServer;func_240787_a_(Lnet/minecraft/world/chunk/listener/IChunkStatusListener;)V", at = @At("HEAD"))
    private void onBeforeCreateWorlds(
        IChunkStatusListener worldGenerationProgressListener, CallbackInfo ci
    ) {
        DimensionGeneratorSettings generatorOptions = serverConfig.getDimensionGeneratorSettings();
        
        DynamicRegistries registryManager = getDynamicRegistries();
        
        IPDimensionAPI.onServerWorldInit.emit(generatorOptions, registryManager);
        
        
    }
    
}
