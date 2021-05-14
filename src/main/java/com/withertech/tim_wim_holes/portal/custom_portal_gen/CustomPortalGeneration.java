package com.withertech.tim_wim_holes.portal.custom_portal_gen;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.ListCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.withertech.tim_wim_holes.Helper;
import com.withertech.tim_wim_holes.McHelper;
import com.withertech.tim_wim_holes.portal.Portal;
import com.withertech.tim_wim_holes.portal.custom_portal_gen.form.PortalGenForm;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.Entity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class CustomPortalGeneration
{
	public static final RegistryKey<World> theSameDimension = RegistryKey.getOrCreateKey(
			Registry.WORLD_KEY,
			new ResourceLocation("imm_ptl:the_same_dimension")
	);

	public static final RegistryKey<World> anyDimension = RegistryKey.getOrCreateKey(
			Registry.WORLD_KEY,
			new ResourceLocation("imm_ptl:any_dimension")
	);

	public static final Codec<List<RegistryKey<World>>> dimensionListCodec =
			new ListCodec<>(World.CODEC);
	public static final Codec<List<String>> stringListCodec =
			new ListCodec<>(Codec.STRING);
	public static final Codec<CustomPortalGeneration> codecV1 = RecordCodecBuilder.create(instance -> instance.group(
			dimensionListCodec.fieldOf("from").forGetter(o -> o.fromDimensions),
			World.CODEC.fieldOf("to").forGetter(o -> o.toDimension),
			Codec.INT.optionalFieldOf("space_ratio_from", 1).forGetter(o -> o.spaceRatioFrom),
			Codec.INT.optionalFieldOf("space_ratio_to", 1).forGetter(o -> o.spaceRatioTo),
			Codec.BOOL.optionalFieldOf("reversible", true).forGetter(o -> o.reversible),
			PortalGenForm.codec.fieldOf("form").forGetter(o -> o.form),
			PortalGenTrigger.triggerCodec.fieldOf("trigger").forGetter(o -> o.trigger),
			stringListCodec.optionalFieldOf("post_invoke_commands", Collections.emptyList())
					.forGetter(o -> o.postInvokeCommands)
	).apply(instance, instance.stable(CustomPortalGeneration::new)));
	public static RegistryKey<Registry<Codec<CustomPortalGeneration>>> schemaRegistryKey = RegistryKey.getOrCreateRootKey(
			new ResourceLocation("imm_ptl:custom_portal_gen_schema")
	);
	public static RegistryKey<Registry<CustomPortalGeneration>> registryRegistryKey =
			RegistryKey.getOrCreateRootKey(new ResourceLocation("imm_ptl:custom_portal_generation"));
	public static SimpleRegistry<Codec<CustomPortalGeneration>> schemaRegistry = Util.make(() ->
	{
		SimpleRegistry<Codec<CustomPortalGeneration>> registry = new SimpleRegistry<>(
				schemaRegistryKey, Lifecycle.stable()
		);
		Registry.register(
				registry, new ResourceLocation("imm_ptl:v1"), codecV1
		);
		return registry;
	});

	public static final MapCodec<CustomPortalGeneration> codec = schemaRegistry.dispatchMap(
			"schema_version", e -> codecV1, Function.identity()
	);


	public final List<RegistryKey<World>> fromDimensions;
	public final RegistryKey<World> toDimension;
	public final int spaceRatioFrom;
	public final int spaceRatioTo;
	public final boolean reversible;
	public final PortalGenForm form;
	public final PortalGenTrigger trigger;
	public final List<String> postInvokeCommands;

	public ResourceLocation identifier = null;

	public CustomPortalGeneration(
			List<RegistryKey<World>> fromDimensions, RegistryKey<World> toDimension,
			int spaceRatioFrom, int spaceRatioTo, boolean reversible,
			PortalGenForm form, PortalGenTrigger trigger,
			List<String> postInvokeCommands
	)
	{
		this.fromDimensions = fromDimensions;
		this.toDimension = toDimension;
		this.spaceRatioFrom = spaceRatioFrom;
		this.spaceRatioTo = spaceRatioTo;
		this.reversible = reversible;
		this.form = form;
		this.trigger = trigger;
		this.postInvokeCommands = postInvokeCommands;
	}

	@Nullable
	public CustomPortalGeneration getReverse()
	{
		if (toDimension == theSameDimension)
		{
			return new CustomPortalGeneration(
					fromDimensions,
					theSameDimension,
					spaceRatioTo,
					spaceRatioFrom,
					false,
					form.getReverse(),
					trigger,
					postInvokeCommands
			);
		}

		if (!fromDimensions.isEmpty())
		{
			return new CustomPortalGeneration(
					Lists.newArrayList(toDimension),
					fromDimensions.get(0),
					spaceRatioTo,
					spaceRatioFrom,
					false,
					form.getReverse(),
					trigger,
					postInvokeCommands
			);
		}

		Helper.error("Cannot get reverse custom portal gen");
		return null;
	}

	public BlockPos mapPosition(BlockPos from)
	{
		return Helper.divide(Helper.scale(from, spaceRatioTo), spaceRatioFrom);
	}

	public boolean initAndCheck()
	{
		// if from dimension is not present, nothing happens

		RegistryKey<World> toDimension = this.toDimension;
		if (toDimension != theSameDimension)
		{
			if (McHelper.getServer().getWorld(toDimension) == null)
			{
				return false;
			}
		}

		return form.initAndCheck() && !fromDimensions.isEmpty();

	}

	@Override
	public String toString()
	{
		return McHelper.serializeToJson(
				this,
				codec.codec()
		);
	}

	public boolean perform(
			ServerWorld world,
			BlockPos startPos,
			@Nullable Entity triggeringEntity
	)
	{
		if (!fromDimensions.contains(world.getDimensionKey()))
		{
			if (fromDimensions.get(0) != anyDimension)
			{
				return false;
			}
		}

		//noinspection deprecation
		if (!world.isBlockLoaded(startPos))
		{
			Helper.info("Skip custom portal generation because chunk not loaded");
			return false;
		}

		RegistryKey<World> destDimension = this.toDimension;

		if (destDimension == theSameDimension)
		{
			destDimension = world.getDimensionKey();
		}

		ServerWorld toWorld = McHelper.getServer().getWorld(destDimension);

		if (toWorld == null)
		{
			Helper.error("Missing dimension " + destDimension.getLocation());
			return false;
		}

		world.getProfiler().startSection("custom_portal_gen_perform");
		boolean result = form.perform(this, world, startPos, toWorld, triggeringEntity);
		world.getProfiler().endSection();
		return result;
	}

	public void onPortalGenerated(Portal portal)
	{
		if (identifier != null)
		{
			portal.portalTag = identifier.toString();
		}

		if (postInvokeCommands.isEmpty())
		{
			return;
		}

		CommandSource commandSource = portal.getCommandSource().withPermissionLevel(4).withFeedbackDisabled();
		Commands commandManager = McHelper.getServer().getCommandManager();

		for (String command : postInvokeCommands)
		{
			commandManager.handleCommand(commandSource, command);
		}
	}
}
