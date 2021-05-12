package com.withertech.tim_wim_holes.ducks;

import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;

public interface IEWorldRendererChunkInfo {
    ChunkRenderDispatcher.ChunkRender getBuiltChunk();
}
