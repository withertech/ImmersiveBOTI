package com.withertech.tim_wim_holes.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.withertech.tim_wim_holes.*;
import com.withertech.tim_wim_holes.block_manipulation.BlockManipulationClient;
import com.withertech.tim_wim_holes.ducks.*;
import com.withertech.tim_wim_holes.my_util.LimitedLogger;
import com.withertech.tim_wim_holes.portal.Portal;
import com.withertech.tim_wim_holes.portal.PortalLike;
import com.withertech.tim_wim_holes.render.context_management.*;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.Util;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;

import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public class MyGameRenderer
{
	private static final LimitedLogger limitedLogger = new LimitedLogger(10);
	public static Minecraft client = Minecraft.getInstance();
	// portal rendering and outer world rendering uses different buffer builder storages
	// theoretically every layer of portal rendering should have its own buffer builder storage
	private static final RenderTypeBuffers secondaryBufferBuilderStorage = new RenderTypeBuffers();

	public static void renderWorldNew(
			WorldRenderInfo worldRenderInfo,
			Consumer<Runnable> invokeWrapper
	)
	{
		WorldRenderInfo.pushRenderInfo(worldRenderInfo);

		switchAndRenderTheWorld(
				worldRenderInfo.world,
				worldRenderInfo.cameraPos,
				worldRenderInfo.cameraPos,
				invokeWrapper,
				worldRenderInfo.renderDistance
		);

		WorldRenderInfo.popRenderInfo();
	}

	private static void switchAndRenderTheWorld(
			ClientWorld newWorld,
			Vector3d thisTickCameraPos,
			Vector3d lastTickCameraPos,
			Consumer<Runnable> invokeWrapper,
			int renderDistance
	)
	{
		resetGlStates();

		Entity cameraEntity = client.renderViewEntity;

		Vector3d oldEyePos = McHelper.getEyePos(cameraEntity);
		Vector3d oldLastTickEyePos = McHelper.getLastTickEyePos(cameraEntity);

		RegistryKey<World> oldEntityDimension = cameraEntity.world.getDimensionKey();
		ClientWorld oldEntityWorld = ((ClientWorld) cameraEntity.world);

		RegistryKey<World> newDimension = newWorld.getDimensionKey();

		//switch the camera entity pos
		McHelper.setEyePos(cameraEntity, thisTickCameraPos, lastTickCameraPos);
		cameraEntity.world = newWorld;

		WorldRenderer worldRenderer = ClientWorldLoader.getWorldRenderer(newDimension);

		CHelper.checkGlError();

		float tickDelta = RenderStates.tickDelta;

		if (CGlobal.useHackedChunkRenderDispatcher)
		{
			((IEWorldRenderer) worldRenderer).getBuiltChunkStorage().updateChunkPositions(
					cameraEntity.getPosX(),
					cameraEntity.getPosZ()
			);
		}


		IEGameRenderer ieGameRenderer = (IEGameRenderer) client.gameRenderer;
		DimensionRenderHelper helper =
				ClientWorldLoader.getDimensionRenderHelper(
						RenderDimensionRedirect.getRedirectedDimension(newDimension)
				);
		NetworkPlayerInfo playerListEntry = CHelper.getClientPlayerListEntry();
		ActiveRenderInfo newCamera = new ActiveRenderInfo();

		//store old state
		WorldRenderer oldWorldRenderer = client.worldRenderer;
		LightTexture oldLightmap = client.gameRenderer.getLightTexture();
		GameType oldGameMode = playerListEntry.getGameType();
		boolean oldNoClip = client.player.noClip;
		boolean oldDoRenderHand = ieGameRenderer.getDoRenderHand();
		OFInterface.createNewRenderInfosNormal.accept(worldRenderer);
		ObjectList oldVisibleChunks = ((IEWorldRenderer) oldWorldRenderer).getVisibleChunks();
		RayTraceResult oldCrosshairTarget = client.objectMouseOver;
		ActiveRenderInfo oldCamera = client.gameRenderer.getActiveRenderInfo();
		ShaderGroup oldTransparencyShader =
				((IEWorldRenderer) oldWorldRenderer).portal_getTransparencyShader();
		ShaderGroup newTransparencyShader = ((IEWorldRenderer) worldRenderer).portal_getTransparencyShader();
		RenderTypeBuffers oldBufferBuilder = ((IEWorldRenderer) worldRenderer).getBufferBuilderStorage();
		RenderTypeBuffers oldClientBufferBuilder = client.getRenderTypeBuffers();
		boolean oldChunkCullingEnabled = client.renderChunksMany;

		((IEWorldRenderer) oldWorldRenderer).setVisibleChunks(new ObjectArrayList());

		int oldRenderDistance = ((IEWorldRenderer) worldRenderer).portal_getRenderDistance();

		//switch
		((IEMinecraftClient) client).setWorldRenderer(worldRenderer);
		client.world = newWorld;
		ieGameRenderer.setLightmapTextureManager(helper.lightmapTexture);

		TileEntityRendererDispatcher.instance.world = newWorld;
		((IEPlayerListEntry) playerListEntry).setGameMode(GameType.SPECTATOR);
		client.player.noClip = true;
		ieGameRenderer.setDoRenderHand(false);
		GlStateManager.matrixMode(GL11.GL_MODELVIEW);
		GlStateManager.pushMatrix();
		FogRendererContext.swappingManager.pushSwapping(
				RenderDimensionRedirect.getRedirectedDimension(newDimension)
		);
		((IEParticleManager) client.particles).mySetWorld(newWorld);
		if (BlockManipulationClient.remotePointedDim == newDimension)
		{
			client.objectMouseOver = BlockManipulationClient.remoteHitResult;
		}
		ieGameRenderer.setCamera(newCamera);

		if (Global.useSecondaryEntityVertexConsumer)
		{
			((IEWorldRenderer) worldRenderer).setBufferBuilderStorage(secondaryBufferBuilderStorage);
			((IEMinecraftClient) client).setBufferBuilderStorage(secondaryBufferBuilderStorage);
		}

		Object newSodiumContext = SodiumInterface.createNewRenderingContext.apply(worldRenderer);
		Object oldSodiumContext = SodiumInterface.switchRenderingContext.apply(worldRenderer, newSodiumContext);

		((IEWorldRenderer) oldWorldRenderer).portal_setTransparencyShader(null);
		((IEWorldRenderer) worldRenderer).portal_setTransparencyShader(null);
		((IEWorldRenderer) worldRenderer).portal_setRenderDistance(renderDistance);

		if (Global.looseVisibleChunkIteration)
		{
			client.renderChunksMany = false;
		}

		//update lightmap
		if (!RenderStates.isDimensionRendered(newDimension))
		{
			helper.lightmapTexture.updateLightmap(0);
		}

		//invoke rendering
		try
		{
			invokeWrapper.accept(() ->
			{
				client.getProfiler().startSection("render_portal_content");
				client.gameRenderer.renderWorld(
						tickDelta,
						Util.nanoTime(),
						new MatrixStack()
				);
				client.getProfiler().endSection();
			});
		} catch (Throwable e)
		{
			limitedLogger.invoke(e::printStackTrace);
		}

		//recover
		SodiumInterface.switchRenderingContext.apply(worldRenderer, oldSodiumContext);

		((IEMinecraftClient) client).setWorldRenderer(oldWorldRenderer);
		client.world = oldEntityWorld;
		ieGameRenderer.setLightmapTextureManager(oldLightmap);
		TileEntityRendererDispatcher.instance.world = oldEntityWorld;
		((IEPlayerListEntry) playerListEntry).setGameMode(oldGameMode);
		client.player.noClip = oldNoClip;
		ieGameRenderer.setDoRenderHand(oldDoRenderHand);
		GlStateManager.matrixMode(GL11.GL_MODELVIEW);
		GlStateManager.popMatrix();
		((IEParticleManager) client.particles).mySetWorld(oldEntityWorld);
		client.objectMouseOver = oldCrosshairTarget;
		ieGameRenderer.setCamera(oldCamera);

		((IEWorldRenderer) oldWorldRenderer).portal_setTransparencyShader(oldTransparencyShader);
		((IEWorldRenderer) worldRenderer).portal_setTransparencyShader(newTransparencyShader);

		FogRendererContext.swappingManager.popSwapping();

		((IEWorldRenderer) oldWorldRenderer).setVisibleChunks(oldVisibleChunks);

		((IEWorldRenderer) worldRenderer).setBufferBuilderStorage(oldBufferBuilder);
		((IEMinecraftClient) client).setBufferBuilderStorage(oldClientBufferBuilder);

		((IEWorldRenderer) worldRenderer).portal_setRenderDistance(oldRenderDistance);

		if (Global.looseVisibleChunkIteration)
		{
			client.renderChunksMany = oldChunkCullingEnabled;
		}

		client.getRenderManager()
				.cacheActiveRenderInfo(
						client.world,
						oldCamera,
						client.pointedEntity
				);

		CHelper.checkGlError();

		//restore the camera entity pos
		cameraEntity.world = oldEntityWorld;
		McHelper.setEyePos(cameraEntity, oldEyePos, oldLastTickEyePos);

		resetGlStates();
	}

	/**
	 * For example the Cull assumes that the culling is enabled before using it
	 * {@link net.minecraft.client.render.RenderPhase.Cull}
	 */
	public static void resetGlStates()
	{
		GlStateManager.disableAlphaTest();
		GlStateManager.enableCull();
		GlStateManager.disableBlend();
		net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();
		Minecraft.getInstance().gameRenderer.getLightTexture().disableLightmap();
		client.gameRenderer.getOverlayTexture().teardownOverlayColor();
	}

	public static void renderPlayerItself(Runnable doRenderEntity)
	{
		EntityRendererManager entityRenderDispatcher =
				((IEWorldRenderer) client.worldRenderer).getEntityRenderDispatcher();
		NetworkPlayerInfo playerListEntry = CHelper.getClientPlayerListEntry();
		GameType originalGameMode = RenderStates.originalGameMode;

		Entity player = client.renderViewEntity;
		assert player != null;

		Vector3d oldPos = player.getPositionVec();
		Vector3d oldLastTickPos = McHelper.lastTickPosOf(player);
		GameType oldGameMode = playerListEntry.getGameType();

		McHelper.setPosAndLastTickPos(
				player, RenderStates.originalPlayerPos, RenderStates.originalPlayerLastTickPos
		);
		((IEPlayerListEntry) playerListEntry).setGameMode(originalGameMode);

		doRenderEntity.run();

		McHelper.setPosAndLastTickPos(
				player, oldPos, oldLastTickPos
		);
		((IEPlayerListEntry) playerListEntry).setGameMode(oldGameMode);
	}

	public static void resetFogState()
	{
		if (OFInterface.isFogDisabled.getAsBoolean())
		{
			return;
		}

		if (OFInterface.isShaders.getAsBoolean())
		{
			return;
		}

		forceResetFogState();
	}

	public static void forceResetFogState()
	{
		ActiveRenderInfo camera = client.gameRenderer.getActiveRenderInfo();
		float g = client.gameRenderer.getFarPlaneDistance();

		Vector3d cameraPos = camera.getProjectedView();
		double d = cameraPos.getX();
		double e = cameraPos.getY();
		double f = cameraPos.getZ();

		boolean bl2 = client.world.getDimensionRenderInfo().func_230493_a_(MathHelper.floor(d), MathHelper.floor(e)) ||
				client.ingameGUI.getBossOverlay().shouldCreateFog();

		FogRenderer.setupFog(camera, FogRenderer.FogType.FOG_TERRAIN, Math.max(g - 16.0F, 32.0F), bl2);
		FogRenderer.applyFog();
	}

	public static void updateFogColor()
	{
		FogRenderer.updateFogColor(
				client.gameRenderer.getActiveRenderInfo(),
				RenderStates.tickDelta,
				client.world,
				client.gameSettings.renderDistanceChunks,
				client.gameRenderer.getBossColorModifier(RenderStates.tickDelta)
		);
	}

	public static void resetDiffuseLighting(MatrixStack matrixStack)
	{
		RenderHelper.setupDiffuseGuiLighting(matrixStack.getLast().getMatrix());
	}

	public static void pruneRenderList(ObjectList<?> visibleChunks)
	{
		if (PortalRendering.isRendering())
		{
			if (Global.cullSectionsBehind)
			{
				// this thing has no optimization effect -_-

				PortalLike renderingPortal = PortalRendering.getRenderingPortal();

				renderingPortal.doAdditionalRenderingCull(visibleChunks);
			}
		}
	}

	// frustum culling is done elsewhere
	// it's culling the sections behind the portal
	public static void cullRenderingSections(
			ObjectList<?> visibleChunks, PortalLike renderingPortal
	)
	{
		if (renderingPortal instanceof Portal)
		{
			int firstInsideOne = Helper.indexOf(
					visibleChunks,
					obj ->
					{
						ChunkRenderDispatcher.ChunkRender builtChunk =
								((IEWorldRendererChunkInfo) obj).getBuiltChunk();
						AxisAlignedBB boundingBox = builtChunk.boundingBox;

						return FrustumCuller.isTouchingInsideContentArea(
								((Portal) renderingPortal), boundingBox
						);
					}
			);

			if (firstInsideOne != -1)
			{
				visibleChunks.removeElements(0, firstInsideOne);
			} else
			{
				visibleChunks.clear();
			}
		}
	}

}
