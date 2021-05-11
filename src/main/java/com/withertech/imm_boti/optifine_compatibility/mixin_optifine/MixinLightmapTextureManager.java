package com.withertech.imm_boti.optifine_compatibility.mixin_optifine;

import com.withertech.imm_boti.ClientWorldLoader;
import com.withertech.imm_boti.render.context_management.RenderDimensionRedirect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(LightTexture.class)
public class MixinLightmapTextureManager {
    @Redirect(
        method = "Lnet/minecraft/client/renderer/LightTexture;updateLightmap(F)V",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/Minecraft;world:Lnet/minecraft/client/world/ClientWorld;"
        )
    )
    ClientWorld redirectWorldInUpdate(Minecraft client) {
        return ClientWorldLoader.getWorld(RenderDimensionRedirect.getRedirectedDimension(
            client.world.getDimensionKey()
        ));
    }
}
