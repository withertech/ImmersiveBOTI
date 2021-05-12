package com.withertech.tim_wim_holes;

import com.withertech.tim_wim_holes.render.PortalRenderer;
import com.withertech.tim_wim_holes.render.RendererDebug;
import com.withertech.tim_wim_holes.render.RendererDummy;
import com.withertech.tim_wim_holes.render.RendererUsingFrameBuffer;
import com.withertech.tim_wim_holes.render.RendererUsingStencil;
import com.withertech.tim_wim_holes.render.ShaderManager;
import com.withertech.tim_wim_holes.teleportation.ClientTeleportationManager;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.DimensionType;

public class CGlobal {
    
    public static PortalRenderer renderer;
    public static RendererUsingStencil rendererUsingStencil;
    public static RendererUsingFrameBuffer rendererUsingFrameBuffer;
    public static RendererDummy rendererDummy = new RendererDummy();
    public static RendererDebug rendererDebug = new RendererDebug();
    
    public static ClientTeleportationManager clientTeleportationManager;
    public static ShaderManager shaderManager;
    
    public static int maxIdleChunkRendererNum = 500;
    
    public static Map<DimensionType, Integer> renderInfoNumMap = new ConcurrentHashMap<>();
    
    public static boolean doUseAdvancedFrustumCulling = true;
    public static boolean useHackedChunkRenderDispatcher = true;
    public static boolean isClientRemoteTickingEnabled = true;
    public static boolean useFrontClipping = true;
    public static boolean doDisableAlphaTestWhenRenderingFrameBuffer = true;
    public static boolean smoothChunkUnload = true;
    public static boolean earlyClientLightUpdate = true;
    public static boolean useSuperAdvancedFrustumCulling = true;
    public static boolean earlyFrustumCullingPortal = true;
}
