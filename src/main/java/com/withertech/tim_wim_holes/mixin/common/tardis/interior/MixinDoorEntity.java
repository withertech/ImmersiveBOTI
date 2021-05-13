package com.withertech.tim_wim_holes.mixin.common.tardis.interior;

import com.withertech.tim_wim_holes.events.TardisEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.concurrent.TickDelayedTask;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.tardis.mod.entity.DoorEntity;
import net.tardis.mod.enums.EnumDoorState;
import net.tardis.mod.helper.TardisHelper;
import net.tardis.mod.tileentities.ConsoleTile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

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
		if(!this.getConsole().isInFlight() && !this.getConsole().getInteriorManager().isInteriorStillRegenerating())
		{
			ci.cancel();
		}
	}

	@Inject(method = "setOpenState(Lnet/tardis/mod/enums/EnumDoorState;)V", at = @At("HEAD"), cancellable = true, remap = false)
	public void setOpenState(EnumDoorState open, CallbackInfo ci)
	{
		TardisHelper.getConsole(Objects.requireNonNull(Objects.requireNonNull(this.world).getServer()), this.world).ifPresent(consoleTile ->
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

	@Shadow @Nullable public abstract ConsoleTile getConsole();
}
