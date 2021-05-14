package com.withertech.tim_wim_holes.render.context_management;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.withertech.tim_wim_holes.ducks.IECamera;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Stack;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * A world rendering task.
 */
public class WorldRenderInfo
{

	private static final Stack<WorldRenderInfo> renderInfoStack = new Stack<>();
	/**
	 * The dimension that it's going to render
	 */
	public final ClientWorld world;
	/**
	 * Camera position
	 */
	public final Vector3d cameraPos;
	public final boolean overwriteCameraTransformation;
	/**
	 * If overwriteCameraTransformation is true,
	 * the world rendering camera transformation will be replaced by this.
	 * If overwriteCameraTransformation is false,
	 * this will be applied to the original camera transformation, and this can be null
	 */
	@Nullable
	public final Matrix4f cameraTransformation;
	/**
	 * Used for visibility prediction optimization
	 */
	@Nullable
	public final UUID description;
	/**
	 * Render distance.
	 * It cannot render the chunks that are not synced to client.
	 */
	public final int renderDistance;

	public WorldRenderInfo(
			ClientWorld world, Vector3d cameraPos,
			@Nullable Matrix4f cameraTransformation,
			boolean overwriteCameraTransformation,
			@Nullable UUID description,
			int renderDistance
	)
	{
		this.world = world;
		this.cameraPos = cameraPos;
		this.cameraTransformation = cameraTransformation;
		this.description = description;
		this.renderDistance = renderDistance;
		this.overwriteCameraTransformation = overwriteCameraTransformation;
	}

	public static void pushRenderInfo(WorldRenderInfo worldRenderInfo)
	{
		renderInfoStack.push(worldRenderInfo);
	}

	public static void popRenderInfo()
	{
		renderInfoStack.pop();
	}

	public static void adjustCameraPos(ActiveRenderInfo camera)
	{
		if (!renderInfoStack.isEmpty())
		{
			WorldRenderInfo currWorldRenderInfo = renderInfoStack.peek();
			((IECamera) camera).portal_setPos(currWorldRenderInfo.cameraPos);
		}
	}

	public static void applyAdditionalTransformations(MatrixStack matrixStack)
	{
		for (WorldRenderInfo worldRenderInfo : renderInfoStack)
		{
			if (worldRenderInfo.overwriteCameraTransformation)
			{
				matrixStack.getLast().getMatrix().setIdentity();
				matrixStack.getLast().getNormal().setIdentity();
			}

			Matrix4f matrix = worldRenderInfo.cameraTransformation;
			if (matrix != null)
			{
				matrixStack.getLast().getMatrix().mul(matrix);
				matrixStack.getLast().getNormal().mul(new Matrix3f(matrix));
			}
		}
	}

	/**
	 * it's different from {@link PortalRendering#isRendering()}
	 * when rendering cross portal third person view, this is true
	 * but {@link PortalRendering#isRendering()} is false
	 */
	public static boolean isRendering()
	{
		return !renderInfoStack.empty();
	}

	public static int getRenderingLayer()
	{
		return renderInfoStack.size();
	}

	// for example rendering portal B inside portal A will always have the same rendering description
	public static List<UUID> getRenderingDescription()
	{
		return renderInfoStack.stream()
				.map(renderInfo -> renderInfo.description).collect(Collectors.toList());
	}
}
