package com.withertech.tim_wim_holes.events;

import com.withertech.tim_wim_holes.ducks.IEExteriorTile;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tardis.api.events.TardisEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TardisEvents
{
//	@SubscribeEvent
//	public static void onTardisLand(TardisEvent.Land event)
//	{
//		event.getConsole().getOrFindExteriorTile().ifPresent(exteriorTile -> ((IEExteriorTile)exteriorTile).genPortals());
//	}

	@SubscribeEvent
	public static void onTardisTakeoff(TardisEvent.Takeoff event)
	{
		event.getConsole().getOrFindExteriorTile().ifPresent(exteriorTile -> ((IEExteriorTile) exteriorTile).clearPortals());
	}
}
