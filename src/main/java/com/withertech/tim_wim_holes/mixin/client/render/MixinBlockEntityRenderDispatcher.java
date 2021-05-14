package com.withertech.tim_wim_holes.mixin.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.withertech.tim_wim_holes.OFInterface;
import com.withertech.tim_wim_holes.portal.PortalLike;
import com.withertech.tim_wim_holes.render.context_management.PortalRendering;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.vector.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TileEntityRendererDispatcher.class)
public class MixinBlockEntityRenderDispatcher
{
	@Inject(
			method = "Lnet/minecraft/client/renderer/tileentity/TileEntityRendererDispatcher;renderTileEntity(Lnet/minecraft/tileentity/TileEntity;FLcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;)V",
			at = @At("HEAD"),
			cancellable = true
	)
	private <E extends TileEntity> void onRenderBlockEntity(
			E blockEntity,
			float tickDelta,
			MatrixStack matrix,
			IRenderTypeBuffer vertexConsumerProvider,
			CallbackInfo ci
	)
	{
		if (OFInterface.isShadowPass.getAsBoolean())
		{
			return;
		}
		if (PortalRendering.isRendering())
		{
			PortalLike renderingPortal = PortalRendering.getRenderingPortal();
			boolean canRender = renderingPortal.isInside(
					Vector3d.copyCentered(blockEntity.getPos()),
					-0.1
			);
			if (!canRender)
			{
				ci.cancel();
			}
		}
	}
}
