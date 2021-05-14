package com.withertech.tim_wim_holes.portal.custom_portal_gen.form;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.withertech.tim_wim_holes.portal.custom_portal_gen.SimpleBlockPredicate;
import com.withertech.tim_wim_holes.portal.nether_portal.BlockPortalShape;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

import java.util.function.Predicate;

public class HeterogeneousForm extends NetherPortalLikeForm
{

	public static final Codec<HeterogeneousForm> codec = RecordCodecBuilder.create(instance ->
	{
		return instance.group(
				Codec.BOOL.fieldOf("generate_frame_if_not_found").forGetter(o -> o.generateFrameIfNotFound),
				SimpleBlockPredicate.codec.fieldOf("area_block").forGetter(o -> o.areaBlock),
				SimpleBlockPredicate.codec.fieldOf("frame_block").forGetter(o -> o.frameBlock)
		).apply(instance, HeterogeneousForm::new);
	});
	public final SimpleBlockPredicate areaBlock;
	public final SimpleBlockPredicate frameBlock;

	public HeterogeneousForm(
			boolean generateFrameIfNotFound, SimpleBlockPredicate areaBlock, SimpleBlockPredicate frameBlock
	)
	{
		super(generateFrameIfNotFound);
		this.areaBlock = areaBlock;
		this.frameBlock = frameBlock;
	}

	@Override
	public void generateNewFrame(ServerWorld fromWorld, BlockPortalShape fromShape, ServerWorld toWorld, BlockPortalShape toShape)
	{
		//clone
		BlockPos offset = toShape.innerAreaBox.l.subtract(fromShape.innerAreaBox.l);
		fromShape.frameAreaWithoutCorner.forEach(blockPos ->
		{
			toWorld.setBlockState(
					blockPos.add(offset),
					fromWorld.getBlockState(blockPos)
			);
		});
	}

	@Override
	public Predicate<BlockState> getOtherSideFramePredicate()
	{
		return frameBlock;
	}

	@Override
	public Predicate<BlockState> getThisSideFramePredicate()
	{
		return frameBlock;
	}

	@Override
	public Predicate<BlockState> getAreaPredicate()
	{
		return areaBlock;
	}

	@Override
	public Codec<? extends PortalGenForm> getCodec()
	{
		return codec;
	}

	@Override
	public PortalGenForm getReverse()
	{
		return new HeterogeneousForm(
				generateFrameIfNotFound, areaBlock, frameBlock
		);
	}
}
