package com.withertech.imm_boti.mixin.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.withertech.imm_boti.CGlobal;
import com.withertech.imm_boti.ClientWorldLoader;
import com.withertech.imm_boti.ModMain;
import com.withertech.imm_boti.ModMainClient;
import com.withertech.imm_boti.ducks.IEGameRenderer;
import com.withertech.imm_boti.render.CrossPortalThirdPersonView;
import com.withertech.imm_boti.render.GuiPortalRendering;
import com.withertech.imm_boti.render.MyRenderHelper;
import com.withertech.imm_boti.render.context_management.PortalRendering;
import com.withertech.imm_boti.render.context_management.RenderStates;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.math.vector.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer implements IEGameRenderer {
    @Shadow
    @Final
    @Mutable
    private LightTexture lightmapTexture;
    
    @Shadow
    private boolean renderHand;
    @Shadow
    @Final
    @Mutable
    private ActiveRenderInfo activeRender;
    
    @Shadow
    @Final
    private Minecraft mc;
    
    @Shadow
    private int rendererUpdateCount;
    
    @Shadow
    private boolean debugView;
    
    @Shadow
    public abstract void resetProjectionMatrix(Matrix4f matrix4f);
    
    @Inject(method = "Lnet/minecraft/client/renderer/GameRenderer;updateCameraAndRender(FJZ)V", at = @At("HEAD"))
    private void onFarBeforeRendering(
        float tickDelta,
        long nanoTime,
        boolean renderWorldIn,
        CallbackInfo ci
    ) {
        ModMain.preTotalRenderTaskList.processTasks();
        if (mc.world == null) {
            return;
        }
        RenderStates.updatePreRenderInfo(tickDelta);
        CGlobal.clientTeleportationManager.manageTeleportation(RenderStates.tickDelta);
        ModMain.preGameRenderSignal.emit();
        if (CGlobal.earlyClientLightUpdate) {
            MyRenderHelper.earlyUpdateLight();
        }
        
        RenderStates.frameIndex++;
    }
    
    //before rendering world (not triggered when rendering portal)
    @Inject(
        method = "Lnet/minecraft/client/renderer/GameRenderer;updateCameraAndRender(FJZ)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/GameRenderer;renderWorld(FJLcom/mojang/blaze3d/matrix/MatrixStack;)V"
        )
    )
    private void onBeforeRenderingCenter(
        float float_1,
        long long_1,
        boolean boolean_1,
        CallbackInfo ci
    ) {
        ModMainClient.switchToCorrectRenderer();
        
        CGlobal.renderer.prepareRendering();
    }
    
    //after rendering world (not triggered when rendering portal)
    @Inject(
        method = "Lnet/minecraft/client/renderer/GameRenderer;updateCameraAndRender(FJZ)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/GameRenderer;renderWorld(FJLcom/mojang/blaze3d/matrix/MatrixStack;)V",
            shift = At.Shift.AFTER
        )
    )
    private void onAfterRenderingCenter(
        float float_1,
        long long_1,
        boolean boolean_1,
        CallbackInfo ci
    ) {
        CGlobal.renderer.finishRendering();
        
        RenderStates.onTotalRenderEnd();
        
        GuiPortalRendering.onGameRenderEnd();
    }
    
    //special rendering in third person view
    @Redirect(
        method = "Lnet/minecraft/client/renderer/GameRenderer;updateCameraAndRender(FJZ)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/GameRenderer;renderWorld(FJLcom/mojang/blaze3d/matrix/MatrixStack;)V"
        )
    )
    private void redirectRenderingWorld(
        GameRenderer gameRenderer, float tickDelta, long limitTime, MatrixStack matrix
    ) {
        if (CrossPortalThirdPersonView.renderCrossPortalThirdPersonView()) {
            return;
        }
        
        gameRenderer.renderWorld(tickDelta, limitTime, matrix);
    }
    
    @Inject(method = "Lnet/minecraft/client/renderer/GameRenderer;renderWorld(FJLcom/mojang/blaze3d/matrix/MatrixStack;)V", at = @At("TAIL"))
    private void onRenderCenterEnded(
        float float_1,
        long long_1,
        MatrixStack matrixStack_1,
        CallbackInfo ci
    ) {
        CGlobal.renderer.onRenderCenterEnded(matrixStack_1);
    }
    
    //resize all world renderers when resizing window
    @Inject(method = "Lnet/minecraft/client/renderer/GameRenderer;updateShaderGroupSize(II)V", at = @At("RETURN"))
    private void onOnResized(int int_1, int int_2, CallbackInfo ci) {
        if (ClientWorldLoader.getIsInitialized()) {
            ClientWorldLoader.worldRendererMap.values().stream()
                .filter(
                    worldRenderer -> worldRenderer != mc.worldRenderer
                )
                .forEach(
                    worldRenderer -> worldRenderer.createBindEntityOutlineFbs(int_1, int_2)
                );
        }
    }
    
    private static boolean portal_isRenderingHand = false;
    
    @Inject(method = "Lnet/minecraft/client/renderer/GameRenderer;renderHand(Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/ActiveRenderInfo;F)V", at = @At("HEAD"))
    private void onRenderHandBegins(MatrixStack matrices, ActiveRenderInfo camera, float tickDelta, CallbackInfo ci) {
        portal_isRenderingHand = true;
    }
    
    @Inject(method = "Lnet/minecraft/client/renderer/GameRenderer;renderHand(Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/ActiveRenderInfo;F)V", at = @At("RETURN"))
    private void onRenderHandEnds(MatrixStack matrices, ActiveRenderInfo camera, float tickDelta, CallbackInfo ci) {
        portal_isRenderingHand = false;
    }
    
    //View bobbing will make the camera pos offset to actual camera pos
    //Teleportation is based on camera pos. If the teleportation is incorrect
    //then rendering will have problem
    //So smoothly disable view bobbing when player is near a portal
    @Redirect(
        method = "Lnet/minecraft/client/renderer/GameRenderer;applyBobbing(Lcom/mojang/blaze3d/matrix/MatrixStack;F)V",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/matrix/MatrixStack;translate(DDD)V"
        )
    )
    private void redirectBobViewTranslate(MatrixStack matrixStack, double x, double y, double z) {
        double viewBobFactor = portal_isRenderingHand ? 1 : RenderStates.viewBobFactor;
        matrixStack.translate(x * viewBobFactor, y * viewBobFactor, z * viewBobFactor);
    }
    
    @Redirect(
        method = "Lnet/minecraft/client/renderer/GameRenderer;renderWorld(FJLcom/mojang/blaze3d/matrix/MatrixStack;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/GameRenderer;resetProjectionMatrix(Lnet/minecraft/util/math/vector/Matrix4f;)V"
        )
    )
    private void redirectLoadProjectionMatrix(GameRenderer gameRenderer, Matrix4f matrix4f) {
        if (PortalRendering.isRendering()) {
            //load recorded projection matrix
            resetProjectionMatrix(RenderStates.projectionMatrix);
        }
        else {
            //load projection matrix normally
            resetProjectionMatrix(matrix4f);
            
            //record projection matrix
            if (RenderStates.projectionMatrix == null) {
                RenderStates.projectionMatrix = matrix4f;
            }
        }
    }
    
    @Override
    public void setLightmapTextureManager(LightTexture manager) {
        lightmapTexture = manager;
    }
    
    @Override
    public boolean getDoRenderHand() {
        return renderHand;
    }
    
    @Override
    public void setDoRenderHand(boolean e) {
        renderHand = e;
    }
    
    @Override
    public void setCamera(ActiveRenderInfo camera_) {
        activeRender = camera_;
    }
    
    @Override
    public void setIsRenderingPanorama(boolean cond) {
        debugView = cond;
    }
}
