package com.withertech.tim_wim_holes.portal.custom_portal_gen.form;

import com.withertech.tim_wim_holes.Global;
import com.withertech.tim_wim_holes.my_util.IntBox;
import com.withertech.tim_wim_holes.portal.Portal;
import com.withertech.tim_wim_holes.portal.custom_portal_gen.CustomPortalGeneration;
import com.withertech.tim_wim_holes.portal.custom_portal_gen.PortalGenInfo;
import com.withertech.tim_wim_holes.portal.nether_portal.BlockPortalShape;
import com.withertech.tim_wim_holes.portal.nether_portal.GeneralBreakablePortal;
import com.withertech.tim_wim_holes.portal.nether_portal.NetherPortalGeneration;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.WorldGenRegion;
import net.minecraft.world.server.ServerWorld;
import javax.annotation.Nullable;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class NetherPortalLikeForm extends PortalGenForm {
    public final boolean generateFrameIfNotFound;
    
    public NetherPortalLikeForm(boolean generateFrameIfNotFound) {
        this.generateFrameIfNotFound = generateFrameIfNotFound;
    }
    
    @Override
    public boolean perform(
        CustomPortalGeneration cpg,
        ServerWorld fromWorld, BlockPos startingPos,
        ServerWorld toWorld,
        @Nullable Entity triggeringEntity
    ) {
        if (!NetherPortalGeneration.checkPortalGeneration(fromWorld, startingPos)) {
            return false;
        }
        
        Predicate<BlockState> areaPredicate = getAreaPredicate();
        Predicate<BlockState> thisSideFramePredicate = getThisSideFramePredicate();
        Predicate<BlockState> otherSideFramePredicate = getOtherSideFramePredicate();
        
        BlockPortalShape fromShape = NetherPortalGeneration.findFrameShape(
            fromWorld, startingPos,
            areaPredicate, thisSideFramePredicate
        );
        
        if (fromShape == null) {
            return false;
        }
        
        if (!testThisSideShape(fromWorld, fromShape)) {
            return false;
        }
        
        if (NetherPortalGeneration.isOtherGenerationRunning(fromWorld, fromShape.innerAreaBox.getCenterVec())) {
            return false;
        }
        
        // clear the area
        if (generateFrameIfNotFound) {
            for (BlockPos areaPos : fromShape.area) {
                fromWorld.setBlockState(areaPos, Blocks.AIR.getDefaultState());
            }
        }
        
        BlockPos toPos = cpg.mapPosition(fromShape.innerAreaBox.getCenter());
        
        Function<WorldGenRegion, Function<BlockPos.Mutable, PortalGenInfo>> frameMatchingFunc =
            getFrameMatchingFunc(fromWorld, toWorld, fromShape);
        NetherPortalGeneration.startGeneratingPortal(
            fromWorld,
            toWorld,
            fromShape,
            toPos,
            Global.netherPortalFindingRadius,
            otherSideFramePredicate,
            toShape -> {
                generateNewFrame(fromWorld, fromShape, toWorld, toShape);
            },
            info -> {
                //generate portal entity
                Portal[] result = generatePortalEntitiesAndPlaceholder(info);
                for (Portal portal : result) {
                    cpg.onPortalGenerated(portal);
                }
            },
            () -> {
                //place frame
                if (!generateFrameIfNotFound) {
                    return null;
                }
                
                return getNewPortalPlacement(toWorld, toPos, fromWorld, fromShape);
            },
            () -> {
                // check portal integrity while loading chunk
                return fromShape.frameAreaWithoutCorner.stream().allMatch(
                    bp -> !fromWorld.isAirBlock(bp)
                );
            },
            frameMatchingFunc
        );
        
        return true;
    }
    
    public Function<WorldGenRegion, Function<BlockPos.Mutable, PortalGenInfo>> getFrameMatchingFunc(
        ServerWorld fromWorld, ServerWorld toWorld,
        BlockPortalShape fromShape
    ) {
        Predicate<BlockState> areaPredicate = getAreaPredicate();
        Predicate<BlockState> otherSideFramePredicate = getOtherSideFramePredicate();
        BlockPos.Mutable temp2 = new BlockPos.Mutable();
        return (region) -> (blockPos) -> {
            BlockPortalShape result = fromShape.matchShapeWithMovedFirstFramePos(
                pos -> areaPredicate.test(region.getBlockState(pos)),
                pos -> otherSideFramePredicate.test(region.getBlockState(pos)),
                blockPos,
                temp2
            );
            if (result != null) {
                if (fromWorld != toWorld || fromShape.anchor != result.anchor) {
                    return new PortalGenInfo(
                        fromWorld.getDimensionKey(),
                        toWorld.getDimensionKey(),
                        fromShape, result
                    );
                }
            }
            return null;
        };
    }
    
    public PortalGenInfo getNewPortalPlacement(
        ServerWorld toWorld, BlockPos toPos,
        ServerWorld fromWorld, BlockPortalShape fromShape
    ) {
        
        
        IntBox airCubePlacement =
            NetherPortalGeneration.findAirCubePlacement(
                toWorld, toPos,
                fromShape.axis, fromShape.totalAreaBox.getSize()
            );
        
        BlockPortalShape placedShape = fromShape.getShapeWithMovedTotalAreaBox(
            airCubePlacement
        );
        
        return new PortalGenInfo(
            fromWorld.getDimensionKey(),
            toWorld.getDimensionKey(),
            fromShape,
            placedShape
        );
    }
    
    public Portal[] generatePortalEntitiesAndPlaceholder(PortalGenInfo info) {
        info.generatePlaceholderBlocks();
        return info.generateBiWayBiFacedPortal(GeneralBreakablePortal.entityType);
    }
    
    public abstract void generateNewFrame(
        ServerWorld fromWorld,
        BlockPortalShape fromShape,
        ServerWorld toWorld,
        BlockPortalShape toShape
    );
    
    public abstract Predicate<BlockState> getOtherSideFramePredicate();
    
    public abstract Predicate<BlockState> getThisSideFramePredicate();
    
    public abstract Predicate<BlockState> getAreaPredicate();
    
    public boolean testThisSideShape(ServerWorld fromWorld, BlockPortalShape fromShape) {
        return true;
    }
}
