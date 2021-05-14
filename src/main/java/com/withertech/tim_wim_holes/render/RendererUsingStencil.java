package com.withertech.tim_wim_holes.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.withertech.tim_wim_holes.CHelper;
import com.withertech.tim_wim_holes.ducks.IEFrameBuffer;
import com.withertech.tim_wim_holes.portal.Portal;
import com.withertech.tim_wim_holes.portal.PortalLike;
import com.withertech.tim_wim_holes.render.context_management.FogRendererContext;
import com.withertech.tim_wim_holes.render.context_management.PortalRendering;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.vector.Vector3d;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import static org.lwjgl.opengl.GL11.*;

//NOTE do not use glDisable(GL_DEPTH_TEST), use GlStateManager.disableDepthTest() instead
//because GlStateManager will cache its state. Do not make its cache not synchronized
public class RendererUsingStencil extends PortalRenderer
{


	public static void clampStencilValue(
			int maximumValue
	)
	{
		//NOTE GL_GREATER means ref > stencil
		//GL_LESS means ref < stencil

		//pass if the stencil value is greater than the maximum value
		GL11.glStencilFunc(GL_LESS, maximumValue, 0xFF);

		//if stencil test passed, encode the stencil value
		GL11.glStencilOp(GL_KEEP, GL_REPLACE, GL_REPLACE);

		//do not manipulate the depth packetBuffer
		GL11.glDepthMask(false);

		//do not manipulate the color packetBuffer
		GL11.glColorMask(false, false, false, false);

		GlStateManager.disableDepthTest();

		MyRenderHelper.renderScreenTriangle();

		GL11.glDepthMask(true);

		GL11.glColorMask(true, true, true, true);

		GlStateManager.enableDepthTest();
	}

	private static boolean shouldSkipRenderingInsideFuseViewPortal(PortalLike portal)
	{
		if (!PortalRendering.isRendering())
		{
			return false;
		}

		PortalLike renderingPortal = PortalRendering.getRenderingPortal();

		if (!renderingPortal.isFuseView())
		{
			return false;
		}

		Vector3d cameraPos = CHelper.getCurrentCameraPos();

		Vector3d transformedCameraPos = portal.transformPoint(renderingPortal.transformPoint(cameraPos));

		// roughly test whether they are reverse portals
		return cameraPos.squareDistanceTo(transformedCameraPos) < 0.1;
	}

	@Override
	public boolean replaceFrameBufferClearing()
	{
		boolean skipClearing = PortalRendering.isRendering();
		if (skipClearing)
		{
			boolean isSkyTransparent = PortalRendering.getRenderingPortal().isFuseView();

			if (!isSkyTransparent)
			{
				RenderSystem.depthMask(false);
				MyRenderHelper.renderScreenTriangle(FogRendererContext.getCurrentFogColor.get());
				RenderSystem.depthMask(true);
			}
		}
		return skipClearing;
	}

	@Override
	public void onBeforeTranslucentRendering(MatrixStack matrixStack)
	{
		doPortalRendering(matrixStack);
	}

	private void doPortalRendering(MatrixStack matrixStack)
	{
		client.getProfiler().endStartSection("render_portal_total");
		renderPortals(matrixStack);
		if (PortalRendering.isRendering())
		{
			setStencilStateForWorldRendering();
		} else
		{
			myFinishRendering();
		}
	}

	@Override
	public void onAfterTranslucentRendering(MatrixStack matrixStack)
	{

	}

	@Override
	public void onRenderCenterEnded(MatrixStack matrixStack)
	{
		//nothing
	}

	@Override
	public void prepareRendering()
	{
		//NOTE calling glClearStencil will not clear it, it just assigns the value for clearing
		GL11.glClearStencil(0);
		GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);

		GlStateManager.enableDepthTest();
		GL11.glEnable(GL_STENCIL_TEST);

