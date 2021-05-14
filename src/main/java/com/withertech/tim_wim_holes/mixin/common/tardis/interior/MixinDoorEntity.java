package com.withertech.tim_wim_holes.mixin.common.tardis.interior;

import com.withertech.tim_wim_holes.ducks.IEExteriorTile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;
import net.tardis.mod.entity.DoorEntity;
import net.tardis.mod.enums.EnumDoorState;
import net.tardis.mod.tileentities.ConsoleTile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.List;

@Mixin(DoorEntity.class)
public abstract class MixinDoorEntity extends Entity
{

	public MixinDoorEntity(EntityType<?> entityTypeIn, World worldIn)
	{
		super(entityTypeIn, worldIn);
	}

	@Inject(method = "teleportEntity(Ljava/util/List;)V", at = @At("HEAD"), cancellable = true, remap = false)
	public void teleportEntity(List<Entity> entity, CallbackInfo ci)
	{
		if (!this.getConsole().isInFlight() && !this.getConsole().getInteriorManager().isInteriorStillRegenerating())
		{
			ci.cancel();
		}
	}

	@Inject(method = "setOpenState(Lnet/tardis/mod/enums/EnumDoorState;)V", at = @At("HEAD"), cancellable = true, remap = false)
	public void setOpenState(EnumDoorState open, CallbackInfo ci)
	{
		ConsoleTile consoleTile = getConsole();
		assert consoleTile != null;
		consoleTile.getOrFindExteriorTile().ifPresent(exteriorTile ->
		{
			if (open == EnumDoorState.CLOSED)
				((IEExteriorTile) exteriorTile).clearPortals();
			else
			{
				((IEExteriorTile) exteriorTile).clearPortals();
				((IEExteriorTile) exteriorTile).genPortals();
			}
		});
	}

	@Shadow
	@Nullable
	public abstract ConsoleTile getConsole();
}
