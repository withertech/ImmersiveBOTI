package com.withertech.tim_wim_holes.api;

import com.mojang.serialization.Lifecycle;
import com.withertech.tim_wim_holes.Helper;
import com.withertech.tim_wim_holes.McHelper;
import com.withertech.tim_wim_holes.my_util.SignalBiArged;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraft.world.Dimension;
import net.minecraft.world.DimensionType;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.settings.DimensionGeneratorSettings;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class IPDimensionAPI
{
	public static final SignalBiArged<DimensionGeneratorSettings, DynamicRegistries> onServerWorldInit = new SignalBiArged<>();

	private static final Set<ResourceLocation> nonPersistentDimensions = new HashSet<>();

	public static void init()
	{
		onServerWorldInit.connect(IPDimensionAPI::addMissingVanillaDimensions);
	}

	public static void addDimension(
			long argSeed,
			SimpleRegistry<Dimension> dimensionOptionsRegistry,
			ResourceLocation dimensionId,
			Supplier<DimensionType> dimensionTypeSupplier,
			ChunkGenerator chunkGenerator
	)
	{
		if (!dimensionOptionsRegistry.keySet().contains(dimensionId))
		{
			dimensionOptionsRegistry.register(
					RegistryKey.getOrCreateKey(Registry.DIMENSION_KEY, dimensionId),
					new Dimension(
							dimensionTypeSupplier,
							chunkGenerator
					),
					Lifecycle.experimental()
			);
		}
	}

	public static void markDimensionNonPersistent(ResourceLocation dimensionId)
	{
		nonPersistentDimensions.add(dimensionId);
	}

	// This is not API
	// When DFU does not recognize a mod dimension (in level.dat) it will throw an error
	// then the nether and the end will be swallowed (https://github.com/TelepathicGrunt/Bumblezone-Fabric/issues/20)
	// to fix that, don't store the custom dimensions into level.dat
	public static SimpleRegistry<Dimension> getAdditionalDimensionsRemoved(
			SimpleRegistry<Dimension> registry
	)
	{
		if (nonPersistentDimensions.isEmpty())
		{
			return registry;
		}

		return McHelper.filterAndCopyRegistry(
				registry,
				(key, obj) ->
				{
					ResourceLocation identifier = key.getLocation();
					return !nonPersistentDimensions.contains(identifier);
				}
		);
	}

	// fix nether and end swallowed by DFU error
	private static void addMissingVanillaDimensions(DimensionGeneratorSettings generatorOptions, DynamicRegistries registryManager)
	{
		SimpleRegistry<Dimension> registry = generatorOptions.func_236224_e_();
		long seed = generatorOptions.getSeed();
		if (!registry.keySet().contains(Dimension.THE_NETHER.getLocation()))
		{
			Helper.error("Missing the nether. This may be caused by DFU. Trying to fix");

			IPDimensionAPI.addDimension(
					seed,
					registry,
					Dimension.THE_NETHER.getLocation(),
					() -> DimensionType.NETHER_TYPE,
					DimensionType.getNetherChunkGenerator(
							registryManager.getRegistry(Registry.BIOME_KEY),
							registryManager.getRegistry(Registry.NOISE_SETTINGS_KEY),
							seed
					)
			);
		}

		if (!registry.keySet().contains(Dimension.THE_END.getLocation()))
		{
			Helper.error("Missing the end. This may be caused by DFU. Trying to fix");
			IPDimensionAPI.addDimension(
					seed,
					registry,
					Dimension.THE_END.getLocation(),
					() -> DimensionType.END_TYPE,
					DimensionType.getEndChunkGenerator(
							registryManager.getRegistry(Registry.BIOME_KEY),
							registryManager.getRegistry(Registry.NOISE_SETTINGS_KEY),
							seed
					)
			);
		}
	}
}
