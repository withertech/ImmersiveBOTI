package com.withertech.tim_wim_holes.render.context_management;

import com.withertech.tim_wim_holes.CHelper;
import com.withertech.tim_wim_holes.Global;
import com.withertech.tim_wim_holes.portal.Mirror;
import com.withertech.tim_wim_holes.portal.PortalLike;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Stack;
import java.util.function.Function;
import java.util.stream.Collectors;

// TODO remove this and use RenderInfo
@OnlyIn(Dist.CLIENT)
public class PortalRendering
{
	private static final Stack<PortalLike> portalLayers = new Stack<>();
	private static boolean isRenderingCache = false;
	private static boolean isRenderingOddNumberOfMirrorsCache = false;

	public static void pushPortalLayer(PortalLike portal)
	{
		portalLayers.push(portal);
		updateCache();
	}

	public static void popPortalLayer()
	{
		portalLayers.pop();
		updateCache();
	}

	private static void updateCache()
	{
		isRenderingCache = getPortalLayer() != 0;

		int number = 0;
		for (PortalLike portal : portalLayers)
		{
			if (portal instanceof Mirror)
			{
				number++;
			}
		}
		isRenderingOddNumberOfMirrorsCache = (number % 2 == 1);
	}

	//0 for rendering outer world
	//1 for rendering world inside portal
	//2 for rendering world inside the portal inside portal
	public static int getPortalLayer()
	{
		return portalLayers.size();
	}

	public static boolean isRendering()
	{
		return isRenderingCache;
	}

	public static boolean isRenderingOddNumberOfMirrors()
	{
		return isRenderingOddNumberOfMirrorsCache;
	}

	public static int getMaxPortalLayer()
	{
		if (RenderStates.isLaggy)
		{
			return 1;
		}
		return Global.maxPortalLayer;
	}

	public static PortalLike getRenderingPortal()
	{
		return portalLayers.peek();
	}

	public static void onBeginPortalWorldRendering()
	{
		List<WeakReference<PortalLike>> currRenderInfo = portalLayers.stream().map(
				(Function<PortalLike, WeakReference<PortalLike>>) WeakReference::new
		).collect(Collectors.toList());
		RenderStates.portalRenderInfos.add(currRenderInfo);

		if (portalLayers.stream().anyMatch(PortalLike::hasScaling))
		{
			RenderStates.renderedScalingPortal = true;
		}

		CHelper.checkGlError();
	}

	public static void onEndPortalWorldRendering()
	{
		RenderStates.renderedDimensions.add(
				portalLayers.peek().getDestDim()
		);
	}

	public static Vector3d getRenderingCameraPos()
	{
		Vector3d pos = RenderStates.originalCamera.getProjectedView();
		for (PortalLike portal : portalLayers)
		{
			pos = portal.transformPoint(pos);
		}
		return pos;
	}

	public static double getAllScaling()
	{
		double scale = 1.0;
		for (PortalLike portal : portalLayers)
		{
			scale *= portal.getScale();
		}
		return scale;
	}

}
