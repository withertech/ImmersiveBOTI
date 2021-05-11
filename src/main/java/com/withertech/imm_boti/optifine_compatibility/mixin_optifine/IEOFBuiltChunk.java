package com.withertech.imm_boti.optifine_compatibility.mixin_optifine;

import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.util.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Invoker;

@Pseudo
@Mixin(targets = "net.minecraft.client.renderer.chunk.ChunkRenderDispatcher$ChunkRender")
public interface IEOFBuiltChunk {
    @Invoker("setRenderChunkNeighbour")
    void ip_setRenderChunkNeighbour(Direction facing, ChunkRenderDispatcher.ChunkRender neighbour);
    
}
