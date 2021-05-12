package com.withertech.tim_wim_holes.portal.global_portals;

import com.withertech.hiding_in_the_bushes.MyNetwork;
import com.withertech.hiding_in_the_bushes.O_O;
import com.withertech.tim_wim_holes.ClientWorldLoader;
import com.withertech.tim_wim_holes.Helper;
import com.withertech.tim_wim_holes.McHelper;
import com.withertech.tim_wim_holes.ModMain;
import com.withertech.tim_wim_holes.ducks.IEClientWorld;
import com.withertech.tim_wim_holes.portal.Portal;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.IPacket;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.Validate;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class GlobalPortalStorage extends WorldSavedData {
    public List<Portal> data;
    public WeakReference<ServerWorld> world;
    private int version = 1;
    private boolean shouldReSync = false;
    
    public static void init() {
        ModMain.postServerTickSignal.connect(() -> {
            McHelper.getServer().getWorlds().forEach(world1 -> {
                GlobalPortalStorage gps = GlobalPortalStorage.get(world1);
                gps.tick();
            });
        });
        
        ModMain.serverCleanupSignal.connect(() -> {
            for (ServerWorld world : McHelper.getServer().getWorlds()) {
                get(world).onServerClose();
            }
        });
        
        if (!O_O.isDedicatedServer()) {
            initClient();
        }
    }
    
    @OnlyIn(Dist.CLIENT)
    private static void initClient() {
        ModMain.clientCleanupSignal.connect(GlobalPortalStorage::onClientCleanup);
    }
    
    @OnlyIn(Dist.CLIENT)
    private static void onClientCleanup() {
        if (ClientWorldLoader.getIsInitialized()) {
            for (ClientWorld clientWorld : ClientWorldLoader.getClientWorlds()) {
                for (Portal globalPortal : McHelper.getGlobalPortals(clientWorld)) {
                    globalPortal.remove();
                }
            }
        }
    }
    
    public GlobalPortalStorage(String string_1, ServerWorld world_) {
        super(string_1);
        world = new WeakReference<>(world_);
        data = new ArrayList<>();
    }
    
    public static void onPlayerLoggedIn(ServerPlayerEntity player) {
        McHelper.getServer().getWorlds().forEach(
            world -> {
                GlobalPortalStorage storage = get(world);
                if (!storage.data.isEmpty()) {
                    player.connection.sendPacket(
                        MyNetwork.createGlobalPortalUpdate(
                            storage
                        )
                    );
                }
            }
        );
        
    }
    
    public void onDataChanged() {
        setDirty(true);
        
        shouldReSync = true;
        
        
    }
    
    public void removePortal(Portal portal) {
        data.remove(portal);
        portal.remove();
        onDataChanged();
    }
    
    public void addPortal(Portal portal) {
        Validate.isTrue(!data.contains(portal));
        
        Validate.isTrue(portal.isPortalValid());
        
        portal.isGlobalPortal = true;
        portal.removed = false;
        data.add(portal);
        onDataChanged();
    }
    
    public void removePortals(Predicate<Portal> predicate) {
        data.removeIf(portal -> {
            final boolean shouldRemove = predicate.test(portal);
            if (shouldRemove) {
                portal.remove();
            }
            return shouldRemove;
        });
        onDataChanged();
    }
    
    private void syncToAllPlayers() {
        IPacket packet = MyNetwork.createGlobalPortalUpdate(this);
        McHelper.getCopiedPlayerList().forEach(
            player -> player.connection.sendPacket(packet)
        );
    }
    
    @Override
    public void read(CompoundNBT tag) {
        
        ServerWorld currWorld = world.get();
        Validate.notNull(currWorld);
        List<Portal> newData = getPortalsFromTag(tag, currWorld);
        
        if (tag.contains("version")) {
            version = tag.getInt("version");
        }
        
        data = newData;
        
        clearAbnormalPortals();
    }
    
    private static List<Portal> getPortalsFromTag(
        CompoundNBT tag,
        World currWorld
    ) {
        /**{@link CompoundTag#getType()}*/
        ListNBT listTag = tag.getList("data", 10);
        
        List<Portal> newData = new ArrayList<>();
        
        for (int i = 0; i < listTag.size(); i++) {
            CompoundNBT compoundTag = listTag.getCompound(i);
            Portal e = readPortalFromTag(currWorld, compoundTag);
            if (e != null) {
                newData.add(e);
            }
            else {
                Helper.err("error reading portal" + compoundTag);
            }
        }
        return newData;
    }
    
    private static Portal readPortalFromTag(World currWorld, CompoundNBT compoundTag) {
        ResourceLocation entityId = new ResourceLocation(compoundTag.getString("entity_type"));
        EntityType<?> entityType = Registry.ENTITY_TYPE.getOrDefault(entityId);
        
        Entity e = entityType.create(currWorld);
        e.read(compoundTag);
        
        ((Portal) e).isGlobalPortal = true;
        
        return (Portal) e;
    }
    
    @Override
    public CompoundNBT write(CompoundNBT tag) {
        if (data == null) {
            return tag;
        }
        
        ListNBT listTag = new ListNBT();
        ServerWorld currWorld = world.get();
        Validate.notNull(currWorld);
        
        for (Portal portal : data) {
            Validate.isTrue(portal.world == currWorld);
            CompoundNBT portalTag = new CompoundNBT();
            portal.writeWithoutTypeId(portalTag);
            portalTag.putString(
                "entity_type",
                EntityType.getKey(portal.getType()).toString()
            );
            listTag.add(portalTag);
        }
        
        tag.put("data", listTag);
        
        tag.putInt("version", version);
        
        return tag;
    }
    
    public static GlobalPortalStorage get(
        ServerWorld world
    ) {
        return world.getSavedData().getOrCreate(
            () -> new GlobalPortalStorage("global_portal", world),
            "global_portal"
        );
    }
    
    public void tick() {
        if (shouldReSync) {
            syncToAllPlayers();
            shouldReSync = false;
        }
        
        if (version <= 1) {
            upgradeData(world.get());
            version = 2;
            setDirty(true);
        }
    }
    
    public void clearAbnormalPortals() {
        data.removeIf(e -> {
            RegistryKey<World> dimensionTo = ((Portal) e).dimensionTo;
            if (McHelper.getServer().getWorld(dimensionTo) == null) {
                Helper.err("Missing Dimension for global portal " + dimensionTo.getLocation());
                return true;
            }
            return false;
        });
    }
    
    private static void upgradeData(ServerWorld world) {
        //removed
    }
    
    @OnlyIn(Dist.CLIENT)
    public static void receiveGlobalPortalSync(RegistryKey<World> dimension, CompoundNBT compoundTag) {
        ClientWorld world = ClientWorldLoader.getWorld(dimension);
        
        List<Portal> oldGlobalPortals = ((IEClientWorld) world).getGlobalPortals();
        if (oldGlobalPortals != null) {
            for (Portal p : oldGlobalPortals) {
                p.remove();
            }
        }
        
        List<Portal> newPortals = getPortalsFromTag(compoundTag, world);
        for (Portal p : newPortals) {
            p.removed = false;
            p.isGlobalPortal = true;
            
            Validate.isTrue(p.isPortalValid());
            
            ClientWorldLoader.getWorld(p.getDestDim());
        }
        
        ((IEClientWorld) world).setGlobalPortals(newPortals);
        
        Helper.info("Global Portals Updated " + dimension.getLocation());
    }
    
    public static void convertNormalPortalIntoGlobalPortal(Portal portal) {
        Validate.isTrue(!portal.getIsGlobal());
        Validate.isTrue(!portal.world.isRemote());
        
        //global portal can only be square
        portal.specialShape = null;
        
        portal.remove();
        
        Portal newPortal = McHelper.copyEntity(portal);
        
        get(((ServerWorld) portal.world)).addPortal(newPortal);
    }
    
    public static void convertGlobalPortalIntoNormalPortal(Portal portal) {
        Validate.isTrue(portal.getIsGlobal());
        Validate.isTrue(!portal.world.isRemote());
        
        get(((ServerWorld) portal.world)).removePortal(portal);
        
        Portal newPortal = McHelper.copyEntity(portal);
        
        McHelper.spawnServerEntity(newPortal);
    }
    
    private void onServerClose() {
        for (Portal portal : data) {
            portal.remove();
        }
    }
}
