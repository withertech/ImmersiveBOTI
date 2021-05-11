package com.withertech.imm_boti.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.withertech.imm_boti.CHelper;
import com.withertech.imm_boti.ClientWorldLoader;
import com.withertech.imm_boti.Global;
import com.withertech.imm_boti.Helper;
import com.withertech.imm_boti.McHelper;
import com.withertech.imm_boti.ModMain;
import com.withertech.imm_boti.OFInterface;
import com.withertech.imm_boti.ducks.IEEntity;
import com.withertech.imm_boti.ducks.IEWorldRenderer;
import com.withertech.imm_boti.optifine_compatibility.ShaderClippingManager;
import com.withertech.imm_boti.portal.Mirror;
import com.withertech.imm_boti.portal.Portal;
import com.withertech.imm_boti.portal.PortalLike;
import com.withertech.imm_boti.render.context_management.PortalRendering;
import com.withertech.imm_boti.render.context_management.RenderStates;
import com.withertech.imm_boti.render.context_management.WorldRenderInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.Validate;

import java.util.WeakHashMap;

@OnlyIn(Dist.CLIENT)
public class CrossPortalEntityRenderer {
    private static final Minecraft client = Minecraft.getInstance();
    
    //there is no weak hash set
    private static final WeakHashMap<Entity, Object> collidedEntities = new WeakHashMap<>();
    
    public static boolean isRendering = false;
    
    public static void init() {
        ModMain.postClientTickSignal.connect(CrossPortalEntityRenderer::onClientTick);
        
        ModMain.clientCleanupSignal.connect(CrossPortalEntityRenderer::cleanUp);
    }
    
    private static void cleanUp() {
        collidedEntities.clear();
    }
    
    private static void onClientTick() {
        collidedEntities.entrySet().removeIf(entry ->
            entry.getKey().removed ||
                ((IEEntity) entry.getKey()).getCollidingPortal() == null
        );
    }
    
    public static void onEntityTickClient(Entity entity) {
        if (entity instanceof Portal) {
            return;
        }
        
        Portal collidingPortal = ((IEEntity) entity).getCollidingPortal();
        if (collidingPortal != null) {
            collidedEntities.put(entity, null);
        }
    }
    
    public static void onBeginRenderingEntities(MatrixStack matrixStack) {
        if (!Global.correctCrossPortalEntityRendering) {
            return;
        }
        
        if (PortalRendering.isRendering()) {
            FrontClipping.setupInnerClipping(
                matrixStack, PortalRendering.getRenderingPortal(), false
            );
        }
    }
    
    // do not use runWithTransformation here (because matrixStack is changed?)
    public static void onEndRenderingEntities(MatrixStack matrixStack) {
        if (!Global.correctCrossPortalEntityRendering) {
            return;
        }
        
        renderEntityProjections(matrixStack);
    }
    
    public static void beforeRenderingEntity(Entity entity, MatrixStack matrixStack) {
        if (!Global.correctCrossPortalEntityRendering) {
            return;
        }
        if (!PortalRendering.isRendering()) {
            if (collidedEntities.containsKey(entity)) {
                Portal collidingPortal = ((IEEntity) entity).getCollidingPortal();
                if (collidingPortal == null) {
                    //Helper.err("Colliding Portal Record Invalid " + entity);
                    return;
                }
                
                //draw already built triangles
                client.getRenderTypeBuffers().getBufferSource().finish();
                
                FrontClipping.setupOuterClipping(matrixStack, collidingPortal);
                if (OFInterface.isShaders.getAsBoolean()) {
                    ShaderClippingManager.update();
                }
            }
        }
    }
    
    public static void afterRenderingEntity(Entity entity) {
        if (!Global.correctCrossPortalEntityRendering) {
            return;
        }
        if (!PortalRendering.isRendering()) {
            if (collidedEntities.containsKey(entity)) {
                //draw it with culling in a separate draw call
                client.getRenderTypeBuffers().getBufferSource().finish();
                FrontClipping.disableClipping();
            }
        }
    }
    
