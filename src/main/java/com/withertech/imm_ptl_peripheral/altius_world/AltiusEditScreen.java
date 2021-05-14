package com.withertech.imm_ptl_peripheral.altius_world;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.withertech.tim_wim_holes.my_util.GuiHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class AltiusEditScreen extends Screen
{


	private final AltiusScreen parent;
	private final DimEntryWidget editing;

	private final TextFieldWidget scaleField;
	private final Button flipButton;
	private final TextFieldWidget horizontalRotationField;

	private final Button backButton;

	private final GuiHelper.Rect scaleLabelRect = new GuiHelper.Rect();
	private final GuiHelper.Rect flipLabelRect = new GuiHelper.Rect();
	private final GuiHelper.Rect horizontalRotationLabelRect = new GuiHelper.Rect();

	private final Button helpButton;

	protected AltiusEditScreen(AltiusScreen parent, DimEntryWidget editing)
	{
		super(new TranslationTextComponent("imm_ptl.dim_stack_edit_screen"));

		this.parent = parent;
		this.editing = editing;

		scaleField = new TextFieldWidget(
				Minecraft.getInstance().fontRenderer,
				0, 0, 0, 20, new StringTextComponent("heh")
		);
		scaleField.setText(Double.toString(editing.entry.scale));
		scaleField.setSelectionPos(0);//without this the text won't render. mc gui is bugged
		scaleField.clampCursorPosition(0);

		flipButton = new Button(
				0, 0, 0, 20,
				new TranslationTextComponent(editing.entry.flipped ? "imm_ptl.enabled" : "imm_ptl.disabled"),
				button ->
				{
					editing.entry.flipped = !editing.entry.flipped;
					button.setMessage(
							new TranslationTextComponent(editing.entry.flipped ? "imm_ptl.enabled" : "imm_ptl.disabled")
					);
				}
		);

		horizontalRotationField = new TextFieldWidget(
				Minecraft.getInstance().fontRenderer,
				0, 0, 0, 20,
				new StringTextComponent("you cannot see me")
		);
		horizontalRotationField.setText(Double.toString(editing.entry.horizontalRotation));
		horizontalRotationField.clampCursorPosition(0);
		horizontalRotationField.setSelectionPos(0);

		backButton = new Button(
				0, 0, 0, 20, new TranslationTextComponent("imm_ptl.back"),
				button ->
				{
					try
					{
						editing.entry.horizontalRotation = Double.parseDouble(horizontalRotationField.getText());
					} catch (NumberFormatException e)
					{
						e.printStackTrace();
						editing.entry.horizontalRotation = 0;
					}

					try
					{
						editing.entry.scale = Double.parseDouble(scaleField.getText());
					} catch (NumberFormatException e)
					{
						e.printStackTrace();
						editing.entry.scale = 1;
					}

					Minecraft.getInstance().displayGuiScreen(parent);
				}
		);

		this.helpButton = AltiusScreen.createHelpButton(this);
	}

	@Override
	public void tick()
	{
		super.tick();
		scaleField.tick();
		horizontalRotationField.tick();
	}

	@Override
	protected void init()
	{
		addListener(scaleField);
		addButton(flipButton);
		addListener(horizontalRotationField);
		addButton(backButton);
		addButton(helpButton);

		GuiHelper.layout(
				0, height,
				GuiHelper.blankSpace(40),
				GuiHelper.fixedLength(20,
						GuiHelper.combine(
								GuiHelper.layoutRectVertically(scaleLabelRect),
								GuiHelper.layoutButtonVertically(scaleField)
						)
				),
				GuiHelper.blankSpace(20),
				GuiHelper.fixedLength(20,
						GuiHelper.combine(
								GuiHelper.layoutRectVertically(flipLabelRect),
								GuiHelper.layoutButtonVertically(flipButton)
						)
				),
				GuiHelper.blankSpace(20),
				GuiHelper.fixedLength(20,
						GuiHelper.combine(
								GuiHelper.layoutRectVertically(horizontalRotationLabelRect),
								GuiHelper.layoutButtonVertically(horizontalRotationField)
						)
				),
				GuiHelper.elasticBlankSpace(),
				GuiHelper.fixedLength(20, GuiHelper.layoutButtonVertically(backButton)),
				GuiHelper.blankSpace(20)
		);

		GuiHelper.layout(
				0, width,
				GuiHelper.elasticBlankSpace(),
				GuiHelper.fixedLength(100,
						GuiHelper.combine(
								GuiHelper.layoutRectHorizontally(scaleLabelRect),
								GuiHelper.layoutRectHorizontally(flipLabelRect),
								GuiHelper.layoutRectHorizontally(horizontalRotationLabelRect)
						)
				),
				GuiHelper.blankSpace(20),
				GuiHelper.fixedLength(100,
						GuiHelper.combine(
								GuiHelper.layoutButtonHorizontally(scaleField),
								GuiHelper.layoutButtonHorizontally(flipButton),
								GuiHelper.layoutButtonHorizontally(horizontalRotationField)
						)
				),
				GuiHelper.elasticBlankSpace()
		);

		GuiHelper.layout(
				0, width,
				GuiHelper.blankSpace(20),
				new GuiHelper.LayoutElement(
						true, 100,
						GuiHelper.layoutButtonHorizontally(backButton)
				),
				GuiHelper.elasticBlankSpace()
		);

		helpButton.x = width - 50;
		helpButton.y = 5;
	}

	@Override
	public void closeScreen()
	{
		Minecraft.getInstance().displayGuiScreen(parent);
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta)
	{
		this.renderBackground(matrices);

		super.render(matrices, mouseX, mouseY, delta);

		scaleField.render(matrices, mouseX, mouseY, delta);
		horizontalRotationField.render(matrices, mouseX, mouseY, delta);

		scaleLabelRect.renderTextLeft(new TranslationTextComponent("imm_ptl.scale"), matrices);
		flipLabelRect.renderTextLeft(new TranslationTextComponent("imm_ptl.flipped"), matrices);
		horizontalRotationLabelRect.renderTextLeft(new TranslationTextComponent("imm_ptl.horizontal_rotation"), matrices);
	}
}
