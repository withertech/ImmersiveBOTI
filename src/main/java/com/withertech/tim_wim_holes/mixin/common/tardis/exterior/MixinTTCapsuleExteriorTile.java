package com.withertech.tim_wim_holes.mixin.common.tardis.exterior;

import com.withertech.tim_wim_holes.ducks.IEExteriorTile;
import net.minecraft.tileentity.TileEntityType;
import net.tardis.mod.tileentities.exteriors.ExteriorTile;
import net.tardis.mod.tileentities.exteriors.TTCapsuleExteriorTile;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(TTCapsuleExteriorTile.class)
public abstract class MixinTTCapsuleExteriorTile extends ExteriorTile implements IEExteriorTile
{
	public MixinTTCapsuleExteriorTile(TileEntityType<?> tileEntityTypeIn)
	{
		super(tileEntityTypeIn);
	}

	@Override
	public void genPortals()
	{

	}
}
