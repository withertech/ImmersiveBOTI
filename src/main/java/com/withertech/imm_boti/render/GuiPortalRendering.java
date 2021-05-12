package com.withertech.imm_boti.render;

import com.withertech.imm_boti.CGlobal;
import com.withertech.imm_boti.CHelper;
import com.withertech.imm_boti.Helper;
import com.withertech.imm_boti.ducks.IECamera;
import com.withertech.imm_boti.ducks.IEMinecraftClient;
import com.withertech.imm_boti.my_util.LimitedLogger;
import com.withertech.imm_boti.render.context_management.RenderStates;
import com.withertech.imm_boti.render.context_management.WorldRenderInfo;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.shader.Framebuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import java.util.HashMap;

@OnlyIn(Dist.CLIENT)
public class GuiPortalRendering {
    private static final LimitedLogger limitedLogger = new LimitedLogger(10);
    
    @Nullable
    private static Framebuffer renderingFrameBuffer = null;
    
    @Nullable
    public static Framebuffer getRenderingFrameBuffer() {
        return renderingFrameBuffer;
    }
    
    public static boolean isRendering() {
        return getRenderingFrameBuffer() != null;
    }
    
    private static void renderWorldIntoFrameBuffer(
        WorldRenderInfo worldRenderInfo,
        Framebuffer framebuffer
    ) {
        RenderStates.projectionMatrix = null;
        
        CHelper.checkGlError();
        
        ((IECamera) RenderStates.originalCamera).resetState(
            worldRenderInfo.cameraPos, worldRenderInfo.world
        );
        
        Validate.isTrue(renderingFrameBuffer == null);
        renderingFrameBuffer = framebuffer;
        
        MyRenderHelper.restoreViewPort();
        
        Framebuffer mcFb = MyGameRenderer.client.getFramebuffer();
        
        Validate.isTrue(mcFb != framebuffer);
        
        ((IEMinecraftClient) MyGameRenderer.client).setFrameBuffer(framebuffer);
        
        framebuffer.bindFramebuffer(true);
        
        CGlobal.renderer.prepareRendering();
        
        CGlobal.renderer.invokeWorldRendering(worldRenderInfo);
        
        CGlobal.renderer.finishRendering();
        
        ((IEMinecraftClient) MyGameRenderer.client).setFrameBuffer(mcFb);
        
        mcFb.bindFramebuffer(true);
        
        renderingFrameBuffer = null;
        
        MyRenderHelper.restoreViewPort();
        
        CHelper.checkGlError();
        
        RenderStates.projectionMatrix = null;
    }
    
    private static final HashMap<Framebuffer, WorldRenderInfo> renderingTasks = new HashMap<>();
    
    public static void submitNextFrameRendering(
        WorldRenderInfo worldRenderInfo,
        Framebuffer renderTarget
    ) {
        Validate.isTrue(!renderingTasks.containsKey(renderTarget));
        
        Framebuffer mcFB = Minecraft.getInstance().getFramebuffer();
        if (renderTarget.framebufferTextureWidth != mcFB.framebufferTextureWidth || renderTarget.framebufferTextureHeight != mcFB.framebufferTextureHeight) {
            renderTarget.resize(mcFB.framebufferTextureWidth, mcFB.framebufferTextureHeight, true);
            Helper.info("Resized Framebuffer for GUI Portal Rendering");
        }
        
        renderingTasks.put(renderTarget, worldRenderInfo);
    }
    
    // Not API
    public static void onGameRenderEnd() {
        renderingTasks.forEach((frameBuffer, worldRendering) -> {
            renderWorldIntoFrameBuffer(
                worldRendering, frameBuffer
            );
        });
        renderingTasks.clear();
    }
}
