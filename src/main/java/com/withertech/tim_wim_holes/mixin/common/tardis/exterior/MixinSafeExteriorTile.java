package com.withertech.tim_wim_holes.mixin.common.tardis.exterior;

import com.withertech.tim_wim_holes.ducks.IEExteriorTile;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.vector.Vector3d;
import net.tardis.mod.tileentities.exteriors.ExteriorTile;
import net.tardis.mod.tileentities.exteriors.SafeExteriorTile;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SafeExteriorTile.class)
public abstract class MixinSafeExteriorTile extends ExteriorTile implements IEExteriorTile
{
	public MixinSafeExteriorTile(TileEntityType<?> tileEntityTypeIn)
	{
		super(tileEntityTypeIn);
	}
}
