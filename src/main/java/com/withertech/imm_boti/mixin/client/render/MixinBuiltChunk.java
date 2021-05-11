package com.withertech.imm_boti.mixin.client.render;

import com.withertech.imm_boti.ducks.IEBuiltChunk;
import com.withertech.imm_boti.render.context_management.PortalRendering;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkRenderDispatcher.ChunkRender.class)
public abstract class MixinBuiltChunk implements IEBuiltChunk {
    
    private long portal_mark;
    
    @Inject(
        method = "Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher$ChunkRender;needsImmediateUpdate()Z",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onNeedsImportantRebuild(CallbackInfoReturnable<Boolean> cir) {
        if (PortalRendering.isRendering()) {
            cir.setReturnValue(false);
        }
    }
    
    @Shadow
    protected abstract void stopCompileTask();
    
    @Override
    public void fullyReset() {
        stopCompileTask();
    }
    
    @Override
    public long getMark() {
        return portal_mark;
    }
    
    @Override
    public void setMark(long arg) {
        portal_mark = arg;
    }
}
