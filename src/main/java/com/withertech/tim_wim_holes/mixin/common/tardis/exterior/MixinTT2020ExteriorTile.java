package com.withertech.tim_wim_holes.mixin.common.tardis.exterior;

import com.withertech.tim_wim_holes.ducks.IEExteriorTile;
import net.minecraft.tileentity.TileEntityType;
import net.tardis.mod.tileentities.exteriors.ExteriorTile;
import net.tardis.mod.tileentities.exteriors.TT2020ExteriorTile;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(TT2020ExteriorTile.class)
public abstract class MixinTT2020ExteriorTile extends ExteriorTile implements IEExteriorTile
{
	public MixinTT2020ExteriorTile(TileEntityType<?> tileEntityTypeIn)
	{
		super(tileEntityTypeIn);
	}

	@Override
	public void genPortals()
	{

	}
}
