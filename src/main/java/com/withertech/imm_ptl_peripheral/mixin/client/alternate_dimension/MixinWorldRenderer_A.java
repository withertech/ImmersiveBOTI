package com.withertech.imm_ptl_peripheral.mixin.client.alternate_dimension;

import net.minecraft.client.renderer.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer_A {
//    //avoid alternate dimension dark sky in low y
//    @Redirect(
//        method = "renderSky",
//        at = @At(
//            value = "INVOKE",
//            target = "Lnet/minecraft/client/world/ClientWorld$Properties;getSkyDarknessHeight()D"
//        )
//    )
//    private double redirectGetSkyDarknessHeight(ClientWorld.Properties properties) {
//        if (AlternateDimensions.isAlternateDimension(MinecraftClient.getInstance().world)) {
//            return -10000;
//        }
//        return properties.getSkyDarknessHeight();
//    }
}
