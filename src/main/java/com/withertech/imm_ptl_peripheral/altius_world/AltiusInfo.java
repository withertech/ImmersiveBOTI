package com.withertech.imm_ptl_peripheral.altius_world;

import com.withertech.tim_wim_holes.Helper;
import com.withertech.tim_wim_holes.McHelper;
import com.withertech.tim_wim_holes.api.PortalAPI;
import com.withertech.tim_wim_holes.portal.Portal;
import com.withertech.tim_wim_holes.portal.global_portals.VerticalConnectingPortal;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.server.ServerWorld;

import java.util.List;

public class AltiusInfo
{

	public final boolean loop;
	public final List<AltiusEntry> entries;

	public AltiusInfo(List<AltiusEntry> entries, boolean loop)
	{
		this.entries = entries;
		this.loop = loop;
	}

	public static void initializeFuseViewProperty(Portal portal)
	{
		if (portal.world.getDimensionType().hasSkyLight())
		{
			if (portal.getNormal().y < 0)
			{
				portal.fuseView = true;
			}
		}
	}

	public static void createConnectionBetween(
			AltiusEntry a, AltiusEntry b
	)
	{
		ServerWorld fromWorld = McHelper.getServerWorld(a.dimension);

		ServerWorld toWorld = McHelper.getServerWorld(b.dimension);

		boolean xorFlipped = a.flipped ^ b.flipped;

		VerticalConnectingPortal connectingPortal = VerticalConnectingPortal.createConnectingPortal(
				fromWorld,
				a.flipped ? VerticalConnectingPortal.ConnectorType.ceil :
						VerticalConnectingPortal.ConnectorType.floor,
				toWorld,
				b.scale / a.scale,
				xorFlipped,
				b.horizontalRotation - a.horizontalRotation
		);

		VerticalConnectingPortal reverse = PortalAPI.createReversePortal(connectingPortal);

		initializeFuseViewProperty(connectingPortal);
		initializeFuseViewProperty(reverse);

		PortalAPI.addGlobalPortal(fromWorld, connectingPortal);
		PortalAPI.addGlobalPortal(toWorld, reverse);
	}

	public static void replaceBedrock(ServerWorld world, IChunk chunk)
	{
		if (AltiusGameRule.getIsDimensionStack())
		{
			BlockPos.Mutable mutable = new BlockPos.Mutable();
			for (int x = 0; x < 16; x++)
			{
				for (int z = 0; z < 16; z++)
				{
					for (int y = 0; y < chunk.getHeight(); y++)
					{
						mutable.setPos(x, y, z);
						BlockState blockState = chunk.getBlockState(mutable);
						if (blockState.getBlock() == Blocks.BEDROCK)
						{
							chunk.setBlockState(
									mutable,
									Blocks.OBSIDIAN.getDefaultState(),
									false
							);
						}
					}
				}
			}
		}
	}

	public void createPortals()
	{

		if (entries.isEmpty())
		{
			McHelper.sendMessageToFirstLoggedPlayer(new StringTextComponent(
					"Error: No dimension for dimension stack"
			));
			return;
		}

		if (!McHelper.getGlobalPortals(McHelper.getServerWorld(entries.get(0).dimension)).isEmpty())
		{
			Helper.error("There are already global portals when initializing dimension stack");
			return;
		}

		Helper.wrapAdjacentAndMap(
				entries.stream(),
				(before, after) ->
				{
					createConnectionBetween(before, after);
					return null;
				}
		).forEach(k ->
		{
		});

		if (loop)
		{
			createConnectionBetween(entries.get(entries.size() - 1), entries.get(0));
		}

		McHelper.sendMessageToFirstLoggedPlayer(
				new TranslationTextComponent("imm_ptl.dim_stack_initialized")
		);
	}

}
