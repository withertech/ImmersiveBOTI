package com.withertech.imm_ptl_peripheral.altius_world;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.withertech.tim_wim_holes.api.IPDimensionAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.Dimension;
import net.minecraft.world.World;
import net.minecraft.world.gen.settings.DimensionGeneratorSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SelectDimensionScreen extends Screen
{
	public final AltiusScreen parent;
	private DimListWidget dimListWidget;
	private Button confirmButton;
	private final Consumer<RegistryKey<World>> outerCallback;
	private final Supplier<DimensionGeneratorSettings> generatorOptionsSupplier;

	protected SelectDimensionScreen(AltiusScreen parent, Consumer<RegistryKey<World>> callback, Supplier<DimensionGeneratorSettings> generatorOptionsSupplier1)
	{
		super(new TranslationTextComponent("imm_ptl.select_dimension"));
		this.parent = parent;
		this.outerCallback = callback;

		generatorOptionsSupplier = generatorOptionsSupplier1;
	}

	public static List<RegistryKey<World>> getDimensionList(
			Supplier<DimensionGeneratorSettings> generatorOptionsSupplier,
			DynamicRegistries.Impl dynamicRegistryManager
	)
	{

		DimensionGeneratorSettings generatorOptions = generatorOptionsSupplier.get();
		SimpleRegistry<Dimension> dimensionMap = generatorOptions.func_236224_e_();

		IPDimensionAPI.onServerWorldInit.emit(generatorOptions, dynamicRegistryManager);

		ArrayList<RegistryKey<World>> dimList = new ArrayList<>();

		for (Map.Entry<RegistryKey<Dimension>, Dimension> entry : dimensionMap.getEntries())
		{
			dimList.add(RegistryKey.getOrCreateKey(Registry.WORLD_KEY, entry.getKey().getLocation()));
		}

		return dimList;
	}

	@Override
	protected void init()
	{
		dimListWidget = new DimListWidget(
				width,
				height,
				20,
				height - 30,
				DimEntryWidget.widgetHeight,
				this,
				DimListWidget.Type.addDimensionList
		);
		children.add(dimListWidget);

		Consumer<DimEntryWidget> callback = w -> dimListWidget.setSelected(w);

		for (RegistryKey<World> dim : getDimensionList(this.generatorOptionsSupplier, this.parent.parent.field_238934_c_.func_239055_b_()))
		{
			dimListWidget.entryWidgets.add(new DimEntryWidget(dim, dimListWidget, callback, DimEntryWidget.Type.simple));
		}

		dimListWidget.update();

		confirmButton = this.addButton(new Button(
				this.width / 2 - 75, this.height - 28, 150, 20,
				new TranslationTextComponent("imm_ptl.confirm_select_dimension"),
				(buttonWidget) ->
				{
					DimEntryWidget selected = dimListWidget.getSelected();
					if (selected == null)
					{
						return;
					}
					outerCallback.accept(selected.dimension);
					Minecraft.getInstance().displayGuiScreen(parent);
				}
		));

	}

	@Override
	public void closeScreen()
	{
		// When `esc` is pressed return to the parent screen rather than setting screen to `null` which returns to the main menu.
		this.minecraft.displayGuiScreen(this.parent);
	}

	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float delta)
	{
		this.renderBackground(matrixStack);

		dimListWidget.render(matrixStack, mouseX, mouseY, delta);

		super.render(matrixStack, mouseX, mouseY, delta);

		drawCenteredString(
				matrixStack, this.font, this.title.getUnformattedComponentText(), this.width / 2, 20, -1
		);
	}
}
