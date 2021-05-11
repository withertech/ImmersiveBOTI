package com.withertech.imm_boti.optifine_compatibility.mixin_optifine;

import com.withertech.imm_boti.render.context_management.PortalRendering;
import net.minecraft.client.renderer.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "net.optifine.DynamicLights", remap = false)
public class MixinDynamicLights {
    //avoid updating dynamic light when rendering portal
    @Inject(method = "update", at = @At("HEAD"), cancellable = true)
    private static void onUpdate(WorldRenderer renderGlobal, CallbackInfo ci) {
        if (PortalRendering.isRendering()) {
            ci.cancel();
        }
    }
    
//    @ModifyConstant(
//        method = "Lnet/optifine/DynamicLights;getLightLevel(Lnet/minecraft/util/math/BlockPos;)D",
//        constant = @Constant(doubleValue = 56.25D)
//    )
//    private static double modifyMaxDist(double original) {
//        return 256.0d;
//    }
//
//    @ModifyConstant(
//        method = "Lnet/optifine/DynamicLights;getLightLevel(Lnet/minecraft/util/math/BlockPos;)D",
//        constant = @Constant(doubleValue = 7.5D)
//    )
//    private static double modifyRatio(double original) {
//        return 16.0d;
//    }
}
