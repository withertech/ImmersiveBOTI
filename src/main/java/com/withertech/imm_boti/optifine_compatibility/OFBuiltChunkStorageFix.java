package com.withertech.imm_boti.optifine_compatibility;

import com.withertech.imm_boti.Helper;
import com.withertech.imm_boti.OFInterface;
import com.withertech.imm_boti.optifine_compatibility.mixin_optifine.IEOFBuiltChunk;
import com.withertech.imm_boti.optifine_compatibility.mixin_optifine.IEOFConfig;
import com.withertech.imm_boti.optifine_compatibility.mixin_optifine.IEOFVboRegion;
import com.withertech.imm_boti.render.MyBuiltChunkStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ViewFrustum;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

public class OFBuiltChunkStorageFix {
    private static Method BuiltChunkStorage_updateVboRegion;
    
    private static Field BuiltChunkStorage_mapVboRegions;
    
    private static Method BuiltChunkStorage_deleteVboRegions;
    
    public static void init() {
        BuiltChunkStorage_updateVboRegion = Helper.noError(() ->
            ViewFrustum.class
                .getDeclaredMethod(
                    "updateVboRegion",
                    ChunkRenderDispatcher.ChunkRender.class
                )
        );
        BuiltChunkStorage_updateVboRegion.setAccessible(true);
        
        BuiltChunkStorage_mapVboRegions = Helper.noError(() ->
            ViewFrustum.class
                .getDeclaredField("mapVboRegions")
        );
        BuiltChunkStorage_mapVboRegions.setAccessible(true);
        
        BuiltChunkStorage_deleteVboRegions = Helper.noError(() ->
            ViewFrustum.class
                .getDeclaredMethod(
                    "deleteVboRegions"
                )
        );
    }
    
    public static void onBuiltChunkCreated(
        ViewFrustum builtChunkStorage,
        ChunkRenderDispatcher.ChunkRender builtChunk
    ) {
        if (!OFInterface.isOptifinePresent) {
            return;
        }
        
        if (IEOFConfig.ip_isRenderRegions()) {
            Helper.noError(() ->
                BuiltChunkStorage_updateVboRegion.invoke(builtChunkStorage, builtChunk)
            );
        }
    }
    
    public static void purgeRenderRegions(
        MyBuiltChunkStorage storage
    ) {
        if (!OFInterface.isOptifinePresent) {
            return;
        }
        
        Minecraft.getInstance().getProfiler().startSection("ip_purge_optifine_render_regions");
        
        Map<ChunkPos, Object> vboRegionMap =
            (Map<ChunkPos, Object>) Helper.noError(() -> BuiltChunkStorage_mapVboRegions.get(storage));
        
        vboRegionMap.entrySet().removeIf(chunkPosObjectEntry -> {
            ChunkPos key = chunkPosObjectEntry.getKey();// it's the start block pos not chunk pos
            Object regionArray = chunkPosObjectEntry.getValue();
            
            // every render region contains 16 * 16 chunks
            
            int regionChunkX = key.x >> 4;
            int regionChunkZ = key.z >> 4;
            
            if (storage.isRegionActive(
                regionChunkX,
                regionChunkZ,
                regionChunkX + 15,
                regionChunkZ + 15
            )) {
                return false;
            }
            else {
                // needs to be deleted
                Object[] regionArray1 = (Object[]) regionArray;
                for (Object o : regionArray1) {
                    ((IEOFVboRegion) o).ip_deleteGlBuffers();
                }
                
                Helper.info("Purged OptiFine render region " + key);
                
                return true;
            }
        });
        
        Minecraft.getInstance().getProfiler().endSection();
    }
    
    public static void updateNeighbor(
        MyBuiltChunkStorage storage,
        ChunkRenderDispatcher.ChunkRender[] chunks
    ) {
        if (!OFInterface.isOptifinePresent) {
            return;
        }
        
        Minecraft.getInstance().getProfiler().startSection("neighbor");
        
        try {
            for (int l = 0; l < Direction.values().length; ++l) {
                Direction facing = Direction.values()[l];
                for (ChunkRenderDispatcher.ChunkRender renderChunk : chunks) {
                    BlockPos neighborPos = renderChunk.getBlockPosOffset16(facing);
                    ChunkRenderDispatcher.ChunkRender neighbour =
                        storage.myGetRenderChunkRaw(neighborPos, chunks);
                    
                    ((IEOFBuiltChunk) renderChunk).ip_setRenderChunkNeighbour(
                        facing, neighbour
                    );
                }
            }
        }
        catch (Throwable e) {
            throw new IllegalStateException(e);
        }
        
        Minecraft.getInstance().getProfiler().endSection();
    }
    
    public static void onBuiltChunkStorageCleanup(ViewFrustum builtChunkStorage) {
        if (!OFInterface.isOptifinePresent) {
            return;
        }
        
        Helper.noError(() -> {
            BuiltChunkStorage_deleteVboRegions.invoke(builtChunkStorage);
            return null;
        });
    }
}
