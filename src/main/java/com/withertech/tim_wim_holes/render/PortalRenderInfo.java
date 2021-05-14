package com.withertech.tim_wim_holes.render;

import com.withertech.tim_wim_holes.Global;
import com.withertech.tim_wim_holes.Helper;
import com.withertech.tim_wim_holes.McHelper;
import com.withertech.tim_wim_holes.ModMain;
import com.withertech.tim_wim_holes.portal.Portal;
import com.withertech.tim_wim_holes.portal.PortalLike;
import com.withertech.tim_wim_holes.render.context_management.RenderStates;
import com.withertech.tim_wim_holes.render.context_management.WorldRenderInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.profiler.IProfiler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

// A portal's rendering related things
@OnlyIn(Dist.CLIENT)
public class PortalRenderInfo
{

	private static final WeakHashMap<Portal, PortalRenderInfo> objectMap =
			new WeakHashMap<>();
	private final Map<List<UUID>, Visibility> infoMap = new HashMap<>();

	public int thisFrameQueryFrameIndex = -1;

	private long mispredictTime1 = 0;
	private long mispredictTime2 = 0;

	private int totalMispredictCount = 0;

	private boolean needsGroupingUpdate = true;
	@Nullable
	private PortalRenderingGroup renderingGroup;

	public PortalRenderInfo()
	{

	}

	public static void init()
	{
		Portal.clientPortalTickSignal.connect(portal ->
		{
			PortalRenderInfo presentation = getOptional(portal);
			if (presentation != null)
			{
				presentation.tick(portal);
			}
		});

		Portal.portalCacheUpdateSignal.connect(portal ->
		{
			if (portal.world.isRemote())
			{
				PortalRenderInfo presentation = getOptional(portal);
				if (presentation != null)
				{
					presentation.onPortalCacheUpdate(portal);
				}
			}
		});

		Portal.portalDisposeSignal.connect(portal ->
		{
			if (portal.world.isRemote())
			{
				PortalRenderInfo presentation = getOptional(portal);
				if (presentation != null)
				{
					presentation.dispose();
					presentation.setGroup(portal, null);
					objectMap.remove(portal);
				}
			}
		});
	}

	@Nullable
	public static PortalRenderInfo getOptional(Portal portal)
	{
		Validate.isTrue(portal.world.isRemote());

		return objectMap.get(portal);
	}

	public static PortalRenderInfo get(Portal portal)
	{
		Validate.isTrue(portal.world.isRemote());

		return objectMap.computeIfAbsent(portal, k -> new PortalRenderInfo());
	}

	public static boolean renderAndDecideVisibility(PortalLike portal, Runnable queryRendering)
	{
		IProfiler profiler = Minecraft.getInstance().getProfiler();

		boolean decision;
		if (Global.offsetOcclusionQuery && portal instanceof Portal)
		{
			PortalRenderInfo presentation = get(((Portal) portal));

			List<UUID> renderingDescription = WorldRenderInfo.getRenderingDescription();

			Visibility visibility = presentation.getVisibility(renderingDescription);

			GlQueryObject lastFrameQuery = visibility.lastFrameQuery;
			GlQueryObject thisFrameQuery = visibility.acquireThisFrameQuery();

			thisFrameQuery.performQueryAnySamplePassed(queryRendering);

			boolean noPredict =
					presentation.isFrequentlyMispredicted() ||
							QueryManager.queryStallCounter <= 3;

			if (lastFrameQuery != null)
			{
				boolean lastFrameVisible = lastFrameQuery.fetchQueryResult();

				if (!lastFrameVisible && noPredict)
				{
					profiler.startSection("fetch_this_frame");
					decision = thisFrameQuery.fetchQueryResult();
					profiler.endSection();
					QueryManager.queryStallCounter++;
				} else
				{
					decision = lastFrameVisible;
					presentation.updatePredictionStatus(visibility, decision);
				}
			} else
			{
				profiler.startSection("fetch_this_frame");
				decision = thisFrameQuery.fetchQueryResult();
				profiler.endSection();
				QueryManager.queryStallCounter++;
			}
		} else
		{
			decision = QueryManager.renderAndGetDoesAnySamplePass(queryRendering);
		}
		return decision;
	}

	private static boolean canMerge(Portal p)
	{
		if (Global.forceMergePortalRendering)
		{
			return true;
		}
		return p.isRenderingMergable();
	}

	@Nullable
	public static PortalRenderingGroup getGroupOf(Portal portal)
	{
		Validate.isTrue(!portal.getIsGlobal());

		return get(portal).renderingGroup;
	}

	// Visibility Predicting -----

	private static void mergeGroup(PortalRenderingGroup g1, PortalRenderingGroup g2)
	{
		if (g1 == g2)
		{
			return;
		}

		ArrayList<Portal> g2Portals = new ArrayList<>(g2.portals);
		for (Portal portal : g2Portals)
		{
			get(portal).setGroup(portal, g1);
		}
	}

	private void tick(Portal portal)
	{
		Validate.isTrue(portal.world.isRemote());

		if (needsGroupingUpdate)
		{
			needsGroupingUpdate = false;
			updateGrouping(portal);
		}

		if (renderingGroup != null)
		{
			renderingGroup.purge();
			if (renderingGroup.portals.size() <= 1)
			{
				setGroup(portal, null);
			}
		}
	}

	// disposing twice is fine
	public void dispose()
	{
		infoMap.values().forEach(Visibility::dispose);
		infoMap.clear();
	}