    //if an entity is in overworld but halfway through a nether portal
    //then it has a projection in nether
    private static void renderEntityProjections(MatrixStack matrixStack) {
        if (!Global.correctCrossPortalEntityRendering) {
            return;
        }
        collidedEntities.keySet().forEach(entity -> {
            Portal collidingPortal = ((IEEntity) entity).getCollidingPortal();
            if (collidingPortal == null) {
                //Helper.err("Colliding Portal Record Invalid " + entity);
                return;
            }
            if (collidingPortal instanceof Mirror) {
                //no need to render entity projection for mirrors
                return;
            }
            RegistryKey<World> projectionDimension = collidingPortal.dimensionTo;
            if (client.world.getDimensionKey() == projectionDimension) {
                renderProjectedEntity(entity, collidingPortal, matrixStack);
            }
        });
    }
    
    public static boolean hasIntersection(
        Vector3d outerPlanePos, Vector3d outerPlaneNormal,
        Vector3d entityPos, Vector3d collidingPortalNormal
    ) {
        return entityPos.subtract(outerPlanePos).dotProduct(outerPlaneNormal) > 0.01 &&
            outerPlanePos.subtract(entityPos).dotProduct(collidingPortalNormal) > 0.01;
    }
    
    private static void renderProjectedEntity(
        Entity entity,
        Portal collidingPortal,
        MatrixStack matrixStack
    ) {
        if (PortalRendering.isRendering()) {
            PortalLike renderingPortal = PortalRendering.getRenderingPortal();
            //correctly rendering it needs two culling planes
            //use some rough check to work around
            
            if (renderingPortal instanceof Portal) {
                if (!Portal.isFlippedPortal(((Portal) renderingPortal), collidingPortal)) {
                    Vector3d cameraPos = client.gameRenderer.getActiveRenderInfo().getProjectedView();
                    
                    boolean isHidden = cameraPos.subtract(collidingPortal.getDestPos())
                        .dotProduct(collidingPortal.getContentDirection()) < 0;
                    if (renderingPortal == collidingPortal || !isHidden) {
                        renderEntityRegardingPlayer(entity, collidingPortal, matrixStack);
                    }
                }
            }
        }
        else {
            FrontClipping.disableClipping();
            // don't draw the existing triangles with culling enabled
            client.getRenderTypeBuffers().getBufferSource().finish();
            
            FrontClipping.setupInnerClipping(matrixStack, collidingPortal, false);
            renderEntityRegardingPlayer(entity, collidingPortal, matrixStack);
            FrontClipping.disableClipping();
        }
    }
    
    private static void renderEntityRegardingPlayer(
        Entity entity,
        Portal transformingPortal,
        MatrixStack matrixStack
    ) {
        if (entity instanceof ClientPlayerEntity) {
            MyGameRenderer.renderPlayerItself(() -> {
                renderEntity(entity, transformingPortal, matrixStack);
            });
        }
        else {
            renderEntity(entity, transformingPortal, matrixStack);
        }
    }
    
    private static void renderEntity(
        Entity entity,
        Portal transformingPortal,
        MatrixStack matrixStack
    ) {
        Vector3d cameraPos = client.gameRenderer.getActiveRenderInfo().getProjectedView();
        
        ClientWorld newWorld = ClientWorldLoader.getWorld(transformingPortal.dimensionTo);
        
        Vector3d oldEyePos = McHelper.getEyePos(entity);
        Vector3d oldLastTickEyePos = McHelper.getLastTickEyePos(entity);
        World oldWorld = entity.world;
        
        Vector3d newEyePos = transformingPortal.transformPoint(oldEyePos);

//        if (PortalRendering.isRendering()) {
//            Portal renderingPortal = PortalRendering.getRenderingPortal();
//            if (!renderingPortal.isInside(newEyePos, -3)) {
//                return;
//            }
//        }
        
        if (entity instanceof ClientPlayerEntity) {
            if (!Global.renderYourselfInPortal) {
                return;
            }
            
            if (client.gameSettings.getPointOfView().func_243192_a()) {
                //avoid rendering player too near and block view
                double dis = newEyePos.distanceTo(cameraPos);
                double valve = 0.5 + McHelper.lastTickPosOf(entity).distanceTo(entity.getPositionVec());
                if (transformingPortal.scaling > 1) {
                    valve *= transformingPortal.scaling;
                }
                if (dis < valve) {
                    return;
                }
                
                AxisAlignedBB transformedBoundingBox =
                    Helper.transformBox(RenderStates.originalPlayerBoundingBox, transformingPortal::transformPoint);
                if (transformedBoundingBox.contains(CHelper.getCurrentCameraPos())) {
                    return;
                }
            }
        }
        
        McHelper.setEyePos(
            entity,
            newEyePos,
            transformingPortal.transformPoint(oldLastTickEyePos)
        );
        
        entity.world = newWorld;
        
        isRendering = true;
        matrixStack.push();
        setupEntityProjectionRenderingTransformation(
            transformingPortal, entity, matrixStack
        );
        
        OFInterface.updateEntityTypeForShader.accept(entity);
        IRenderTypeBuffer.Impl consumers = client.getRenderTypeBuffers().getBufferSource();
        ((IEWorldRenderer) client.worldRenderer).myRenderEntity(
            entity,
            cameraPos.x, cameraPos.y, cameraPos.z,
            RenderStates.tickDelta, matrixStack,
            consumers
        );
        //immediately invoke draw call
        consumers.finish();
        
        matrixStack.pop();
        isRendering = false;
        
        McHelper.setEyePos(
            entity, oldEyePos, oldLastTickEyePos
        );
        entity.world = oldWorld;
    }
    
