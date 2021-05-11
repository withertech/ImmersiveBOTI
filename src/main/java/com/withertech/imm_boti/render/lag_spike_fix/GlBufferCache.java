package com.withertech.imm_boti.render.lag_spike_fix;

import com.withertech.imm_boti.Global;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL15;

public class GlBufferCache {
    public static Minecraft client = Minecraft.getInstance();
    
    private static final IntList bufferIds = new IntArrayList();
    
    public static int getNewBufferId() {
        if (bufferIds.isEmpty()) {
            reserve(1000);
        }
        
        int taken = bufferIds.removeInt(bufferIds.size() - 1);
        return taken;
    }
    
    public static void init() {
    
    }
    
    @Deprecated
    private static void onPreRender() {
        if (!Global.cacheGlBuffer) {
            return;
        }
        
        if (client.world == null) {
            return;
        }
        
        if (client.player == null) {
            return;
        }
        
        client.getProfiler().startSection("gl_buffer_cache");
        
        int expectedBufferSize = getExpectedBufferSize();
        if (bufferIds.size() < expectedBufferSize) {
            reserve(expectedBufferSize / 120);
        }
        
        client.getProfiler().endSection();
        
    }
    
    private static void reserve(int num) {
        int[] buf = new int[num];
        GL15.glGenBuffers(buf);
        bufferIds.addElements(bufferIds.size(), buf);
    }
    
    private static int getExpectedBufferSize() {
        int viewDistance = client.gameSettings.renderDistanceChunks;
        int diameter = viewDistance * 2 + 1;
        
        //every column has 16 sections, every section has 5 layers
        return diameter * diameter * 16 * 5 * 4;
    }
}
