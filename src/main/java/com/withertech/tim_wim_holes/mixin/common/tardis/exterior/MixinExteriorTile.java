package com.withertech.tim_wim_holes.mixin.common.tardis.exterior;

import com.withertech.tim_wim_holes.Helper;
import com.withertech.tim_wim_holes.McHelper;
import com.withertech.tim_wim_holes.ducks.IEExteriorTile;
import com.withertech.tim_wim_holes.portal.global_portals.GlobalPortalStorage;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
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
import java.util.stream.Collectors;

@Mixin(ExteriorTile.class)
public abstract class MixinExteriorTile extends TileEntity implements IEExteriorTile
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
		if (state == EnumDoorState.CLOSED)
			this.clearPortals();
		else
		{
			this.clearPortals();
			this.genPortals();
		}
	}

	@Shadow
	public abstract RegistryKey<World> getInteriorDimensionKey();

	@Override
	public abstract void genPortals();

	@Override
	public void clearPortals()
	{
		TardisHelper.getConsole(Objects.requireNonNull(Objects.requireNonNull(this.getWorld()).getServer()), this.getInteriorDimensionKey()).ifPresent(console ->
		{
			console.getDoor().ifPresent(doorEntity ->
					McHelper.getNearbyPortals(doorEntity, 3).collect(Collectors.toList()).forEach(portal ->
					{
						Helper.debug("Interior Portal Destroyed: " + portal);
						GlobalPortalStorage.get((ServerWorld) portal.world).removePortal(portal);
					})
			);
			console.getOrFindExteriorTile().ifPresent(exteriorTile ->
					McHelper.getNearbyPortals(exteriorTile.getWorld(), Vector3d.copyCentered(exteriorTile.getPos()), 3).collect(Collectors.toList()).forEach(portal ->
					{
						Helper.debug("Exterior Portal Destroyed: " + portal);
						GlobalPortalStorage.get((ServerWorld) portal.world).removePortal(portal);
					})
			);
		});
	}
}
