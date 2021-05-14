package com.withertech.tim_wim_holes.mixin.common.portal_generation;

import net.minecraft.server.management.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public class MixinPlayerManager_P
{
	@Inject(
			method = "Lnet/minecraft/server/management/PlayerList;reloadResources()V",
			at = @At("RETURN")
	)
	private void onOnDatapackReloaded(CallbackInfo ci)
	{
//        CustomPortalGenManagement.onDatapackReload();
	}
}
