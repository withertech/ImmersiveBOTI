package com.withertech.tim_wim_holes.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.withertech.tim_wim_holes.CGlobal;
import com.withertech.tim_wim_holes.ducks.IEMinecraftClient;
import com.withertech.tim_wim_holes.portal.Portal;
import com.withertech.tim_wim_holes.portal.PortalLike;
import com.withertech.tim_wim_holes.render.context_management.PortalRendering;
import net.minecraft.client.Minecraft;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public class RendererUsingFrameBuffer extends PortalRenderer
{
	SecondaryFrameBuffer secondaryFrameBuffer = new SecondaryFrameBuffer();

	@Override
	public void onBeforeTranslucentRendering(MatrixStack matrixStack)
	{
		renderPortals(matrixStack);
	}

	@Override
	public void onAfterTranslucentRendering(MatrixStack matrixStack)
	{

	}

	@Override
	public void onRenderCenterEnded(MatrixStack matrixStack)
	{

	}

	@Override
	public void finishRendering()
	{

	}

	@Override
	public void prepareRendering()
	{
		secondaryFrameBuffer.prepare();

		GlStateManager.enableDepthTest();

		GL11.glDisable(GL11.GL_STENCIL_TEST);

		if (CGlobal.shaderManager == null)
		{
			CGlobal.shaderManager = new ShaderManager();
		}
	}

	@Override
	protected void doRenderPortal(
			PortalLike portal,
			MatrixStack matrixStack
	)
	{
		if (PortalRendering.isRendering())
		{
			//only support one-layer portal
			return;
		}

		if (!testShouldRenderPortal(portal, matrixStack))
		{
			return;
		}

		PortalRendering.pushPortalLayer(portal);

		Framebuffer oldFrameBuffer = client.getFramebuffer();

		((IEMinecraftClient) client).setFrameBuffer(secondaryFrameBuffer.fb);
		secondaryFrameBuffer.fb.bindFramebuffer(true);

		GlStateManager.clearColor(1, 0, 1, 1);
		GlStateManager.clearDepth(1);
		GlStateManager.clear(
				GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT,
				Minecraft.IS_RUNNING_ON_MAC
		);
		GL11.glDisable(GL11.GL_STENCIL_TEST);

		renderPortalContent(portal);

		((IEMinecraftClient) client).setFrameBuffer(oldFrameBuffer);
		oldFrameBuffer.bindFramebuffer(true);

		PortalRendering.popPortalLayer();

		renderSecondBufferIntoMainBuffer(portal, matrixStack);
	}

	@Override
	public void renderPortalInEntityRenderer(Portal portal)
	{
		//nothing
	}

	@Override
	public boolean replaceFrameBufferClearing()
	{
		return false;
	}

	private boolean testShouldRenderPortal(
			PortalLike portal,
			MatrixStack matrixStack
	)
	{
		return QueryManager.renderAndGetDoesAnySamplePass(() ->
		{
			GlStateManager.enableDepthTest();
			GlStateManager.depthMask(false);
			GL20.glUseProgram(0);
			ViewAreaRenderer.drawPortalViewTriangle(portal, matrixStack, true, true);
			GlStateManager.depthMask(true);
		});
	}

	private void renderSecondBufferIntoMainBuffer(PortalLike portal, MatrixStack matrixStack)
	{
		MyRenderHelper.drawFrameBufferUp(
				portal,
				secondaryFrameBuffer.fb,
				matrixStack
		);
	}

}
