package com.withertech.imm_ptl_peripheral.mixin.common.portal_generation;

import com.withertech.hiding_in_the_bushes.O_O;
import com.withertech.imm_ptl_peripheral.portal_generation.IntrinsicPortalGeneration;
import com.withertech.tim_wim_holes.Global;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.PortalSize;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

@Mixin(AbstractFireBlock.class)
public class MixinAbstractFireBlock {
    @Redirect(
        method = "Lnet/minecraft/block/AbstractFireBlock;onBlockAdded(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Z)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/block/PortalSize;func_242964_a(Lnet/minecraft/world/IWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/Direction$Axis;)Ljava/util/Optional;"
        )
    )
    Optional<PortalSize> redirectCreateAreaHelper(IWorld worldAccess, BlockPos blockPos, Direction.Axis axis) {
        if (Global.netherPortalMode == Global.NetherPortalMode.disabled) {
            return Optional.empty();
        }
        
        if (Global.netherPortalMode == Global.NetherPortalMode.vanilla) {
            return PortalSize.func_242964_a(worldAccess, blockPos, axis);
        }
        
        if (isNearObsidian(worldAccess, blockPos)) {
            IntrinsicPortalGeneration.onFireLitOnObsidian(
                ((ServerWorld) worldAccess),
                blockPos
            );
        }
        
        return Optional.empty();
    }
    
    private static boolean isNearObsidian(IWorld access, BlockPos blockPos) {
        for (Direction value : Direction.values()) {
            if (O_O.isObsidian(access.getBlockState(blockPos.offset(value)))) {
                return true;
            }
        }
        return false;
    }
    
    // allow lighting fire on the side of obsidian
    // for lighting horizontal portals
    @Redirect(
        method = "Lnet/minecraft/block/AbstractFireBlock;shouldLightPortal(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/Direction;)Z",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/Optional;isPresent()Z"
        )
    )
    private static boolean redirectIsPresent(Optional optional) {
        if (Global.netherPortalMode != Global.NetherPortalMode.vanilla) {
            return true;
        }
        else {
            return optional.isPresent();
        }
    }
}
