package com.withertech.hiding_in_the_bushes.mixin.client;

import com.withertech.forge_model_data_fix.ForgeModelDataManagerPerWorld;
import com.withertech.hiding_in_the_bushes.ModMainForge;
import com.withertech.tim_wim_holes.Helper;
import com.withertech.tim_wim_holes.ModMain;
import com.withertech.tim_wim_holes.chunk_loading.MyClientChunkManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelDataManager;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.event.world.ChunkEvent;
import org.apache.commons.lang3.Validate;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(value = ModelDataManager.class, remap = false)
public class MixinForgeModelDataManager
{
	@Shadow
	private static WeakReference<World> currentWorld;
	@Shadow
	@Final
	private static Map<ChunkPos, Set<BlockPos>> needModelDataRefresh;
	@Shadow
	@Final
	private static Map<ChunkPos, Map<BlockPos, IModelData>> modelDataCache;


	private static final ConcurrentHashMap<RegistryKey<World>, ForgeModelDataManagerPerWorld>
			portal_modelDataManagerMap;

	static
	{
		portal_modelDataManagerMap = new ConcurrentHashMap<>();

		ModMain.clientTaskList.addTask(() ->
		{
			if (ModMainForge.enableModelDataFix)
			{
				ModMain.clientCleanupSignal.connect(MixinForgeModelDataManager::portal_cleanup);

				MyClientChunkManager.clientChunkUnloadSignal.connect((chunk) -> portal_getManager(chunk.getWorld()).onChunkUnload(chunk));

				Helper.info("IP Forge Model Data Fix Initialized");
			} else
			{
				Helper.info("IP Forge Model Data Fix is Disabled");
			}

			return true;
		});

	}

	private static void portal_cleanup()
	{
		portal_modelDataManagerMap.clear();
	}

	private static ForgeModelDataManagerPerWorld portal_getManager(World world)
	{
		RegistryKey<World> key = world.getDimensionKey();
		return portal_modelDataManagerMap.computeIfAbsent(key, k -> new ForgeModelDataManagerPerWorld());
	}

	/**
	 * @author qouteall
	 * @reason null
	 */
	@Overwrite
	private static void cleanCaches(World world)
	{
		if (!ModMainForge.enableModelDataFix)
		{
			if (world != currentWorld.get())
			{
				currentWorld = new WeakReference<>(world);
				needModelDataRefresh.clear();
				modelDataCache.clear();
			}
		}
	}

	@Inject(method = "requestModelDataRefresh", at = @At("HEAD"), cancellable = true)
	private static void onRequestModelDataRefresh(TileEntity te, CallbackInfo ci)
	{
		if (!ModMainForge.enableModelDataFix)
		{
			return;
		}

		Validate.notNull(te);
		portal_getManager(Objects.requireNonNull(te.getWorld())).requestModelDataRefresh(te);

		ci.cancel();
	}

	@Inject(method = "refreshModelData", at = @At("HEAD"), cancellable = true)
	private static void onRefreshModelData(World world, ChunkPos chunk, CallbackInfo ci)
	{
		if (!ModMainForge.enableModelDataFix)
		{
			return;
		}

		ci.cancel();
	}

	@Inject(method = "onChunkUnload", at = @At("HEAD"), cancellable = true)
	private static void onOnChunkUnload(ChunkEvent.Unload event, CallbackInfo ci)
	{
		if (!ModMainForge.enableModelDataFix)
		{
			return;
		}

		ci.cancel();
	}

	@Inject(
			method = "getModelData(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraftforge/client/model/data/IModelData;",
			at = @At("HEAD"),
			cancellable = true
	)
	private static void onGetModelData1(World world, BlockPos pos, CallbackInfoReturnable<IModelData> cir)
	{
		if (!ModMainForge.enableModelDataFix)
		{
			return;
		}

		cir.setReturnValue(portal_getManager(world).getModelData(world, pos));
	}

	@Inject(
			method = "getModelData(Lnet/minecraft/world/World;Lnet/minecraft/util/math/ChunkPos;)Ljava/util/Map;",
			at = @At("HEAD"),
			cancellable = true
	)
	private static void onGetModelData2(World world, ChunkPos pos, CallbackInfoReturnable<Map<BlockPos, IModelData>> cir)
	{
		if (!ModMainForge.enableModelDataFix)
		{
			return;
		}

		cir.setReturnValue(portal_getManager(world).getModelData(world, pos));
	}
}
