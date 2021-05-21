package com.withertech.tim_wim_holes.mixin.common.tardis.exterior;

import com.withertech.tim_wim_holes.Helper;
import net.minecraft.block.BlockState;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.tardis.mod.blocks.CubeBlock;
import net.tardis.mod.blocks.ExteriorBlock;
import net.tardis.mod.blocks.TardisBottomBlock;
import net.tardis.mod.helper.VoxelShapeUtils;
import net.tardis.mod.tileentities.exteriors.TrunkExteriorTile;
import org.spongepowered.asm.mixin.Mixin;

import javax.annotation.Nonnull;

@Mixin(TardisBottomBlock.class)
public abstract class MixinTardisBottomBlock extends CubeBlock
{

	public MixinTardisBottomBlock(Properties prop)
	{
		super(prop);
	}

	@Nonnull
	@Override
	public VoxelShape getShape(@Nonnull BlockState state, IBlockReader worldIn, BlockPos pos, @Nonnull ISelectionContext context)
	{
		return VoxelShapes.empty();
	}
}