	private void updateQuerySet()
	{
		if (RenderStates.frameIndex != thisFrameQueryFrameIndex)
		{

			if (RenderStates.frameIndex == thisFrameQueryFrameIndex + 1)
			{
				infoMap.entrySet().removeIf(entry ->
				{
					Visibility visibility = entry.getValue();

					return visibility.lastFrameQuery == null &&
							visibility.thisFrameQuery == null;
				});

				infoMap.values().forEach(Visibility::update);
			} else
			{
				infoMap.values().forEach(Visibility::dispose);
				infoMap.clear();
			}

			thisFrameQueryFrameIndex = RenderStates.frameIndex;
		}
	}

	@Nonnull
	private Visibility getVisibility(List<UUID> desc)
	{
		updateQuerySet();

		return infoMap.computeIfAbsent(desc, k -> new Visibility());
	}

	private void onMispredict()
	{
		mispredictTime1 = mispredictTime2;
		mispredictTime2 = System.nanoTime();
		totalMispredictCount++;
	}

	// Grouping -----

	private boolean isFrequentlyMispredicted()
	{
		if (totalMispredictCount > 5)
		{
			return true;
		}

		long currTime = System.nanoTime();

		return (currTime - mispredictTime1) < Helper.secondToNano(30);
	}

	private void updatePredictionStatus(Visibility visibility, boolean thisFrameDecision)
	{
		visibility.thisFrameRendered = thisFrameDecision;

		if (thisFrameDecision)
		{
			if (visibility.lastFrameRendered != null)
			{
				if (!visibility.lastFrameRendered)
				{
					if (!isFrequentlyMispredicted())
					{
						onMispredict();
					}
				}
			}
		}
	}

	private void onPortalCacheUpdate(Portal portal)
	{
		needsGroupingUpdate = true;
		setGroup(portal, null);
	}

	private void setGroup(Portal portal, @Nullable PortalRenderingGroup group)
	{
		if (renderingGroup != null)
		{
			renderingGroup.removePortal(portal);
		}

		renderingGroup = group;
		if (renderingGroup != null)
		{
			renderingGroup.addPortal(portal);
		}
	}

	private void updateGrouping(Portal portal)
	{
		Validate.isTrue(!portal.isGlobalPortal);

		if (!Global.enablePortalRenderingMerge)
		{
			return;
		}

		if (!canMerge(portal))
		{
			return;
		}

		List<Portal> nearbyPortals = McHelper.findEntitiesByBox(
				Portal.class,
				portal.getOriginWorld(),
				portal.getBoundingBox().grow(0.5),
				Math.min(64, portal.getSizeEstimation()) * 2 + 5,
				p -> p != portal && !Portal.isFlippedPortal(p, portal) && canMerge(p)
		);

		Portal.TransformationDesc thisDesc = portal.getTransformationDesc();

		for (Portal that : nearbyPortals)
		{
			PortalRenderInfo nearbyPortalPresentation = get(that);

			PortalRenderingGroup itsGroup = nearbyPortalPresentation.renderingGroup;
			if (itsGroup != null)
			{
				//flipped portal pairs cannot be in the same group
				if (itsGroup.portals.stream().noneMatch(p -> Portal.isFlippedPortal(p, portal)))
				{
					if (itsGroup.transformationDesc.equals(thisDesc))
					{
						if (renderingGroup == null)
						{
							// this is not in group, put into its group
							setGroup(portal, itsGroup);
						} else
						{
							// this and that are both in group, merge
							mergeGroup(renderingGroup, itsGroup);
						}
						return;
					}
				}
			} else
			{
				Portal.TransformationDesc itsDesc = that.getTransformationDesc();
				if (thisDesc.equals(itsDesc))
				{
					if (renderingGroup == null)
					{
						// this and that are not in any group
						PortalRenderingGroup newGroup = new PortalRenderingGroup(thisDesc);
						setGroup(portal, newGroup);
						get(that).setGroup(that, newGroup);
					} else
					{
						// this is in group and that is not in group
						get(that).setGroup(that, renderingGroup);
					}

					return;
				}
			}

		}

		setGroup(portal, null);
	}

	@Override
	protected void finalize() throws Throwable
	{
		super.finalize();

		// normally if a portal is removed by calling remove() it will dispose normally
		// but that cannot be guaranteed
		// use this to avoid potential resource leak
		ModMain.clientTaskList.addTask(() ->
		{
			dispose();
			return true;
		});
	}

	public static class Visibility
	{
		public GlQueryObject lastFrameQuery;
		public GlQueryObject thisFrameQuery;
		public Boolean lastFrameRendered;
		public Boolean thisFrameRendered;

		public Visibility()
		{
			lastFrameQuery = null;
			thisFrameQuery = null;
			lastFrameRendered = null;
		}

		void update()
		{
			if (lastFrameQuery != null)
			{
				GlQueryObject.returnQueryObject(lastFrameQuery);
			}
			lastFrameQuery = thisFrameQuery;
			thisFrameQuery = null;
			lastFrameRendered = thisFrameRendered;
			thisFrameRendered = null;
		}

		void dispose()
		{
			if (lastFrameQuery != null)
			{
				GlQueryObject.returnQueryObject(lastFrameQuery);
			}
			if (thisFrameQuery != null)
			{
				GlQueryObject.returnQueryObject(thisFrameQuery);
			}
		}

		GlQueryObject acquireThisFrameQuery()
		{
			if (thisFrameQuery == null)
			{
				thisFrameQuery = GlQueryObject.acquireQueryObject();
			}
			return thisFrameQuery;
		}
	}
}
