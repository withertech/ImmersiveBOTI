package com.withertech.tim_wim_holes.mixin.client.tardis.renderer;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.tardis.mod.client.renderers.boti.BOTIRenderer;
import net.tardis.mod.client.renderers.boti.PortalInfo;
import net.tardis.mod.config.TConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BOTIRenderer.class)
public class MixinBOTIRenderer
{
	@Inject(method = "renderBOTI(Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/tardis/mod/client/renderers/boti/PortalInfo;)V", at = @At("HEAD"), cancellable = true, remap = false)
	private static void renderBOTI(MatrixStack ms, PortalInfo info, CallbackInfo ci) {
		if (!TConfig.CLIENT.enableBoti.get()) ci.cancel();
	}
}
