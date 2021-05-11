package com.withertech.imm_boti.block_manipulation;

import com.withertech.imm_boti.ClientWorldLoader;
import com.withertech.imm_boti.commands.PortalCommand;
import com.withertech.imm_boti.portal.Portal;
import com.withertech.imm_boti.portal.PortalPlaceholderBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class BlockManipulationClient {
    private static final Minecraft client = Minecraft.getInstance();
    
    public static RegistryKey<World> remotePointedDim;
    public static RayTraceResult remoteHitResult;
    public static boolean isContextSwitched = false;
    
    public static boolean isPointingToPortal() {
        return remotePointedDim != null;
    }
    
    private static BlockRayTraceResult createMissedHitResult(Vector3d from, Vector3d to) {
        Vector3d dir = to.subtract(from).normalize();
        
        return BlockRayTraceResult.createMiss(to, Direction.getFacingFromVector(dir.x, dir.y, dir.z), new BlockPos(to));
    }
    
    private static boolean hitResultIsMissedOrNull(RayTraceResult bhr) {
        return bhr == null || bhr.getType() == RayTraceResult.Type.MISS;
    }
    
    public static void updatePointedBlock(float tickDelta) {
        if (client.playerController == null || client.world == null) {
            return;
        }
        
        remotePointedDim = null;
        remoteHitResult = null;
        
        Vector3d cameraPos = client.gameRenderer.getActiveRenderInfo().getProjectedView();
        
        float reachDistance = client.playerController.getBlockReachDistance();
        
        PortalCommand.getPlayerPointingPortalRaw(
            client.player, tickDelta, reachDistance, true
        ).ifPresent(pair -> {
            if (pair.getFirst().isInteractable()) {
                double distanceToPortalPointing = pair.getSecond().distanceTo(cameraPos);
                if (distanceToPortalPointing < getCurrentTargetDistance() + 0.2) {
                    client.objectMouseOver = createMissedHitResult(cameraPos, pair.getSecond());
                    
                    updateTargetedBlockThroughPortal(
                        cameraPos,
                        client.player.getLook(tickDelta),
                        client.player.world.getDimensionKey(),
                        distanceToPortalPointing,
                        reachDistance,
                        pair.getFirst()
                    );
                }
            }
        });
    }
    
    private static double getCurrentTargetDistance() {
        Vector3d cameraPos = client.gameRenderer.getActiveRenderInfo().getProjectedView();
        
        if (hitResultIsMissedOrNull(client.objectMouseOver)) {
            return 23333;
        }
        
        if (client.objectMouseOver instanceof BlockRayTraceResult) {
            BlockPos hitPos = ((BlockRayTraceResult) client.objectMouseOver).getPos();
            if (client.world.getBlockState(hitPos).getBlock() == PortalPlaceholderBlock.instance) {
                return 23333;
            }
        }
        
        return cameraPos.distanceTo(client.objectMouseOver.getHitVec());
    }
    
    private static void updateTargetedBlockThroughPortal(
        Vector3d cameraPos,
        Vector3d viewVector,
        RegistryKey<World> playerDimension,
        double beginDistance,
        double endDistance,
        Portal portal
    ) {
        
        Vector3d from = portal.transformPoint(
            cameraPos.add(viewVector.scale(beginDistance))
        );
        Vector3d to = portal.transformPoint(
            cameraPos.add(viewVector.scale(endDistance))
        );
        
        //do not touch barrier block through world wrapping portal
//        from = from.add(to.subtract(from).normalize().multiply(0.00151));
        
        RayTraceContext context = new RayTraceContext(
            from,
            to,
            RayTraceContext.BlockMode.OUTLINE,
            RayTraceContext.FluidMode.NONE,
            client.player
        );
        
        ClientWorld world = ClientWorldLoader.getWorld(portal.dimensionTo);
        
        remoteHitResult = IBlockReader.doRayTrace(
            context,
            (rayTraceContext, blockPos) -> {
                BlockState blockState = world.getBlockState(blockPos);
                
                if (blockState.getBlock() == PortalPlaceholderBlock.instance) {
                    return null;
                }
                if (blockState.getBlock() == Blocks.BARRIER) {
                    return null;
                }
                
                FluidState fluidState = world.getFluidState(blockPos);
                Vector3d start = rayTraceContext.getStartVec();
                Vector3d end = rayTraceContext.getEndVec();
                /**{@link VoxelShape#rayTrace(Vec3d, Vec3d, BlockPos)}*/
                //correct the start pos to avoid being considered inside block
                Vector3d correctedStart = start.subtract(end.subtract(start).scale(0.0015));
//                Vec3d correctedStart = start;
                VoxelShape solidShape = rayTraceContext.getBlockShape(blockState, world, blockPos);
                BlockRayTraceResult blockHitResult = world.rayTraceBlocks(
                    correctedStart, end, blockPos, solidShape, blockState
                );
                VoxelShape fluidShape = rayTraceContext.getFluidShape(fluidState, world, blockPos);
                BlockRayTraceResult blockHitResult2 = fluidShape.rayTrace(start, end, blockPos);
                double d = blockHitResult == null ? Double.MAX_VALUE :
                    rayTraceContext.getStartVec().squareDistanceTo(blockHitResult.getHitVec());
                double e = blockHitResult2 == null ? Double.MAX_VALUE :
                    rayTraceContext.getStartVec().squareDistanceTo(blockHitResult2.getHitVec());
                return d <= e ? blockHitResult : blockHitResult2;
            },
            (rayTraceContext) -> {
                Vector3d vec3d = rayTraceContext.getStartVec().subtract(rayTraceContext.getEndVec());
                return BlockRayTraceResult.createMiss(
                    rayTraceContext.getEndVec(),
                    Direction.getFacingFromVector(vec3d.x, vec3d.y, vec3d.z),
                    new BlockPos(rayTraceContext.getEndVec())
                );
            }
        );
        
        if (remoteHitResult.getHitVec().y < 0.1) {
            remoteHitResult = new BlockRayTraceResult(
                remoteHitResult.getHitVec(),
                Direction.DOWN,
                ((BlockRayTraceResult) remoteHitResult).getPos(),
                ((BlockRayTraceResult) remoteHitResult).isInside()
            );
        }
        
        if (remoteHitResult != null) {
            if (!world.getBlockState(((BlockRayTraceResult) remoteHitResult).getPos()).isAir()) {
                client.objectMouseOver = createMissedHitResult(from, to);
                remotePointedDim = portal.dimensionTo;
            }
        }
        
    }
    
    public static void myHandleBlockBreaking(boolean isKeyPressed) {
//        if (remoteHitResult == null) {
//            return;
//        }
        
        
        if (!client.player.isHandActive()) {
            if (isKeyPressed && isPointingToPortal()) {
                BlockRayTraceResult blockHitResult = (BlockRayTraceResult) remoteHitResult;
                BlockPos blockPos = blockHitResult.getPos();
                ClientWorld remoteWorld =
                    ClientWorldLoader.getWorld(remotePointedDim);
                if (!remoteWorld.getBlockState(blockPos).isAir()) {
                    Direction direction = blockHitResult.getFace();
                    if (myUpdateBlockBreakingProgress(blockPos, direction)) {
                        client.particles.addBlockHitEffects(blockPos, direction);
                        client.player.swingArm(Hand.MAIN_HAND);
                    }
                }
                
            }
            else {
                client.playerController.resetBlockRemoving();
            }
        }
    }
    
    //hacky switch
    public static boolean myUpdateBlockBreakingProgress(
        BlockPos blockPos,
        Direction direction
    ) {
//        if (remoteHitResult == null) {
//            return false;
//        }
        
        ClientWorld oldWorld = client.world;
        client.world = ClientWorldLoader.getWorld(remotePointedDim);
        isContextSwitched = true;
        
        try {
            return client.playerController.onPlayerDamageBlock(blockPos, direction);
        }
        finally {
            client.world = oldWorld;
            isContextSwitched = false;
        }
        
    }
    
    public static void myAttackBlock() {
//        if (remoteHitResult == null) {
//            return;
//        }
        
        
        ClientWorld targetWorld =
            ClientWorldLoader.getWorld(remotePointedDim);
        BlockPos blockPos = ((BlockRayTraceResult) remoteHitResult).getPos();
        
        if (targetWorld.isAirBlock(blockPos)) {
            return;
        }
        
        ClientWorld oldWorld = client.world;
        
        client.world = targetWorld;
        isContextSwitched = true;
        
        try {
            client.playerController.clickBlock(
                blockPos,
                ((BlockRayTraceResult) remoteHitResult).getFace()
            );
        }
        finally {
            client.world = oldWorld;
            isContextSwitched = false;
        }
        
        client.player.swingArm(Hand.MAIN_HAND);
    }
    
    //too lazy to rewrite the whole interaction system so hack there and here
    public static void myItemUse(Hand hand) {
//        if (remoteHitResult == null) {
//            return;
//        }
        
        ClientWorld targetWorld =
            ClientWorldLoader.getWorld(remotePointedDim);
        
        ItemStack itemStack = client.player.getHeldItem(hand);
        BlockRayTraceResult blockHitResult = (BlockRayTraceResult) remoteHitResult;
        
        Tuple<BlockRayTraceResult, RegistryKey<World>> result =
            BlockManipulationServer.getHitResultForPlacing(targetWorld, blockHitResult);
        blockHitResult = result.getA();
        targetWorld = ClientWorldLoader.getWorld(result.getB());
        remoteHitResult = blockHitResult;
        remotePointedDim = result.getB();
        
        int i = itemStack.getCount();
        ActionResultType actionResult2 = myInteractBlock(hand, targetWorld, blockHitResult);
        if (actionResult2.isSuccessOrConsume()) {
            if (actionResult2.isSuccess()) {
                client.player.swingArm(hand);
                if (!itemStack.isEmpty() && (itemStack.getCount() != i || client.playerController.isInCreativeMode())) {
                    client.gameRenderer.itemRenderer.resetEquippedProgress(hand);
                }
            }
            
            return;
        }
        
        if (actionResult2 == ActionResultType.FAIL) {
            return;
        }
        
        if (!itemStack.isEmpty()) {
            ActionResultType actionResult3 = client.playerController.processRightClick(
                client.player,
                targetWorld,
                hand
            );
            if (actionResult3.isSuccessOrConsume()) {
                if (actionResult3.isSuccess()) {
                    client.player.swingArm(hand);
                }
                
                client.gameRenderer.itemRenderer.resetEquippedProgress(hand);
                return;
            }
        }
    }
    
    private static ActionResultType myInteractBlock(
        Hand hand,
        ClientWorld targetWorld,
        BlockRayTraceResult blockHitResult
    ) {
//        if (remoteHitResult == null) {
//            return null;
//        }
        
        ClientWorld oldWorld = client.world;
        
        try {
            client.player.world = targetWorld;
            client.world = targetWorld;
            isContextSwitched = true;
            
            return client.playerController.func_217292_a(
                client.player, targetWorld, hand, blockHitResult
            );
        }
        finally {
            client.player.world = oldWorld;
            client.world = oldWorld;
            isContextSwitched = false;
        }
    }
    
}
