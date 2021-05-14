package com.withertech.tim_wim_holes.render;

import com.withertech.tim_wim_holes.CGlobal;
import com.withertech.tim_wim_holes.CHelper;
import com.withertech.tim_wim_holes.Helper;
import com.withertech.tim_wim_holes.ducks.IECamera;
import com.withertech.tim_wim_holes.ducks.IEMinecraftClient;
import com.withertech.tim_wim_holes.my_util.LimitedLogger;
import com.withertech.tim_wim_holes.render.context_management.RenderStates;
import com.withertech.tim_wim_holes.render.context_management.WorldRenderInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.shader.Framebuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nullable;
import java.util.HashMap;

@OnlyIn(Dist.CLIENT)
public class GuiPortalRendering
{
	private static final LimitedLogger limitedLogger = new LimitedLogger(10);
	private static final HashMap<Framebuffer, WorldRenderInfo> renderingTasks = new HashMap<>();
	@Nullable
	private static Framebuffer renderingFrameBuffer = null;

	@Nullable
	public static Framebuffer getRenderingFrameBuffer()
	{
		return renderingFrameBuffer;
	}

	public static boolean isRendering()
	{
		return getRenderingFrameBuffer() != null;
	}

	private static void renderWorldIntoFrameBuffer(
			WorldRenderInfo worldRenderInfo,
			Framebuffer framebuffer
	)
	{
		RenderStates.projectionMatrix = null;

		CHelper.checkGlError();

		((IECamera) RenderStates.originalCamera).resetState(
				worldRenderInfo.cameraPos, worldRenderInfo.world
		);

		Validate.isTrue(renderingFrameBuffer == null);
		renderingFrameBuffer = framebuffer;

		MyRenderHelper.restoreViewPort();

		Framebuffer mcFb = MyGameRenderer.client.getFramebuffer();

		Validate.isTrue(mcFb != framebuffer);

		((IEMinecraftClient) MyGameRenderer.client).setFrameBuffer(framebuffer);

		framebuffer.bindFramebuffer(true);

		CGlobal.renderer.prepareRendering();

		CGlobal.renderer.invokeWorldRendering(worldRenderInfo);

		CGlobal.renderer.finishRendering();

		((IEMinecraftClient) MyGameRenderer.client).setFrameBuffer(mcFb);

		mcFb.bindFramebuffer(true);

		renderingFrameBuffer = null;

		MyRenderHelper.restoreViewPort();

		CHelper.checkGlError();

		RenderStates.projectionMatrix = null;
	}

	public static void submitNextFrameRendering(
			WorldRenderInfo worldRenderInfo,
			Framebuffer renderTarget
	)
	{
		Validate.isTrue(!renderingTasks.containsKey(renderTarget));

		Framebuffer mcFB = Minecraft.getInstance().getFramebuffer();
		if (renderTarget.framebufferTextureWidth != mcFB.framebufferTextureWidth || renderTarget.framebufferTextureHeight != mcFB.framebufferTextureHeight)
		{
			renderTarget.resize(mcFB.framebufferTextureWidth, mcFB.framebufferTextureHeight, true);
			Helper.info("Resized Framebuffer for GUI Portal Rendering");
		}

		renderingTasks.put(renderTarget, worldRenderInfo);
	}

	// Not API
	public static void onGameRenderEnd()
	{
		renderingTasks.forEach((frameBuffer, worldRendering) ->
		{
			renderWorldIntoFrameBuffer(
					worldRendering, frameBuffer
			);
		});
		renderingTasks.clear();
	}
}
