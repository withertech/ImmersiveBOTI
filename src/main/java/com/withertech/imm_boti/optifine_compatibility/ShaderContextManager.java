package com.withertech.imm_boti.optifine_compatibility;

import com.withertech.imm_boti.Helper;
import com.withertech.imm_boti.render.context_management.RenderStates;
import com.withertech.imm_boti.render.context_management.WorldRenderInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;
import net.optifine.shaders.Shaders;

import java.util.HashMap;
import java.util.Map;

public class ShaderContextManager {
    private Map<RegistryKey<World>, PerDimensionContext> managedContext = new HashMap<>();
    private Map<RegistryKey<World>, PerDimensionContext> abundantContext = new HashMap<>();
    
    //null indicates that the context is not switched
    private RegistryKey<World> currentContextDimension;
    
    private PerDimensionContext recordedOriginalContext = new PerDimensionContext();
    
    private PerDimensionContext templateContext;
    
    private boolean isCleaningUp = false;
    
    public static boolean doUseDuplicateContextForCurrentDimension = false;
    
    public ShaderContextManager() {
    
    }
    
   
    
    public boolean isContextSwitched() {
        return currentContextDimension != null;
    }
    
    public void cleanup() {
        isCleaningUp = true;
        
        Helper.info("start cleaning shader context " + managedContext.keySet());
        
        managedContext.forEach(
            (dimension, context) -> {
                forceSwitchToContextAndRun(context, Shaders::uninit);
            }
        );
        managedContext.clear();
        abundantContext.forEach(
            (dimension, context) -> {
                forceSwitchToContextAndRun(context, Shaders::uninit);
            }
        );
        abundantContext.clear();
        recordedOriginalContext = new PerDimensionContext();
        currentContextDimension = null;
        
        isCleaningUp = false;
    }
    
    public PerDimensionContext getOrCreateContext(RegistryKey<World> dimension) {
        if (!doUseDuplicateContextForCurrentDimension) {
            if (isContextSwitched()) {
                if (dimension == getOriginalDimension()) {
                    return recordedOriginalContext;
                }
            }
        }
        return managedContext.computeIfAbsent(
            dimension, k -> {
                if (abundantContext.containsKey(dimension)) {
                    Helper.info("Employed abundant context" + k);
                    return abundantContext.remove(dimension);
                }
                else {
                    Helper.info("Context object created " + k);
                    return createContextByTemplate();
                }
            }
        );
        
    }
    
    public void switchContextAndRun(Runnable func) {
        RegistryKey<World> oldContextDimension = this.currentContextDimension;
        ClientWorld currentClientWorld = Minecraft.getInstance().world;
        RegistryKey<World> dimensionToSwitchTo = currentClientWorld.getDimensionKey();
        
        if (currentContextDimension == null) {
            //currently the context was not switched
            currentContextDimension = getOriginalDimension();
            OFGlobal.copyContextToObject.accept(recordedOriginalContext);
        }
        
        if (currentContextDimension == dimensionToSwitchTo) {
            check(dimensionToSwitchTo);
            func.run();
        }
        else {
            currentContextDimension = dimensionToSwitchTo;
            PerDimensionContext newContext = getOrCreateContext(dimensionToSwitchTo);
            
            if (newContext.currentWorld != null) {
                checkState(currentClientWorld, dimensionToSwitchTo, newContext);
            }
            
            forceSwitchToContextAndRun(newContext, func);
        }
        
        currentContextDimension = oldContextDimension;
    }
    
    private void check(RegistryKey<World> dimensionToSwitchTo) {
        World shadersCurrentWorld = OFGlobal.getCurrentWorld.get();
        if (shadersCurrentWorld != null) {
            RegistryKey<World> shaderCurrentDimension = shadersCurrentWorld.getDimensionKey();
            if (shaderCurrentDimension != dimensionToSwitchTo) {
                Helper.err(
                    "Shader Context Abnormal. Shader: " +
                        shaderCurrentDimension +
                        "Main: " +
                        dimensionToSwitchTo
                );
            }
        }
    }
    
    private static void checkState(
        ClientWorld currentClientWorld,
        RegistryKey<World> dimensionToSwitchTo,
        PerDimensionContext newContext
    ) {
        RegistryKey<World> newContextDimension = newContext.currentWorld.getDimensionKey();
        
        if (newContextDimension != dimensionToSwitchTo) {
            Helper.err(
                "Shader Context Abnormal. Shader: " +
                    newContextDimension +
                    "Main: " +
                    dimensionToSwitchTo
            );
            newContext.currentWorld = currentClientWorld;
            Helper.info("Force corrected");
            forceCorrectedNum++;
            if (forceCorrectedNum > 100) {
                throw new IllegalStateException();
            }
        }
    }
    
    public static int forceCorrectedNum = 0;
    
    private void forceSwitchToContextAndRun(
        PerDimensionContext contextToSwitchTo,
        Runnable func
    ) {
        PerDimensionContext originalContext = new PerDimensionContext();
        OFGlobal.copyContextToObject.accept(originalContext);
        
        OFGlobal.copyContextFromObject.accept(contextToSwitchTo);
        
        try {
            func.run();
        }
        finally {
            OFGlobal.copyContextToObject.accept(contextToSwitchTo);
            
            OFGlobal.copyContextFromObject.accept(originalContext);
        }
    }
    
    public boolean isCurrentDimensionRendered() {
        if (currentContextDimension == null) {
            return false;
        }
        return RenderStates.isDimensionRendered(currentContextDimension);
    }
    
    public void onPlayerTraveled(RegistryKey<World> from, RegistryKey<World> to) {
        if (managedContext.containsKey(from)) {
            assert !abundantContext.containsKey(from);
            abundantContext.put(from, managedContext.remove(from));
        }
        
        PerDimensionContext oldContext = new PerDimensionContext();
        OFGlobal.copyContextToObject.accept(oldContext);
        managedContext.put(from, oldContext);
        
        PerDimensionContext newContext = getOrCreateContext(to);
        OFGlobal.copyContextFromObject.accept(newContext);
        managedContext.remove(to);
        
        check(to);
    }
    
    public void onShaderUninit() {
        if (!isCleaningUp) {
            cleanup();
        }
    }
    
    //we need to record the template when shader is loaded but not initialized
    public void updateTemplateContext() {
        assert !isContextSwitched();
        
        Shaders.uninit();
        cleanup();
        
        templateContext = new PerDimensionContext();
        
        OFGlobal.copyContextToObject.accept(templateContext);
        
        Helper.info("shader context template updated");
    }
    
    private PerDimensionContext createContextByTemplate() {
        assert templateContext != null;
        PerDimensionContext newContext = new PerDimensionContext();
        forceSwitchToContextAndRun(templateContext, () -> {
            OFGlobal.copyContextToObject.accept(newContext);
        });
        newContext.doSpecialInit();
        return newContext;
    }
    
    public static RegistryKey<World> getOriginalDimension() {
        if (WorldRenderInfo.isRendering()) {
            return RenderStates.originalPlayerDimension;
        }
        else {
            return Minecraft.getInstance().player.world.getDimensionKey();
        }
    }
}
