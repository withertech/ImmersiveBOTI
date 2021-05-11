package com.withertech.imm_boti.mixin.client.render;

import com.withertech.imm_boti.render.context_management.PortalRendering;
import net.minecraft.client.renderer.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = FogRenderer.class, priority = 900)
public class MixinBackgroundRenderer_R {
    // avoid thick fog when rendering the box view end portal
    @ModifyArg(
        method = "applyFog",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/systems/RenderSystem;fogStart(F)V"
        ),
        require = 0// avoid crashing with optifine
    )
    private static float modifyFogStart(float fogStart) {
        return multiplyByPortalScale(fogStart);
    }
    
    @ModifyArg(
        method = "applyFog",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/systems/RenderSystem;fogEnd(F)V"
        ),
        require = 0
    )
    private static float modifyFogEnd(float fogStart) {
        return multiplyByPortalScale(fogStart);
    }
    
    private static float multiplyByPortalScale(float value) {
        if (PortalRendering.isRendering()) {
            double scaling = PortalRendering.getRenderingPortal().getScale();
            if (scaling > 10) {
                return ((float) (value * scaling));
            }
        }
        return value;
    }
}
