package com.withertech.tim_wim_holes.render.context_management;

import com.withertech.tim_wim_holes.ducks.IEGameRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.World;

public class DimensionRenderHelper
{
	private static final Minecraft client = Minecraft.getInstance();
	public final LightTexture lightmapTexture;
	public World world;

	public DimensionRenderHelper(World world)
	{
		this.world = world;

		if (client.world == world)
		{
			IEGameRenderer gameRenderer = (IEGameRenderer) client.gameRenderer;

			lightmapTexture = client.gameRenderer.getLightTexture();
		} else
		{
			lightmapTexture = new LightTexture(client.gameRenderer, client);
		}
	}

	public void tick()
	{
		lightmapTexture.tick();
	}

	public void cleanUp()
	{
		if (lightmapTexture != client.gameRenderer.getLightTexture())
		{
			lightmapTexture.close();
		}
	}

}
