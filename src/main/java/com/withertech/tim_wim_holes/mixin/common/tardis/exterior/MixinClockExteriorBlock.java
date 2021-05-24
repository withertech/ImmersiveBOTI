package com.withertech.tim_wim_holes.mixin.common.tardis.exterior;

import net.minecraft.block.BlockState;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.tardis.mod.blocks.ExteriorBlock;
import net.tardis.mod.blocks.exteriors.ClockExteriorBlock;
import net.tardis.mod.helper.VoxelShapeUtils;
import org.spongepowered.asm.mixin.Mixin;

import javax.annotation.Nonnull;

@Mixin(ClockExteriorBlock.class)
public class MixinClockExteriorBlock extends ExteriorBlock
{
	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public VoxelShape getShape(BlockState state, @Nonnull IBlockReader worldIn, @Nonnull BlockPos pos, @Nonnull ISelectionContext context) {

		Direction dir = state.get(BlockStateProperties.HORIZONTAL_FACING);

		return VoxelShapeUtils.rotateHorizontal(VoxelShapes.or(VoxelShapes.create(0, 0, 0.5, 1, 1, 0.85), VoxelShapes.create(0, 0, 0.5, 1, 1, 0.85).withOffset(0, -1, 0)), dir);
	}
}
