package com.withertech.imm_boti.portal.global_portals;

import com.withertech.imm_boti.Helper;
import com.withertech.imm_boti.McHelper;
import com.withertech.imm_boti.my_util.DQuaternion;
import com.withertech.imm_boti.portal.Portal;
import com.withertech.imm_boti.portal.PortalExtension;
import net.minecraft.entity.EntityType;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import java.util.function.Predicate;

public class VerticalConnectingPortal extends GlobalTrackedPortal {
    public static EntityType<VerticalConnectingPortal> entityType;
    
    public static enum ConnectorType {
        ceil, floor
    }
    
    private static Predicate<Portal> getPredicate(ConnectorType connectorType) {
        switch (connectorType) {
            case floor:
                return portal -> portal instanceof VerticalConnectingPortal && portal.getNormal().y > 0;
            default:
            case ceil:
                return portal -> portal instanceof VerticalConnectingPortal && portal.getNormal().y < 0;
        }
    }
    
    public VerticalConnectingPortal(
        EntityType<?> entityType_1,
        World world_1
    ) {
        super(entityType_1, world_1);
    }
    
    public static void connect(
        RegistryKey<World> from,
        ConnectorType connectorType,
        RegistryKey<World> to
    ) {
        connect(from, connectorType, to, false);
    }
    
    public static void connect(
        RegistryKey<World> from,
        ConnectorType connectorType,
        RegistryKey<World> to,
        boolean respectSpaceRatio
    ) {
        removeConnectingPortal(connectorType, from);
        
        ServerWorld fromWorld = McHelper.getServer().getWorld(from);
        
        VerticalConnectingPortal connectingPortal = createConnectingPortal(
            fromWorld,
            connectorType,
            McHelper.getServer().getWorld(to),
            respectSpaceRatio ?
                fromWorld.getDimensionType().getCoordinateScale() /
                    McHelper.getServer().getWorld(to).getDimensionType().getCoordinateScale()
                : 1.0,
            false,
            0
        );
        
        GlobalPortalStorage storage = GlobalPortalStorage.get(fromWorld);
        
        storage.addPortal(connectingPortal);
    }
    
    public static void connectMutually(
        RegistryKey<World> up,
        RegistryKey<World> down,
        boolean respectSpaceRatio
    ) {
        
        connect(up, ConnectorType.floor, down, respectSpaceRatio);
        connect(down, ConnectorType.ceil, up, respectSpaceRatio);
    }
    
    public static VerticalConnectingPortal createConnectingPortal(
        ServerWorld fromWorld,
        ConnectorType connectorType,
        ServerWorld toWorld,
        double scaling,
        boolean inverted,
        double rotationAlongYDegrees
    ) {
        VerticalConnectingPortal verticalConnectingPortal = new VerticalConnectingPortal(
            entityType, fromWorld
        );
        
        verticalConnectingPortal.dimensionTo = toWorld.getDimensionKey();
        verticalConnectingPortal.width = 23333333333.0d;
        verticalConnectingPortal.height = 23333333333.0d;
        
        switch (connectorType) {
            case floor:
                verticalConnectingPortal.setPosition(0, 0, 0);
                verticalConnectingPortal.axisW = new Vector3d(0, 0, 1);
                verticalConnectingPortal.axisH = new Vector3d(1, 0, 0);
                break;
            case ceil:
                verticalConnectingPortal.setPosition(0, fromWorld.func_234938_ad_(), 0);
                verticalConnectingPortal.axisW = new Vector3d(1, 0, 0);
                verticalConnectingPortal.axisH = new Vector3d(0, 0, 1);
                break;
        }
        
        if (!inverted) {
            switch (connectorType) {
                case floor:
                    verticalConnectingPortal.setDestination(new Vector3d(0, toWorld.func_234938_ad_(), 0));
                    break;
                case ceil:
                    verticalConnectingPortal.setDestination(new Vector3d(0, 0, 0));
                    break;
            }
        }
        else {
            switch (connectorType) {
                case floor:
                    verticalConnectingPortal.setDestination(new Vector3d(0, 0, 0));
                    break;
                case ceil:
                    verticalConnectingPortal.setDestination(new Vector3d(0, toWorld.func_234938_ad_(), 0));
                    break;
            }
        }
        
        DQuaternion inversionRotation = inverted ?
            DQuaternion.rotationByDegrees(new Vector3d(1, 0, 0), 180) : null;
        DQuaternion additionalRotation = rotationAlongYDegrees != 0 ?
            DQuaternion.rotationByDegrees(new Vector3d(0, 1, 0), rotationAlongYDegrees) : null;
        DQuaternion rotation = Helper.combineNullable(
            inversionRotation, additionalRotation, DQuaternion::hamiltonProduct
        );
        verticalConnectingPortal.rotation = rotation != null ? rotation.toMcQuaternion() : null;
        
        if (scaling != 1.0) {
            verticalConnectingPortal.scaling = scaling;
            verticalConnectingPortal.teleportChangesScale = false;
            PortalExtension.get(verticalConnectingPortal).adjustPositionAfterTeleport = false;
        }
        
        return verticalConnectingPortal;
    }
    
    public static void removeConnectingPortal(
        ConnectorType connectorType,
        RegistryKey<World> dimension
    ) {
        removeConnectingPortal(getPredicate(connectorType), dimension);
    }
    
    private static void removeConnectingPortal(
        Predicate<Portal> predicate, RegistryKey<World> dimension
    ) {
        ServerWorld endWorld = McHelper.getServer().getWorld(dimension);
        GlobalPortalStorage storage = GlobalPortalStorage.get(endWorld);
        
        storage.removePortals(
            portal -> portal instanceof VerticalConnectingPortal && predicate.test(portal)
        );
    }
    
    public static VerticalConnectingPortal getConnectingPortal(
        World world, ConnectorType type
    ) {
        return (VerticalConnectingPortal) McHelper.getGlobalPortals(world).stream()
            .filter(getPredicate(type))
            .findFirst().orElse(null);
    }
    
    public static int getHeight(RegistryKey<World> dim) {
        return McHelper.getServer().getWorld(dim).func_234938_ad_();
    }
}
