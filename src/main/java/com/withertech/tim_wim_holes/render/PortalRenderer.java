package com.withertech.tim_wim_holes.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.withertech.tim_wim_holes.*;
import com.withertech.tim_wim_holes.portal.Mirror;
import com.withertech.tim_wim_holes.portal.Portal;
import com.withertech.tim_wim_holes.portal.PortalLike;
import com.withertech.tim_wim_holes.render.context_management.PortalRendering;
import com.withertech.tim_wim_holes.render.context_management.RenderStates;
import com.withertech.tim_wim_holes.render.context_management.WorldRenderInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

public abstract class PortalRenderer
{

	public static final Minecraft client = Minecraft.getInstance();

	private static int getPortalRenderDistance(PortalLike portal)
	{
		if (portal.getScale() > 2)
		{
			double radiusBlocks = portal.getDestAreaRadiusEstimation() * 1.4;

			radiusBlocks = Math.min(radiusBlocks, 32 * 16);

			return Math.max((int) (radiusBlocks / 16), client.gameSettings.renderDistanceChunks);
		}
		if (Global.reducedPortalRendering)
		{
			return client.gameSettings.renderDistanceChunks / 3;
		}
		return client.gameSettings.renderDistanceChunks;
	}

	@Nullable
	public static Matrix4f getPortalTransformation(Portal portal)
	{
		Matrix4f rot = getPortalRotationMatrix(portal);

		Matrix4f mirror = portal instanceof Mirror ?
				TransformationManager.getMirrorTransformation(portal.getNormal()) : null;

		Matrix4f scale = getPortalScaleMatrix(portal);

		return combineNullable(rot, combineNullable(mirror, scale));
	}

	@Nullable
	public static Matrix4f getPortalRotationMatrix(Portal portal)
	{
		if (portal.rotation == null)
		{
			return null;
		}

		Quaternion rot = portal.rotation.copy();
		rot.conjugate();
		return new Matrix4f(rot);
	}

	@Nullable
	public static Matrix4f combineNullable(@Nullable Matrix4f a, @Nullable Matrix4f b)
	{
		return Helper.combineNullable(a, b, (m1, m2) ->
		{
			m1.mul(m2);
			return m1;
		});
	}

	@Nullable
	public static Matrix4f getPortalScaleMatrix(Portal portal)
	{
		// if it's not a fuseView portal
		// whether to apply scale transformation to camera does not change triangle position
		// to avoid abrupt fog change, do not apply for non-fuse-view portal
		// for fuse-view portal, the depth value should be correct so the scale should be applied
		if (portal.hasScaling() && (portal.isFuseView() || TransformationManager.isIsometricView))
		{
			float v = (float) (1.0 / portal.getScale());
			return Matrix4f.makeScale(v, v, v);
		}
		return null;
	}

	// this WILL be called when rendering portal
	public abstract void onBeforeTranslucentRendering(MatrixStack matrixStack);

	// this WILL be called when rendering portal
	public abstract void onAfterTranslucentRendering(MatrixStack matrixStack);

	// this WILL be called when rendering portal
	public abstract void onRenderCenterEnded(MatrixStack matrixStack);

	// this will NOT be called when rendering portal
	public abstract void prepareRendering();

	// this will NOT be called when rendering portal
	public abstract void finishRendering();

	// this will be called when rendering portal entities
	public abstract void renderPortalInEntityRenderer(Portal portal);

	// return true to skip framebuffer clear
	// this will also be called in outer world rendering
	public abstract boolean replaceFrameBufferClearing();

