package com.withertech.imm_boti.mixin.client;

import com.withertech.imm_boti.Helper;
import com.withertech.imm_boti.ducks.IEEntity;
import com.withertech.imm_boti.portal.Portal;
import com.withertech.imm_boti.render.QueryManager;
import com.withertech.imm_boti.render.context_management.RenderStates;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.overlay.DebugOverlayGui;

@Mixin(DebugOverlayGui.class)
public class MixinDebugHud {
    @Inject(method = "Lnet/minecraft/client/gui/overlay/DebugOverlayGui;getDebugInfoRight()Ljava/util/List;", at = @At("RETURN"), cancellable = true)
    private void onGetRightText(CallbackInfoReturnable<List<String>> cir) {
        List<String> returnValue = cir.getReturnValue();
        returnValue.add("Rendered Portals: " + RenderStates.lastPortalRenderInfos.size());
        
        ClientPlayerEntity player = Minecraft.getInstance().player;
        if (player != null) {
            Portal collidingPortal = ((IEEntity) player).getCollidingPortal();
            if (collidingPortal != null) {
                String text = "Colliding " + collidingPortal.toString();
                returnValue.addAll(Helper.splitStringByLen(text, 50));
            }
        }
        
        returnValue.add("Occlusion Query Stall: " + QueryManager.queryStallCounter);
        
        if (RenderStates.debugText != null && !RenderStates.debugText.isEmpty()) {
            returnValue.add("Debug: " + RenderStates.debugText);
        }
    }
}
