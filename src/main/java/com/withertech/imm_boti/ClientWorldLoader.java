package com.withertech.imm_boti;

import com.mojang.authlib.GameProfile;
import com.withertech.imm_boti.dimension_sync.DimensionTypeSync;
import com.withertech.imm_boti.ducks.IECamera;
import com.withertech.imm_boti.ducks.IEClientPlayNetworkHandler;
import com.withertech.imm_boti.ducks.IEClientWorld;
import com.withertech.imm_boti.ducks.IEMinecraftClient;
import com.withertech.imm_boti.ducks.IEParticleManager;
import com.withertech.imm_boti.ducks.IEWorld;
import com.withertech.imm_boti.my_util.LimitedLogger;
import com.withertech.imm_boti.my_util.SignalArged;
import com.withertech.imm_boti.portal.Portal;
import com.withertech.imm_boti.render.context_management.DimensionRenderHelper;
import com.withertech.imm_boti.render.context_management.RenderStates;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketDirection;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@OnlyIn(Dist.CLIENT)
public class ClientWorldLoader {
    private static final Map<RegistryKey<World>, ClientWorld> clientWorldMap = new HashMap<>();
    public static final Map<RegistryKey<World>, WorldRenderer> worldRendererMap = new HashMap<>();
    public static final Map<RegistryKey<World>, DimensionRenderHelper> renderHelperMap = new HashMap<>();
    
    private static final Minecraft client = Minecraft.getInstance();
    
    private static boolean isInitialized = false;
    
    private static boolean isCreatingClientWorld = false;
    
    public static boolean isClientRemoteTicking = false;
    
    public static final SignalArged<ClientWorld> clientWorldLoadSignal = new SignalArged<>();
    
    public static void init() {
        ModMain.postClientTickSignal.connect(ClientWorldLoader::tick);
        
        ModMain.clientCleanupSignal.connect(ClientWorldLoader::cleanUp);
    }
    
    public static boolean getIsInitialized() {
        return isInitialized;
    }
    
    public static boolean getIsCreatingClientWorld() {
        return isCreatingClientWorld;
    }
    
    private static void tick() {
        if (CGlobal.isClientRemoteTickingEnabled) {
            isClientRemoteTicking = true;
            clientWorldMap.values().forEach(world -> {
                if (client.world != world) {
                    tickRemoteWorld(world);
                }
            });
            worldRendererMap.values().forEach(worldRenderer -> {
                if (worldRenderer != client.worldRenderer) {
                    worldRenderer.tick();
                }
            });
            isClientRemoteTicking = false;
        }
        
        boolean lightmapTextureConflict = false;
        for (DimensionRenderHelper helper : renderHelperMap.values()) {
            helper.tick();
            if (helper.world != client.world) {
                if (helper.lightmapTexture == client.gameRenderer.getLightTexture()) {
                    Helper.err(String.format(
                        "Lightmap Texture Conflict %s %s",
                        helper.world.getDimensionKey(),
                        client.world.getDimensionKey()
                    ));
                    lightmapTextureConflict = true;
                }
            }
        }
        if (lightmapTextureConflict) {
            renderHelperMap.values().forEach(DimensionRenderHelper::cleanUp);
            renderHelperMap.clear();
            Helper.log("Refreshed Lightmaps");
        }
        
    }
    
    private static final LimitedLogger limitedLogger = new LimitedLogger(10);
    
    private static void tickRemoteWorld(ClientWorld newWorld) {
        List<Portal> nearbyPortals = CHelper.getClientNearbyPortals(10).collect(Collectors.toList());
        
        WorldRenderer newWorldRenderer = getWorldRenderer(newWorld.getDimensionKey());
        
        ClientWorld oldWorld = client.world;
        WorldRenderer oldWorldRenderer = client.worldRenderer;
        
        client.world = newWorld;
        ((IEParticleManager) client.particles).mySetWorld(newWorld);
        
        //the world renderer's world field is used for particle spawning
        ((IEMinecraftClient) client).setWorldRenderer(newWorldRenderer);
        
        try {
            newWorld.tickEntities();
            newWorld.tick(() -> true);
            
            if (!client.isGamePaused()) {
                tickRemoteWorldRandomTicksClient(newWorld, nearbyPortals);
            }
        }
        catch (Throwable e) {
            limitedLogger.invoke(e::printStackTrace);
        }
        finally {
            client.world = oldWorld;
            ((IEParticleManager) client.particles).mySetWorld(oldWorld);
            ((IEMinecraftClient) client).setWorldRenderer(oldWorldRenderer);
        }
    }
    
