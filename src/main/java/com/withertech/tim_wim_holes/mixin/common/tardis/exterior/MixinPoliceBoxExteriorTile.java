package com.withertech.tim_wim_holes.mixin.common.tardis.exterior;

import com.withertech.tim_wim_holes.ducks.IEExteriorTile;
import net.minecraft.tileentity.TileEntityType;
import net.tardis.mod.tileentities.exteriors.ExteriorTile;
import net.tardis.mod.tileentities.exteriors.PoliceBoxExteriorTile;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PoliceBoxExteriorTile.class)
public abstract class MixinPoliceBoxExteriorTile extends ExteriorTile implements IEExteriorTile
{
	public MixinPoliceBoxExteriorTile(TileEntityType<?> tileEntityTypeIn)
	{
		super(tileEntityTypeIn);
	}

	@Override
	public void genPortals()
	{

	}
}
