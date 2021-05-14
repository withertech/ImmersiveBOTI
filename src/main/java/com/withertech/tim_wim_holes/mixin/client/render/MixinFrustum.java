package com.withertech.tim_wim_holes.mixin.client.render;

import com.withertech.tim_wim_holes.CGlobal;
import com.withertech.tim_wim_holes.ducks.IEFrustum;
import com.withertech.tim_wim_holes.render.FrustumCuller;
import net.minecraft.client.renderer.culling.ClippingHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClippingHelper.class)
public class MixinFrustum implements IEFrustum
{
	@Shadow
	private double cameraX;
	@Shadow
	private double cameraY;
	@Shadow
	private double cameraZ;

	private FrustumCuller portal_frustumCuller;

	@Inject(
			method = "Lnet/minecraft/client/renderer/culling/ClippingHelper;setCameraPosition(DDD)V",
			at = @At("TAIL")
	)
	private void onSetOrigin(double double_1, double double_2, double double_3, CallbackInfo ci)
	{
		if (portal_frustumCuller == null)
		{
			portal_frustumCuller = new FrustumCuller();
		}
		portal_frustumCuller.update(cameraX, cameraY, cameraZ);
	}

	@Inject(
			method = "Lnet/minecraft/client/renderer/culling/ClippingHelper;isBoxInFrustum(DDDDDD)Z",
			at = @At("HEAD"),
			cancellable = true
	)
	private void onIntersectionTest(
			double minX, double minY, double minZ, double maxX, double maxY, double maxZ,
			CallbackInfoReturnable<Boolean> cir
	)
	{
		if (CGlobal.doUseAdvancedFrustumCulling)
		{
			boolean canDetermineInvisible = portal_frustumCuller.canDetermineInvisible(
					minX - cameraX, minY - cameraY, minZ - cameraZ,
					maxX - cameraX, maxY - cameraY, maxZ - cameraZ
			);
			if (canDetermineInvisible)
			{
				cir.setReturnValue(false);
			}
		}
	}

	@Override
	public boolean canDetermineInvisible(double minX, double minY, double minZ, double maxX, double maxY, double maxZ)
	{
		return portal_frustumCuller.canDetermineInvisible(
				minX - cameraX, minY - cameraY, minZ - cameraZ,
				maxX - cameraX, maxY - cameraY, maxZ - cameraZ
		);
	}
}