    // show nether particles through portal
    // TODO optimize it
    private static void tickRemoteWorldRandomTicksClient(
        ClientWorld newWorld, List<Portal> nearbyPortals
    ) {
        nearbyPortals.stream().filter(
            portal -> portal.dimensionTo == newWorld.getDimensionKey()
        ).findFirst().ifPresent(portal -> {
            Vector3d playerPos = client.player.getPositionVec();
            Vector3d center = portal.transformPoint(playerPos);
            
            ActiveRenderInfo camera = client.gameRenderer.getActiveRenderInfo();
            Vector3d oldCameraPos = camera.getProjectedView();
            
            ((IECamera) camera).portal_setPos(center);
            
            newWorld.animateTick(
                (int) center.x, (int) center.y, (int) center.z
            );
            
            client.particles.tick();
            
            ((IECamera) camera).portal_setPos(oldCameraPos);
        });
    }
    
    private static void cleanUp() {
        worldRendererMap.values().forEach(
            worldRenderer -> worldRenderer.setWorldAndLoadRenderers(null)
        );
        
        clientWorldMap.clear();
        worldRendererMap.clear();
        
        renderHelperMap.values().forEach(DimensionRenderHelper::cleanUp);
        renderHelperMap.clear();
        
        isInitialized = false;
        
        
    }
    
    //@Nullable
    public static WorldRenderer getWorldRenderer(RegistryKey<World> dimension) {
        initializeIfNeeded();
        
        return worldRendererMap.get(dimension);
    }
    
    public static ClientWorld getWorld(RegistryKey<World> dimension) {
        Validate.notNull(dimension);
        
        initializeIfNeeded();
        
        if (!clientWorldMap.containsKey(dimension)) {
            return createSecondaryClientWorld(dimension);
        }
        
        return clientWorldMap.get(dimension);
    }
    
    public static DimensionRenderHelper getDimensionRenderHelper(RegistryKey<World> dimension) {
        initializeIfNeeded();
        
        DimensionRenderHelper result = renderHelperMap.computeIfAbsent(
            dimension,
            dimensionType -> {
                return new DimensionRenderHelper(
                    getWorld(dimension)
                );
            }
        );
        
        Validate.isTrue(result.world.getDimensionKey() == dimension);
        
        return result;
    }
    
    public static void initializeIfNeeded() {
        if (!isInitialized) {
            Validate.isTrue(client.world != null);
            Validate.isTrue(client.worldRenderer != null);
            
            Validate.notNull(client.player);
            Validate.isTrue(client.player.world == client.world);
            
            RegistryKey<World> playerDimension = client.world.getDimensionKey();
            clientWorldMap.put(playerDimension, client.world);
            worldRendererMap.put(playerDimension, client.worldRenderer);
            renderHelperMap.put(
                client.world.getDimensionKey(),
                new DimensionRenderHelper(client.world)
            );
            
            isInitialized = true;
        }
    }
    
