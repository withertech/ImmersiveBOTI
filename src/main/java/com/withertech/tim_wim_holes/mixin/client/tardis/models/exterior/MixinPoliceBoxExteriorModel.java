package com.withertech.tim_wim_holes.mixin.client.tardis.models.exterior;

import net.minecraft.client.renderer.model.ModelRenderer;
import net.tardis.mod.client.models.exteriors.ExteriorModel;
import net.tardis.mod.client.models.exteriors.PoliceBoxExteriorModel;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PoliceBoxExteriorModel.class)
public abstract class MixinPoliceBoxExteriorModel extends ExteriorModel
{
	@Shadow @Final private ModelRenderer boti;

	@Inject(method = "<init>()V", at = @At("TAIL"))
	public void init(CallbackInfo ci)
	{
		boti.showModel = false;
	}
}
