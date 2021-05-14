package com.withertech.tim_wim_holes.mixin.common.tardis.exterior;

import com.withertech.tim_wim_holes.ducks.IEExteriorTile;
import net.minecraft.tileentity.TileEntityType;
import net.tardis.mod.tileentities.exteriors.ClockExteriorTile;
import net.tardis.mod.tileentities.exteriors.ExteriorTile;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ClockExteriorTile.class)
public abstract class MixinClockExteriorTile extends ExteriorTile implements IEExteriorTile
{
	public MixinClockExteriorTile(TileEntityType<?> tileEntityTypeIn)
	{
		super(tileEntityTypeIn);
	}

	@Override
	public void genPortals()
	{

	}
}
