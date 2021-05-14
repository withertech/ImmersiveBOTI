package com.withertech.tim_wim_holes.api;

import com.withertech.tim_wim_holes.Helper;
import com.withertech.tim_wim_holes.McHelper;
import com.withertech.tim_wim_holes.chunk_loading.ChunkLoader;
import com.withertech.tim_wim_holes.chunk_loading.NewChunkTrackingGraph;
import com.withertech.tim_wim_holes.my_util.DQuaternion;
import com.withertech.tim_wim_holes.portal.Portal;
import com.withertech.tim_wim_holes.portal.PortalManipulation;
import com.withertech.tim_wim_holes.portal.global_portals.GlobalPortalStorage;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;

public class PortalAPI
{

	public static void setPortalPositionOrientationAndSize(
			Portal portal,
			Vector3d position,
			DQuaternion orientation,
			double width, double height
	)
	{
		portal.setOriginPos(position);
		portal.setOrientationAndSize(
				orientation.rotate(new Vector3d(1, 0, 0)),
				orientation.rotate(new Vector3d(0, 1, 0)),
				width, height
		);
	}

	public static void setPortalOrthodoxShape(Portal portal, Direction facing, AxisAlignedBB portalArea)
	{
		Tuple<Direction, Direction> directions = Helper.getPerpendicularDirections(facing);

		Vector3d areaSize = Helper.getBoxSize(portalArea);

		AxisAlignedBB boxSurface = Helper.getBoxSurface(portalArea, facing);
		Vector3d center = boxSurface.getCenter();
		portal.setPosition(center.x, center.y, center.z);

		portal.axisW = Vector3d.copy(directions.getA().getDirectionVec());
		portal.axisH = Vector3d.copy(directions.getB().getDirectionVec());
		portal.width = Helper.getCoordinate(areaSize, directions.getA().getAxis());
		portal.height = Helper.getCoordinate(areaSize, directions.getB().getAxis());
	}

	public static void setPortalTransformation(
			Portal portal,
			RegistryKey<World> destinationDimension,
			Vector3d destinationPosition,
			@Nullable DQuaternion rotation,
			double scale
	)
	{
		portal.setDestinationDimension(destinationDimension);
		portal.setDestination(destinationPosition);
		portal.setRotationTransformation(rotation.toMcQuaternion());
		portal.setScaleTransformation(scale);
	}

	public static void spawnServerEntity(Entity entity)
	{
		McHelper.spawnServerEntity(entity);
	}

	public static <T extends Portal> T createReversePortal(T portal)
	{
		return (T) PortalManipulation.createReversePortal(
				portal, (EntityType<? extends Portal>) portal.getType()
		);
	}

	public static <T extends Portal> T createFlippedPortal(T portal)
	{
		return (T) PortalManipulation.createFlippedPortal(
				portal, (EntityType<? extends Portal>) portal.getType()
		);
	}

	public static <T extends Portal> T copyPortal(Portal portal, EntityType<T> entityType)
	{
		return (T) PortalManipulation.copyPortal(portal, (EntityType<Portal>) entityType);
	}

	public static void addGlobalPortal(
			ServerWorld world, Portal portal
	)
	{
		McHelper.validateOnServerThread();
		GlobalPortalStorage.get(world).addPortal(portal);
	}

	public static void removeGlobalPortal(
			ServerWorld world, Portal portal
	)
	{
		McHelper.validateOnServerThread();
		GlobalPortalStorage.get(world).removePortal(portal);
	}

	public static void addChunkLoaderForPlayer(ServerPlayerEntity player, ChunkLoader chunkLoader)
	{
		McHelper.validateOnServerThread();
		NewChunkTrackingGraph.addPerPlayerAdditionalChunkLoader(player, chunkLoader);
	}

	public static void removeChunkLoaderForPlayer(ServerPlayerEntity player, ChunkLoader chunkLoader)
	{
		McHelper.validateOnServerThread();
		NewChunkTrackingGraph.removePerPlayerAdditionalChunkLoader(player, chunkLoader);
	}
}