		IEFrameBuffer ieFrameBuffer = (IEFrameBuffer) client.getFramebuffer();
		if (!ieFrameBuffer.getIsStencilBufferEnabled())
		{
			ieFrameBuffer.setIsStencilBufferEnabledAndReload(true);
			if (Minecraft.isFabulousGraphicsEnabled())
			{
				client.worldRenderer.loadRenderers();
			}
		}

	}

	@Override
	public void finishRendering()
	{
		//nothing
	}

	private void myFinishRendering()
	{
		GL11.glStencilFunc(GL_ALWAYS, 2333, 0xFF);
		GL11.glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP);

		GL11.glDisable(GL_STENCIL_TEST);
		GlStateManager.enableDepthTest();
	}

	@Override
	protected void doRenderPortal(
			PortalLike portal,
			MatrixStack matrixStack
	)
	{
		if (shouldSkipRenderingInsideFuseViewPortal(portal))
		{
			return;
		}

		int outerPortalStencilValue = PortalRendering.getPortalLayer();

		client.getProfiler().startSection("render_view_area");

		boolean anySamplePassed = PortalRenderInfo.renderAndDecideVisibility(portal, () ->
		{
			renderPortalViewAreaToStencil(portal, matrixStack);
		});

		client.getProfiler().endSection();

		if (!anySamplePassed)
		{
			setStencilStateForWorldRendering();
			return;
		}

		PortalRendering.pushPortalLayer(portal);

		int thisPortalStencilValue = outerPortalStencilValue + 1;

		if (!portal.isFuseView())
		{
			client.getProfiler().startSection("clear_depth_of_view_area");
			clearDepthOfThePortalViewArea(portal);
			client.getProfiler().endSection();
		}

		setStencilStateForWorldRendering();

		renderPortalContent(portal);

		if (!portal.isFuseView())
		{
			restoreDepthOfPortalViewArea(portal, matrixStack);
		}

		clampStencilValue(outerPortalStencilValue);

		PortalRendering.popPortalLayer();

		// because the vanilla VertexConsumerProvider does not delay transparent drawing
		// rendering the overlay in entity renderer will cause it to render too early
		// if delayed into another buffer, the nested portal overlay cannot be correctly rendered
		// so render the overlay here
		// overlay incompatible with optifine shaders
		if (OverlayRendering.shouldRenderOverlay(portal))
		{
			setStencilStateForWorldRendering();
			OverlayRendering.onPortalRendered(portal, matrixStack);
		}
	}

	@Override
	public void renderPortalInEntityRenderer(Portal portal)
	{
		//nothing
	}

	private void renderPortalViewAreaToStencil(
			PortalLike portal, MatrixStack matrixStack
	)
	{
		int outerPortalStencilValue = PortalRendering.getPortalLayer();

		//is the mask here different from the mask of glStencilMask?
		GL11.glStencilFunc(GL_EQUAL, outerPortalStencilValue, 0xFF);

		//if stencil and depth test pass, the data in stencil packetBuffer will increase by 1
		GL11.glStencilOp(GL_KEEP, GL_KEEP, GL_INCR);
		//NOTE about GL_INCR:
		//if multiple triangles occupy the same pixel and passed stencil and depth tests,
		//its stencil value will still increase by one

		GL11.glStencilMask(0xFF);

		GlStateManager.disableBlend();

		GL20.glUseProgram(0);


		if (portal.isFuseView())
		{
			GlStateManager.colorMask(false, false, false, false);

			RenderSystem.disableDepthTest();
		} else
		{
			RenderSystem.enableDepthTest();
			GlStateManager.depthMask(true);
		}

		GlStateManager.disableTexture();

		ViewAreaRenderer.drawPortalViewTriangle(portal, matrixStack, true, true);

		if (portal.isFuseView())
		{
			GlStateManager.colorMask(true, true, true, true);
		}

		GlStateManager.enableTexture();

		GlStateManager.enableBlend();

		CHelper.checkGlError();
	}

	private void clearDepthOfThePortalViewArea(
			PortalLike portal
	)
	{
		GlStateManager.enableDepthTest();

		setStencilStateForWorldRendering();

		//do not manipulate color buffer
		GL11.glColorMask(false, false, false, false);

		//save the state
		int originalDepthFunc = GL11.glGetInteger(GL_DEPTH_FUNC);

		//always passes depth test
		GL11.glDepthFunc(GL_ALWAYS);

		//the pixel's depth will be 1, which is the furthest
		GL11.glDepthRange(1, 1);

		MyRenderHelper.renderScreenTriangle();

		//retrieve the state
		GL11.glColorMask(true, true, true, true);
		GL11.glDepthFunc(originalDepthFunc);
		GL11.glDepthRange(0, 1);
	}

	private void restoreDepthOfPortalViewArea(
			PortalLike portal, MatrixStack matrixStack
	)
	{
		setStencilStateForWorldRendering();

		//do not manipulate color buffer
		GL11.glColorMask(false, false, false, false);

		//do manipulate the depth buffer
		GL11.glDepthMask(true);

		GL20.glUseProgram(0);

		int originalDepthFunc = GL11.glGetInteger(GL_DEPTH_FUNC);

		GL11.glDepthFunc(GL_ALWAYS);

		GlStateManager.enableDepthTest();

		GlStateManager.disableTexture();

		ViewAreaRenderer.drawPortalViewTriangle(portal, matrixStack, false, false);

		GlStateManager.enableTexture();

		GL11.glColorMask(true, true, true, true);

		GL11.glDepthFunc(originalDepthFunc);
	}

	private void setStencilStateForWorldRendering()
	{
		int thisPortalStencilValue = PortalRendering.getPortalLayer();

		//draw content in the mask
		GL11.glStencilFunc(GL_EQUAL, thisPortalStencilValue, 0xFF);

		//do not manipulate stencil packetBuffer now
		GL11.glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP);
	}
}
