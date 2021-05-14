package com.withertech.imm_ptl_peripheral.altius_world;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.withertech.imm_ptl_peripheral.alternate_dimension.AlternateDimensions;
import com.withertech.tim_wim_holes.CHelper;
import com.withertech.tim_wim_holes.Global;
import com.withertech.tim_wim_holes.Helper;
import com.withertech.tim_wim_holes.ModMain;
import com.withertech.tim_wim_holes.my_util.GuiHelper;
import com.withertech.tim_wim_holes.my_util.MyTaskList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.CreateWorldScreen;
import net.minecraft.client.gui.screen.DirtMessageScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.gen.settings.DimensionGeneratorSettings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@OnlyIn(Dist.CLIENT)
public class AltiusScreen extends Screen
{
	public final DimListWidget dimListWidget;
	private final Button backButton;
	private final Button toggleButton;
	private final Button addDimensionButton;
	private final Button removeDimensionButton;
	private final Button editButton;

	private final Button helpButton;
	private final Supplier<DimensionGeneratorSettings> generatorOptionsSupplier1;
	public boolean isEnabled = false;
	public boolean loopEnabled = false;
	CreateWorldScreen parent;
	private int titleY;

	public AltiusScreen(CreateWorldScreen parent)
	{
		super(new TranslationTextComponent("imm_ptl.altius_screen"));
		this.parent = parent;

		toggleButton = new Button(
				0, 0, 150, 20,
				new TranslationTextComponent("imm_ptl.toggle_altius"),
				(buttonWidget) ->
				{
					setEnabled(!isEnabled);
				}
		);

		backButton = new Button(
				0, 0, 72, 20,
				new TranslationTextComponent("imm_ptl.back"),
				(buttonWidget) ->
				{
					Minecraft.getInstance().displayGuiScreen(parent);
				}
		);
		addDimensionButton = new Button(
				0, 0, 72, 20,
				new TranslationTextComponent("imm_ptl.dim_stack_add"),
				(buttonWidget) ->
				{
					onAddEntry();
				}
		);
		removeDimensionButton = new Button(
				0, 0, 72, 20,
				new TranslationTextComponent("imm_ptl.dim_stack_remove"),
				(buttonWidget) ->
				{
					onRemoveEntry();
				}
		);

		editButton = new Button(
				0, 0, 72, 20,
				new TranslationTextComponent("imm_ptl.dim_stack_edit"),
				(buttonWidget) ->
				{
					onEditEntry();
				}
		);

		dimListWidget = new DimListWidget(
				width,
				height,
				100,
				200,
				DimEntryWidget.widgetHeight,
				this,
				DimListWidget.Type.mainDimensionList
		);

		Consumer<DimEntryWidget> callback = getElementSelectCallback();
		if (Global.enableAlternateDimensions)
		{
			dimListWidget.entryWidgets.add(createDimEntryWidget(AlternateDimensions.alternate5));
			dimListWidget.entryWidgets.add(createDimEntryWidget(AlternateDimensions.alternate1));
		}
		dimListWidget.entryWidgets.add(createDimEntryWidget(World.OVERWORLD));
		dimListWidget.entryWidgets.add(createDimEntryWidget(World.THE_NETHER));

		generatorOptionsSupplier1 = Helper.cached(() ->
		{
			DimensionGeneratorSettings rawGeneratorOptions =
					this.parent.field_238934_c_.func_239054_a_(false);
			return WorldCreationDimensionHelper.getPopulatedGeneratorOptions(
					this.parent, rawGeneratorOptions
			);
		});

		helpButton = createHelpButton(this);
	}

	public static Button createHelpButton(Screen parent)
	{
		return new Button(
				0, 0, 30, 20,
				new StringTextComponent("?"),
				button ->
				{
					CHelper.openLinkConfirmScreen(
							parent, "https://qouteall.fun/immptl/wiki/Dimension-Stack"
					);
				}
		);
	}

	private DimEntryWidget createDimEntryWidget(RegistryKey<World> dimension)
	{
		return new DimEntryWidget(dimension, dimListWidget, getElementSelectCallback(), DimEntryWidget.Type.withAdvancedOptions);
	}

	@Nullable
	public AltiusInfo getAltiusInfo()
	{
		if (isEnabled)
		{
			return new AltiusInfo(
					dimListWidget.entryWidgets.stream().map(
							dimEntryWidget -> dimEntryWidget.entry
					).collect(Collectors.toList()), loopEnabled
			);
		} else
		{
			return null;
		}
	}

