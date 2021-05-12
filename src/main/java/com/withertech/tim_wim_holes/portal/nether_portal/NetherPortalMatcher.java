package com.withertech.tim_wim_holes.portal.nether_portal;

import com.withertech.tim_wim_holes.Helper;
import com.withertech.tim_wim_holes.my_util.IntBox;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.IntStream;

public class NetherPortalMatcher {
    private static boolean isAir(IWorld world, BlockPos pos) {
        return world.isAirBlock(pos);
    }
    
    static IntBox findVerticalPortalPlacement(
        BlockPos areaSize,
        IWorld world,
        BlockPos searchingCenter
    ) {
        int radius = 16;
        IntBox airCube = getAirCubeOnSolidGround(
            areaSize, new BlockPos(6, 0, 6), world, searchingCenter,
            radius, true
        );
        
        if (airCube == null) {
            Helper.info("Cannot Find Portal Placement on Ground with 3 Spacing");
            airCube = getAirCubeOnSolidGround(
                areaSize, new BlockPos(2, 0, 2), world, searchingCenter,
                radius, true
            );
        }
        
        if (airCube == null) {
            Helper.info("Cannot Find Portal Placement on Ground with 1 Spacing");
            airCube = getAirCubeOnSolidGround(
                areaSize, new BlockPos(6, 0, 6), world, searchingCenter,
                radius, false
            );
        }
        
        if (airCube == null) {
            Helper.info("Cannot Find Portal Placement on Non Solid Surface");
            return null;
        }
        
        if (world.getBlockState(airCube.l.down()).getMaterial().isSolid()) {
            Helper.info("Generated Portal On Ground");
            
            return pushDownBox(world, airCube.getSubBoxInCenter(areaSize));
        }
        else {
            Helper.info("Generated Portal On Non Solid Surface");
            
            return levitateBox(world, airCube.getSubBoxInCenter(areaSize), 40);
        }
        
    }
    
    private static IntBox expandFromBottomCenter(IntBox box, BlockPos spacing) {
        BlockPos boxSize = box.getSize();
        
        return box.getAdjusted(
            -spacing.getX() / 2, 0, -spacing.getZ() / 2,
            spacing.getX() / 2, spacing.getY(), spacing.getZ() / 2
        );
    }
    
    private static IntBox getAirCubeOnSolidGround(
        BlockPos areaSize,
        BlockPos ambientSpaceReserved,
        IWorld world,
        BlockPos searchingCenter,
        int findingRadius,
        boolean solidGround
    ) {
        Predicate<BlockPos> isAirOnGroundPredicate =
            blockPos -> solidGround ? isAirOnSolidGround(world, blockPos) :
                isAirOnGround(world, blockPos);
        
        return BlockTraverse.searchColumned(
            searchingCenter.getX(), searchingCenter.getZ(), findingRadius,
            5, world.func_234938_ad_() - 5,
            mutable -> {
                if (isAirOnGroundPredicate.test(mutable)) {
                    IntBox box = IntBox.getBoxByBasePointAndSize(areaSize, mutable);
                    
                    IntBox expanded = expandFromBottomCenter(box, ambientSpaceReserved);
                    if (isAirCubeMediumPlace(world, expanded)) {
                        if (solidGround) {
                            if (BlockTraverse.boxAllMatch(box.getSurfaceLayer(Direction.DOWN), isAirOnGroundPredicate)) {
                                if (isAirOnGroundPredicate.test(expanded.l)) {
                                    return box;
                                }
                            }
                        }
                        else {
                            return box;
                        }
                    }
                }
                return null;
            }
        );
    }
    
    //make it possibly generate above ground
    static IntBox findHorizontalPortalPlacement(
        BlockPos areaSize,
        IWorld world,
        BlockPos searchingCenter
    ) {
        IntBox result = findHorizontalPortalPlacementWithVerticalSpaceReserved(
            areaSize, world, searchingCenter,
            30, 12
        );
        if (result == null) {
            result = findHorizontalPortalPlacementWithVerticalSpaceReserved(
                areaSize, world, searchingCenter,
                10, 12
            );
        }
        if (result == null) {
            result = findHorizontalPortalPlacementWithVerticalSpaceReserved(
                areaSize, world, searchingCenter,
                1, 12
            );
        }
        return result;
    }
    
