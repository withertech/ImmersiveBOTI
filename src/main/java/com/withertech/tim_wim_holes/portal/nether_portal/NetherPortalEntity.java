package com.withertech.tim_wim_holes.portal.nether_portal;

import com.withertech.hiding_in_the_bushes.O_O;
import com.withertech.tim_wim_holes.portal.PortalPlaceholderBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Random;

public class NetherPortalEntity extends BreakablePortalEntity
{
	public static EntityType<NetherPortalEntity> entityType;

	public NetherPortalEntity(
			EntityType<?> entityType_1,
			World world_1
	)
	{
		super(entityType_1, world_1);
	}

	@Override
	protected boolean isPortalIntactOnThisSide()
	{

		return blockPortalShape.area.stream()
				.allMatch(blockPos ->
						world.getBlockState(blockPos).getBlock() == PortalPlaceholderBlock.instance
				) &&
				blockPortalShape.frameAreaWithoutCorner.stream()
						.allMatch(blockPos ->
								O_O.isObsidian(world.getBlockState(blockPos))
						);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	protected void addSoundAndParticle()
	{
		Random random = world.getRandom();

		for (int i = 0; i < (int) Math.ceil(width * height / 20); i++)
		{
			if (random.nextInt(10) == 0)
			{
				double px = (random.nextDouble() * 2 - 1) * (width / 2);
				double py = (random.nextDouble() * 2 - 1) * (height / 2);

				Vector3d pos = getPointInPlane(px, py);

				double speedMultiplier = 20;

				double vx = speedMultiplier * ((double) random.nextFloat() - 0.5D) * 0.5D;
				double vy = speedMultiplier * ((double) random.nextFloat() - 0.5D) * 0.5D;
				double vz = speedMultiplier * ((double) random.nextFloat() - 0.5D) * 0.5D;

				world.addParticle(
						ParticleTypes.PORTAL,
						pos.x, pos.y, pos.z,
						vx, vy, vz
				);
			}
		}

		if (random.nextInt(800) == 0)
		{
			world.playSound(
					getPosX(),
					getPosY(),
					getPosZ(),
					SoundEvents.BLOCK_PORTAL_AMBIENT,
					SoundCategory.BLOCKS,
					0.5F,
					random.nextFloat() * 0.4F + 0.8F,
					false
			);
		}
	}

}
