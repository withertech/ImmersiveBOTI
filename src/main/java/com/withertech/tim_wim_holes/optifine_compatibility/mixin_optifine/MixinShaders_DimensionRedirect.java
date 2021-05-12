package com.withertech.tim_wim_holes.optifine_compatibility.mixin_optifine;

import com.withertech.tim_wim_holes.ClientWorldLoader;
import com.withertech.tim_wim_holes.Helper;
import com.withertech.tim_wim_holes.render.context_management.RenderDimensionRedirect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;
import net.optifine.shaders.IShaderPack;
import net.optifine.shaders.ShaderPackDefault;
import net.optifine.shaders.Shaders;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(value = Shaders.class)
public class MixinShaders_DimensionRedirect {
    
    @Shadow(remap = false)
    private static ClientWorld currentWorld;
    
    @Shadow(remap = false)
    private static IShaderPack shaderPack;
    
    @Inject(method = "init", at = @At("HEAD"), remap = false)
    private static void onInit(CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        RegistryKey<World> currDimension = client.world.getDimensionKey();
        
        Helper.info("Shader init " + currDimension);
        
        if (RenderDimensionRedirect.isNoShader(currentWorld.getDimensionKey())) {
            shaderPack = new ShaderPackDefault();
            Helper.info("Set to internal shader");
        }
    }
    
    //redirect dimension for shadow camera
    @Redirect(
        method = "setCameraShadow",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/Minecraft;world:Lnet/minecraft/client/world/ClientWorld;",
            remap = true
        ),
        remap = false
    )
    private static ClientWorld redirectWorldForShadowCamera(Minecraft client) {
        return ClientWorldLoader.getWorld(RenderDimensionRedirect.getRedirectedDimension(
                client.world.getDimensionKey()
            ));
    }
    
    @Redirect(
        method = "beginRender",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/Minecraft;world:Lnet/minecraft/client/world/ClientWorld;",
            ordinal = 1,
            remap = true
        ),
        remap = false
    )
    private static ClientWorld redirectWorldInBeginRender(Minecraft client) {
        return ClientWorldLoader.getWorld(RenderDimensionRedirect.getRedirectedDimension(
                client.world.getDimensionKey()
            ));
    }
    
}
