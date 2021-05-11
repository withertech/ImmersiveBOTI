package com.withertech.imm_boti.optifine_compatibility.mixin_optifine;

import com.withertech.imm_boti.optifine_compatibility.OFGlobal;
import net.optifine.Config;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(value = Config.class, remap = false)
public class MixinConfig {
    @Inject(method = "isShaders", at = @At("HEAD"), cancellable = true)
    private static void onIsShaders(CallbackInfoReturnable<Boolean> cir) {
        if (OFGlobal.shaderContextManager.isContextSwitched()) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }
}
