package com.withertech.hiding_in_the_bushes.network;

import com.withertech.hiding_in_the_bushes.ModMainForge;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketDirection;
import net.minecraft.network.ProtocolType;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class NetworkMain {
    private static final String protocol_version = "1";
    public static final SimpleChannel channel = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(ModMainForge.MODID, "main"),
        () -> protocol_version,
        protocol_version::equals,
        protocol_version::equals
    );
    
    public static void init() {
        channel.registerMessage(
            0,
            StcRedirected.class,
            StcRedirected::encode,
            StcRedirected::new,
            StcRedirected::handle
        );
        channel.registerMessage(
            1,
            StcDimensionConfirm.class,
            StcDimensionConfirm::encode,
            StcDimensionConfirm::new,
            StcDimensionConfirm::handle
        );
        channel.registerMessage(
            2,
            StcDimensionSync.class,
            StcDimensionSync::encode,
            StcDimensionSync::new,
            StcDimensionSync::handle
        );
        channel.registerMessage(
            3,
            CtsTeleport.class,
            CtsTeleport::encode,
            CtsTeleport::new,
            CtsTeleport::handle
        );
        channel.registerMessage(
            4,
            StcUpdateGlobalPortals.class,
            StcUpdateGlobalPortals::encode,
            StcUpdateGlobalPortals::new,
            StcUpdateGlobalPortals::handle
        );
        channel.registerMessage(
            6,
            StcSpawnEntity.class,
            StcSpawnEntity::encode,
            StcSpawnEntity::new,
            StcSpawnEntity::handle
        );
        channel.registerMessage(
            7,
            CtsPlayerAction.class,
            CtsPlayerAction::encode,
            CtsPlayerAction::new,
            CtsPlayerAction::handle
        );
        channel.registerMessage(
            8,
            CtsRightClick.class,
            CtsRightClick::encode,
            CtsRightClick::new,
            CtsRightClick::handle
        );
    }
    
    public static <T> void sendToServer(T t) {
        channel.sendToServer(t);
    }
    
    public static <T> void sendToPlayer(ServerPlayerEntity player, T t) {
        channel.send(PacketDistributor.PLAYER.with(() -> player), t);
    }
    
    public static void sendRedirected(
        ServerPlayerEntity player,
        RegistryKey<World> dimension,
        IPacket t
    ) {
        sendToPlayer(player, new StcRedirected(dimension, t));
    }
    
    public static IPacket getRedirectedPacket(
        RegistryKey<World> dimension,
        IPacket t
    ) {
        return channel.toVanillaPacket(
            new StcRedirected(dimension, t), NetworkDirection.PLAY_TO_CLIENT
        );
    }
    
    public static IPacket createEmptyPacketByType(
        int messageType
    ) {
        return ProtocolType.PLAY.getPacket(PacketDirection.CLIENTBOUND, messageType);
    }
}
