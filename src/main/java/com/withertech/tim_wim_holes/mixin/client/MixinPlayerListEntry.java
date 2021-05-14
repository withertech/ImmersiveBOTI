package com.withertech.tim_wim_holes.mixin.client;

import com.withertech.tim_wim_holes.ducks.IEPlayerListEntry;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.world.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(NetworkPlayerInfo.class)
public class MixinPlayerListEntry implements IEPlayerListEntry
{
	@Shadow
	private GameType gameType;

	@Override
	public void setGameMode(GameType mode)
	{
		gameType = mode;
	}
}