    private static void setupEntityProjectionRenderingTransformation(
        Portal portal, Entity entity, MatrixStack matrixStack
    ) {
        if (portal.scaling == 1.0 && portal.rotation == null) {
            return;
        }
        
        Vector3d cameraPos = CHelper.getCurrentCameraPos();
        
        Vector3d anchor = entity.getEyePosition(RenderStates.tickDelta).subtract(cameraPos);
        
        matrixStack.translate(anchor.x, anchor.y, anchor.z);
        
        float scaling = (float) portal.scaling;
        matrixStack.scale(scaling, scaling, scaling);
        
        if (portal.rotation != null) {
            matrixStack.rotate(portal.rotation);
        }
        
        matrixStack.translate(-anchor.x, -anchor.y, -anchor.z);
    }
    
    public static boolean shouldRenderPlayerDefault() {
        if (!Global.renderYourselfInPortal) {
            return false;
        }
        if (!WorldRenderInfo.isRendering()) {
            return false;
        }
        if (client.renderViewEntity.world.getDimensionKey() == RenderStates.originalPlayerDimension) {
            return true;
        }
        return false;
    }
    
    public static boolean shouldRenderEntityNow(Entity entity) {
        Validate.notNull(entity);
        if (OFInterface.isShadowPass.getAsBoolean()) {
            return true;
        }
        if (PortalRendering.isRendering()) {
            PortalLike renderingPortal = PortalRendering.getRenderingPortal();
            Portal collidingPortal = ((IEEntity) entity).getCollidingPortal();
            
            // client colliding portal update is not immediate
            if (collidingPortal != null && !(entity instanceof ClientPlayerEntity)) {
                if (renderingPortal instanceof Portal) {
                    if (!Portal.isReversePortal(collidingPortal, ((Portal) renderingPortal))) {
                        Vector3d cameraPos = PortalRenderer.client.gameRenderer.getActiveRenderInfo().getProjectedView();
                        
                        boolean isHidden = cameraPos.subtract(collidingPortal.getOriginPos())
                            .dotProduct(collidingPortal.getNormal()) < 0;
                        if (isHidden) {
                            return false;
                        }
                    }
                }
            }
            
            return renderingPortal.isInside(
                getRenderingCameraPos(entity), -0.01
            );
        }
        return true;
    }
    
    public static boolean shouldRenderPlayerNormally(Entity entity) {
        if (!client.gameSettings.getPointOfView().func_243192_a()) {
            return true;
        }
        
        if (RenderStates.originalPlayerBoundingBox.contains(CHelper.getCurrentCameraPos())) {
            return false;
        }
        
        double distanceToCamera =
            getRenderingCameraPos(entity)
                .distanceTo(client.gameRenderer.getActiveRenderInfo().getProjectedView());
        //avoid rendering player too near and block view except mirror
        return distanceToCamera > 1 || PortalRendering.isRenderingOddNumberOfMirrors();
    }
    
    public static Vector3d getRenderingCameraPos(Entity entity) {
        if (entity instanceof ClientPlayerEntity) {
            return RenderStates.originalPlayerPos.add(0, entity.getEyeHeight(), 0);
        }
        return entity.getEyePosition(RenderStates.tickDelta);
    }
}
