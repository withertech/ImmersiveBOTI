package com.withertech.tim_wim_holes.portal.custom_portal_gen;

import com.withertech.tim_wim_holes.McHelper;
import com.withertech.tim_wim_holes.my_util.SignalArged;
import com.withertech.tim_wim_holes.portal.Portal;
import com.withertech.tim_wim_holes.portal.PortalExtension;
import com.withertech.tim_wim_holes.portal.PortalManipulation;
import com.withertech.tim_wim_holes.portal.nether_portal.BlockPortalShape;
import com.withertech.tim_wim_holes.portal.nether_portal.BreakablePortalEntity;
import com.withertech.tim_wim_holes.portal.nether_portal.NetherPortalGeneration;
import net.minecraft.entity.EntityType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;

public class PortalGenInfo
{
	public static final SignalArged<PortalGenInfo> generatedSignal = new SignalArged<>();

	public RegistryKey<World> from;
	public RegistryKey<World> to;
	public BlockPortalShape fromShape;
	public BlockPortalShape toShape;
	@Nullable
	public Quaternion rotation = null;
	public double scale = 1.0;

	public PortalGenInfo(
			RegistryKey<World> from,
			RegistryKey<World> to,
			BlockPortalShape fromShape,
			BlockPortalShape toShape
	)
	{
		this.from = from;
		this.to = to;
		this.fromShape = fromShape;
		this.toShape = toShape;
	}

	public PortalGenInfo(
			RegistryKey<World> from,
			RegistryKey<World> to,
			BlockPortalShape fromShape,
			BlockPortalShape toShape,
			Quaternion rotation,
			double scale
	)
	{
		this.from = from;
		this.to = to;
		this.fromShape = fromShape;
		this.toShape = toShape;
		this.rotation = rotation;
		this.scale = scale;

		//floating point inaccuracy may make the portal to have near identity rotation or scale
		if (rotation != null)
		{
			if (Math.abs(1.0 - rotation.getW()) < 0.001)
			{
				this.rotation = null;
			}
		}

		if (Math.abs(this.scale - 1.0) < 0.00001)
		{
			this.scale = 1.0;
		}
	}

	public <T extends Portal> T createTemplatePortal(EntityType<T> entityType)
	{
		ServerWorld fromWorld = McHelper.getServer().getWorld(from);

		T portal = entityType.create(fromWorld);
		fromShape.initPortalPosAxisShape(portal, false);
		portal.dimensionTo = to;
		portal.setDestination(toShape.innerAreaBox.getCenterVec());
		portal.scaling = scale;
		portal.rotation = rotation;

		if (portal.hasScaling() || portal.rotation != null)
		{
			PortalExtension.get(portal).adjustPositionAfterTeleport = true;
		}

		return portal;
	}

	public <T extends BreakablePortalEntity> BreakablePortalEntity[] generateBiWayBiFacedPortal(
			EntityType<T> entityType
	)
	{
		ServerWorld fromWorld = McHelper.getServer().getWorld(from);
		ServerWorld toWorld = McHelper.getServer().getWorld(to);

		T f1 = createTemplatePortal(entityType);

		T f2 = PortalManipulation.createFlippedPortal(f1, entityType);

		T t1 = PortalManipulation.createReversePortal(f1, entityType);
		T t2 = PortalManipulation.createFlippedPortal(t1, entityType);

		f1.blockPortalShape = fromShape;
		f2.blockPortalShape = fromShape;
		t1.blockPortalShape = toShape;
		t2.blockPortalShape = toShape;

		f1.reversePortalId = t1.getUniqueID();
		t1.reversePortalId = f1.getUniqueID();
		f2.reversePortalId = t2.getUniqueID();
		t2.reversePortalId = f2.getUniqueID();

		McHelper.spawnServerEntity(f1);
		McHelper.spawnServerEntity(f2);
		McHelper.spawnServerEntity(t1);
		McHelper.spawnServerEntity(t2);

		return (new BreakablePortalEntity[]{f1, f2, t1, t2});
	}

	public void generatePlaceholderBlocks()
	{
		MinecraftServer server = McHelper.getServer();

		NetherPortalGeneration.fillInPlaceHolderBlocks(
				server.getWorld(from), fromShape
		);
		NetherPortalGeneration.fillInPlaceHolderBlocks(
				server.getWorld(to), toShape
		);

		generatedSignal.emit(this);
	}
}
