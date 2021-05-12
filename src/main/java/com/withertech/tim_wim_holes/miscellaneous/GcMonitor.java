package com.withertech.tim_wim_holes.miscellaneous;

import com.withertech.hiding_in_the_bushes.O_O;
import com.withertech.tim_wim_holes.Helper;
import com.withertech.tim_wim_holes.McHelper;
import com.withertech.tim_wim_holes.ModMain;
import com.withertech.tim_wim_holes.my_util.LimitedLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.WeakHashMap;

public class GcMonitor {
    private static boolean memoryNotEnough = false;
    
    private static final WeakHashMap<GarbageCollectorMXBean, Long> lastCollectCount =
        new WeakHashMap<>();
    
    private static final LimitedLogger limitedLogger = new LimitedLogger(3);
    
    @OnlyIn(Dist.CLIENT)
    public static void initClient() {
        ModMain.preGameRenderSignal.connect(GcMonitor::update);
    }
    
    public static void initCommon() {
        ModMain.postServerTickSignal.connect(() -> {
            MinecraftServer server = McHelper.getServer();
            if (server != null) {
                if (server.isDedicatedServer()) {
                    update();
                }
            }
        });
    }
    
    private static void update() {
        
        for (GarbageCollectorMXBean garbageCollectorMXBean : ManagementFactory.getGarbageCollectorMXBeans()) {
            long currCount = garbageCollectorMXBean.getCollectionCount();
            
            Long lastCount = lastCollectCount.get(garbageCollectorMXBean);
            lastCollectCount.put(garbageCollectorMXBean, currCount);
            
            if (lastCount != null) {
                if (lastCount != currCount) {
                    onGced();
                }
            }
        }
    }
    
    private static void onGced() {
        long maxMemory = Runtime.getRuntime().maxMemory();
        long totalMemory = Runtime.getRuntime().totalMemory();
        long freeMemory = Runtime.getRuntime().freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        double usage = ((double) usedMemory) / maxMemory;
        
        if (usage > 0.8) {
            if (memoryNotEnough) {
                // show message the second time
                
                if (!O_O.isDedicatedServer()) {
                    informMemoryNotEnoughClient();
                }
            }
            
            Helper.err(
                "Memory not enough. Try to Shrink loading distance or allocate more memory." +
                    " If this happens with low loading distance, it usually indicates memory leak"
            );
            
            memoryNotEnough = true;
        }
        else {
            memoryNotEnough = false;
        }
    }
    
    @OnlyIn(Dist.CLIENT)
    private static void informMemoryNotEnoughClient() {
        limitedLogger.invoke(() -> {
            Minecraft.getInstance().ingameGUI.sendChatMessage(
                ChatType.SYSTEM,
                new TranslationTextComponent("imm_ptl.memory_not_enough"),
                Util.DUMMY_UUID
            );
        });
    }
    
    public static boolean isMemoryNotEnough() {
        return memoryNotEnough;
    }
}
