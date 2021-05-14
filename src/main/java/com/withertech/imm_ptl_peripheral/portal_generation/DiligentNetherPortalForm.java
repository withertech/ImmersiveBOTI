package com.withertech.imm_ptl_peripheral.portal_generation;

import com.mojang.serialization.Codec;
import com.withertech.hiding_in_the_bushes.O_O;
import com.withertech.tim_wim_holes.Global;
import com.withertech.tim_wim_holes.portal.custom_portal_gen.PortalGenInfo;
import com.withertech.tim_wim_holes.portal.custom_portal_gen.form.AbstractDiligentForm;
import com.withertech.tim_wim_holes.portal.custom_portal_gen.form.PortalGenForm;
import com.withertech.tim_wim_holes.portal.nether_portal.BlockPortalShape;
import com.withertech.tim_wim_holes.portal.nether_portal.BreakablePortalEntity;
import com.withertech.tim_wim_holes.portal.nether_portal.NetherPortalEntity;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

import java.util.function.Predicate;

public class DiligentNetherPortalForm extends AbstractDiligentForm
{
	public DiligentNetherPortalForm()
	{
		super(true);
	}

	@Override
	public void generateNewFrame(ServerWorld fromWorld, BlockPortalShape fromShape, ServerWorld toWorld, BlockPortalShape toShape)
	{
		for (BlockPos blockPos : toShape.frameAreaWithCorner)
		{
			toWorld.setBlockState(blockPos, Blocks.OBSIDIAN.getDefaultState());
		}
	}

	@Override
	public BreakablePortalEntity[] generatePortalEntitiesAndPlaceholder(PortalGenInfo info)
	{
		info.generatePlaceholderBlocks();
		BreakablePortalEntity[] portals = info.generateBiWayBiFacedPortal(NetherPortalEntity.entityType);

		if (Global.netherPortalOverlay)
		{
			IntrinsicNetherPortalForm.initializeOverlay(portals[0], info.fromShape);
			IntrinsicNetherPortalForm.initializeOverlay(portals[1], info.fromShape);
			IntrinsicNetherPortalForm.initializeOverlay(portals[2], info.toShape);
			IntrinsicNetherPortalForm.initializeOverlay(portals[3], info.toShape);
		}

		return portals;
	}

	@Override
	public Predicate<BlockState> getOtherSideFramePredicate()
	{
		return O_O::isObsidian;
	}

	@Override
	public Predicate<BlockState> getThisSideFramePredicate()
	{
		return O_O::isObsidian;
	}

	@Override
	public Predicate<BlockState> getAreaPredicate()
	{
		return AbstractBlock.AbstractBlockState::isAir;
	}

	@Override
	public Codec<? extends PortalGenForm> getCodec()
	{
		throw new RuntimeException();
	}

	@Override
	public PortalGenForm getReverse()
	{
		return this;
	}
}
