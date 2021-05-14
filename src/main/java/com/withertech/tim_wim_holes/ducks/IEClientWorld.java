package com.withertech.tim_wim_holes.ducks;

import com.withertech.tim_wim_holes.portal.Portal;
import net.minecraft.client.network.play.ClientPlayNetHandler;

import javax.annotation.Nullable;
import java.util.List;

public interface IEClientWorld
{
	ClientPlayNetHandler getNetHandler();

	void setNetHandler(ClientPlayNetHandler handler);

	@Nullable
	List<Portal> getGlobalPortals();

	void setGlobalPortals(List<Portal> arg);
}
