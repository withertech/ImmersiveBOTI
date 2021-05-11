package com.withertech.imm_boti.mixin.client.block_manipulation;

import com.withertech.imm_boti.block_manipulation.BlockManipulationClient;
import com.withertech.imm_boti.render.context_management.PortalRendering;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class MixinGameRenderer_B {
    
    //do not update target when rendering portal
    @Inject(method = "Lnet/minecraft/client/renderer/GameRenderer;getMouseOver(F)V", at = @At("HEAD"), cancellable = true)
    private void onUpdateTargetedEntity(float tickDelta, CallbackInfo ci) {
        if (Minecraft.getInstance().world != null) {
            if (PortalRendering.isRendering()) {
                ci.cancel();
            }
        }
    }
    
    @Inject(method = "Lnet/minecraft/client/renderer/GameRenderer;getMouseOver(F)V", at = @At("RETURN"))
    private void onUpdateTargetedEntityFinish(float tickDelta, CallbackInfo ci) {
        if (Minecraft.getInstance().world != null) {
            BlockManipulationClient.updatePointedBlock(tickDelta);
        }
    }
}
