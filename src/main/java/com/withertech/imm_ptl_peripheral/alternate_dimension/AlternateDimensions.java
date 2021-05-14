package com.withertech.imm_ptl_peripheral.alternate_dimension;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.withertech.hiding_in_the_bushes.ModMainForge;
import com.withertech.tim_wim_holes.Global;
import com.withertech.tim_wim_holes.Helper;
import com.withertech.tim_wim_holes.McHelper;
import com.withertech.tim_wim_holes.ModMain;
import com.withertech.tim_wim_holes.api.IPDimensionAPI;
import com.withertech.tim_wim_holes.ducks.IEWorld;
import net.minecraft.block.Blocks;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.MutableRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraft.world.Dimension;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.provider.OverworldBiomeProvider;
import net.minecraft.world.gen.*;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.settings.DimensionGeneratorSettings;
import net.minecraft.world.gen.settings.DimensionStructuresSettings;
import net.minecraft.world.gen.settings.StructureSeparationSettings;
import net.minecraft.world.server.ServerWorld;

import java.util.HashMap;
import java.util.Optional;

public class AlternateDimensions
{
	public static final RegistryKey<Dimension> alternate1Option = RegistryKey.getOrCreateKey(
			Registry.DIMENSION_KEY,
			new ResourceLocation(ModMainForge.MODID + ":alternate1")
	);
	public static final RegistryKey<Dimension> alternate2Option = RegistryKey.getOrCreateKey(
			Registry.DIMENSION_KEY,
			new ResourceLocation(ModMainForge.MODID + ":alternate2")
	);
	public static final RegistryKey<Dimension> alternate3Option = RegistryKey.getOrCreateKey(
			Registry.DIMENSION_KEY,
			new ResourceLocation(ModMainForge.MODID + ":alternate3")
	);
	public static final RegistryKey<Dimension> alternate4Option = RegistryKey.getOrCreateKey(
			Registry.DIMENSION_KEY,
			new ResourceLocation(ModMainForge.MODID + ":alternate4")
	);
	public static final RegistryKey<Dimension> alternate5Option = RegistryKey.getOrCreateKey(
			Registry.DIMENSION_KEY,
			new ResourceLocation(ModMainForge.MODID + ":alternate5")
	);
	public static final RegistryKey<DimensionType> surfaceType = RegistryKey.getOrCreateKey(
			Registry.DIMENSION_TYPE_KEY,
			new ResourceLocation(ModMainForge.MODID + ":surface_type")
	);
	public static final RegistryKey<World> alternate1 = RegistryKey.getOrCreateKey(
			Registry.WORLD_KEY,
			new ResourceLocation(ModMainForge.MODID + ":alternate1")
	);
	public static final RegistryKey<World> alternate2 = RegistryKey.getOrCreateKey(
			Registry.WORLD_KEY,
			new ResourceLocation(ModMainForge.MODID + ":alternate2")
	);
	public static final RegistryKey<World> alternate3 = RegistryKey.getOrCreateKey(
			Registry.WORLD_KEY,
			new ResourceLocation(ModMainForge.MODID + ":alternate3")
	);
	public static final RegistryKey<World> alternate4 = RegistryKey.getOrCreateKey(
			Registry.WORLD_KEY,
			new ResourceLocation(ModMainForge.MODID + ":alternate4")
	);
	public static final RegistryKey<World> alternate5 = RegistryKey.getOrCreateKey(
			Registry.WORLD_KEY,
			new ResourceLocation(ModMainForge.MODID + ":alternate5")
	);

	public static void init()
	{
		IPDimensionAPI.onServerWorldInit.connect(AlternateDimensions::initializeAlternateDimensions);

		ModMain.postServerTickSignal.connect(AlternateDimensions::tick);
	}

	private static void initializeAlternateDimensions(
			DimensionGeneratorSettings generatorOptions, DynamicRegistries registryManager
	)
	{
		SimpleRegistry<Dimension> registry = generatorOptions.func_236224_e_();
		long seed = generatorOptions.getSeed();
		if (!Global.enableAlternateDimensions)
		{
			return;
		}

		DimensionType surfaceTypeObject = registryManager.getRegistry(Registry.DIMENSION_TYPE_KEY).getOrDefault(new ResourceLocation(ModMainForge.MODID + ":surface_type"));

		if (surfaceTypeObject == null)
		{
			Helper.error("Missing dimension type immersive_portals:surface_type");
			return;
		}

		//different seed
		IPDimensionAPI.addDimension(
				seed,
				registry,
				alternate1Option.getLocation(),
				() -> surfaceTypeObject,
				createSkylandGenerator(seed + 1, registryManager)
		);
		IPDimensionAPI.markDimensionNonPersistent(alternate1Option.getLocation());

		IPDimensionAPI.addDimension(
				seed,
				registry,
				alternate2Option.getLocation(),
				() -> surfaceTypeObject,
				createSkylandGenerator(seed, registryManager)
		);
		IPDimensionAPI.markDimensionNonPersistent(alternate2Option.getLocation());

		//different seed
		IPDimensionAPI.addDimension(
				seed,
				registry,
				alternate3Option.getLocation(),
				() -> surfaceTypeObject,
				createErrorTerrainGenerator(seed + 1, registryManager)
		);
		IPDimensionAPI.markDimensionNonPersistent(alternate3Option.getLocation());

		IPDimensionAPI.addDimension(
				seed,
				registry,
				alternate4Option.getLocation(),
				() -> surfaceTypeObject,
				createErrorTerrainGenerator(seed, registryManager)
		);
		IPDimensionAPI.markDimensionNonPersistent(alternate4Option.getLocation());

		IPDimensionAPI.addDimension(
				seed,
				registry,
				alternate5Option.getLocation(),
				() -> surfaceTypeObject,
				createVoidGenerator(registryManager)
		);
		IPDimensionAPI.markDimensionNonPersistent(alternate5Option.getLocation());
	}
//    public static DimensionType surfaceTypeObject;

