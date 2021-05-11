package com.withertech.imm_boti.chunk_loading;

import com.withertech.hiding_in_the_bushes.MyNetwork;
import com.withertech.imm_boti.McHelper;
import com.withertech.imm_boti.ModMain;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SChangeGameStatePacket;
import net.minecraft.network.play.server.SUpdateTimePacket;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import java.util.Set;

public class WorldInfoSender {
    public static void init() {
        ModMain.postServerTickSignal.connect(() -> {
            McHelper.getServer().getProfiler().startSection("portal_send_world_info");
            if (McHelper.getServerGameTime() % 100 == 42) {
                for (ServerPlayerEntity player : McHelper.getCopiedPlayerList()) {
                    Set<RegistryKey<World>> visibleDimensions = NewChunkTrackingGraph.getVisibleDimensions(player);
                    
                    if (player.world.getDimensionKey() != World.OVERWORLD) {
                        sendWorldInfo(
                            player,
                            McHelper.getServer().getWorld(World.OVERWORLD)
                        );
                    }
                    
                    McHelper.getServer().getWorlds().forEach(thisWorld -> {
                        if (isNonOverworldSurfaceDimension(thisWorld)) {
                            if (visibleDimensions.contains(thisWorld.getDimensionKey())) {
                                sendWorldInfo(
                                    player,
                                    thisWorld
                                );
                            }
                        }
                    });
                    
                }
            }
            McHelper.getServer().getProfiler().endSection();
        });
    }
    
    //send the daytime and weather info to player when player is in nether
    public static void sendWorldInfo(ServerPlayerEntity player, ServerWorld world) {
        RegistryKey<World> remoteDimension = world.getDimensionKey();
        
        player.connection.sendPacket(
            MyNetwork.createRedirectedMessage(
                remoteDimension,
                new SUpdateTimePacket(
                    world.getGameTime(),
                    world.getDayTime(),
                    world.getGameRules().getBoolean(
                        GameRules.DO_DAYLIGHT_CYCLE
                    )
                )
            )
        );
        
        /**{@link net.minecraft.client.network.ClientPlayNetworkHandler#onGameStateChange(GameStateChangeS2CPacket)}*/
        
        if (world.isRaining()) {
            player.connection.sendPacket(MyNetwork.createRedirectedMessage(
                world.getDimensionKey(),
                new SChangeGameStatePacket(
                    SChangeGameStatePacket.RAINING,
                    0.0F
                )
            ));
        }
        else {
            //if the weather is already not raining when the player logs in then no need to sync
            //if the weather turned to not raining then elsewhere syncs it
        }
        
        player.connection.sendPacket(MyNetwork.createRedirectedMessage(
            world.getDimensionKey(),
            new SChangeGameStatePacket(
                SChangeGameStatePacket.SET_RAIN_STRENGTH,
                world.getRainStrength(1.0F)
            )
        ));
        player.connection.sendPacket(MyNetwork.createRedirectedMessage(
            world.getDimensionKey(),
            new SChangeGameStatePacket(
                SChangeGameStatePacket.SET_THUNDER_STRENGTH,
                world.getThunderStrength(1.0F)
            )
        ));
    }
    
    public static boolean isNonOverworldSurfaceDimension(World world) {
        return world.getDimensionType().hasSkyLight() && world.getDimensionKey() != World.OVERWORLD;
    }
}
