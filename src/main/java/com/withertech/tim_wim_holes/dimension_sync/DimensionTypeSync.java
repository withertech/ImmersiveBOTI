package com.withertech.tim_wim_holes.dimension_sync;

import com.google.common.collect.Streams;
import com.withertech.tim_wim_holes.Helper;
import com.withertech.tim_wim_holes.McHelper;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class DimensionTypeSync {
    
    @OnlyIn(Dist.CLIENT)
    public static Map<RegistryKey<World>, RegistryKey<DimensionType>> clientTypeMap;
    
    @OnlyIn(Dist.CLIENT)
    private static DynamicRegistries currentDimensionTypeTracker;
    
    @OnlyIn(Dist.CLIENT)
    public static void onGameJoinPacketReceived(DynamicRegistries tracker) {
        currentDimensionTypeTracker = tracker;
    }
    
    @OnlyIn(Dist.CLIENT)
    private static Map<RegistryKey<World>, RegistryKey<DimensionType>> typeMapFromTag(CompoundNBT tag) {
        Map<RegistryKey<World>, RegistryKey<DimensionType>> result = new HashMap<>();
        tag.keySet().forEach(key -> {
            RegistryKey<World> worldKey = DimId.idToKey(key);
            
            String val = tag.getString(key);
            
            RegistryKey<DimensionType> typeKey =
                RegistryKey.getOrCreateKey(Registry.DIMENSION_TYPE_KEY, new ResourceLocation(val));
            
            result.put(worldKey, typeKey);
        });
        
        return result;
    }
    
    @OnlyIn(Dist.CLIENT)
    public static void acceptTypeMapData(CompoundNBT tag) {
        clientTypeMap = typeMapFromTag(tag);
        
        Helper.info("Received Dimension Type Sync");
        Helper.info("\n" + Helper.myToString(
            clientTypeMap.entrySet().stream().map(
                e -> e.getKey().getLocation().toString() + " -> " + e.getValue().getLocation()
            )
        ));
    }
    
    public static CompoundNBT createTagFromServerWorldInfo() {
        DynamicRegistries registryManager = McHelper.getServer().getDynamicRegistries();
        Registry<DimensionType> dimensionTypes = registryManager.func_230520_a_();
        return typeMapToTag(
            Streams.stream(McHelper.getServer().getWorlds()).collect(
                Collectors.toMap(
                    World::getDimensionKey,
                    w -> {
                        DimensionType dimensionType = w.getDimensionType();
                        ResourceLocation id = dimensionTypes.getKey(dimensionType);
                        if (id == null) {
                            Helper.error("Missing dim type id for " + w.getDimensionKey());
                            Helper.error("Registered dimension types " +
                                Helper.myToString(dimensionTypes.keySet().stream()));
                            return DimensionType.OVERWORLD;
                        }
                        return idToDimType(id);
                    }
                )
            )
        );
    }
    
    public static RegistryKey<DimensionType> idToDimType(ResourceLocation id) {
        return RegistryKey.getOrCreateKey(Registry.DIMENSION_TYPE_KEY, id);
    }
    
    private static CompoundNBT typeMapToTag(Map<RegistryKey<World>, RegistryKey<DimensionType>> data) {
        CompoundNBT tag = new CompoundNBT();
        data.forEach((worldKey, typeKey) -> {
            tag.put(worldKey.getLocation().toString(), StringNBT.valueOf(typeKey.getLocation().toString()));
        });
        return tag;
    }
    
    @OnlyIn(Dist.CLIENT)
    public static RegistryKey<DimensionType> getDimensionTypeKey(RegistryKey<World> worldKey) {
        if (worldKey == World.OVERWORLD) {
            return DimensionType.OVERWORLD;
        }
        
        if (worldKey == World.THE_NETHER) {
            return DimensionType.THE_NETHER;
        }
        
        if (worldKey == World.THE_END) {
            return DimensionType.THE_END;
        }
        
        RegistryKey<DimensionType> obj = clientTypeMap.get(worldKey);
        
        if (obj == null) {
            Helper.error("Missing Dimension Type For " + worldKey);
            return DimensionType.OVERWORLD;
        }
        
        return obj;
    }
    
    @OnlyIn(Dist.CLIENT)
    public static DimensionType getDimensionType(RegistryKey<DimensionType> registryKey) {
        return currentDimensionTypeTracker.func_230520_a_().getValueForKey(registryKey);
    }
    
}
