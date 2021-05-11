package com.withertech.imm_boti.mixin.common.chunk_sync;

import com.withertech.imm_boti.ducks.IEChunkTicketManager;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.SortedArraySet;
import net.minecraft.util.math.SectionPos;
import net.minecraft.world.server.Ticket;
import net.minecraft.world.server.TicketManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TicketManager.class)
public abstract class MixinChunkTicketManager implements IEChunkTicketManager {
    
    @Shadow
    @Final
    private Long2ObjectMap<ObjectSet<ServerPlayerEntity>> playersByChunkPos;
    
    
    @Shadow
    protected abstract void setViewDistance(int viewDistance);
    
    @Shadow protected abstract SortedArraySet<Ticket<?>> getTicketSet(long position);
    
    //avoid NPE
    @Inject(method = "Lnet/minecraft/world/server/TicketManager;removePlayer(Lnet/minecraft/util/math/SectionPos;Lnet/minecraft/entity/player/ServerPlayerEntity;)V", at = @At("HEAD"))
    private void onHandleChunkLeave(
        SectionPos chunkSectionPos_1,
        ServerPlayerEntity serverPlayerEntity_1,
        CallbackInfo ci
    ) {
        long long_1 = chunkSectionPos_1.asChunkPos().asLong();
        playersByChunkPos.putIfAbsent(long_1, new ObjectOpenHashSet<>());
    }
    
    @Override
    public void mySetWatchDistance(int newWatchDistance) {
        setViewDistance(newWatchDistance);
    }
    
    @Override
    public SortedArraySet<Ticket<?>> portal_getTicketSet(long chunkPos) {
        return getTicketSet(chunkPos);
    }
}
