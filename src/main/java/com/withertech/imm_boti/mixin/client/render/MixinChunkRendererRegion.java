package com.withertech.imm_boti.mixin.client.render;

import net.minecraft.client.renderer.chunk.ChunkRenderCache;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkRenderCache.class)
public class MixinChunkRendererRegion {
    //will this avoid that random crash?
    @Inject(
        method = "Lnet/minecraft/client/renderer/chunk/ChunkRenderCache;generateCache(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;I)Lnet/minecraft/client/renderer/chunk/ChunkRenderCache;",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void onCreate(
        World worldIn,
        BlockPos from,
        BlockPos to,
        int padding,
        CallbackInfoReturnable<ChunkRenderCache> cir
    ) {
        if (worldIn == null) {
            cir.setReturnValue(null);
            cir.cancel();
        }
    }
    
    
}
