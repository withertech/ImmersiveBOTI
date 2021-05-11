package com.withertech.imm_boti.events;

import com.withertech.hiding_in_the_bushes.ModMainForge;
import com.withertech.imm_boti.McHelper;
import com.withertech.imm_boti.api.PortalAPI;
import com.withertech.imm_boti.my_util.DQuaternion;
import com.withertech.imm_boti.portal.GeometryPortalShape;
import com.withertech.imm_boti.portal.Portal;
import com.withertech.imm_boti.portal.PortalManipulation;
import com.withertech.imm_boti.portal.global_portals.GlobalPortalStorage;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tardis.api.events.TardisEvent;
import net.tardis.mod.entity.DoorEntity;
import net.tardis.mod.tileentities.ConsoleTile;
import net.tardis.mod.tileentities.exteriors.ExteriorTile;

import java.util.Objects;
import java.util.stream.Collectors;

import static com.withertech.imm_boti.portal.PortalManipulation.*;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TardisEvents
{
	@SubscribeEvent
	public static void onTardisLand(TardisEvent.Land event)
	{
		genPortals(event.getConsole());
	}

	@SubscribeEvent
	public static void onTardisTakeoff(TardisEvent.Takeoff event)
	{
		clearPortals(event.getConsole());
	}

	public static void genPortals(ConsoleTile console)
	{
		console.getOrFindExteriorTile().ifPresent(exterior ->
			console.getDoor().ifPresent(door ->
			{
				Portal interiorPortal = Portal.entityType.create(Objects.requireNonNull(console.getWorld()));
				assert interiorPortal != null;
				interiorPortal.setOriginPos(door.getPositionVec().add(0, 1, -0.4));
				interiorPortal.setDestinationDimension(Objects.requireNonNull(exterior.getWorld()).getDimensionKey());

				switch (console.getTrueExteriorFacingDirection())
				{
					case NORTH:
						interiorPortal.setRotationTransformation(DQuaternion.rotationByDegrees(new Vector3d(0, 1, 0), 0).toMcQuaternion());
						interiorPortal.setDestination(Vector3d.copyCenteredWithVerticalOffset(console.getDestinationPosition(), 1.0).subtract(0, 0, 0.6));
						break;
					case SOUTH:
						interiorPortal.setRotationTransformation(DQuaternion.rotationByDegrees(new Vector3d(0, 1, 0), 180).toMcQuaternion());
						interiorPortal.setDestination(Vector3d.copyCenteredWithVerticalOffset(console.getDestinationPosition(), 1.0).add(0, 0, 0.6));
						break;
					case WEST:
						interiorPortal.setRotationTransformation(DQuaternion.rotationByDegrees(new Vector3d(0, 1, 0), 90).toMcQuaternion());
						interiorPortal.setDestination(Vector3d.copyCenteredWithVerticalOffset(console.getDestinationPosition(), 1.0).subtract(0.6, 0, 0));
						break;
					case EAST:
						interiorPortal.setRotationTransformation(DQuaternion.rotationByDegrees(new Vector3d(0, 1, 0), 270).toMcQuaternion());
						interiorPortal.setDestination(Vector3d.copyCenteredWithVerticalOffset(console.getDestinationPosition(), 1.0).add(0.6, 0, 0));
						break;
				}
				interiorPortal.setOrientationAndSize(
						new Vector3d(1, 0, 0), // axisW
						new Vector3d(0, 1, 0), // axisH
						1.125, // width
						2.4375 // height
				);
				Portal exteriorPortal = PortalManipulation.createReversePortal(interiorPortal, Portal.entityType);
				interiorPortal.setCustomName(new StringTextComponent("Interior"));
				exteriorPortal.setCustomName(new StringTextComponent("Exterior"));
				GlobalPortalStorage.convertNormalPortalIntoGlobalPortal(interiorPortal);
				GlobalPortalStorage.convertNormalPortalIntoGlobalPortal(exteriorPortal);
			})
		);
	}

	public static void clearPortals(ConsoleTile console)
	{
		console.getDoor().ifPresent(doorEntity ->
			McHelper.getNearbyPortals(doorEntity, 3).collect(Collectors.toList()).forEach(portal ->
			{
				ModMainForge.LOGGER.debug(portal);
				GlobalPortalStorage.get((ServerWorld) portal.world).removePortal(portal);
			})
		);
		console.getOrFindExteriorTile().ifPresent(exteriorTile ->
			McHelper.getNearbyPortals(exteriorTile.getWorld(), Vector3d.copyCentered(exteriorTile.getPos()), 3).collect(Collectors.toList()).forEach(portal ->
			{
				ModMainForge.LOGGER.debug(portal);
				GlobalPortalStorage.get((ServerWorld) portal.world).removePortal(portal);
			})
		);
	}
}
