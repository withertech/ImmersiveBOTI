package com.withertech.hiding_in_the_bushes.network;

import com.withertech.tim_wim_holes.dimension_sync.DimId;
import com.withertech.tim_wim_holes.portal.global_portals.GlobalPortalStorage;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class StcUpdateGlobalPortals {
    private CompoundNBT data;
    private RegistryKey<World> dimensionType;
    
    public StcUpdateGlobalPortals(
        CompoundNBT data,
        RegistryKey<World> dimensionType
    ) {
        this.data = data;
        this.dimensionType = dimensionType;
    }
    
    public StcUpdateGlobalPortals(PacketBuffer buf) {
        dimensionType = DimId.readWorldId(buf, true);
        data = buf.readCompoundTag();
    }
    
    public void encode(PacketBuffer buf) {
        DimId.writeWorldId(buf,dimensionType,false);
        buf.writeCompoundTag(data);
    }
    
    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(this::clientHandle);
        context.get().setPacketHandled(true);
    }
    
    @OnlyIn(Dist.CLIENT)
    private void clientHandle() {
        GlobalPortalStorage.receiveGlobalPortalSync(dimensionType, data);
    }
}
