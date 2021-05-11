package com.withertech.imm_boti.mixin.common;

import com.withertech.imm_boti.McHelper;
import com.withertech.imm_boti.ducks.IERayTraceContext;
import com.withertech.imm_boti.portal.Portal;
import com.withertech.imm_boti.portal.PortalPlaceholderBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RayTraceContext.class)
public abstract class MixinRaycastContext implements IERayTraceContext {
    @SuppressWarnings("ShadowModifiers")
    @Shadow
    private Vector3d startVec;
    
    @SuppressWarnings("ShadowModifiers")
    @Shadow
    private Vector3d endVec;
    
    @Shadow
    @Final
    private RayTraceContext.BlockMode blockMode;
    
    @Shadow
    @Final
    private ISelectionContext context;
    
    @Override
    public IERayTraceContext setStart(Vector3d newStart) {
        startVec = newStart;
        return this;
    }
    
    @Override
    public IERayTraceContext setEnd(Vector3d newEnd) {
        endVec = newEnd;
        return this;
    }
    
    // portal placeholder does not have outline if colliding with portal
    // placeholder blocks entity view
    @Inject(
        at = @At("HEAD"),
        method = "Lnet/minecraft/util/math/RayTraceContext;getBlockShape(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/math/shapes/VoxelShape;",
        cancellable = true
    )
    private void onGetBlockShape(
        BlockState blockState,
        IBlockReader blockView,
        BlockPos blockPos,
        CallbackInfoReturnable<VoxelShape> cir
    ) {
        if (blockState.getBlock() == PortalPlaceholderBlock.instance) {
            if (blockMode == RayTraceContext.BlockMode.OUTLINE) {
                if (blockView instanceof World) {
                    boolean isIntersectingWithPortal = McHelper.getEntitiesRegardingLargeEntities(
                        (World) blockView, new AxisAlignedBB(blockPos),
                        10, Portal.class, e -> true
                    ).isEmpty();
                    if (!isIntersectingWithPortal) {
                        cir.setReturnValue(VoxelShapes.empty());
                    }
                }
            }
            else if (blockMode == RayTraceContext.BlockMode.COLLIDER) {
                cir.setReturnValue(PortalPlaceholderBlock.instance.getShape(
                    blockState, blockView, blockPos, context
                ));
            }
        }
    }
}
