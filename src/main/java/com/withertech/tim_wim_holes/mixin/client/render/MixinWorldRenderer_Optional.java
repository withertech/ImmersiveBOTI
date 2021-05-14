package com.withertech.tim_wim_holes.mixin.client.render;

import com.withertech.tim_wim_holes.CGlobal;
import com.withertech.tim_wim_holes.render.context_management.PortalRendering;
import com.withertech.tim_wim_holes.render.context_management.RenderStates;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ViewFrustum;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.util.math.vector.Vector3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// avoid crashing with sodium
// [Needs Confirmation] higher priority means apply earlier, so it won't fail with the overwrite
@Mixin(value = WorldRenderer.class, priority = 1100)
public class MixinWorldRenderer_Optional
{
	@Shadow
	private ViewFrustum viewFrustum;

	@Shadow
	@Final
	private Minecraft mc;

	@Shadow
	private boolean displayListEntitiesDirty;

	//avoid translucent sort while rendering portal
	@Redirect(
			method = "Lnet/minecraft/client/renderer/WorldRenderer;renderBlockLayer(Lnet/minecraft/client/renderer/RenderType;Lcom/mojang/blaze3d/matrix/MatrixStack;DDD)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/renderer/RenderType;getTranslucent()Lnet/minecraft/client/renderer/RenderType;",
					ordinal = 0
			),
			require = 0
	)
	private RenderType redirectGetTranslucent()
	{
		if (PortalRendering.isRendering())
		{
			return null;
		}
		return RenderType.getTranslucent();
	}

	//update builtChunkStorage every frame
	//update terrain when rendering portal
	@Inject(
			method = "Lnet/minecraft/client/renderer/WorldRenderer;setupTerrain(Lnet/minecraft/client/renderer/ActiveRenderInfo;Lnet/minecraft/client/renderer/culling/ClippingHelper;ZIZ)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher;setRenderPosition(Lnet/minecraft/util/math/vector/Vector3d;)V"
			),
			require = 0
	)
	private void onBeforeChunkBuilderSetCameraPosition(
			ActiveRenderInfo camera_1,
			ClippingHelper frustum_1,
			boolean boolean_1,
			int int_1,
			boolean boolean_2,
			CallbackInfo ci
	)
	{
		if (CGlobal.useHackedChunkRenderDispatcher)
		{
			this.viewFrustum.updateChunkPositions(this.mc.player.getPosX(), this.mc.player.getPosZ());
		}

		if (PortalRendering.isRendering())
		{
			displayListEntitiesDirty = true;
		}
	}

	//rebuild less chunk in render thread while rendering portal to reduce lag spike
	//minecraft has two places rebuilding chunks in render thread
	//one in updateChunks() one in setupTerrain()
	@ModifyConstant(
			method = "Lnet/minecraft/client/renderer/WorldRenderer;setupTerrain(Lnet/minecraft/client/renderer/ActiveRenderInfo;Lnet/minecraft/client/renderer/culling/ClippingHelper;ZIZ)V",
			constant = @Constant(doubleValue = 768.0D),
			require = 0
	)
	private double modifyRebuildRange(double original)
	{
		if (PortalRendering.isRendering())
		{
			return 256.0;
		} else
		{
			return original;
		}
	}

	//the camera position is used for translucent sort
	//avoid messing it
	@Redirect(
			method = "Lnet/minecraft/client/renderer/WorldRenderer;setupTerrain(Lnet/minecraft/client/renderer/ActiveRenderInfo;Lnet/minecraft/client/renderer/culling/ClippingHelper;ZIZ)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher;setRenderPosition(Lnet/minecraft/util/math/vector/Vector3d;)V"
			),
			require = 0
	)
	private void onSetChunkBuilderCameraPosition(ChunkRenderDispatcher chunkBuilder, Vector3d cameraPosition)
	{
		if (PortalRendering.isRendering())
		{
			if (mc.world.getDimensionKey() == RenderStates.originalPlayerDimension)
			{
				return;
			}
		}
		chunkBuilder.setRenderPosition(cameraPosition);
	}
}
