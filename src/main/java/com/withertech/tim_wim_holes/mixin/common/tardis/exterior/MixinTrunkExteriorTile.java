package com.withertech.tim_wim_holes.mixin.common.tardis.exterior;

import com.withertech.tim_wim_holes.ducks.IEExteriorTile;
import net.minecraft.tileentity.TileEntityType;
import net.tardis.mod.tileentities.exteriors.ExteriorTile;
import net.tardis.mod.tileentities.exteriors.TrunkExteriorTile;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(TrunkExteriorTile.class)
public abstract class MixinTrunkExteriorTile extends ExteriorTile implements IEExteriorTile
{
	public MixinTrunkExteriorTile(TileEntityType<?> tileEntityTypeIn)
	{
		super(tileEntityTypeIn);
	}

	@Override
	public void genPortals()
	{

	}
}
