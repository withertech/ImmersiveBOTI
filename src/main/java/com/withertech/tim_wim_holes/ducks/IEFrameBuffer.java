package com.withertech.tim_wim_holes.ducks;

public interface IEFrameBuffer {
    boolean getIsStencilBufferEnabled();
    
    void setIsStencilBufferEnabledAndReload(boolean cond);
}
