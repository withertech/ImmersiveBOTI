package com.withertech.tim_wim_holes;

import com.withertech.hiding_in_the_bushes.MyNetworkClient;
import com.withertech.hiding_in_the_bushes.O_O;
import com.withertech.tim_wim_holes.miscellaneous.GcMonitor;
import com.withertech.tim_wim_holes.my_util.MyTaskList;
import com.withertech.tim_wim_holes.optifine_compatibility.OFBuiltChunkStorageFix;
import com.withertech.tim_wim_holes.optifine_compatibility.OFGlobal;
import com.withertech.tim_wim_holes.optifine_compatibility.OFInterfaceInitializer;
import com.withertech.tim_wim_holes.render.CrossPortalEntityRenderer;
import com.withertech.tim_wim_holes.render.PortalRenderInfo;
import com.withertech.tim_wim_holes.render.PortalRenderer;
import com.withertech.tim_wim_holes.render.RendererUsingFrameBuffer;
import com.withertech.tim_wim_holes.render.RendererUsingStencil;
import com.withertech.tim_wim_holes.render.ShaderManager;
import com.withertech.tim_wim_holes.render.context_management.CloudContext;
import com.withertech.tim_wim_holes.render.context_management.PortalRendering;
import com.withertech.tim_wim_holes.render.lag_spike_fix.GlBufferCache;
import com.withertech.tim_wim_holes.teleportation.ClientTeleportationManager;
import com.withertech.tim_wim_holes.teleportation.CollisionHelper;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.TranslationTextComponent;

public class ModMainClient {
    
    public static void switchToCorrectRenderer() {
        if (PortalRendering.isRendering()) {
            //do not switch when rendering
            return;
        }
        if (OFInterface.isShaders.getAsBoolean()) {
            switch (Global.renderMode) {
                case normal:
                    switchRenderer(OFGlobal.rendererMixed);
                    break;
                case compatibility:
                    switchRenderer(OFGlobal.rendererDeferred);
                    break;
                case debug:
                    switchRenderer(OFGlobal.rendererDebugWithShader);
                    break;
                case none:
                    switchRenderer(CGlobal.rendererDummy);
                    break;
            }
        }
        else {
            switch (Global.renderMode) {
                case normal:
                    switchRenderer(CGlobal.rendererUsingStencil);
                    break;
                case compatibility:
                    switchRenderer(CGlobal.rendererUsingFrameBuffer);
                    break;
                case debug:
                    switchRenderer(CGlobal.rendererDebug);
                    break;
                case none:
                    switchRenderer(CGlobal.rendererDummy);
                    break;
            }
        }
    }
    
    private static void switchRenderer(PortalRenderer renderer) {
        if (CGlobal.renderer != renderer) {
            Helper.info("switched to renderer " + renderer.getClass());
            CGlobal.renderer = renderer;
        }
    }
    
    private static void showOptiFineWarning() {
        ModMain.clientTaskList.addTask(MyTaskList.withDelayCondition(
            () -> Minecraft.getInstance().world == null,
            MyTaskList.oneShotTask(() -> {
                Minecraft.getInstance().ingameGUI.sendChatMessage(
                    ChatType.CHAT,
                    new TranslationTextComponent("imm_ptl.optifine_warning"),
                    UUID.randomUUID()
                );
            })
        ));
    }
    
    public static void init() {
        MyNetworkClient.init();
        
        ClientWorldLoader.init();
        
        Minecraft.getInstance().execute(() -> {
            CGlobal.rendererUsingStencil = new RendererUsingStencil();
            CGlobal.rendererUsingFrameBuffer = new RendererUsingFrameBuffer();
            
            CGlobal.renderer = CGlobal.rendererUsingStencil;
            CGlobal.clientTeleportationManager = new ClientTeleportationManager();
            
            if (CGlobal.shaderManager == null) {
                CGlobal.shaderManager = new ShaderManager();
            }
        });
        
        O_O.loadConfigFabric();
        
        DubiousThings.init();
        
        CrossPortalEntityRenderer.init();
        
        GlBufferCache.init();
        
        CollisionHelper.initClient();
        
        PortalRenderInfo.init();
        
        CloudContext.init();
        
        OFInterface.isOptifinePresent = O_O.detectOptiFine();
        if (OFInterface.isOptifinePresent) {
            OFInterfaceInitializer.init();
            OFBuiltChunkStorageFix.init();
            showOptiFineWarning();
        }
        
        GcMonitor.initClient();
        
        Helper.info(OFInterface.isOptifinePresent ? "Optifine is present" : "Optifine is not present");
    }
    
}
