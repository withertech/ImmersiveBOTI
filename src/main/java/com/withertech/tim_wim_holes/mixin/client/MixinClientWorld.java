package com.withertech.tim_wim_holes.mixin.client;

import com.withertech.hiding_in_the_bushes.O_O;
import com.withertech.tim_wim_holes.ClientWorldLoader;
import com.withertech.tim_wim_holes.ducks.IEClientWorld;
import com.withertech.tim_wim_holes.portal.Portal;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.multiplayer.ClientChunkProvider;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.profiler.IProfiler;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.EmptyChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.function.Supplier;

@Mixin(ClientWorld.class)
public abstract class MixinClientWorld implements IEClientWorld {
    @Shadow
    @Final
    @Mutable
    private ClientPlayNetHandler connection;
    
    @Mutable
    @Shadow
    @Final
    private ClientChunkProvider chunkProvider;
    
    @Shadow
    public abstract Entity getEntityByID(int id);
    
    @Shadow
    @Final
    private Minecraft mc;
    private List<Portal> globalTrackedPortals;
    
    @Override
    public ClientPlayNetHandler getNetHandler() {
        return connection;
    }
    
    @Override
    public void setNetHandler(ClientPlayNetHandler handler) {
        connection = handler;
    }
    
    @Override
    public List<Portal> getGlobalPortals() {
        return globalTrackedPortals;
    }
    
    @Override
    public void setGlobalPortals(List<Portal> arg) {
        globalTrackedPortals = arg;
    }
    
    //use my client chunk manager
    @Inject(
        method = "<init>",
        at = @At("RETURN")
    )
    void onConstructed(
        ClientPlayNetHandler clientPlayNetworkHandler, ClientWorld.ClientWorldInfo properties,
        RegistryKey<World> registryKey, DimensionType dimensionType, int i,
        Supplier<IProfiler> supplier, WorldRenderer worldRenderer, boolean bl,
        long l, CallbackInfo ci
    ) {
        ClientWorld clientWorld = (ClientWorld) (Object) this;
        ClientChunkProvider myClientChunkManager =
            O_O.createMyClientChunkManager(clientWorld, i);
        chunkProvider = myClientChunkManager;
    }
    
    // avoid entity duplicate when an entity travels
    @Inject(
        method = "Lnet/minecraft/client/world/ClientWorld;addEntityImpl(ILnet/minecraft/entity/Entity;)V",
        at = @At("TAIL")
    )
    private void onOnEntityAdded(int entityId, Entity entityIn, CallbackInfo ci) {
        if (ClientWorldLoader.getIsInitialized()) {
            for (ClientWorld world : ClientWorldLoader.getClientWorlds()) {
                if (world != (Object) this) {
                    world.removeEntityFromWorld(entityId);
                }
            }
        }
    }
    
    /**
     * If the player goes into a portal when the other side chunk is not yet loaded
     * freeze the player so the player won't drop
     * {@link ClientPlayerEntity#tick()}
     */
    @Inject(
        method = "Lnet/minecraft/client/world/ClientWorld;chunkExists(II)Z",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onIsChunkLoaded(int chunkX, int chunkZ, CallbackInfoReturnable<Boolean> cir) {
        Chunk chunk = chunkProvider.getChunk(chunkX, chunkZ, ChunkStatus.FULL, false);
        if (chunk == null || chunk instanceof EmptyChunk) {
            cir.setReturnValue(false);
//            Helper.log("chunk not loaded");
//            new Throwable().printStackTrace();
        }
    }
    
    // for debug
    @Inject(method = "Lnet/minecraft/client/world/ClientWorld;toString()Ljava/lang/String;", at = @At("HEAD"), cancellable = true)
    private void onToString(CallbackInfoReturnable<String> cir) {
        ClientWorld this_ = (ClientWorld) (Object) this;
        cir.setReturnValue("ClientWorld " + this_.getDimensionKey().getLocation());
    }
}
