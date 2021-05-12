package com.withertech.tim_wim_holes.portal.custom_portal_gen;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import com.withertech.tim_wim_holes.Helper;
import com.withertech.tim_wim_holes.McHelper;
import com.withertech.tim_wim_holes.ModMain;
import com.withertech.tim_wim_holes.my_util.UCoordinate;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUseContext;
import net.minecraft.resources.IResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraft.util.registry.WorldSettingsImport;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CustomPortalGenManagement {
    private static final Multimap<Item, CustomPortalGeneration> useItemGen = HashMultimap.create();
    private static final Multimap<Item, CustomPortalGeneration> throwItemGen = HashMultimap.create();
    
    private static final ArrayList<CustomPortalGeneration> convGen = new ArrayList<>();
    private static final Map<UUID, UCoordinate> playerPosBeforeTravel = new HashMap<>();
    
    public static void onDatapackReload() {
        useItemGen.clear();
        throwItemGen.clear();
        convGen.clear();
        playerPosBeforeTravel.clear();
        
        Helper.info("Loading custom portal gen");
        
        MinecraftServer server = McHelper.getServer();
        
        DynamicRegistries.Impl registryTracker =
            ((DynamicRegistries.Impl) server.getDynamicRegistries());
        
        IResourceManager resourceManager = server.resourceManager.getResourceManager();
        
        WorldSettingsImport<JsonElement> registryOps = new WorldSettingsImport<>(
            JsonOps.INSTANCE,
            WorldSettingsImport.IResourceAccess.create(resourceManager),
            registryTracker,
            Maps.newIdentityHashMap()
        );
        
        SimpleRegistry<CustomPortalGeneration> emptyRegistry = new SimpleRegistry<>(
            CustomPortalGeneration.registryRegistryKey,
            Lifecycle.stable()
        );
        
        DataResult<SimpleRegistry<CustomPortalGeneration>> dataResult =
            registryOps.decode(
                emptyRegistry,
                CustomPortalGeneration.registryRegistryKey,
                CustomPortalGeneration.codec.codec()
            );
        
        SimpleRegistry<CustomPortalGeneration> result = dataResult.get().left().orElse(null);
        
        if (result == null) {
            DataResult.PartialResult<SimpleRegistry<CustomPortalGeneration>> r =
                dataResult.get().right().get();
            McHelper.sendMessageToFirstLoggedPlayer(new StringTextComponent(
                "Error when parsing custom portal generation\n" +
                    r.message()
            ));
            return;
        }
        
        result.getEntries().forEach((entry) -> {
            CustomPortalGeneration gen = entry.getValue();
            gen.identifier = entry.getKey().getLocation();
            
            if (!gen.initAndCheck()) {
                Helper.info("Custom Portal Gen Is Not Activated " + gen.toString());
                return;
            }
            
            Helper.info("Loaded Custom Portal Gen " + entry.getKey().getLocation());
            
            load(gen);
            
            if (gen.reversible) {
                CustomPortalGeneration reverse = gen.getReverse();
                
                if (reverse != null) {
                    reverse.identifier = entry.getKey().getLocation();
                    if (gen.initAndCheck()) {
                        load(reverse);
                    }
                }
                else {
                    McHelper.sendMessageToFirstLoggedPlayer(new StringTextComponent(
                        "Cannot create reverse generation of " + gen
                    ));
                }
            }
        });
    }
    
    private static void load(CustomPortalGeneration gen) {
        PortalGenTrigger trigger = gen.trigger;
        if (trigger instanceof PortalGenTrigger.UseItemTrigger) {
            useItemGen.put(((PortalGenTrigger.UseItemTrigger) trigger).item, gen);
        }
        else if (trigger instanceof PortalGenTrigger.ThrowItemTrigger) {
            throwItemGen.put(
                ((PortalGenTrigger.ThrowItemTrigger) trigger).item,
                gen
            );
        }
        else if (trigger instanceof PortalGenTrigger.ConventionalDimensionChangeTrigger) {
            convGen.add(gen);
        }
    }
    
    public static void onItemUse(ItemUseContext context, ActionResultType actionResult) {
        if (context.getWorld().isRemote()) {
            return;
        }
        
        Item item = context.getItem().getItem();
        if (useItemGen.containsKey(item)) {
            ModMain.serverTaskList.addTask(() -> {
                for (CustomPortalGeneration gen : useItemGen.get(item)) {
                    boolean result = gen.perform(
                        ((ServerWorld) context.getWorld()),
                        context.getPos().offset(context.getFace()),
                        context.getPlayer()
                    );
                    if (result) {
                        if (gen.trigger instanceof PortalGenTrigger.UseItemTrigger) {
                            PortalGenTrigger.UseItemTrigger trigger =
                                (PortalGenTrigger.UseItemTrigger) gen.trigger;
                            if (trigger.consume) {
                                context.getItem().shrink(1);
                            }
                        }
                        break;
                    }
                }
                return true;
            });
        }
    }
    
    public static void onItemTick(ItemEntity entity) {
        if (entity.world.isRemote()) {
            return;
        }
        if (entity.getThrowerId() == null) {
            return;
        }
        
        if (entity.cannotPickup()) {
            Item item = entity.getItem().getItem();
            if (throwItemGen.containsKey(item)) {
                ModMain.serverTaskList.addTask(() -> {
                    for (CustomPortalGeneration gen : throwItemGen.get(item)) {
                        boolean result = gen.perform(
                            ((ServerWorld) entity.world),
                            entity.getPosition(),
                            entity
                        );
                        if (result) {
                            entity.getItem().shrink(1);
                            break;
                        }
                    }
                    return true;
                });
            }
        }
    }
    
    public static void onBeforeConventionalDimensionChange(
        ServerPlayerEntity player
    ) {
        playerPosBeforeTravel.put(player.getUniqueID(), new UCoordinate(player));
    }
    
    public static void onAfterConventionalDimensionChange(
        ServerPlayerEntity player
    ) {
        UUID uuid = player.getUniqueID();
        if (playerPosBeforeTravel.containsKey(uuid)) {
            UCoordinate startCoord = playerPosBeforeTravel.get(uuid);
            
            ServerWorld startWorld = McHelper.getServerWorld(startCoord.dimension);
            
            BlockPos startPos = new BlockPos(startCoord.pos);
            
            for (CustomPortalGeneration gen : convGen) {
                boolean succeeded = gen.perform(startWorld, startPos, player);
                
                if (succeeded) {
                    playerPosBeforeTravel.remove(uuid);
                    return;
                }
            }
        }
        playerPosBeforeTravel.remove(uuid);
    }
}
