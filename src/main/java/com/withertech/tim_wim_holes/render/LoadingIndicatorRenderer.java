package com.withertech.tim_wim_holes.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.withertech.tim_wim_holes.portal.LoadingIndicatorEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class LoadingIndicatorRenderer extends EntityRenderer<LoadingIndicatorEntity>
{
	public LoadingIndicatorRenderer(EntityRendererManager entityRenderDispatcher_1)
	{
		super(entityRenderDispatcher_1);
	}

	@Override
	public ResourceLocation getEntityTexture(LoadingIndicatorEntity var1)
	{
		return null;
	}

	@Override
	public void render(
			LoadingIndicatorEntity entity_1,
			float float_1,
			float float_2,
			MatrixStack matrixStack_1,
			IRenderTypeBuffer vertexConsumerProvider_1,
			int int_1
	)
	{
//        String[] splited = entity_1.getText().getString().split("\n");
//        for (int i = 0; i < splited.length; i++) {
//            matrixStack_1.push();
//            matrixStack_1.translate(0, -i * 0.25 - 0.5, 0);
//            this.renderLabelIfPresent(
//                entity_1,
//                new LiteralText(splited[i]),
//                matrixStack_1,
//                vertexConsumerProvider_1,
//                int_1
//            );
//            matrixStack_1.pop();
//        }
	}
}
