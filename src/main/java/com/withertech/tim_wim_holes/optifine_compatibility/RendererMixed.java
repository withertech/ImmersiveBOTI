package com.withertech.tim_wim_holes.optifine_compatibility;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.withertech.tim_wim_holes.CGlobal;
import com.withertech.tim_wim_holes.CHelper;
import com.withertech.tim_wim_holes.ModMain;
import com.withertech.tim_wim_holes.ducks.IEFrameBuffer;
import com.withertech.tim_wim_holes.portal.Portal;
import com.withertech.tim_wim_holes.portal.PortalLike;
import com.withertech.tim_wim_holes.render.*;
import com.withertech.tim_wim_holes.render.context_management.PortalRendering;
import com.withertech.tim_wim_holes.render.context_management.RenderStates;
import com.withertech.tim_wim_holes.render.context_management.WorldRenderInfo;
import net.minecraft.client.shader.Framebuffer;
import net.optifine.shaders.Shaders;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import static org.lwjgl.opengl.GL11.*;

public class RendererMixed extends PortalRenderer
{
	private SecondaryFrameBuffer[] deferredFbs = new SecondaryFrameBuffer[0];

	//OptiFine messes up with transformations so store it
	private MatrixStack modelView = new MatrixStack();

	private boolean portalRenderingNeeded = false;
	private boolean nextFramePortalRenderingNeeded = false;

	public RendererMixed()
	{
		ModMain.preGameRenderSignal.connect(() ->
		{
			updateNeedsPortalRendering();
		});
	}

	@Override
	public boolean replaceFrameBufferClearing()
	{
		return false;
	}

	@Override
	public void onRenderCenterEnded(MatrixStack matrixStack)
	{
		// avoid this thing needs to be invoked when no portal is rendered
		// it may cost performance
		if (portalRenderingNeeded)
		{
			int portalLayer = PortalRendering.getPortalLayer();

			initStencilForLayer(portalLayer);

			deferredFbs[portalLayer].fb.bindFramebuffer(true);
			deferredFbs[portalLayer].fb.checkFramebufferComplete();

			glEnable(GL_STENCIL_TEST);
			glStencilFunc(GL_EQUAL, portalLayer, 0xFF);
			glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP);

			Framebuffer mcFrameBuffer = client.getFramebuffer();

			MyRenderHelper.clearAlphaTo1(mcFrameBuffer);

			deferredFbs[portalLayer].fb.bindFramebuffer(true);
			deferredFbs[portalLayer].fb.checkFramebufferComplete();
			MyRenderHelper.drawScreenFrameBuffer(mcFrameBuffer, false, true);

			glDisable(GL_STENCIL_TEST);

			deferredFbs[portalLayer].fb.unbindFramebuffer();
		}

		MatrixStack effectiveTransformation = this.modelView;
		modelView = new MatrixStack();

