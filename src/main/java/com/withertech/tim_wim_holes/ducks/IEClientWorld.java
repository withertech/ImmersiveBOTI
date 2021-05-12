package com.withertech.tim_wim_holes.ducks;

import com.withertech.tim_wim_holes.portal.Portal;
import javax.annotation.Nullable;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import java.util.List;

public interface IEClientWorld {
    ClientPlayNetHandler getNetHandler();
    
    void setNetHandler(ClientPlayNetHandler handler);
    
    @Nullable
    List<Portal> getGlobalPortals();
    
    void setGlobalPortals(List<Portal> arg);
}
