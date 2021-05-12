package com.withertech.tim_wim_holes.mixin.common;

import com.withertech.tim_wim_holes.ducks.IEWorld;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.storage.ISpawnWorldInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(World.class)
public abstract class MixinWorld implements IEWorld {
    
    @Shadow
    @Final
    protected ISpawnWorldInfo worldInfo;
    
    @Shadow
    public abstract RegistryKey<World> getDimensionKey();
    
    @Shadow
    protected float rainingStrength;
    
    @Shadow
    protected float thunderingStrength;
    
    @Shadow
    protected float prevRainingStrength;
    
    @Shadow
    protected float prevThunderingStrength;
    
    // Fix overworld rain cause nether fog change
    @Inject(method = "Lnet/minecraft/world/World;calculateInitialWeather()V", at = @At("TAIL"))
    private void onInitWeatherGradients(CallbackInfo ci) {
        if (getDimensionKey() == World.THE_NETHER) {
            rainingStrength = 0;
            prevRainingStrength = 0;
            thunderingStrength = 0;
            prevThunderingStrength = 0;
        }
    }
    
    @Override
    public ISpawnWorldInfo myGetProperties() {
        return worldInfo;
    }
    
    @Override
    public void portal_setWeather(float rainGradPrev, float rainGrad, float thunderGradPrev, float thunderGrad) {
        prevRainingStrength = rainGradPrev;
        rainingStrength = rainGrad;
        prevThunderingStrength = thunderGradPrev;
        thunderingStrength = thunderGrad;
    }
}
