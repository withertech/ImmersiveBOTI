package com.withertech.imm_boti.mixin.common.tardis.interior;

import com.withertech.hiding_in_the_bushes.ModMainForge;
import com.withertech.imm_boti.McHelper;
import com.withertech.imm_boti.events.TardisEvents;
import com.withertech.imm_boti.portal.IHasPortal;
import com.withertech.imm_boti.portal.Portal;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.tardis.mod.Tardis;
import net.tardis.mod.entity.DoorEntity;
import net.tardis.mod.enums.EnumDoorState;
import net.tardis.mod.helper.TardisHelper;
import net.tardis.mod.tileentities.exteriors.ModernPoliceBoxExteriorTile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Objects;

@Mixin(DoorEntity.class)
public class MixinDoorEntity
{

	@Inject(method = "teleportEntity(Ljava/util/List;)V", at = @At("HEAD"), cancellable = true, remap = false)
	public void teleportEntity(List<Entity> entity, CallbackInfo ci)
	{
		ci.cancel();
	}

	@Inject(method = "teleportEntities(Ljava/util/List;)V", at = @At("HEAD"), cancellable = true, remap = false)
	public void teleportEntities(List<Entity> entity, CallbackInfo ci)
	{
		ci.cancel();
	}

	@Inject(method = "setOpenState(Lnet/tardis/mod/enums/EnumDoorState;)V", at = @At("HEAD"), cancellable = true, remap = false)
	public void setOpenState(EnumDoorState open, CallbackInfo ci)
	{
		DoorEntity this_ = ((DoorEntity)(Object)this);

		TardisHelper.getConsole(Objects.requireNonNull(Objects.requireNonNull(this_.world).getServer()), this_.world).ifPresent(consoleTile ->
		{
			if (open == EnumDoorState.CLOSED)
				TardisEvents.clearPortals(consoleTile);
			else
			{
				TardisEvents.clearPortals(consoleTile);
				TardisEvents.genPortals(consoleTile);
			}
		});
	}
}
