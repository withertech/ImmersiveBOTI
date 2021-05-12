package com.withertech.tim_wim_holes.mixin.client;

import com.withertech.tim_wim_holes.render.context_management.WorldRenderInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Entity.class)
public class MixinEntity_C {
    // avoid invisible armor stands to be visible through portal
    @Redirect(
        method = "Lnet/minecraft/entity/Entity;isInvisibleToPlayer(Lnet/minecraft/entity/player/PlayerEntity;)Z",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/player/PlayerEntity;isSpectator()Z"
        )
    )
    private boolean redirectIsSpectator(PlayerEntity playerEntity) {
        if (WorldRenderInfo.isRendering()) {
            return false;
        }
        return playerEntity.isSpectator();
    }
}
