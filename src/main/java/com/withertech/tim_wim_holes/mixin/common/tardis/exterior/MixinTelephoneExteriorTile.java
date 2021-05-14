package com.withertech.tim_wim_holes.mixin.common.tardis.exterior;

import com.withertech.tim_wim_holes.ducks.IEExteriorTile;
import net.minecraft.tileentity.TileEntityType;
import net.tardis.mod.tileentities.exteriors.ExteriorTile;
import net.tardis.mod.tileentities.exteriors.TelephoneExteriorTile;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(TelephoneExteriorTile.class)
public abstract class MixinTelephoneExteriorTile extends ExteriorTile implements IEExteriorTile
{
	public MixinTelephoneExteriorTile(TileEntityType<?> tileEntityTypeIn)
	{
		super(tileEntityTypeIn);
	}

	@Override
	public void genPortals()
	{

	}
}
