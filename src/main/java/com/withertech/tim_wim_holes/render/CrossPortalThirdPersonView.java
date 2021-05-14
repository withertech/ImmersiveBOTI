package com.withertech.tim_wim_holes.render;

import com.mojang.datafixers.util.Pair;
import com.withertech.tim_wim_holes.CGlobal;
import com.withertech.tim_wim_holes.ClientWorldLoader;
import com.withertech.tim_wim_holes.PehkuiInterface;
import com.withertech.tim_wim_holes.commands.PortalCommand;
import com.withertech.tim_wim_holes.ducks.IECamera;
import com.withertech.tim_wim_holes.portal.Portal;
import com.withertech.tim_wim_holes.render.context_management.RenderStates;
import com.withertech.tim_wim_holes.render.context_management.WorldRenderInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.vector.Vector3d;

public class CrossPortalThirdPersonView
{
	public static final Minecraft client = Minecraft.getInstance();

	// if rendered, return true
	public static boolean renderCrossPortalThirdPersonView()
	{
		if (!(isThirdPerson() || TransformationManager.isIsometricView))
		{
			return false;
		}

		Entity cameraEntity = client.renderViewEntity;

		ActiveRenderInfo resuableCamera = new ActiveRenderInfo();
		float cameraY = ((IECamera) RenderStates.originalCamera).getCameraY();
		((IECamera) resuableCamera).setCameraY(cameraY, cameraY);
		resuableCamera.update(
				client.world, cameraEntity,
				true,
				isFrontView(),
				RenderStates.tickDelta
		);
		Vector3d originalCameraPos = resuableCamera.getProjectedView();
		Vector3d isometricAdjustedOriginalCameraPos =
				TransformationManager.getIsometricAdjustedCameraPos(resuableCamera);

		resuableCamera.update(
				client.world, cameraEntity,
				false, false, RenderStates.tickDelta
		);
		Vector3d playerHeadPos = resuableCamera.getProjectedView();

		Pair<Portal, Vector3d> portalHit = PortalCommand.raytracePortals(
				client.world, playerHeadPos, isometricAdjustedOriginalCameraPos, true
		).orElse(null);

		if (portalHit == null)
		{
			return false;
		}

		Portal portal = portalHit.getFirst();
		Vector3d hitPos = portalHit.getSecond();

		double distance = getThirdPersonMaxDistance();

		Vector3d thirdPersonPos = originalCameraPos.subtract(playerHeadPos).normalize()
				.scale(distance).add(playerHeadPos);

		if (!portal.isInteractable())
		{
			return false;
		}

		Vector3d renderingCameraPos = getThirdPersonCameraPos(thirdPersonPos, portal, hitPos);
		((IECamera) RenderStates.originalCamera).portal_setPos(renderingCameraPos);


		WorldRenderInfo worldRenderInfo = new WorldRenderInfo(
				ClientWorldLoader.getWorld(portal.dimensionTo), renderingCameraPos, portal.getAdditionalCameraTransformation(), false, null,
				Minecraft.getInstance().gameSettings.renderDistanceChunks
		);

		CGlobal.renderer.invokeWorldRendering(worldRenderInfo);

		return true;
	}

	private static boolean isFrontView()
	{
		return client.gameSettings.getPointOfView().func_243193_b();
	}

	private static boolean isThirdPerson()
	{
		return !client.gameSettings.getPointOfView().func_243192_a();
	}

	/**
	 * {@link Camera#update(BlockView, Entity, boolean, boolean, float)}
	 */
	private static Vector3d getThirdPersonCameraPos(Vector3d endPos, Portal portal, Vector3d startPos)
	{
		Vector3d rtStart = portal.transformPoint(startPos);
		Vector3d rtEnd = portal.transformPoint(endPos);
		BlockRayTraceResult blockHitResult = portal.getDestinationWorld().rayTraceBlocks(
				new RayTraceContext(
						rtStart,
						rtEnd,
						RayTraceContext.BlockMode.VISUAL,
						RayTraceContext.FluidMode.NONE,
						client.renderViewEntity
				)
		);

		if (blockHitResult == null)
		{
			return rtStart.add(rtEnd.subtract(rtStart).normalize().scale(
					getThirdPersonMaxDistance()
			));
		}

		return blockHitResult.getHitVec();
	}

	private static double getThirdPersonMaxDistance()
	{
		return 4.0d * PehkuiInterface.getScale.apply(Minecraft.getInstance().player);
	}

//    private static Vec3d getThirdPersonCameraPos(Portal portalHit, Camera resuableCamera) {
//        return CHelper.withWorldSwitched(
//            client.cameraEntity,
//            portalHit,
//            () -> {
//                World destinationWorld = portalHit.getDestinationWorld();
//                resuableCamera.update(
//                    destinationWorld,
//                    client.cameraEntity,
//                    true,
//                    isInverseView(),
//                    RenderStates.tickDelta
//                );
//                return resuableCamera.getPos();
//            }
//        );
//    }
}
