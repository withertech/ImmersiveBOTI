package com.withertech.tim_wim_holes.mixin.client.gui_portal;

import net.minecraft.client.MainWindow;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MainWindow.class)
public class MixinWindow
{
//    @Inject(method = "getFramebufferWidth", at = @At("HEAD"), cancellable = true)
//    private void onGetFramebufferWidth(CallbackInfoReturnable<Integer> cir) {
//        Framebuffer guiPortalRenderingFb = GuiPortalRendering.getRenderingFrameBuffer();
//        if (guiPortalRenderingFb != null) {
//            cir.setReturnValue(guiPortalRenderingFb.viewportWidth);
//        }
//    }
//
//    @Inject(method = "getFramebufferHeight", at = @At("HEAD"), cancellable = true)
//    private void onGetFramebufferHeight(CallbackInfoReturnable<Integer> cir) {
//        Framebuffer guiPortalRenderingFb = GuiPortalRendering.getRenderingFrameBuffer();
//        if (guiPortalRenderingFb != null) {
//            cir.setReturnValue(guiPortalRenderingFb.viewportHeight);
//        }
//    }
}
