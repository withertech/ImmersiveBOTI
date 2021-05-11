package com.withertech.hiding_in_the_bushes.network;

import com.withertech.imm_boti.Helper;
import com.withertech.imm_boti.dimension_sync.DimensionIdRecord;
import com.withertech.imm_boti.dimension_sync.DimensionTypeSync;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class StcDimensionSync {
    
    private CompoundNBT idInfo;
    private CompoundNBT typeInfo;
    
    public StcDimensionSync(CompoundNBT idInfo, CompoundNBT typeInfo) {
        this.idInfo = idInfo;
        this.typeInfo = typeInfo;
    }
    
    public StcDimensionSync(PacketBuffer buf) {
        idInfo = buf.readCompoundTag();
        typeInfo = buf.readCompoundTag();
    }
    
    public void encode(PacketBuffer buf) {
        buf.writeCompoundTag(idInfo);
        buf.writeCompoundTag(typeInfo);
    }
    
    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(this::clientHandle);
        context.get().setPacketHandled(true);
    }
    
    @OnlyIn(Dist.CLIENT)
    private void clientHandle() {
        DimensionIdRecord.clientRecord = DimensionIdRecord.tagToRecord(idInfo);
        
        DimensionTypeSync.acceptTypeMapData(typeInfo);
        
        Helper.log("Received Dimension Int Id Sync");
        Helper.log("\n" + DimensionIdRecord.clientRecord);
    }
}
