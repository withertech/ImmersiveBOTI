package com.withertech.tim_wim_holes.mixin.common.tardis.exterior;

import com.withertech.tim_wim_holes.ducks.IEExteriorTile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;
import net.tardis.mod.entity.TardisEntity;
import net.tardis.mod.tileentities.ConsoleTile;
import net.tardis.mod.tileentities.exteriors.ExteriorTile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(value = TardisEntity.class, remap = false)
public abstract class MixinTardisEntity extends Entity
{
	@Shadow private boolean hasLanded;
	@Shadow private ExteriorTile tile;

	@Shadow public abstract ConsoleTile getConsole();

	public MixinTardisEntity(EntityType<?> entityTypeIn, World worldIn)
	{
		super(entityTypeIn, worldIn);
	}

	@Inject(method = "tick()V", at = @At("HEAD"))
	public void tick(CallbackInfo ci)
	{
		if(!this.world.isRemote() && !this.hasLanded)
		{
			IEExteriorTile tile = (IEExteriorTile)this.getExteriorTile();
			assert tile != null;
			tile.updateFallingPortal(this.getPositionVec(), this.getConsole());
		}
	}

	@Inject(method = "land()V", at = @At("TAIL"))
	public void land(CallbackInfo ci)
	{
		if(!this.world.isRemote())
		{
			IEExteriorTile tile = (IEExteriorTile)this.getExteriorTile();
			assert tile != null;
			tile.updatePortals();
		}
	}

	@Shadow @Nullable public abstract ExteriorTile getExteriorTile();
}
