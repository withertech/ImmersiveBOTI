package com.withertech.tim_wim_holes.mixin.common.tardis.exterior;

import com.withertech.tim_wim_holes.ducks.IEExteriorTile;
import net.minecraft.tileentity.TileEntityType;
import net.tardis.mod.tileentities.exteriors.DisguiseExteriorTile;
import net.tardis.mod.tileentities.exteriors.ExteriorTile;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(DisguiseExteriorTile.class)
public abstract class MixinDisguiseExteriorTile extends ExteriorTile implements IEExteriorTile
{
	public MixinDisguiseExteriorTile(TileEntityType<?> tileEntityTypeIn)
	{
		super(tileEntityTypeIn);
	}

	@Override
	public void genPortals()
	{

	}
}