	@Override
	protected void init()
	{

		addButton(toggleButton);
		addButton(backButton);
		addButton(addDimensionButton);
		addButton(removeDimensionButton);

		addButton(editButton);

		addButton(helpButton);

		setEnabled(isEnabled);

		children.add(dimListWidget);

		dimListWidget.update();

		GuiHelper.layout(
				0, height,
				GuiHelper.blankSpace(5),
				new GuiHelper.LayoutElement(true, 20, (from, to) ->
				{
					helpButton.x = width - 50;
					helpButton.y = from;
					helpButton.setWidth(30);
				}),
				new GuiHelper.LayoutElement(true, 20, (a, b) ->
				{
					toggleButton.x = 10;
					toggleButton.y = a;
				}),
				GuiHelper.blankSpace(5),
				new GuiHelper.LayoutElement(false, 1, (from, to) ->
				{
					dimListWidget.updateSize(
							width, height,
							from, to
					);
				}),
				GuiHelper.blankSpace(5),
				new GuiHelper.LayoutElement(true, 20, (from, to) ->
				{
					backButton.y = from;
					addDimensionButton.y = from;
					removeDimensionButton.y = from;
					editButton.y = from;
					GuiHelper.layout(
							0, width,
							GuiHelper.blankSpace(10),
							new GuiHelper.LayoutElement(
									false, 1,
									GuiHelper.layoutButtonHorizontally(backButton)
							),
							GuiHelper.blankSpace(5),
							new GuiHelper.LayoutElement(
									false, 1,
									GuiHelper.layoutButtonHorizontally(addDimensionButton)
							),
							GuiHelper.blankSpace(5),
							new GuiHelper.LayoutElement(
									false, 1,
									GuiHelper.layoutButtonHorizontally(removeDimensionButton)
							),
							GuiHelper.blankSpace(5),
							new GuiHelper.LayoutElement(
									false, 1,
									GuiHelper.layoutButtonHorizontally(editButton)
							),
							GuiHelper.blankSpace(10)
					);
				}),
				GuiHelper.blankSpace(5)
		);
	}

	@Override
	public void closeScreen()
	{
		// When `esc` is pressed return to the parent screen rather than setting screen to `null` which returns to the main menu.
		this.minecraft.displayGuiScreen(this.parent);
	}

	private Consumer<DimEntryWidget> getElementSelectCallback()
	{
		return w -> dimListWidget.setSelected(w);
	}

	@Override
	public void render(MatrixStack matrixStack, int mouseY, int i, float f)
	{
		this.renderBackground(matrixStack);


		if (isEnabled)
		{
			dimListWidget.render(matrixStack, mouseY, i, f);
		}

		super.render(matrixStack, mouseY, i, f);

		FontRenderer textRenderer = Minecraft.getInstance().fontRenderer;
		textRenderer.drawTextWithShadow(
				matrixStack, this.title,
				20, 10, -1
		);

	}

	private void setEnabled(boolean cond)
	{
		isEnabled = cond;
		if (isEnabled)
		{
			toggleButton.setMessage(new TranslationTextComponent("imm_ptl.altius_toggle_true"));
		} else
		{
			toggleButton.setMessage(new TranslationTextComponent("imm_ptl.altius_toggle_false"));
		}
		addDimensionButton.visible = isEnabled;
		removeDimensionButton.visible = isEnabled;

		editButton.visible = isEnabled;
	}

	private void onAddEntry()
	{
		DimEntryWidget selected = dimListWidget.getSelected();

		int position;
		if (selected == null)
		{
			position = 0;
		} else
		{
			position = dimListWidget.entryWidgets.indexOf(selected);
		}

		if (position < 0 || position > dimListWidget.entryWidgets.size())
		{
			position = -1;
		}

		int insertingPosition = position + 1;

		Minecraft.getInstance().displayGuiScreen(
				new DirtMessageScreen(new TranslationTextComponent("imm_ptl.loading_datapack_dimensions"))
		);

		ModMain.preTotalRenderTaskList.addTask(MyTaskList.withDelay(1, () ->
		{
			Minecraft.getInstance().displayGuiScreen(
					new SelectDimensionScreen(
							this,
							dimensionType ->
							{
								dimListWidget.entryWidgets.add(
										insertingPosition,
										createDimEntryWidget(dimensionType)
								);
								removeDuplicate(insertingPosition);
								dimListWidget.update();
							}, generatorOptionsSupplier1
					)
			);
			return true;
		}));
	}

	private void onRemoveEntry()
	{
		DimEntryWidget selected = dimListWidget.getSelected();
		if (selected == null)
		{
			return;
		}

		int position = dimListWidget.entryWidgets.indexOf(selected);

		if (position == -1)
		{
			return;
		}

		dimListWidget.entryWidgets.remove(position);
		dimListWidget.update();
	}

	private void onEditEntry()
	{
		DimEntryWidget selected = dimListWidget.getSelected();
		if (selected == null)
		{
			return;
		}

		Minecraft.getInstance().displayGuiScreen(new AltiusEditScreen(
				this, selected
		));
	}

	private void removeDuplicate(int insertedIndex)
	{
		RegistryKey<World> inserted = dimListWidget.entryWidgets.get(insertedIndex).dimension;
		for (int i = dimListWidget.entryWidgets.size() - 1; i >= 0; i--)
		{
			if (dimListWidget.entryWidgets.get(i).dimension == inserted)
			{
				if (i != insertedIndex)
				{
					dimListWidget.entryWidgets.remove(i);
				}
			}
		}
	}

}
