package com.withertech.tim_wim_holes.mixin.common.tardis.interior;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.LazyOptional;
import net.tardis.mod.ars.ConsoleRoom;
import net.tardis.mod.config.TConfig;
import net.tardis.mod.enums.EnumDoorState;
import net.tardis.mod.exterior.AbstractExterior;
import net.tardis.mod.helper.WorldHelper;
import net.tardis.mod.sounds.TSounds;
import net.tardis.mod.tileentities.ConsoleTile;
import net.tardis.mod.tileentities.console.misc.ArtronUse;
import net.tardis.mod.tileentities.console.misc.InteriorManager;
import net.tardis.mod.tileentities.exteriors.ExteriorTile;
import net.tardis.mod.world.dimensions.TDimensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = ConsoleTile.class, remap = false)
public abstract class MixinConsoleTile extends TileEntity
{
	@Shadow
	private ConsoleRoom nextRoomToChange;
	@Shadow
	private ConsoleRoom consoleRoom;
	@Shadow
	private AbstractExterior exterior;

	public MixinConsoleTile(TileEntityType<?> tileEntityTypeIn)
	{
		super(tileEntityTypeIn);
	}

	/**
	 * @author Witherking25
	 */
	@Overwrite
	public void startInteriorChangeProcess(ServerWorld destWorld)
	{
		if (!WorldHelper.areDimensionTypesSame(destWorld, TDimensions.DimensionTypes.TARDIS_TYPE) && this.getInteriorManager().isInteriorStillRegenerating())
		{


			if (this.nextRoomToChange != null)
			{
				ServerWorld consoleWorld = this.getWorld().getServer().getWorld(this.getWorld().getDimensionKey());
				this.setConsoleRoom(this.nextRoomToChange);
				this.consoleRoom.spawnConsoleRoom(consoleWorld, false);
			}

			this.getOrFindExteriorTile().ifPresent((ext) ->
			{
				ext.setInteriorRegenerating(true);
				ext.setDoorState(EnumDoorState.CLOSED);
				ext.setLocked(true);
				ext.setAdditionalLockLevel(1);
				ext.getWorld().playSound(null, this.getPos(), this.exterior.getDoorSounds().getClosedSound(), SoundCategory.BLOCKS, 0.5F, 1.0F);
				ext.getWorld().playSound(null, this.getPos(), TSounds.DOOR_LOCK.get(), SoundCategory.BLOCKS, 0.5F, 1.0F);
			});
			int fuelUsage = TConfig.SERVER.interiorChangeArtronUse.get();
			int processingTime = this.getInteriorManager().getInteriorProcessingTime();
			ArtronUse use = this.getOrCreateArtronUse(ArtronUse.ArtronType.INTERIOR_CHANGE);
			use.setArtronUsePerTick((float) (fuelUsage / processingTime));
			use.setTicksToDrain(processingTime);
		}
	}

	@Shadow
	public abstract void setConsoleRoom(ConsoleRoom room);

	@Shadow
	public abstract InteriorManager getInteriorManager();

	@Shadow
	public abstract ArtronUse getOrCreateArtronUse(ArtronUse.IArtronType type);

	@Shadow
	public abstract LazyOptional<ExteriorTile> getOrFindExteriorTile();
}
