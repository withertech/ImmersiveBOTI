package com.withertech.tim_wim_holes_peripheral.altius_world;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.withertech.tim_wim_holes.Helper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.widget.list.AbstractOptionList;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

// extending EntryListWidget.Entry is also fine
public class DimEntryWidget extends AbstractOptionList.Entry<DimEntryWidget>
{

	public final static int widgetHeight = 50;
	public final RegistryKey<World> dimension;
	public final DimListWidget parent;
	public final AltiusEntry entry;
	private final Consumer<DimEntryWidget> selectCallback;
	private final ResourceLocation dimIconPath;
	private final ITextComponent dimensionName;
	private final Type type;
	private final List<IGuiEventListener> children = new ArrayList<>();
	private boolean dimensionIconPresent = true;

	public DimEntryWidget(
			RegistryKey<World> dimension,
			DimListWidget parent,
			Consumer<DimEntryWidget> selectCallback,
			Type type
	)
	{
		this.dimension = dimension;
		this.parent = parent;
		this.selectCallback = selectCallback;
		this.type = type;

		this.dimIconPath = getDimensionIconPath(this.dimension);

		this.dimensionName = getDimensionName(dimension);

		try
		{
			Minecraft.getInstance().getResourceManager().getResource(dimIconPath);
		} catch (IOException e)
		{
			Helper.error("Cannot load texture " + dimIconPath);
			dimensionIconPresent = false;
		}

		entry = new AltiusEntry(dimension);
	}

	public static ResourceLocation getDimensionIconPath(RegistryKey<World> dimension)
	{
		ResourceLocation id = dimension.getLocation();
		return new ResourceLocation(
				id.getNamespace(),
				"textures/dimension/" + id.getPath() + ".png"
		);
	}

	private static TranslationTextComponent getDimensionName(RegistryKey<World> dimension)
	{
		return new TranslationTextComponent(
				"dimension." + dimension.getLocation().getNamespace() + "."
						+ dimension.getLocation().getPath()
		);
	}

	@Override
	public List<? extends IGuiEventListener> getEventListeners()
	{
		return children;
	}

	@Override
	public void render(
			MatrixStack matrixStack,
			int index,
			int y,
			int x,
			int rowWidth,
			int itemHeight,
			int mouseX,
			int mouseY,
			boolean bl,
			float delta
	)
	{
		Minecraft client = Minecraft.getInstance();

		client.fontRenderer.drawString(
				matrixStack, dimensionName.getString(),
				x + widgetHeight + 3, (float) (y),
				0xFFFFFFFF
		);

		client.fontRenderer.drawString(
				matrixStack, dimension.getLocation().toString(),
				x + widgetHeight + 3, (float) (y + 10),
				0xFF999999
		);

		if (dimensionIconPresent)
		{
			client.getTextureManager().bindTexture(dimIconPath);
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

			AbstractGui.blit(
					matrixStack,
					x, y, 0, (float) 0,
					widgetHeight - 4, widgetHeight - 4,
					widgetHeight - 4, widgetHeight - 4
			);
		}

		if (type == Type.withAdvancedOptions)
		{
			client.fontRenderer.drawText(
					matrixStack, getText1(),
					x + widgetHeight + 3, (float) (y + 20),
					0xFF999999
			);
			client.fontRenderer.drawText(
					matrixStack, getText2(),
					x + widgetHeight + 3, (float) (y + 30),
					0xFF999999
			);
		}
	}

	private ITextComponent getText1()
	{
		IFormattableTextComponent scaleText = entry.scale != 1.0 ?
				new TranslationTextComponent("imm_ptl.scale")
						.appendSibling(new StringTextComponent(":" + entry.scale))
				: new StringTextComponent("");

		return scaleText;
	}

	private ITextComponent getText2()
	{
		IFormattableTextComponent horizontalRotationText = entry.horizontalRotation != 0 ?
				new TranslationTextComponent("imm_ptl.horizontal_rotation")
						.appendSibling(new StringTextComponent(":" + entry.horizontalRotation))
						.appendSibling(new StringTextComponent(" "))
				: new StringTextComponent("");

		IFormattableTextComponent flippedText = entry.flipped ?
				new TranslationTextComponent("imm_ptl.flipped")
				: new StringTextComponent("");

		return horizontalRotationText.appendSibling(flippedText);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button)
	{
		selectCallback.accept(this);
		super.mouseClicked(mouseX, mouseY, button);
		return true;//allow outer dragging
		/**
		 * {@link EntryListWidget#mouseClicked(double, double, int)}
		 */
	}

	public enum Type
	{
		simple, withAdvancedOptions
	}
}