    private static ClientWorld createSecondaryClientWorld(RegistryKey<World> dimension) {
        Validate.isTrue(client.player.world.getDimensionKey() != dimension);
        
        isCreatingClientWorld = true;
        
        client.getProfiler().startSection("create_world");
        
        int chunkLoadDistance = 3;// my own chunk manager doesn't need it
        
        WorldRenderer worldRenderer = new WorldRenderer(client, client.getRenderTypeBuffers());
        
        ClientWorld newWorld;
        try {
            ClientPlayNetHandler newNetworkHandler = new ClientPlayNetHandler(
                client,
                new ChatScreen("You should not be seeing me. I'm just a faked screen."),
                new NetworkManager(PacketDirection.CLIENTBOUND),
                new GameProfile(null, "faked_profiler_id")
            );
            //multiple net handlers share the same playerListEntries object
            ClientPlayNetHandler mainNetHandler = client.player.connection;
            ((IEClientPlayNetworkHandler) newNetworkHandler).setPlayerListEntries(
                ((IEClientPlayNetworkHandler) mainNetHandler).getPlayerListEntries()
            );
            RegistryKey<DimensionType> dimensionTypeKey =
                DimensionTypeSync.getDimensionTypeKey(dimension);
            ClientWorld.ClientWorldInfo currentProperty =
                (ClientWorld.ClientWorldInfo) ((IEWorld) client.world).myGetProperties();
            DynamicRegistries dimensionTracker = mainNetHandler.getRegistries();
            ((IEClientPlayNetworkHandler) newNetworkHandler).portal_setRegistryManager(
                dimensionTracker);
            DimensionType dimensionType = dimensionTracker
                .func_230520_a_().getValueForKey(dimensionTypeKey);
            
            ClientWorld.ClientWorldInfo properties = new ClientWorld.ClientWorldInfo(
                currentProperty.getDifficulty(),
                currentProperty.isHardcore(),
                currentProperty.getVoidFogHeight() < 1.0
            );
            newWorld = new ClientWorld(
                newNetworkHandler,
                properties,
                dimension,
                dimensionType,
                chunkLoadDistance,
                () -> client.getProfiler(),
                worldRenderer,
                client.world.isDebug(),
                client.world.getBiomeManager().seed
            );
        }
        catch (Exception e) {
            throw new IllegalStateException(
                "Creating Client World " + dimension + " " + clientWorldMap.keySet(),
                e
            );
        }
        
        worldRenderer.setWorldAndLoadRenderers(newWorld);
        
        worldRenderer.onResourceManagerReload(client.getResourceManager());
        
        ((IEClientPlayNetworkHandler) ((IEClientWorld) newWorld).getNetHandler())
            .setWorld(newWorld);
        
        clientWorldMap.put(dimension, newWorld);
        worldRendererMap.put(dimension, worldRenderer);
        
        Helper.log("Client World Created " + dimension.getLocation());
//        new Throwable().printStackTrace();
        
        isCreatingClientWorld = false;
        
        clientWorldLoadSignal.emit(newWorld);
        
        client.getProfiler().endSection();
        
        return newWorld;
    }
    
    public static Collection<ClientWorld> getClientWorlds() {
        Validate.isTrue(isInitialized);
        
        return clientWorldMap.values();
    }
    
    @Nullable
    public static Vector3d getTransformedSoundPosition(
        ClientWorld soundWorld,
        Vector3d soundPos
    ) {
        if (client.player == null) {
            return null;
        }
        
        soundWorld.getProfiler().startSection("cross_portal_sound");
        
        Vector3d result = McHelper.getNearbyPortals(
            soundWorld, soundPos, 10
        ).filter(
            portal -> portal.getDestDim() == RenderStates.originalPlayerDimension &&
                portal.transformPoint(soundPos).distanceTo(RenderStates.originalPlayerPos) < 20
        ).findFirst().map(
            portal -> {
                // sound goes to portal then goes through portal then goes to player
                
                Vector3d playerCameraPos = RenderStates.originalPlayerPos.add(
                    0, client.player.getEyeHeight(), 0
                );
                
                Vector3d soundEnterPortalPoint = portal.getNearestPointInPortal(soundPos);
                double soundEnterPortalDistance = soundEnterPortalPoint.distanceTo(soundPos);
                Vector3d soundExitPortalPoint = portal.transformPoint(soundEnterPortalPoint);
                
                Vector3d playerToSoundExitPoint = soundExitPortalPoint.subtract(playerCameraPos);
                
                // the distance between sound source and the portal is applied by
                //  moving the pos further away from the player
                Vector3d projectedPos = portal.getDestPos().add(
                    playerToSoundExitPoint.normalize().scale(soundEnterPortalDistance)
                );
                
                // lerp to actual position when you get close to the portal
                // this helps smooth the transition when the player is going through the portal
                
                Vector3d actualPos = portal.transformPoint(soundPos);
                
                double playerDistanceToPortalDest = soundExitPortalPoint.distanceTo(playerCameraPos);
                final double fadeDistance = 5.0;
                // 0 means close, 1 means far
                double lerpRatio = MathHelper.clamp(
                    playerDistanceToPortalDest / fadeDistance, 0.0, 1.0
                );
                
                // do the lerp
                Vector3d resultPos = actualPos.add(projectedPos.subtract(actualPos).scale(lerpRatio));
                
                return resultPos;
            }
        ).orElse(null);
        
        soundWorld.getProfiler().endSection();
        
        return result;
    }
}
