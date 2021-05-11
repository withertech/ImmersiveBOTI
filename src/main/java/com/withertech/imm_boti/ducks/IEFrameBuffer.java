package com.withertech.imm_boti.ducks;

public interface IEFrameBuffer {
    boolean getIsStencilBufferEnabled();
    
    void setIsStencilBufferEnabledAndReload(boolean cond);
}