	public static boolean isAlternateDimension(World world)
	{
		final RegistryKey<World> key = world.getDimensionKey();
		return key == alternate1 ||
				key == alternate2 ||
				key == alternate3 ||
				key == alternate4 ||
				key == alternate5;
	}

	private static void syncWithOverworldTimeWeather(ServerWorld world, ServerWorld overworld)
	{
		((IEWorld) world).portal_setWeather(
				overworld.getRainStrength(1), overworld.getRainStrength(1),
				overworld.getThunderStrength(1), overworld.getThunderStrength(1)
		);
	}

	public static ChunkGenerator createSkylandGenerator(long seed, DynamicRegistries rm)
	{

		MutableRegistry<Biome> biomeRegistry = rm.getRegistry(Registry.BIOME_KEY);
		OverworldBiomeProvider biomeSource = new OverworldBiomeProvider(
				seed, false, false, biomeRegistry
		);

		MutableRegistry<DimensionSettings> settingsRegistry = rm.getRegistry(Registry.NOISE_SETTINGS_KEY);

		HashMap<Structure<?>, StructureSeparationSettings> structureMap = new HashMap<>();
		structureMap.putAll(DimensionStructuresSettings.field_236191_b_);
		structureMap.remove(Structure.MINESHAFT);
		structureMap.remove(Structure.STRONGHOLD);

		DimensionStructuresSettings structuresConfig = new DimensionStructuresSettings(
				Optional.empty(), structureMap
		);
		DimensionSettings skylandSetting = DimensionSettings.func_242742_a(
				structuresConfig, Blocks.STONE.getDefaultState(),
				Blocks.WATER.getDefaultState(), new ResourceLocation("imm_ptl:skyland_gen_id"),
				false, false
		);

		return new NoiseChunkGenerator(
				biomeSource, seed, () -> skylandSetting
		);
	}

	public static ChunkGenerator createErrorTerrainGenerator(long seed, DynamicRegistries rm)
	{
		MutableRegistry<Biome> biomeRegistry = rm.getRegistry(Registry.BIOME_KEY);

		ChaosBiomeSource chaosBiomeSource = new ChaosBiomeSource(seed, biomeRegistry);
		return new ErrorTerrainGenerator(seed, chaosBiomeSource);
	}

	public static ChunkGenerator createVoidGenerator(DynamicRegistries rm)
	{
		MutableRegistry<Biome> biomeRegistry = rm.getRegistry(Registry.BIOME_KEY);

		DimensionStructuresSettings structuresConfig = new DimensionStructuresSettings(
				Optional.of(DimensionStructuresSettings.field_236192_c_),
				Maps.newHashMap(ImmutableMap.of())
		);
		FlatGenerationSettings flatChunkGeneratorConfig =
				new FlatGenerationSettings(structuresConfig, biomeRegistry);
		flatChunkGeneratorConfig.getFlatLayers().add(new FlatLayerInfo(1, Blocks.AIR));
		flatChunkGeneratorConfig.updateLayers();

		return new FlatChunkGenerator(flatChunkGeneratorConfig);
	}


	private static void tick()
	{
		if (!Global.enableAlternateDimensions)
		{
			return;
		}

		ServerWorld overworld = McHelper.getServerWorld(World.OVERWORLD);

		syncWithOverworldTimeWeather(McHelper.getServerWorld(alternate1), overworld);
		syncWithOverworldTimeWeather(McHelper.getServerWorld(alternate2), overworld);
		syncWithOverworldTimeWeather(McHelper.getServerWorld(alternate3), overworld);
		syncWithOverworldTimeWeather(McHelper.getServerWorld(alternate4), overworld);
		syncWithOverworldTimeWeather(McHelper.getServerWorld(alternate5), overworld);
	}


}
