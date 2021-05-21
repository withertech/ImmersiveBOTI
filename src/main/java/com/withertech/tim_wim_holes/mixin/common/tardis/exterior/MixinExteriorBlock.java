package com.withertech.tim_wim_holes.mixin.common.tardis.exterior;

import com.withertech.tim_wim_holes.Helper;
import com.withertech.tim_wim_holes.McHelper;
import net.minecraft.block.BlockState;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.tardis.mod.blocks.ExteriorBlock;
import net.tardis.mod.blocks.TileBlock;
import net.tardis.mod.enums.EnumDoorState;
import net.tardis.mod.helper.VoxelShapeUtils;
import net.tardis.mod.tileentities.exteriors.TrunkExteriorTile;
import org.spongepowered.asm.mixin.Mixin;

import javax.annotation.Nonnull;

@Mixin(ExteriorBlock.class)
public abstract class MixinExteriorBlock extends TileBlock
{
	public MixinExteriorBlock(Properties prop)
	{
		super(prop);
	}

//	@SuppressWarnings("deprecation")
//	@Nonnull
//	@Override
//	public VoxelShape getCollisionShape(@Nonnull BlockState state, @Nonnull IBlockReader worldIn, @Nonnull BlockPos pos, @Nonnull ISelectionContext context)
//	{
//		return VoxelShapes.empty();
//	}

	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, @Nonnull BlockPos pos, @Nonnull ISelectionContext context)
	{
		Direction dir = state.get(BlockStateProperties.HORIZONTAL_FACING);
		TileEntity te = worldIn.getTileEntity(pos);
		if (te instanceof TrunkExteriorTile)
		{
			TrunkExteriorTile trunk = (TrunkExteriorTile) te;
			if (trunk.getOpen() != EnumDoorState.CLOSED)
				return VoxelShapeUtils.rotateHorizontal(VoxelShapes.create(0, -1, 0.5, 1, 1, 1), dir);
			else
				return VoxelShapes.or(VoxelShapes.fullCube(), VoxelShapes.fullCube().withOffset(0, -1, 0));
		}
		else
			return VoxelShapes.or(VoxelShapes.fullCube(), VoxelShapes.fullCube().withOffset(0, -1, 0));
	}
}
