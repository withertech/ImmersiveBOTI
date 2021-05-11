package com.withertech.imm_boti.ducks;

import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;

public interface IEWorldRendererChunkInfo {
    ChunkRenderDispatcher.ChunkRender getBuiltChunk();
}
