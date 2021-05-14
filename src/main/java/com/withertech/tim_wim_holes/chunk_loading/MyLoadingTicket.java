package com.withertech.tim_wim_holes.chunk_loading;

import com.withertech.tim_wim_holes.Global;
import com.withertech.tim_wim_holes.ducks.IEChunkTicketManager;
import com.withertech.tim_wim_holes.ducks.IEServerChunkManager;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import net.minecraft.util.SortedArraySet;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.server.Ticket;
import net.minecraft.world.server.TicketManager;
import net.minecraft.world.server.TicketType;

import java.util.Comparator;
import java.util.WeakHashMap;

public class MyLoadingTicket
{
	public static final TicketType<ChunkPos> portalLoadingTicketType =
			TicketType.create("imm_ptl", Comparator.comparingLong(ChunkPos::asLong));

	public static final TicketType<ChunkPos> temporalLoadingTicketType =
			TicketType.create(
					"imm_ptl_temportal",
					Comparator.comparingLong(ChunkPos::asLong),
					300//15 seconds
			);
	public static final WeakHashMap<ServerWorld, LongSortedSet>
			loadedChunkRecord = new WeakHashMap<>();

	private static TicketManager getTicketManager(ServerWorld world)
	{
		return ((IEServerChunkManager) world.getChunkProvider()).getTicketManager();
	}

	private static boolean hasOtherChunkTicket(ServerWorld world, ChunkPos chunkPos)
	{
		SortedArraySet<Ticket<?>> chunkTickets =
				((IEChunkTicketManager) getTicketManager(world))
						.portal_getTicketSet(chunkPos.asLong());
		return chunkTickets.stream().anyMatch(t -> t.getType() != portalLoadingTicketType);
	}

	public static void addTicketIfNotLoaded(ServerWorld world, ChunkPos chunkPos)
	{
		boolean isNewlyAdded = getRecord(world).add(chunkPos.asLong());
		if (isNewlyAdded)
		{
			getTicketManager(world).register(
					portalLoadingTicketType, chunkPos, getLoadingRadius(), chunkPos
			);
		}
	}

	public static void removeTicket(ServerWorld world, ChunkPos chunkPos)
	{
		boolean isNewlyRemoved = getRecord(world).remove(chunkPos.asLong());

		if (isNewlyRemoved)
		{
			getTicketManager(world).release(
					portalLoadingTicketType, chunkPos, getLoadingRadius(), chunkPos
			);
		}
	}

	public static int getLoadingRadius()
	{
		if (Global.activeLoading)
		{
			return 2;
		} else
		{
			return 1;
		}
	}

	public static LongSortedSet getRecord(ServerWorld world)
	{
		return loadedChunkRecord.computeIfAbsent(
				world, k -> new LongLinkedOpenHashSet()
		);
	}

	public static void loadTemporally(ServerWorld world, ChunkPos chunkPos)
	{
		getTicketManager(world).release(
				temporalLoadingTicketType, chunkPos, 2, chunkPos
		);
	}

	public static void loadTemporally(ServerWorld world, ChunkPos centerChunkPos, int radius)
	{
		for (int dx = -radius; dx <= radius; dx++)
		{
			for (int dz = -radius; dz <= radius; dz++)
			{
				loadTemporally(
						world,
						new ChunkPos(centerChunkPos.x + dx, centerChunkPos.z + dz)
				);
			}
		}
	}
}
