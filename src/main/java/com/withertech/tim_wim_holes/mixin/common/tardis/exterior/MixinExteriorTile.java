package com.withertech.tim_wim_holes.mixin.common.tardis.exterior;

import com.withertech.tim_wim_holes.events.TardisEvents;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;
import net.tardis.mod.enums.EnumDoorState;
import net.tardis.mod.helper.TardisHelper;
import net.tardis.mod.tileentities.exteriors.ExteriorTile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Objects;

@Mixin(ExteriorTile.class)
public abstract class MixinExteriorTile extends TileEntity
{

	public MixinExteriorTile(TileEntityType<?> tileEntityTypeIn)
	{
		super(tileEntityTypeIn);
	}

	@Inject(method = "transferEntities(Ljava/util/List;)V", at = @At("HEAD"), cancellable = true, remap = false)
	public void transferEntities(List<Entity> entityList, CallbackInfo ci)
	{
		ci.cancel();
	}

	@Inject(method = "setDoorState(Lnet/tardis/mod/enums/EnumDoorState;)V", at = @At("HEAD"), cancellable = true, remap = false)
	public void setDoorState(EnumDoorState state, CallbackInfo ci)
	{
		TardisHelper.getConsole(Objects.requireNonNull(Objects.requireNonNull(this.getWorld()).getServer()), this.getInteriorDimensionKey()).ifPresent(consoleTile ->
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

	@Shadow public abstract RegistryKey<World> getInteriorDimensionKey();

}