    private static IntBox findHorizontalPortalPlacementWithVerticalSpaceReserved(
        BlockPos areaSize,
        IWorld world,
        BlockPos searchingCenter,
        int verticalSpaceReserve,
        int findingRadius
    ) {
        BlockPos growVertically = new BlockPos(
            areaSize.getX(),
            verticalSpaceReserve,
            areaSize.getZ()
        );
        IntBox foundCubeArea = findCubeAirAreaAtAnywhere(
            growVertically, world, searchingCenter, findingRadius
        );
        if (foundCubeArea == null) {
            return null;
        }
        return foundCubeArea.getSubBoxInCenter(areaSize);
    }
    
    // does not contain lava water
    public static boolean isSolidGroundBlock(BlockState blockState) {
        return blockState.getMaterial().isSolid();
    }
    
    // includes lava water
    public static boolean isGroundBlock(BlockState blockState) {
        return !blockState.isAir();
    }
    
    private static boolean isAirOnSolidGround(IWorld world, BlockPos blockPos) {
        return world.isAirBlock(blockPos) &&
            isSolidGroundBlock(world.getBlockState(blockPos.add(0, -1, 0)));
    }
    
    private static boolean isAirOnGround(IWorld world, BlockPos blockPos) {
        return world.isAirBlock(blockPos) &&
            isGroundBlock(world.getBlockState(blockPos.add(0, -1, 0)));
    }
    
    static IntBox findCubeAirAreaAtAnywhere(
        BlockPos areaSize,
        IWorld world,
        BlockPos searchingCenter,
        int findingRadius
    ) {
        return BlockTraverse.searchColumned(
            searchingCenter.getX(), searchingCenter.getZ(),
            findingRadius,
            5, world.func_234938_ad_() - 5,
            mutable -> {
                IntBox box = IntBox.getBoxByBasePointAndSize(areaSize, mutable);
                if (isAirCubeMediumPlace(world, box)) {
                    return box;
                }
                else {
                    return null;
                }
            }
        );
    }
    
    public static boolean isAirCubeMediumPlace(IWorld world, IntBox box) {
        //the box out of height limit is not accepted
        if (box.h.getY() + 5 >= ((World) world).func_234938_ad_()) {
            return false;
        }
        if (box.l.getY() - 5 <= 0) {
            return false;
        }
        
        return isAllAir(world, box);
    }
    
    public static boolean isAllAir(IWorld world, IntBox box) {
        boolean roughTest = Arrays.stream(box.getEightVertices()).allMatch(
            blockPos -> isAir(world, blockPos)
        );
        if (!roughTest) {
            return false;
        }
        return box.stream().allMatch(
            blockPos -> isAir(world, blockPos)
        );
    }
    
    
    //move the box up
    public static IntBox levitateBox(
        IWorld world, IntBox airCube, int maxOffset
    ) {
        Integer maxUpShift = Helper.getLastSatisfying(
            IntStream.range(1, maxOffset * 3 / 2).boxed(),
            upShift -> isAirCubeMediumPlace(
                world,
                airCube.getMoved(new Vector3i(0, upShift, 0))
            )
        );
        if (maxUpShift == null) {
            maxUpShift = 0;
        }
        
        return airCube.getMoved(new Vector3i(0, maxUpShift * 2 / 3, 0));
    }
    
    public static IntBox pushDownBox(
        IWorld world, IntBox airCube
    ) {
        Integer downShift = Helper.getLastSatisfying(
            IntStream.range(0, 40).boxed(),
            i -> isAirCubeMediumPlace(
                world,
                airCube.getMoved(new Vector3i(0, -i, 0))
            )
        );
        if (downShift == null) {
            downShift = 0;
        }
        
        return airCube.getMoved(new Vector3i(0, -downShift, 0));
    }
    
}
