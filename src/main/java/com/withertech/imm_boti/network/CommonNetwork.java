package com.withertech.imm_boti.network;

import com.withertech.hiding_in_the_bushes.MyNetwork;
import com.withertech.imm_boti.McHelper;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nullable;

// common between Fabric and Forge
public class CommonNetwork {
    
    @Nullable
    public static RegistryKey<World> forceRedirect = null;
    
    public static boolean getIsProcessingRedirectedMessage() {
        return CommonNetworkClient.isProcessingRedirectedMessage;
    }
    
    public static void withForceRedirect(RegistryKey<World> dimension, Runnable func) {
        Validate.isTrue(
            McHelper.getServer().getExecutionThread() == Thread.currentThread(),
            "Maybe a mod is trying to add entity in a non-server thread. This is probably not IP's issue"
        );
        
        RegistryKey<World> oldForceRedirect = forceRedirect;
        forceRedirect = dimension;
        try {
            func.run();
        }
        finally {
            forceRedirect = oldForceRedirect;
        }
    }
    
    /**
     * If it's not null, all sent packets will be wrapped into redirected packet
     * {@link com.withertech.imm_boti.mixin.common.entity_sync.MixinServerPlayNetworkHandler_E}
     */
    @Nullable
    public static RegistryKey<World> getForceRedirectDimension() {
        return forceRedirect;
    }
    
    // avoid duplicate redirect nesting
    public static void sendRedirectedPacket(
        ServerPlayNetHandler serverPlayNetworkHandler,
        IPacket<?> packet,
        RegistryKey<World> dimension
    ) {
        if (getForceRedirectDimension() == dimension) {
            serverPlayNetworkHandler.sendPacket(packet);
        }
        else {
            serverPlayNetworkHandler.sendPacket(
                MyNetwork.createRedirectedMessage(
                    dimension,
                    packet
                )
            );
        }
    }
}
