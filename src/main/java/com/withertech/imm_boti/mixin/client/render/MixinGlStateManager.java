package com.withertech.imm_boti.mixin.client.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.withertech.imm_boti.Global;
import com.withertech.imm_boti.render.context_management.RenderStates;
import com.withertech.imm_boti.render.lag_spike_fix.GlBufferCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GlStateManager.class)
public abstract class MixinGlStateManager {
    @Shadow
    public static void disableCull() {
        throw new IllegalStateException();
    }
    
    @Inject(
        method = "Lcom/mojang/blaze3d/platform/GlStateManager;enableCull()V",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void onEnableCull(CallbackInfo ci) {
        if (RenderStates.shouldForceDisableCull) {
            disableCull();
            ci.cancel();
        }
    }
    
    @Inject(
        method = "Lcom/mojang/blaze3d/platform/GlStateManager;genBuffers()I",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void onGenBuffers(CallbackInfoReturnable<Integer> cir) {
        if (Global.cacheGlBuffer) {
            cir.setReturnValue(GlBufferCache.getNewBufferId());
            cir.cancel();
        }
    }
    
    @Inject(method = "Lcom/mojang/blaze3d/platform/GlStateManager;enableFog()V", at = @At("HEAD"), cancellable = true)
    private static void onEnableFog(CallbackInfo ci) {
        if (Global.debugDisableFog) {
            ci.cancel();
        }
    }
    
}
