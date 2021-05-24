package com.withertech.tim_wim_holes.mixin.client.tardis.models.interior;

import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.tardis.mod.client.models.interiordoors.IInteriorDoorRenderer;
import net.tardis.mod.client.models.interiordoors.PoliceBoxInteriorModel;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PoliceBoxInteriorModel.class)
public abstract class MixinPoliceBoxInteriorModel extends EntityModel<Entity> implements IInteriorDoorRenderer
{
	@Shadow
	@Final
	private ModelRenderer boti;

	@Inject(method = "<init>()V", at = @At("TAIL"))
	public void init(CallbackInfo ci)
	{
		boti.showModel = false;
	}
}
