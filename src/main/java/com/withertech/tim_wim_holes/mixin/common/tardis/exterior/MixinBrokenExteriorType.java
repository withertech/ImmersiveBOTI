package com.withertech.tim_wim_holes.mixin.common.tardis.exterior;

import com.withertech.tim_wim_holes.ducks.IEExteriorTile;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.tardis.mod.helper.TardisHelper;
import net.tardis.mod.misc.BrokenExteriorType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BrokenExteriorType.class)
public class MixinBrokenExteriorType
{
	@Inject(method = "swapWithReal(Lnet/minecraft/world/server/ServerWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/World;Lnet/minecraft/util/Direction;)V", at = @At("TAIL"), cancellable = true, remap = false)
	public void tameTardis(ServerWorld world, BlockPos pos, World interior, Direction dir, CallbackInfo ci)
	{
		TardisHelper.getConsole(world.getServer(), interior).ifPresent(consoleTile -> consoleTile.getOrFindExteriorTile().ifPresent(exteriorTile -> ((IEExteriorTile) exteriorTile).genPortals()));
	}
}
