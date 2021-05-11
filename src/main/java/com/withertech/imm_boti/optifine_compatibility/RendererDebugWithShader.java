package com.withertech.imm_boti.optifine_compatibility;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.withertech.imm_boti.CGlobal;
import com.withertech.imm_boti.portal.Portal;
import com.withertech.imm_boti.portal.PortalLike;
import com.withertech.imm_boti.render.MyGameRenderer;
import com.withertech.imm_boti.render.PortalRenderer;
import com.withertech.imm_boti.render.SecondaryFrameBuffer;
import com.withertech.imm_boti.render.ShaderManager;
import com.withertech.imm_boti.render.context_management.PortalRendering;
import com.withertech.imm_boti.render.context_management.RenderStates;
import com.withertech.imm_boti.render.context_management.WorldRenderInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.GL13;

public class RendererDebugWithShader extends PortalRenderer {
    SecondaryFrameBuffer deferredBuffer = new SecondaryFrameBuffer();
    
    @Override
    public boolean replaceFrameBufferClearing() {
        return false;
    }
    
    @Override
    public void renderPortalInEntityRenderer(Portal portal) {
    
    }
    
    @Override
    public void onBeforeTranslucentRendering(MatrixStack matrixStack) {
    
    }
    
    @Override
    public void onAfterTranslucentRendering(MatrixStack matrixStack) {
        renderPortals(matrixStack);
    }
    
    @Override
    public void prepareRendering() {
        if (CGlobal.shaderManager == null) {
            CGlobal.shaderManager = new ShaderManager();
        }
        
        deferredBuffer.prepare();
        
        deferredBuffer.fb.setFramebufferColor(1, 0, 0, 0);
        deferredBuffer.fb.framebufferClear(Minecraft.IS_RUNNING_ON_MAC);
        
        OFGlobal.bindToShaderFrameBuffer.run();
        
    }
    
    @Override
    public void finishRendering() {
    
    }
    
    @Override
    protected void doRenderPortal(PortalLike portal, MatrixStack matrixStack) {
        if (RenderStates.getRenderedPortalNum() >= 1) {
            return;
        }
    
        PortalRendering.pushPortalLayer(portal);
        
        renderPortalContent(portal);
        //it will bind the gbuffer of rendered dimension
    
        PortalRendering.popPortalLayer();
        
        deferredBuffer.fb.bindFramebuffer(true);
        
        GlStateManager.activeTexture(GL13.GL_TEXTURE0);
        client.getFramebuffer().framebufferRender(
            deferredBuffer.fb.framebufferWidth,
            deferredBuffer.fb.framebufferHeight
        );

        OFGlobal.bindToShaderFrameBuffer.run();
    }
    
    @Override
    public void invokeWorldRendering(
        WorldRenderInfo worldRenderInfo
    ) {
        MyGameRenderer.renderWorldNew(
            worldRenderInfo,
            runnable -> {
                OFGlobal.shaderContextManager.switchContextAndRun(()->{
                    OFGlobal.bindToShaderFrameBuffer.run();
                    runnable.run();
                });
            }
        );
    }
    
    @Override
    public void onRenderCenterEnded(MatrixStack matrixStack) {
        if (PortalRendering.isRendering()) {
            return;
        }
        
        if (RenderStates.getRenderedPortalNum() == 0) {
            return;
        }
        
        GlStateManager.enableAlphaTest();
        Framebuffer mainFrameBuffer = client.getFramebuffer();
        mainFrameBuffer.bindFramebuffer(true);
        
        deferredBuffer.fb.framebufferRender(mainFrameBuffer.framebufferWidth, mainFrameBuffer.framebufferHeight);
    }
}
