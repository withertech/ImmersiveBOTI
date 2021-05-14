package com.withertech.tim_wim_holes.mixin.common.tardis.exterior;

import com.withertech.tim_wim_holes.ducks.IEExteriorTile;
import net.minecraft.tileentity.TileEntityType;
import net.tardis.mod.tileentities.exteriors.ExteriorTile;
import net.tardis.mod.tileentities.exteriors.JapanExteriorTile;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(JapanExteriorTile.class)
public abstract class MixinJapanExteriorTile extends ExteriorTile implements IEExteriorTile
{
	public MixinJapanExteriorTile(TileEntityType<?> tileEntityTypeIn)
	{
		super(tileEntityTypeIn);
	}

	@Override
	public void genPortals()
	{

	}
}
