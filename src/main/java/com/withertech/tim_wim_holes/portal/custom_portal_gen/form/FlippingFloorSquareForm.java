package com.withertech.tim_wim_holes.portal.custom_portal_gen.form;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.ListCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.withertech.tim_wim_holes.McHelper;
import com.withertech.tim_wim_holes.my_util.IntBox;
import com.withertech.tim_wim_holes.portal.PortalExtension;
import com.withertech.tim_wim_holes.portal.PortalManipulation;
import com.withertech.tim_wim_holes.portal.PortalPlaceholderBlock;
import com.withertech.tim_wim_holes.portal.custom_portal_gen.CustomPortalGeneration;
import com.withertech.tim_wim_holes.portal.custom_portal_gen.SimpleBlockPredicate;
import com.withertech.tim_wim_holes.portal.nether_portal.BlockPortalShape;
import com.withertech.tim_wim_holes.portal.nether_portal.GeneralBreakablePortal;
import com.withertech.tim_wim_holes.portal.nether_portal.NetherPortalGeneration;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.function.Predicate;
import java.util.stream.IntStream;

public class FlippingFloorSquareForm extends PortalGenForm
{

	public static final ListCodec<Block> blockListCodec = new ListCodec<>(Registry.BLOCK);

	public static final Codec<FlippingFloorSquareForm> codec = RecordCodecBuilder.create(instance ->
	{
		return instance.group(
				Codec.INT.fieldOf("length").forGetter(o -> o.length),

				SimpleBlockPredicate.codec.fieldOf("frame_block").forGetter(o -> o.frameBlock),
				SimpleBlockPredicate.codec.fieldOf("area_block").forGetter(o -> o.areaBlock),
				SimpleBlockPredicate.codec.optionalFieldOf("up_frame_block", SimpleBlockPredicate.pass)
						.forGetter(o -> o.upFrameBlock),
				SimpleBlockPredicate.codec.optionalFieldOf("bottom_block", SimpleBlockPredicate.pass)
						.forGetter(o -> o.bottomBlock)

		).apply(instance, instance.stable(FlippingFloorSquareForm::new));
	});

	public final int length;
	public final SimpleBlockPredicate frameBlock;
	public final SimpleBlockPredicate areaBlock;
	public final SimpleBlockPredicate upFrameBlock;
	public final SimpleBlockPredicate bottomBlock;

	public FlippingFloorSquareForm(
			int length,
			SimpleBlockPredicate frameBlock, SimpleBlockPredicate areaBlock,
			SimpleBlockPredicate upFrameBlock, SimpleBlockPredicate bottomBlock
	)
	{
		this.length = length;
		this.frameBlock = frameBlock;
		this.areaBlock = areaBlock;
		this.upFrameBlock = upFrameBlock;
		this.bottomBlock = bottomBlock;
	}

	public static IntBox findPortalPlacement(ServerWorld toWorld, BlockPos areaSize, BlockPos toPos)
	{
		return IntStream.range(toPos.getX() - 8, toPos.getX() + 8).boxed()
				.flatMap(x -> IntStream.range(toPos.getZ() - 8, toPos.getZ() + 8).boxed()
						.flatMap(z -> IntStream.range(5, toWorld.func_234938_ad_() - 5).map(
								y -> toWorld.func_234938_ad_() - y
						).mapToObj(y -> new BlockPos(x, y, z)))
				)
				.map(blockPos -> IntBox.getBoxByBasePointAndSize(areaSize, blockPos))
				.filter(intBox -> intBox.stream().allMatch(
						pos ->
						{
							BlockState blockState = toWorld.getBlockState(pos);
							return !blockState.isOpaqueCube(toWorld, pos) &&
									blockState.getBlock() != PortalPlaceholderBlock.instance &&
									blockState.getFluidState().isEmpty();
						}
				))
				.filter(intBox -> intBox.getSurfaceLayer(Direction.DOWN)
						.getMoved(Direction.DOWN.getDirectionVec())
						.stream().allMatch(
								blockPos ->
								{
									BlockState blockState = toWorld.getBlockState(blockPos);
									return !blockState.isAir() &&
											blockState.getBlock() != PortalPlaceholderBlock.instance;
								}
						)
				)
				.findFirst().orElseGet(() -> IntBox.getBoxByBasePointAndSize(areaSize, toPos))
				.getMoved(Direction.DOWN.getDirectionVec());
	}

