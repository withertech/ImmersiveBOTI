package com.withertech.tim_wim_holes.mixin.common.tardis.exterior;

import com.withertech.tim_wim_holes.ducks.IEExteriorTile;
import net.minecraft.tileentity.TileEntityType;
import net.tardis.mod.tileentities.exteriors.ApertureExteriorTile;
import net.tardis.mod.tileentities.exteriors.ExteriorTile;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ApertureExteriorTile.class)
public abstract class MixinApertureExteriorTile extends ExteriorTile implements IEExteriorTile
{

	public MixinApertureExteriorTile(TileEntityType<?> tileEntityTypeIn)
	{
		super(tileEntityTypeIn);
	}

	@Override
	public void genPortals()
	{

	}
}
