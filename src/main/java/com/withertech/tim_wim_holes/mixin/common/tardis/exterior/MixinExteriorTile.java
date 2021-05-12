package com.withertech.tim_wim_holes.mixin.common.tardis.exterior;

import com.withertech.tim_wim_holes.events.TardisEvents;
import net.minecraft.entity.Entity;
import net.tardis.mod.enums.EnumDoorState;
import net.tardis.mod.helper.TardisHelper;
import net.tardis.mod.tileentities.exteriors.ExteriorTile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Objects;

@Mixin(ExteriorTile.class)
public class MixinExteriorTile
{

	@Inject(method = "transferEntities(Ljava/util/List;)V", at = @At("HEAD"), cancellable = true, remap = false)
	public void transferEntities(List<Entity> entityList, CallbackInfo ci)
	{
		ci.cancel();
	}

	@Inject(method = "setDoorState(Lnet/tardis/mod/enums/EnumDoorState;)V", at = @At("HEAD"), cancellable = true, remap = false)
	public void setDoorState(EnumDoorState state, CallbackInfo ci)
	{
		ExteriorTile this_ = ((ExteriorTile)(Object)this);
		TardisHelper.getConsole(Objects.requireNonNull(Objects.requireNonNull(this_.getWorld()).getServer()), this_.getInteriorDimensionKey()).ifPresent(consoleTile ->
		{

			if (state == EnumDoorState.CLOSED)
				TardisEvents.clearPortals(Objects.requireNonNull(consoleTile));
			else
			{
				TardisEvents.clearPortals(Objects.requireNonNull(consoleTile));
				TardisEvents.genPortals(Objects.requireNonNull(consoleTile));
			}
		});
	}
}
