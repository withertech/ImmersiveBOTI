package com.withertech.tim_wim_holes.mixin.client;

import io.netty.channel.Channel;
import net.minecraft.network.NetworkManager;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetworkManager.class)
public class MixinClientConnection
{
	@Shadow
	private Channel channel;

	//avoid crashing by npe
	@Inject(method = "Lnet/minecraft/network/NetworkManager;closeChannel(Lnet/minecraft/util/text/ITextComponent;)V", at = @At("HEAD"), cancellable = true)
	private void onBeforeDisconnect(ITextComponent text_1, CallbackInfo ci)
	{
		if (channel == null)
		{
			ci.cancel();
		}
	}
}
