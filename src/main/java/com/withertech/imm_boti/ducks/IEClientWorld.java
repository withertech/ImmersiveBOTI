package com.withertech.imm_boti.ducks;

import com.withertech.imm_boti.portal.Portal;
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