	protected void renderPortals(MatrixStack matrixStack)
	{
		Validate.isTrue(client.renderViewEntity.world == client.world);

		Supplier<ClippingHelper> frustumSupplier = Helper.cached(() ->
		{
			ClippingHelper frustum = new ClippingHelper(
					matrixStack.getLast().getMatrix(),
					RenderStates.projectionMatrix
			);

			Vector3d cameraPos = client.gameRenderer.getActiveRenderInfo().getProjectedView();
			frustum.setCameraPosition(cameraPos.x, cameraPos.y, cameraPos.z);

			return frustum;
		});

		List<PortalLike> portalsToRender = new ArrayList<>();
		List<Portal> globalPortals = McHelper.getGlobalPortals(client.world);
		for (Portal globalPortal : globalPortals)
		{
			if (!shouldSkipRenderingPortal(globalPortal, frustumSupplier))
			{
				portalsToRender.add(globalPortal);
			}
		}

		client.world.getAllEntities().forEach(e ->
		{
			if (e instanceof Portal)
			{
				Portal portal = (Portal) e;
				if (!shouldSkipRenderingPortal(portal, frustumSupplier))
				{

					PortalLike renderingDelegate = portal.getRenderingDelegate();

					if (renderingDelegate != portal)
					{
						// a portal rendering group
						if (!portalsToRender.contains(renderingDelegate))
						{
							portalsToRender.add(renderingDelegate);
						}
					} else
					{
						// a normal portal
						portalsToRender.add(renderingDelegate);
					}
				}
			}
		});

		Vector3d cameraPos = CHelper.getCurrentCameraPos();
		portalsToRender.sort(Comparator.comparingDouble(portalEntity ->
				portalEntity.getDistanceToNearestPointInPortal(cameraPos)
		));

		for (PortalLike portal : portalsToRender)
		{
			doRenderPortal(portal, matrixStack);
		}
	}

	private boolean shouldSkipRenderingPortal(Portal portal, Supplier<ClippingHelper> frustumSupplier)
	{
		if (!portal.isPortalValid())
		{
			return true;
		}

		if (RenderStates.getRenderedPortalNum() >= Global.portalRenderLimit)
		{
			return true;
		}

		Vector3d cameraPos = TransformationManager.getIsometricAdjustedCameraPos();

		if (!portal.isRoughlyVisibleTo(cameraPos))
		{
			return true;
		}

		if (PortalRendering.isRendering())
		{
			PortalLike outerPortal = PortalRendering.getRenderingPortal();

			if (outerPortal.cannotRenderInMe(portal))
			{
				return true;
			}
		}

		if (isOutOfDistance(portal))
		{
			return true;
		}

		if (CGlobal.earlyFrustumCullingPortal)
		{
			ClippingHelper frustum = frustumSupplier.get();
            return !frustum.isBoundingBoxInFrustum(portal.getExactAreaBox());
		}
		return false;
	}

	protected final double getRenderRange()
	{
		double range = client.gameSettings.renderDistanceChunks * 16;
		if (RenderStates.isLaggy || Global.reducedPortalRendering)
		{
			range = 16;
		}
		if (PortalRendering.getPortalLayer() > 1)
		{
			//do not render deep layers of mirror when far away
			range /= (PortalRendering.getPortalLayer());
		}
		if (PortalRendering.getPortalLayer() >= 1)
		{
			double outerPortalScale = PortalRendering.getRenderingPortal().getScale();
			if (outerPortalScale > 2)
			{
				range *= outerPortalScale;
				range = Math.min(range, 32 * 16);
			}
		}
		return range;
	}

	protected abstract void doRenderPortal(
			PortalLike portal,
			MatrixStack matrixStack
	);

	protected final void renderPortalContent(
			PortalLike portal
	)
	{
		if (PortalRendering.getPortalLayer() > PortalRendering.getMaxPortalLayer())
		{
			return;
		}

		Entity cameraEntity = client.renderViewEntity;

		ClientWorld newWorld = ClientWorldLoader.getWorld(portal.getDestDim());

		ActiveRenderInfo camera = client.gameRenderer.getActiveRenderInfo();

		PortalRendering.onBeginPortalWorldRendering();

		int renderDistance = getPortalRenderDistance(portal);

		invokeWorldRendering(new WorldRenderInfo(
				newWorld,
				PortalRendering.getRenderingCameraPos(),
				portal.getAdditionalCameraTransformation(),
				false, portal.getDiscriminator(),
				renderDistance
		));

		PortalRendering.onEndPortalWorldRendering();

		GlStateManager.enableDepthTest();
		GlStateManager.disableBlend();

		MyRenderHelper.restoreViewPort();


	}

	public void invokeWorldRendering(
			WorldRenderInfo worldRenderInfo
	)
	{
		MyGameRenderer.renderWorldNew(
				worldRenderInfo,
				Runnable::run
		);
	}

	private boolean isOutOfDistance(PortalLike portal)
	{

		Vector3d cameraPos = CHelper.getCurrentCameraPos();
        return portal.getDistanceToNearestPointInPortal(cameraPos) > getRenderRange();
    }

}
