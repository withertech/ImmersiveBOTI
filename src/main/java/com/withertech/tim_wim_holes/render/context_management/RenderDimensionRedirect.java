package com.withertech.tim_wim_holes.render.context_management;

import com.withertech.tim_wim_holes.CHelper;
import com.withertech.tim_wim_holes.ModMain;
import com.withertech.tim_wim_holes.dimension_sync.DimId;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import java.util.HashMap;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class RenderDimensionRedirect {
    private static Map<String, String> idMap = new HashMap<>();
    
    //null indicates no shader
    private static Map<RegistryKey<World>, RegistryKey<World>> redirectMap = new HashMap<>();
    
    public static void updateIdMap(Map<String, String> redirectIdMap) {
        idMap = redirectIdMap;
    }
    
    private static void updateRedirectMap() {
        redirectMap.clear();
        idMap.forEach((key, value) -> {
            RegistryKey<World> from = DimId.idToKey(new ResourceLocation(key));
            RegistryKey<World> to = DimId.idToKey(new ResourceLocation(value));
            if (from == null) {
                ModMain.clientTaskList.addTask(() -> {
                    CHelper.printChat("Invalid Dimension " + key);
                    return true;
                });
                return;
            }
            if (to == null) {
                if (!value.equals("vanilla")) {
                    ModMain.clientTaskList.addTask(() -> {
                        CHelper.printChat("Invalid Dimension " + value);
                        return true;
                    });
                    return;
                }
            }
            
            redirectMap.put(from, to);
        });
    }
    
    public static boolean isNoShader(RegistryKey<World> dimension) {
        if (redirectMap.containsKey(dimension)) {
            RegistryKey<World> r = redirectMap.get(dimension);
            if (r == null) {
                return true;
            }
        }
        return false;
    }
    
    public static RegistryKey<World> getRedirectedDimension(RegistryKey<World> dimension) {
        if (redirectMap.containsKey(dimension)) {
            RegistryKey<World> r = redirectMap.get(dimension);
            if (r == null) {
                return dimension;
            }
            return r;
        }
        else {
            return dimension;
        }
    }
    
    public static boolean hasSkylight(ClientWorld world) {
        updateRedirectMap();
        RegistryKey<World> redirectedDimension = getRedirectedDimension(world.getDimensionKey());
        if (redirectedDimension == world.getDimensionKey()) {
            return world.getDimensionType().hasSkyLight();
        }
        
        //if it's redirected, it's probably redirected to a vanilla dimension
        if (redirectedDimension == World.OVERWORLD) {
            return true;
        }
        else {
            return false;
        }
    }
}