	public static GeneralBreakablePortal[] createPortals(
			ServerWorld fromWorld, ServerWorld toWorld,
			BlockPortalShape fromShape, BlockPortalShape toShape
	)
	{
		GeneralBreakablePortal pa = GeneralBreakablePortal.entityType.create(fromWorld);
		fromShape.initPortalPosAxisShape(pa, true);

		pa.setDestination(toShape.innerAreaBox.getCenterVec());
		pa.dimensionTo = toWorld.getDimensionKey();
		pa.rotation = new Quaternion(
				new Vector3f(1, 0, 0),
				180,
				true
		);

		GeneralBreakablePortal pb = PortalManipulation.createReversePortal(pa, GeneralBreakablePortal.entityType);

		pa.blockPortalShape = fromShape;
		pb.blockPortalShape = toShape;
		pa.reversePortalId = pb.getUniqueID();
		pb.reversePortalId = pa.getUniqueID();

		PortalExtension.get(pa).motionAffinity = 0.1;
		PortalExtension.get(pb).motionAffinity = 0.1;

		McHelper.spawnServerEntity(pa);
		McHelper.spawnServerEntity(pb);

		return new GeneralBreakablePortal[]{pa, pb};
	}

	@Override
	public Codec<? extends PortalGenForm> getCodec()
	{
		return codec;
	}

	@Override
	public PortalGenForm getReverse()
	{
		return this;
	}

	@Override
	public boolean perform(
			CustomPortalGeneration cpg,
			ServerWorld fromWorld, BlockPos startingPos,
			ServerWorld toWorld,
			@Nullable Entity triggeringEntity
	)
	{
		Predicate<BlockState> areaPredicate = areaBlock;
		Predicate<BlockState> framePredicate = frameBlock;
		Predicate<BlockState> bottomPredicate = bottomBlock;

		if (!areaPredicate.test(fromWorld.getBlockState(startingPos)))
		{
			return false;
		}

		if (!bottomPredicate.test(fromWorld.getBlockState(startingPos.down())))
		{
			return false;
		}

		BlockPortalShape fromShape = BlockPortalShape.findArea(
				startingPos,
				Direction.Axis.Y,
				blockPos -> areaPredicate.test(fromWorld.getBlockState(blockPos)),
				blockPos -> framePredicate.test(fromWorld.getBlockState(blockPos))
		);

		if (fromShape == null)
		{
			return false;
		}

		if (!checkFromShape(fromWorld, fromShape))
		{
			return false;
		}

		BlockPos areaSize = fromShape.innerAreaBox.getSize();

		BlockPos toPos = cpg.mapPosition(fromShape.innerAreaBox.l);

		IntBox placingBox = findPortalPlacement(toWorld, areaSize, toPos);

		BlockPos offset = placingBox.l.subtract(fromShape.innerAreaBox.l);
		BlockPortalShape toShape = fromShape.getShapeWithMovedAnchor(
				fromShape.anchor.add(offset)
		);

		// clone the frame into the destination
		fromShape.frameAreaWithoutCorner.forEach(fromWorldPos ->
		{
			BlockPos toWorldPos = fromWorldPos.add(offset);
			toWorld.setBlockState(toWorldPos, fromWorld.getBlockState(fromWorldPos));
			toWorld.setBlockState(toWorldPos.up(), fromWorld.getBlockState(fromWorldPos.up()));
		});
		NetherPortalGeneration.fillInPlaceHolderBlocks(fromWorld, fromShape);
		NetherPortalGeneration.fillInPlaceHolderBlocks(toWorld, toShape);

		GeneralBreakablePortal[] portals = createPortals(fromWorld, toWorld, fromShape, toShape);

		for (GeneralBreakablePortal portal : portals)
		{
			cpg.onPortalGenerated(portal);
		}

		return true;
	}

	public boolean checkFromShape(ServerWorld fromWorld, BlockPortalShape fromShape)
	{
		boolean areaSizeTest = BlockPortalShape.isSquareShape(fromShape, length);
		if (!areaSizeTest)
		{
			return false;
		}

		return fromShape.frameAreaWithoutCorner.stream().allMatch(
				blockPos -> (upFrameBlock).test(fromWorld.getBlockState(blockPos.up()))
		) && fromShape.area.stream().allMatch(
				blockPos -> (bottomBlock).test(fromWorld.getBlockState(blockPos.down()))
		);
	}

}
