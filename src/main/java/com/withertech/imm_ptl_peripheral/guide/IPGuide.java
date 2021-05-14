package com.withertech.imm_ptl_peripheral.guide;

import com.withertech.tim_wim_holes.Global;
import com.withertech.tim_wim_holes.McHelper;
import com.withertech.tim_wim_holes.network.CommonNetworkClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

@OnlyIn(Dist.CLIENT)
public class IPGuide
{
	private static GuideInfo guideInfo = new GuideInfo();

	private static GuideInfo readFromFile()
	{
		File storageFile = getStorageFile();

		if (storageFile.exists())
		{

			GuideInfo result = null;
			try (FileReader fileReader = new FileReader(storageFile))
			{
				result = Global.gson.fromJson(fileReader, GuideInfo.class);
			} catch (IOException e)
			{
				e.printStackTrace();
				return new GuideInfo();
			}

			if (result == null)
			{
				return new GuideInfo();
			}

			return result;
		}

		return new GuideInfo();
	}

	private static File getStorageFile()
	{
		return new File(Minecraft.getInstance().gameDir, "imm_ptl_state.json");
	}

	private static void writeToFile(GuideInfo guideInfo)
	{
		try (FileWriter fileWriter = new FileWriter(getStorageFile()))
		{

			Global.gson.toJson(guideInfo, fileWriter);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public static void initClient()
	{
		guideInfo = readFromFile();

		CommonNetworkClient.clientPortalSpawnSignal.connect(p ->
		{
			ClientPlayerEntity player = Minecraft.getInstance().player;

			if (!guideInfo.wikiInformed)
			{
				if (player != null && player.isCreative())
				{
					guideInfo.wikiInformed = true;
					writeToFile(guideInfo);
					informWithURL(
							"https://qouteall.fun/immptl/wiki/Portal-Customization",
							new TranslationTextComponent("imm_ptl.inform_wiki")
					);
				}
			}
		});
	}

	public static void onClientPlacePortalHelper()
	{
		if (!guideInfo.portalHelperInformed)
		{
			guideInfo.portalHelperInformed = true;
			writeToFile(guideInfo);

			informWithURL(
					"https://qouteall.fun/immptl/wiki/Portal-Customization#portal-helper-block",
					new TranslationTextComponent("imm_ptl.inform_portal_helper")
			);
		}
	}

	private static void informWithURL(String link, IFormattableTextComponent text)
	{
		Minecraft.getInstance().ingameGUI.sendChatMessage(
				ChatType.SYSTEM,
				text.appendSibling(
						McHelper.getLinkText(link)
				),
				Util.DUMMY_UUID
		);
	}

	public static class GuideInfo
	{
		public boolean wikiInformed = false;
		public boolean portalHelperInformed = false;

		public GuideInfo()
		{
		}
	}

	@OnlyIn(Dist.CLIENT)
	public static class RemoteCallables
	{
		public static void showWiki()
		{
			informWithURL(
					"https://qouteall.fun/immptl/wiki/Commands-Reference",
					new StringTextComponent("")
			);
		}
	}
}
