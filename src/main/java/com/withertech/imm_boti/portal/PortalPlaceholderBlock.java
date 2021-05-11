package com.withertech.imm_boti.portal;

import com.withertech.imm_boti.McHelper;
import com.withertech.imm_boti.portal.nether_portal.BreakablePortalEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import java.util.Random;

public class PortalPlaceholderBlock extends Block {
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;
    public static final VoxelShape X_AABB = Block.makeCuboidShape(
        6.0D,
        0.0D,
        0.0D,
        10.0D,
        16.0D,
        16.0D
    );
    public static final VoxelShape Y_AABB = Block.makeCuboidShape(
        0.0D,
        6.0D,
        0.0D,
        16.0D,
        10.0D,
        16.0D
    );
    public static final VoxelShape Z_AABB = Block.makeCuboidShape(
        0.0D,
        0.0D,
        6.0D,
        16.0D,
        16.0D,
        10.0D
    );
    
    public static PortalPlaceholderBlock instance;
    
    public PortalPlaceholderBlock(Properties properties) {
        super(properties);
        this.setDefaultState(
            (BlockState) ((BlockState) this.getStateContainer().getBaseState()).with(
                AXIS, Direction.Axis.X
            )
        );
    }
    
    @Override
    public VoxelShape getShape(
        BlockState state, IBlockReader world, BlockPos blockPos, ISelectionContext shapeContext
    ) {
        switch ((Direction.Axis) state.get(AXIS)) {
            case Z:
                return Z_AABB;
            case Y:
                return Y_AABB;
            case X:
            default:
                return X_AABB;
        }
    }
    
    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }
    
    @Override
    public BlockState updatePostPlacement(
        BlockState thisState,
        Direction direction,
        BlockState neighborState,
        IWorld world,
        BlockPos blockPos,
        BlockPos neighborPos
    ) {
        if (!world.isRemote()) {
            Direction.Axis axis = thisState.get(AXIS);
            if (direction.getAxis() != axis) {
                McHelper.findEntitiesRough(
                    BreakablePortalEntity.class,
                    ((World) world),
                    Vector3d.copy(blockPos),
                    2,
                    e -> true
                ).forEach(
                    portal -> {
                        ((BreakablePortalEntity) portal).notifyPlaceholderUpdate();
                    }
                );
            }
        }
        
        return super.updatePostPlacement(
            thisState,
            direction,
            neighborState,
            world,
            blockPos,
            neighborPos
        );
    }
    
    //copied from PortalBlock
    @Override
    public void animateTick(
        BlockState blockState_1,
        World world_1,
        BlockPos blockPos_1,
        Random random_1
    ) {
        //nothing
    }
    
    
    //---------These are copied from BlockBarrier
    @Override
    public boolean propagatesSkylightDown(
        BlockState blockState_1,
        IBlockReader blockView_1,
        BlockPos blockPos_1
    ) {
        return true;
    }
    
    @Override
    public BlockRenderType getRenderType(BlockState blockState_1) {
        return BlockRenderType.INVISIBLE;
    }
    
    @OnlyIn(Dist.CLIENT)
    @Override
    public float getAmbientOcclusionLightValue(
        BlockState blockState_1,
        IBlockReader blockView_1,
        BlockPos blockPos_1
    ) {
        return 1.0F;
    }
    
}
