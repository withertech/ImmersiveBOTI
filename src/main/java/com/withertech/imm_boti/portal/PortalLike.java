package com.withertech.imm_boti.portal;

import com.withertech.imm_boti.Helper;
import com.withertech.imm_boti.my_util.BoxPredicate;
import com.withertech.imm_boti.my_util.Plane;
import com.withertech.imm_boti.render.MyGameRenderer;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import javax.annotation.Nullable;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * The PortalLike interface is introduced for the merge portal rendering optimization.
 * (A portal or a portal rendering group is a PortalLike)
 * You probably need to manipulate portal entities, not PortalLike
 */
public interface PortalLike {
    @OnlyIn(Dist.CLIENT)
    BoxPredicate getInnerFrustumCullingFunc(
        double cameraX, double cameraY, double cameraZ
    );
    
    boolean isConventionalPortal();
    
    // bounding box
    AxisAlignedBB getExactAreaBox();
    
    Vector3d transformPoint(Vector3d pos);
    
    Vector3d transformLocalVec(Vector3d localVec);
    
    // TODO remove this and use the area box
    double getDistanceToNearestPointInPortal(
        Vector3d point
    );
    
    // TODO remove this and use the area box
    double getDestAreaRadiusEstimation();
    
    Vector3d getOriginPos();
    
    Vector3d getDestPos();
    
    World getOriginWorld();
    
    World getDestWorld();
    
    RegistryKey<World> getDestDim();
    
    boolean isRoughlyVisibleTo(Vector3d cameraPos);
    
    @Nullable
    Plane getInnerClipping();
    
    @Nullable
    Quaternion getRotation();
    
    double getScale();
    
    boolean getIsGlobal();
    
    // used for super advanced frustum culling
    @Nullable
    Vector3d[] getOuterFrustumCullingVertices();
    
    @OnlyIn(Dist.CLIENT)
    void renderViewAreaMesh(Vector3d portalPosRelativeToCamera, Consumer<Vector3d> vertexOutput);
    
    // Scaling does not interfere camera transformation
    @Nullable
    Matrix4f getAdditionalCameraTransformation();
    
    @Nullable
    UUID getDiscriminator();
    
    boolean cannotRenderInMe(Portal portal);
    
    boolean isFuseView();
    
    default boolean hasScaling() {
        return getScale() != 1.0;
    }
    
    default RegistryKey<World> getOriginDim() {
        return getOriginWorld().getDimensionKey();
    }
    
    default boolean isInside(Vector3d entityPos, double valve) {
        Plane innerClipping = getInnerClipping();
        
        if (innerClipping == null) {
            return true;
        }
        
        double v = entityPos.subtract(innerClipping.pos).dotProduct(innerClipping.normal);
        return v > valve;
    }
    
    default double getSizeEstimation() {
        final Vector3d boxSize = Helper.getBoxSize(getExactAreaBox());
        final double maxDimension = Math.max(Math.max(boxSize.x, boxSize.y), boxSize.z);
        return maxDimension;
    }
    
    // the container contains WorldRenderer.ChunkInfo
    @OnlyIn(Dist.CLIENT)
    default void doAdditionalRenderingCull(ObjectList<?> visibleChunks) {
        MyGameRenderer.cullRenderingSections(visibleChunks, this);
    }
    
//    // do additional cull when sodium is present
//    @Environment(EnvType.CLIENT)
//    @Nullable
//    default TriIntPredicate getAdditionalCullPredicateSodium() {
//        return null;
//    }
    
}
