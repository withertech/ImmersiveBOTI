package com.withertech.tim_wim_holes.mixin.common.chunk_sync;

import com.withertech.hiding_in_the_bushes.MyNetwork;
import com.withertech.tim_wim_holes.chunk_loading.NewChunkTrackingGraph;
import com.withertech.tim_wim_holes.ducks.IEChunkHolder;
import com.withertech.tim_wim_holes.ducks.IEThreadedAnvilChunkStorage;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ChunkHolder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.Consumer;

@Mixin(ChunkHolder.class)
public class MixinChunkHolder implements IEChunkHolder
{

	@Shadow
	@Final
	private ChunkPos pos;

	@Shadow
	@Final
	private ChunkHolder.IPlayerProvider playerProvider;

	/**
	 * @author qouteall
	 */
	@Overwrite
	private void sendToTracking(IPacket<?> packet_1, boolean onlyOnRenderDistanceEdge)
	{
		RegistryKey<World> dimension =
				((IEThreadedAnvilChunkStorage) playerProvider).getWorld().getDimensionKey();

		Consumer<ServerPlayerEntity> func = player ->
				player.connection.sendPacket(
						MyNetwork.createRedirectedMessage(
								dimension, packet_1
						)
				);

		if (onlyOnRenderDistanceEdge)
		{
			NewChunkTrackingGraph.getFarWatchers(
					dimension, pos.x, pos.z
			).forEach(func);
		} else
		{
			NewChunkTrackingGraph.getPlayersViewingChunk(
					dimension, pos.x, pos.z
			).forEach(func);
		}

	}

}
