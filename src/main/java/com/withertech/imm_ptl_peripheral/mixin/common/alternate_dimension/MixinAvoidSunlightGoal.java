package com.withertech.imm_ptl_peripheral.mixin.common.alternate_dimension;

import com.withertech.tim_wim_holes.Helper;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.ai.goal.RestrictSunGoal;
import net.minecraft.pathfinding.GroundPathNavigator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RestrictSunGoal.class)
public class MixinAvoidSunlightGoal
{
	@Shadow
	@Final
	private CreatureEntity entity;

	//fix crash
	@Inject(
			method = "Lnet/minecraft/entity/ai/goal/RestrictSunGoal;resetTask()V",
			at = @At("HEAD"),
			cancellable = true
	)
	private void onStop(CallbackInfo ci)
	{
		if (!(entity.getNavigator() instanceof GroundPathNavigator))
		{
			Helper.error("Avoid sunlight goal abnormal");
			ci.cancel();
		}
	}
}
