package com.withertech.tim_wim_holes.mixin.common.dimension;

import com.mojang.serialization.Lifecycle;
import com.withertech.tim_wim_holes.api.IPDimensionAPI;
import com.withertech.tim_wim_holes.ducks.IEGeneratorOptions;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.gen.settings.DimensionGeneratorSettings;
import net.minecraft.world.storage.ServerWorldInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorldInfo.class)
public class MixinLevelProperties_D {
    @Shadow
    @Final
    private DimensionGeneratorSettings generatorSettings;
    
    @Shadow
    @Final
    @Mutable
    private Lifecycle lifecycle;
    
//    @Inject(
//        method = "<init>(Lcom/mojang/datafixers/DataFixer;ILnet/minecraft/nbt/CompoundTag;ZIIIFJJIIIZIZZZLnet/minecraft/world/border/WorldBorder$Properties;IILjava/util/UUID;Ljava/util/LinkedHashSet;Lnet/minecraft/world/timer/Timer;Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/world/level/LevelInfo;Lnet/minecraft/world/gen/GeneratorOptions;Lcom/mojang/serialization/Lifecycle;)V",
//        at = @At("RETURN")
//    )
//    private void onConstructedFromLevelInfo(
//        DataFixer dataFixer, int dataVersion, CompoundTag playerData,
//        boolean modded, int spawnX, int spawnY, int spawnZ, float spawnAngle,
//        long time, long timeOfDay, int version, int clearWeatherTime, int rainTime,
//        boolean raining, int thunderTime, boolean thundering, boolean initialized,
//        boolean difficultyLocked, WorldBorder.Properties worldBorder, int wanderingTraderSpawnDelay,
//        int wanderingTraderSpawnChance, UUID wanderingTraderId, LinkedHashSet<String> serverBrands,
//        Timer<MinecraftServer> scheduledEvents, CompoundTag customBossEvents, CompoundTag dragonFight,
//        LevelInfo levelInfo, GeneratorOptions generatorOptions, Lifecycle lifecycle_p, CallbackInfo ci
//    ) {
//        // TODO use more appropriate way to get rid of the warning screen
//        if (Global.enableAlternateDimensions && generatorOptions.getDimensions().getIds().size() == 8) {
//            lifecycle = Lifecycle.stable();
//        }
//    }
    
    @Inject(
        method = "Lnet/minecraft/world/storage/ServerWorldInfo;serialize(Lnet/minecraft/util/registry/DynamicRegistries;Lnet/minecraft/nbt/CompoundNBT;Lnet/minecraft/nbt/CompoundNBT;)V",
        at = @At("HEAD")
    )
    private void onUpdateProperties(
        DynamicRegistries dynamicRegistryManager, CompoundNBT compoundTag,
        CompoundNBT compoundTag2, CallbackInfo ci
    ) {
        ((IEGeneratorOptions) generatorSettings).setDimOptionRegistry(
            IPDimensionAPI.getAdditionalDimensionsRemoved(
                    generatorSettings.func_236224_e_()
            )
        );
    }
}
