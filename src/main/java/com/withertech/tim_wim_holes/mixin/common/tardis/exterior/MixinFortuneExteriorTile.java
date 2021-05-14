package com.withertech.tim_wim_holes.mixin.common.tardis.exterior;

import com.withertech.tim_wim_holes.ducks.IEExteriorTile;
import net.minecraft.tileentity.TileEntityType;
import net.tardis.mod.tileentities.exteriors.ExteriorTile;
import net.tardis.mod.tileentities.exteriors.FortuneExteriorTile;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(FortuneExteriorTile.class)
public abstract class MixinFortuneExteriorTile extends ExteriorTile implements IEExteriorTile
{
	public MixinFortuneExteriorTile(TileEntityType<?> tileEntityTypeIn)
	{
		super(tileEntityTypeIn);
	}

	@Override
	public void genPortals()
	{

	}
}
