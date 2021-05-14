package com.withertech.tim_wim_holes.mixin.common.tardis.exterior;

import com.withertech.tim_wim_holes.Helper;
import com.withertech.tim_wim_holes.ducks.IEExteriorTile;
import com.withertech.tim_wim_holes.my_util.DQuaternion;
import com.withertech.tim_wim_holes.portal.Portal;
import com.withertech.tim_wim_holes.portal.PortalManipulation;
import com.withertech.tim_wim_holes.portal.global_portals.GlobalPortalStorage;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.tardis.mod.helper.TardisHelper;
import net.tardis.mod.tileentities.exteriors.ExteriorTile;
import net.tardis.mod.tileentities.exteriors.ModernPoliceBoxExteriorTile;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Objects;

@Mixin(ModernPoliceBoxExteriorTile.class)
public abstract class MixinModernPoliceBoxExteriorTile extends ExteriorTile implements IEExteriorTile
{

	public MixinModernPoliceBoxExteriorTile(TileEntityType<?> tileEntityTypeIn)
	{
		super(tileEntityTypeIn);
	}

	@Override
	public void genPortals()
	{
		TardisHelper.getConsole(Objects.requireNonNull(Objects.requireNonNull(this.getWorld()).getServer()), this.getInteriorDimensionKey()).ifPresent(console ->
				console.getOrFindExteriorTile().ifPresent(exterior ->
						console.getDoor().ifPresent(door ->
						{
							Portal interiorPortal = Portal.entityType.create(Objects.requireNonNull(console.getWorld()));
							assert interiorPortal != null;
							switch (door.getHorizontalFacing())
							{
								case NORTH:
									interiorPortal.setOriginPos(door.getPositionVec().add(0, 1.125, -0.4));
									interiorPortal.setOrientationAndSize(
											new Vector3d(1.0, 0, 0), // axisW
											new Vector3d(0, 1.0, 0), // axisH
											1.125, // width
											2.25 // height
									);

									switch (console.getTrueExteriorFacingDirection())
									{
										case NORTH:
											interiorPortal.setRotationTransformation(DQuaternion.rotationByDegrees(new Vector3d(0, 1, 0), 0).toMcQuaternion());
											interiorPortal.setDestination(Vector3d.copyCenteredWithVerticalOffset(console.getDestinationPosition(), 1.125).subtract(0, 0, 0.6));
											break;
										case WEST:
											interiorPortal.setRotationTransformation(DQuaternion.rotationByDegrees(new Vector3d(0, 1, 0), 90).toMcQuaternion());
											interiorPortal.setDestination(Vector3d.copyCenteredWithVerticalOffset(console.getDestinationPosition(), 1.125).subtract(0.6, 0, 0));
											break;
										case SOUTH:
											interiorPortal.setRotationTransformation(DQuaternion.rotationByDegrees(new Vector3d(0, 1, 0), 180).toMcQuaternion());
											interiorPortal.setDestination(Vector3d.copyCenteredWithVerticalOffset(console.getDestinationPosition(), 1.125).add(0, 0, 0.6));
											break;
										case EAST:
											interiorPortal.setRotationTransformation(DQuaternion.rotationByDegrees(new Vector3d(0, 1, 0), 270).toMcQuaternion());
											interiorPortal.setDestination(Vector3d.copyCenteredWithVerticalOffset(console.getDestinationPosition(), 1.125).add(0.6, 0, 0));
											break;
									}

									break;
								case SOUTH:
									interiorPortal.setOriginPos(door.getPositionVec().add(0, 1.125, 0.4));
									interiorPortal.setOrientationAndSize(
											new Vector3d(-1.0, 0, 0), // axisW
											new Vector3d(0, 1.0, 0), // axisH
											1.125, // width
											2.25 // height
									);

									switch (console.getTrueExteriorFacingDirection())
									{
										case NORTH:
											interiorPortal.setRotationTransformation(DQuaternion.rotationByDegrees(new Vector3d(0, 1, 0), 180).toMcQuaternion());
											interiorPortal.setDestination(Vector3d.copyCenteredWithVerticalOffset(console.getDestinationPosition(), 1.125).subtract(0, 0, 0.6));
											break;
										case WEST:
											interiorPortal.setRotationTransformation(DQuaternion.rotationByDegrees(new Vector3d(0, 1, 0), 270).toMcQuaternion());
											interiorPortal.setDestination(Vector3d.copyCenteredWithVerticalOffset(console.getDestinationPosition(), 1.125).subtract(0.6, 0, 0));
											break;
										case SOUTH:
											interiorPortal.setRotationTransformation(DQuaternion.rotationByDegrees(new Vector3d(0, 1, 0), 0).toMcQuaternion());
											interiorPortal.setDestination(Vector3d.copyCenteredWithVerticalOffset(console.getDestinationPosition(), 1.125).add(0, 0, 0.6));
											break;
										case EAST:
											interiorPortal.setRotationTransformation(DQuaternion.rotationByDegrees(new Vector3d(0, 1, 0), 90).toMcQuaternion());
											interiorPortal.setDestination(Vector3d.copyCenteredWithVerticalOffset(console.getDestinationPosition(), 1.125).add(0.6, 0, 0));
											break;
									}

									break;
								case WEST:
									interiorPortal.setOriginPos(door.getPositionVec().add(-0.4, 1.125, 0));
									interiorPortal.setOrientationAndSize(
											new Vector3d(0, 0, -1.0), // axisW
											new Vector3d(0, 1.0, 0), // axisH
											1.125, // width
											2.25 // height
									);

									switch (console.getTrueExteriorFacingDirection())
									{
										case NORTH:
											interiorPortal.setRotationTransformation(DQuaternion.rotationByDegrees(new Vector3d(0, 1, 0), 270).toMcQuaternion());
											interiorPortal.setDestination(Vector3d.copyCenteredWithVerticalOffset(console.getDestinationPosition(), 1.125).subtract(0, 0, 0.6));
											break;
										case WEST:
											interiorPortal.setRotationTransformation(DQuaternion.rotationByDegrees(new Vector3d(0, 1, 0), 0).toMcQuaternion());
											interiorPortal.setDestination(Vector3d.copyCenteredWithVerticalOffset(console.getDestinationPosition(), 1.125).subtract(0.6, 0, 0));
											break;
										case SOUTH:
											interiorPortal.setRotationTransformation(DQuaternion.rotationByDegrees(new Vector3d(0, 1, 0), 90).toMcQuaternion());
											interiorPortal.setDestination(Vector3d.copyCenteredWithVerticalOffset(console.getDestinationPosition(), 1.125).add(0, 0, 0.6));
											break;
										case EAST:
											interiorPortal.setRotationTransformation(DQuaternion.rotationByDegrees(new Vector3d(0, 1, 0), 180).toMcQuaternion());
											interiorPortal.setDestination(Vector3d.copyCenteredWithVerticalOffset(console.getDestinationPosition(), 1.125).add(0.6, 0, 0));
											break;
									}

									break;
								case EAST:
									interiorPortal.setOriginPos(door.getPositionVec().add(0.4, 1.125, 0));
									interiorPortal.setOrientationAndSize(
											new Vector3d(0, 0, 1.0), // axisW
											new Vector3d(0, 1.0, 0), // axisH
											1.125, // width
											2.25 // height
									);

									switch (console.getTrueExteriorFacingDirection())
									{
										case NORTH:
											interiorPortal.setRotationTransformation(DQuaternion.rotationByDegrees(new Vector3d(0, 1, 0), 90).toMcQuaternion());
											interiorPortal.setDestination(Vector3d.copyCenteredWithVerticalOffset(console.getDestinationPosition(), 1.125).subtract(0, 0, 0.6));
											break;
										case WEST:
											interiorPortal.setRotationTransformation(DQuaternion.rotationByDegrees(new Vector3d(0, 1, 0), 180).toMcQuaternion());
											interiorPortal.setDestination(Vector3d.copyCenteredWithVerticalOffset(console.getDestinationPosition(), 1.125).subtract(0.6, 0, 0));
											break;
										case SOUTH:
											interiorPortal.setRotationTransformation(DQuaternion.rotationByDegrees(new Vector3d(0, 1, 0), 270).toMcQuaternion());
											interiorPortal.setDestination(Vector3d.copyCenteredWithVerticalOffset(console.getDestinationPosition(), 1.125).add(0, 0, 0.6));
											break;
										case EAST:
											interiorPortal.setRotationTransformation(DQuaternion.rotationByDegrees(new Vector3d(0, 1, 0), 0).toMcQuaternion());
											interiorPortal.setDestination(Vector3d.copyCenteredWithVerticalOffset(console.getDestinationPosition(), 1.125).add(0.6, 0, 0));
											break;
									}

									break;
							}
							interiorPortal.setDestinationDimension(Objects.requireNonNull(exterior.getWorld()).getDimensionKey());

							Portal exteriorPortal = PortalManipulation.createReversePortal(interiorPortal, Portal.entityType);
							interiorPortal.setCustomName(new StringTextComponent("Interior"));
							exteriorPortal.setCustomName(new StringTextComponent("Exterior"));
							Helper.debug("Interior Portal Created: " + interiorPortal);
							Helper.debug("Exterior Portal Created: " + exteriorPortal);
							GlobalPortalStorage.convertNormalPortalIntoGlobalPortal(interiorPortal);
							GlobalPortalStorage.convertNormalPortalIntoGlobalPortal(exteriorPortal);
						})
				)
		);
	}
}
