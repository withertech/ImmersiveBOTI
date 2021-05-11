package com.withertech.imm_boti.ducks;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.RenderTypeBuffers;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.shader.Framebuffer;

public interface IEMinecraftClient {
    void setFrameBuffer(Framebuffer buffer);
    
    Screen getCurrentScreen();
    
    void setWorldRenderer(WorldRenderer r);
    
    void setBufferBuilderStorage(RenderTypeBuffers arg);
}
