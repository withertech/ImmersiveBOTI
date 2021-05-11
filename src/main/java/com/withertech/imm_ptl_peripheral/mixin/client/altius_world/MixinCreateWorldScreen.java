package com.withertech.imm_ptl_peripheral.mixin.client.altius_world;

import com.mojang.datafixers.util.Pair;
import com.withertech.imm_ptl_peripheral.altius_world.AltiusGameRule;
import com.withertech.imm_ptl_peripheral.altius_world.AltiusInfo;
import com.withertech.imm_ptl_peripheral.altius_world.AltiusManagement;
import com.withertech.imm_ptl_peripheral.altius_world.AltiusScreen;
import com.withertech.imm_ptl_peripheral.ducks.IECreateWorldScreen;
import com.withertech.imm_boti.Helper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.CreateWorldScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.WorldOptionsScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.resources.ResourcePackList;
import net.minecraft.util.datafix.codec.DatapackCodec;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.GameRules;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.gen.settings.DimensionGeneratorSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.io.File;

@Mixin(CreateWorldScreen.class)
public abstract class MixinCreateWorldScreen extends Screen implements IECreateWorldScreen {
    @Shadow
    public abstract void onClose();
    
    @Shadow
    protected DatapackCodec field_238933_b_;
    
    @Shadow
    @Nullable
    protected abstract Pair<File, ResourcePackList> func_243423_B();
    
    private Button altiusButton;
    
    @Nullable
    private AltiusScreen altiusScreen;
    
    protected MixinCreateWorldScreen(ITextComponent title) {
        super(title);
        throw new RuntimeException();
    }
    
    @Inject(
        method = "<init>(Lnet/minecraft/client/gui/screen/Screen;Lnet/minecraft/util/datafix/codec/DatapackCodec;Lnet/minecraft/client/gui/screen/WorldOptionsScreen;)V",
        at = @At("RETURN")
    )
    private void onConstructEnded(
        Screen screen, DatapackCodec dataPackSettings, WorldOptionsScreen moreOptionsDialog,
        CallbackInfo ci
    ) {
    
    }
    
    @Inject(
        method = "Lnet/minecraft/client/gui/screen/CreateWorldScreen;init()V",
        at = @At("HEAD")
    )
    private void onInitEnded(CallbackInfo ci) {
        
        altiusButton = this.addButton(new Button(
                width / 2 + 5, 151, 150, 20,
            new TranslationTextComponent("imm_ptl.altius_screen_button"),
            (buttonWidget) -> {
                openAltiusScreen();
            }
        ));
        altiusButton.visible = false;
        
    }
    
    @Inject(
        method = "Lnet/minecraft/client/gui/screen/CreateWorldScreen;showMoreWorldOptions(Z)V",
        at = @At("RETURN")
    )
    private void onMoreOptionsOpen(boolean moreOptionsOpen, CallbackInfo ci) {
        if (moreOptionsOpen) {
            altiusButton.visible = true;
        }
        else {
            altiusButton.visible = false;
        }
    }
    
    @Redirect(
        method = "Lnet/minecraft/client/gui/screen/CreateWorldScreen;createWorld()V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/Minecraft;createWorld(Ljava/lang/String;Lnet/minecraft/world/WorldSettings;Lnet/minecraft/util/registry/DynamicRegistries$Impl;Lnet/minecraft/world/gen/settings/DimensionGeneratorSettings;)V"
        )
    )
    private void redirectOnCreateLevel(
        Minecraft client, String worldName, WorldSettings levelInfo,
        DynamicRegistries.Impl registryTracker, DimensionGeneratorSettings generatorOptions
    ) {
        if (altiusScreen != null) {
            AltiusInfo info = altiusScreen.getAltiusInfo();
            
            if (info != null) {
                AltiusManagement.dimensionStackPortalsToGenerate = info;
                
                GameRules.BooleanValue rule = levelInfo.getGameRules().get(AltiusGameRule.dimensionStackKey);
                rule.set(true, null);
                
                Helper.log("Generating dimension stack world");
            }
        }
        
        client.createWorld(worldName, levelInfo, registryTracker, generatorOptions);
    }
    
    private void openAltiusScreen() {
        if (altiusScreen == null) {
            altiusScreen = new AltiusScreen((CreateWorldScreen) (Object) this);
        }
        
        Minecraft.getInstance().displayGuiScreen(altiusScreen);
    }
    
    @Override
    public ResourcePackList portal_getResourcePackManager() {
        return func_243423_B().getSecond();
    }
    
    @Override
    public DatapackCodec portal_getDataPackSettings() {
        return field_238933_b_;
    }
}
