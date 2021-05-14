package com.withertech.tim_wim_holes.render;

import com.withertech.tim_wim_holes.Helper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.shader.Framebuffer;

//it will always be the same size as the main frame buffer
public class SecondaryFrameBuffer
{
	public Framebuffer fb;

	public void prepare()
	{
		Framebuffer mainFrameBuffer = Minecraft.getInstance().getFramebuffer();
		int width = mainFrameBuffer.framebufferWidth;
		int height = mainFrameBuffer.framebufferHeight;
		prepare(width, height);
	}

	public void prepare(int width, int height)
	{
		if (fb == null)
		{
			fb = new Framebuffer(
					width, height,
					true,//has depth attachment
					Minecraft.IS_RUNNING_ON_MAC
			);
			Helper.info("Deferred buffer init");
		}
		if (width != fb.framebufferWidth ||
				height != fb.framebufferHeight
		)
		{
			fb.resize(
					width, height, Minecraft.IS_RUNNING_ON_MAC
			);
			Helper.info("Deferred buffer resized");
		}
	}


}
