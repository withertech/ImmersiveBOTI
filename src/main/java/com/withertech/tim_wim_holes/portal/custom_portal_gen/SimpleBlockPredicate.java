package com.withertech.tim_wim_holes.portal.custom_portal_gen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import com.withertech.tim_wim_holes.McHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ITagCollectionSupplier;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;

import java.util.function.Predicate;

// it could be either specified by a block or a block tag
public class SimpleBlockPredicate implements Predicate<BlockState>
{
	public static final SimpleBlockPredicate pass = new SimpleBlockPredicate();
	public static final Codec<SimpleBlockPredicate> codec =
			Codec.STRING.comapFlatMap(
					SimpleBlockPredicate::deserialize,
					SimpleBlockPredicate::serialize
			);
	private final ITag<Block> tag;
	private final Block block;

	public SimpleBlockPredicate(ITag<Block> tag)
	{
		this.tag = tag;
		this.block = null;
	}

	public SimpleBlockPredicate(Block block)
	{
		this.block = block;
		this.tag = null;
	}

	private SimpleBlockPredicate()
	{
		this.tag = null;
		this.block = null;
	}

	private static DataResult<SimpleBlockPredicate> deserialize(String string)
	{
		MinecraftServer server = McHelper.getServer();

		if (server == null)
		{
			return DataResult.error(
					"[Immersive Portals] Simple block predicate should not be deserialized in client"
			);
		}

		ITagCollectionSupplier tagManager = server.resourceManager.func_244358_d();
		ResourceLocation id = new ResourceLocation(string);
		ITag<Block> blockTag = tagManager.getBlockTags().get(id);

		if (blockTag != null)
		{
			return DataResult.success(new SimpleBlockPredicate(blockTag), Lifecycle.stable());
		}

		if (Registry.BLOCK.keySet().contains(id))
		{
			Block block = Registry.BLOCK.getOrDefault(id);
			return DataResult.success(new SimpleBlockPredicate(block), Lifecycle.stable());
		}

		return DataResult.error("Unknown block or block tag:" + string);
	}

	private static String serialize(SimpleBlockPredicate predicate)
	{
		MinecraftServer server = McHelper.getServer();

		if (server == null)
		{
			throw new RuntimeException(
					"Simple block predicate should not be serialized in client"
			);
		}

		if (predicate.block != null)
		{
			return Registry.BLOCK.getKey(predicate.block).toString();
		}

		ITag<Block> tag = predicate.tag;

		ITagCollectionSupplier tagManager = server.resourceManager.func_244358_d();

		ResourceLocation id = tagManager.getBlockTags().getValidatedIdFromTag(tag);

		if (id == null)
		{
			throw new RuntimeException(
					"Cannot get id from tag " + tag
			);
		}

		return id.toString();
	}

	@Override
	public boolean test(BlockState blockState)
	{
		if (tag != null)
		{
			return blockState.isIn(tag);
		} else
		{
			if (block != null)
			{
				return blockState.getBlock() == block;
			} else
			{
				return true;
			}
		}
	}


}