		renderPortals(effectiveTransformation);
	}

	private void initStencilForLayer(int portalLayer)
	{
		if (portalLayer == 0)
		{
			deferredFbs[portalLayer].fb.bindFramebuffer(true);
			deferredFbs[portalLayer].fb.checkFramebufferComplete();
			GlStateManager.clearStencil(0);
			GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
		} else
		{
			deferredFbs[portalLayer - 1].fb.bindFramebuffer(false);
			deferredFbs[portalLayer - 1].fb.checkFramebufferComplete();
			deferredFbs[portalLayer].fb.bindFramebuffer(false);
			deferredFbs[portalLayer].fb.checkFramebufferComplete();


			GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, deferredFbs[portalLayer - 1].fb.framebufferObject);
			GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, deferredFbs[portalLayer].fb.framebufferObject);

			GL30.glBlitFramebuffer(
					0, 0, deferredFbs[0].fb.framebufferWidth, deferredFbs[0].fb.framebufferHeight,
					0, 0, deferredFbs[0].fb.framebufferWidth, deferredFbs[0].fb.framebufferHeight,
					GL_STENCIL_BUFFER_BIT, GL_NEAREST
			);
		}
	}

	@Override
	public void onBeforeTranslucentRendering(MatrixStack matrixStack)
	{

	}

	@Override
	public void onAfterTranslucentRendering(MatrixStack matrixStack)
	{
		if (portalRenderingNeeded)
		{
			deferredFbs[PortalRendering.getPortalLayer()].fb.bindFramebuffer(false);
			deferredFbs[PortalRendering.getPortalLayer()].fb.checkFramebufferComplete();

			OFHelper.copyFromShaderFbTo(
					deferredFbs[PortalRendering.getPortalLayer()].fb,
					GL_DEPTH_BUFFER_BIT
			);
		}

		modelView.push();
		modelView.getLast().getMatrix().mul(matrixStack.getLast().getMatrix());
		modelView.getLast().getNormal().mul(matrixStack.getLast().getNormal());
	}

	@Override
	public void prepareRendering()
	{
		if (CGlobal.shaderManager == null)
		{
			CGlobal.shaderManager = new ShaderManager();
		}

		if (deferredFbs.length != PortalRendering.getMaxPortalLayer() + 1)
		{
			for (SecondaryFrameBuffer fb : deferredFbs)
			{
				fb.fb.deleteFramebuffer();
			}

			deferredFbs = new SecondaryFrameBuffer[PortalRendering.getMaxPortalLayer() + 1];
			for (int i = 0; i < deferredFbs.length; i++)
			{
				deferredFbs[i] = new SecondaryFrameBuffer();
			}
		}

		CHelper.checkGlError();

		for (SecondaryFrameBuffer deferredFb : deferredFbs)
		{
			deferredFb.prepare();
			((IEFrameBuffer) deferredFb.fb).setIsStencilBufferEnabledAndReload(true);

			deferredFb.fb.bindFramebuffer(true);
			GlStateManager.clearColor(1, 0, 1, 0);
			GlStateManager.clearDepth(1);
			GlStateManager.clearStencil(0);
			GL11.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);

			CHelper.checkGlError();

			deferredFb.fb.unbindFramebuffer();
		}
	}

	private void updateNeedsPortalRendering()
	{
		portalRenderingNeeded = nextFramePortalRenderingNeeded;
		nextFramePortalRenderingNeeded = false;
	}

	@Override
	public void finishRendering()
	{
		GlStateManager.colorMask(true, true, true, true);
		Shaders.useProgram(Shaders.ProgramNone);

		if (RenderStates.getRenderedPortalNum() == 0)
		{
			return;
		}

		if (!portalRenderingNeeded)
		{
			return;
		}

		Framebuffer mainFrameBuffer = client.getFramebuffer();
		mainFrameBuffer.bindFramebuffer(true);
		mainFrameBuffer.checkFramebufferComplete();

		deferredFbs[0].fb.framebufferRender(mainFrameBuffer.framebufferWidth, mainFrameBuffer.framebufferHeight);

		CHelper.checkGlError();
	}

	@Override
	protected void doRenderPortal(PortalLike portal, MatrixStack matrixStack)
	{
		nextFramePortalRenderingNeeded = true;

		if (!portalRenderingNeeded)
		{
			return;
		}

		//reset projection matrix
		client.gameRenderer.resetProjectionMatrix(RenderStates.projectionMatrix);

		//write to deferred buffer
		if (!tryRenderViewAreaInDeferredBufferAndIncreaseStencil(portal, matrixStack))
		{
			return;
		}

		PortalRendering.pushPortalLayer(portal);

//        OFGlobal.bindToShaderFrameBuffer.run();
		renderPortalContent(portal);

		int innerLayer = PortalRendering.getPortalLayer();

		PortalRendering.popPortalLayer();

		int outerLayer = PortalRendering.getPortalLayer();

		if (innerLayer > PortalRendering.getMaxPortalLayer())
		{
			return;
		}

		deferredFbs[outerLayer].fb.bindFramebuffer(true);
		deferredFbs[outerLayer].fb.checkFramebufferComplete();

		MyRenderHelper.drawScreenFrameBuffer(
				deferredFbs[innerLayer].fb,
				true,
				true
		);
	}

	private boolean tryRenderViewAreaInDeferredBufferAndIncreaseStencil(
			PortalLike portal, MatrixStack matrixStack
	)
	{
		int portalLayer = PortalRendering.getPortalLayer();

		initStencilForLayer(portalLayer);

		deferredFbs[portalLayer].fb.bindFramebuffer(true);
		deferredFbs[portalLayer].fb.checkFramebufferComplete();

		GL20.glUseProgram(0);

		GL11.glEnable(GL_STENCIL_TEST);
		GL11.glStencilFunc(GL11.GL_EQUAL, portalLayer, 0xFF);
		GL11.glStencilOp(GL_KEEP, GL_KEEP, GL_INCR);

		GlStateManager.enableDepthTest();

		boolean result = QueryManager.renderAndGetDoesAnySamplePass(() ->
		{
			ViewAreaRenderer.drawPortalViewTriangle(
					portal, matrixStack, true, true
			);
		});

		GL11.glDisable(GL_STENCIL_TEST);

		OFGlobal.bindToShaderFrameBuffer.run();

		return result;
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
//                    OFGlobal.bindToShaderFrameBuffer.run();
						runnable.run();
					});
				}
		);
	}

	@Override
	public void renderPortalInEntityRenderer(Portal portal)
	{
//        if (Shaders.isShadowPass) {
//            ViewAreaRenderer.drawPortalViewTriangle(portal);
//        }
	}
}
