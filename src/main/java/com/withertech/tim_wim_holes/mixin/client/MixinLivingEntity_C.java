package com.withertech.tim_wim_holes.mixin.client;

import com.withertech.tim_wim_holes.McHelper;
import com.withertech.tim_wim_holes.ducks.IEEntity;
import com.withertech.tim_wim_holes.portal.Portal;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.vector.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class MixinLivingEntity_C
{
	@Shadow
	protected double interpTargetX;

	@Shadow
	protected double interpTargetY;

	@Shadow
	protected double interpTargetZ;

	@Shadow
	protected int newPosRotationIncrements;

	//avoid entity position interpolate when crossing portal when not travelling dimension
	@Inject(
			method = "Lnet/minecraft/entity/LivingEntity;setPositionAndRotationDirect(DDDFFIZ)V",
			at = @At("RETURN")
	)
	private void onUpdateTrackedPositionAndAngles(
			double x,
			double y,
			double z,
			float yaw,
			float pitch,
			int interpolationSteps,
			boolean interpolate,
			CallbackInfo ci
	)
	{
		Portal collidingPortal = ((IEEntity) this).getCollidingPortal();
		if (collidingPortal != null)
		{
			LivingEntity this_ = ((LivingEntity) (Object) this);
			double dx = this_.getPosX() - interpTargetX;
			double dy = this_.getPosY() - interpTargetY;
			double dz = this_.getPosZ() - interpTargetZ;
			if (dx * dx + dy * dy + dz * dz > 4)
			{
				Vector3d currPos = new Vector3d(interpTargetX, interpTargetY, interpTargetZ);
				McHelper.setPosAndLastTickPos(
						this_,
						currPos,
						currPos.subtract(this_.getMotion())
				);
				McHelper.updateBoundingBox(this_);
			}
		}
	}
}
