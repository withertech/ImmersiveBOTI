package com.withertech.tim_wim_holes.optifine_compatibility;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.withertech.tim_wim_holes.CGlobal;
import com.withertech.tim_wim_holes.CHelper;
import com.withertech.tim_wim_holes.OFInterface;
import com.withertech.tim_wim_holes.portal.Portal;
import com.withertech.tim_wim_holes.portal.PortalLike;
import com.withertech.tim_wim_holes.render.*;
import com.withertech.tim_wim_holes.render.context_management.PortalRendering;
import com.withertech.tim_wim_holes.render.context_management.RenderStates;
import com.withertech.tim_wim_holes.render.context_management.WorldRenderInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import static org.lwjgl.opengl.GL11.GL_NEAREST;

public class RendererDeferred extends PortalRenderer
{
	private final SecondaryFrameBuffer deferredBuffer = new SecondaryFrameBuffer();
	private final MatrixStack modelView = new MatrixStack();

	@Override
	public boolean replaceFrameBufferClearing()
	{
		return false;
	}

	@Override
	public void onBeforeTranslucentRendering(MatrixStack matrixStack)
	{
		if (PortalRendering.isRendering())
		{
			return;
		}
		modelView.push();
		modelView.getLast().getMatrix().mul(matrixStack.getLast().getMatrix());
		modelView.getLast().getNormal().mul(matrixStack.getLast().getNormal());
	}

	@Override
	public void onAfterTranslucentRendering(MatrixStack matrixStack)
	{

	}

	@Override
	public void finishRendering()
	{

	}

	@Override
	public void prepareRendering()
	{
		if (CGlobal.shaderManager == null)
		{
			CGlobal.shaderManager = new ShaderManager();
		}

		deferredBuffer.prepare();

		deferredBuffer.fb.setFramebufferColor(1, 0, 0, 0);
		deferredBuffer.fb.framebufferClear(Minecraft.IS_RUNNING_ON_MAC);

		OFGlobal.bindToShaderFrameBuffer.run();

	}

	@Override
	protected void doRenderPortal(PortalLike portal, MatrixStack matrixStack)
	{
		if (PortalRendering.isRendering())
		{
			//currently only support one-layer portal
			return;
		}


		if (!testShouldRenderPortal(portal, matrixStack))
		{
			return;
		}

		OFGlobal.bindToShaderFrameBuffer.run();

		PortalRendering.pushPortalLayer(portal);

		renderPortalContent(portal);

		PortalRendering.popPortalLayer();

		RenderSystem.enableDepthTest();

		GlStateManager.activeTexture(GL13.GL_TEXTURE0);

		client.gameRenderer.resetProjectionMatrix(RenderStates.projectionMatrix);

		OFInterface.resetViewport.run();

		deferredBuffer.fb.bindFramebuffer(true);
		MyRenderHelper.drawFrameBufferUp(
				portal,
				client.getFramebuffer(),
				matrixStack
		);

		RenderSystem.colorMask(true, true, true, true);

		OFGlobal.bindToShaderFrameBuffer.run();
	}

	@Override
	public void invokeWorldRendering(
			WorldRenderInfo worldRenderInfo
	)
	{
		MyGameRenderer.renderWorldNew(
				worldRenderInfo,
				runnable ->
				{
					OFGlobal.shaderContextManager.switchContextAndRun(() ->
					{
						OFGlobal.bindToShaderFrameBuffer.run();
						runnable.run();
					});
				}
		);
	}

	@Override
	public void renderPortalInEntityRenderer(Portal portal)
	{

	}

	private boolean testShouldRenderPortal(PortalLike portal, MatrixStack matrixStack)
	{
		//reset projection matrix
		client.gameRenderer.resetProjectionMatrix(RenderStates.projectionMatrix);

		deferredBuffer.fb.bindFramebuffer(true);
		return QueryManager.renderAndGetDoesAnySamplePass(() ->
		{
			GlStateManager.enableDepthTest();

			GlStateManager.disableTexture();
			GlStateManager.colorMask(false, false, false, false);

			GlStateManager.depthMask(false);
			GL20.glUseProgram(0);

			GlStateManager.disableTexture();

			ViewAreaRenderer.drawPortalViewTriangle(
					portal, matrixStack, true, true
			);

			GlStateManager.enableTexture();
			GlStateManager.colorMask(true, true, true, true);
			GlStateManager.depthMask(true);
		});
	}

	@Override
	public void onRenderCenterEnded(MatrixStack matrixStack)
	{
		if (PortalRendering.isRendering())
		{
			return;
		}

		GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, client.getFramebuffer().framebufferObject);
		GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, deferredBuffer.fb.framebufferObject);
		GL30.glBlitFramebuffer(
				0, 0, deferredBuffer.fb.framebufferTextureWidth, deferredBuffer.fb.framebufferTextureHeight,
				0, 0, deferredBuffer.fb.framebufferTextureWidth, deferredBuffer.fb.framebufferTextureHeight,
				GL11.GL_COLOR_BUFFER_BIT, GL_NEAREST
		);

		CHelper.checkGlError();

		OFHelper.copyFromShaderFbTo(deferredBuffer.fb, GL11.GL_DEPTH_BUFFER_BIT);

		renderPortals(modelView);
		modelView.pop();

		Framebuffer mainFrameBuffer = client.getFramebuffer();
		mainFrameBuffer.bindFramebuffer(true);

		MyRenderHelper.drawScreenFrameBuffer(
				deferredBuffer.fb,
				false,
				false
		);
	}
}
