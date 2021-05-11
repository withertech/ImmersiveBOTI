package com.withertech.imm_boti;

import com.withertech.hiding_in_the_bushes.MyNetwork;
import com.withertech.imm_boti.api.IPDimensionAPI;
import com.withertech.imm_boti.chunk_loading.ChunkDataSyncManager;
import com.withertech.imm_boti.chunk_loading.EntitySync;
import com.withertech.imm_boti.chunk_loading.NewChunkTrackingGraph;
import com.withertech.imm_boti.chunk_loading.WorldInfoSender;
import com.withertech.imm_boti.miscellaneous.GcMonitor;
import com.withertech.imm_boti.my_util.MyTaskList;
import com.withertech.imm_boti.my_util.Signal;
import com.withertech.imm_boti.portal.PortalExtension;
import com.withertech.imm_boti.portal.global_portals.GlobalPortalStorage;
import com.withertech.imm_boti.teleportation.CollisionHelper;
import com.withertech.imm_boti.teleportation.ServerTeleportationManager;

public class ModMain {
    public static final Signal postClientTickSignal = new Signal();
    public static final Signal postServerTickSignal = new Signal();
    public static final Signal preGameRenderSignal = new Signal();
    public static final MyTaskList clientTaskList = new MyTaskList();
    public static final MyTaskList serverTaskList = new MyTaskList();
    public static final MyTaskList preGameRenderTaskList = new MyTaskList();
    
    public static final MyTaskList preTotalRenderTaskList = new MyTaskList();
    
    public static final Signal clientCleanupSignal = new Signal();
    public static final Signal serverCleanupSignal = new Signal();
    
    public static void init() {
        Helper.log("Immersive Portals Mod Initializing");
        
        MyNetwork.init();
        
        postClientTickSignal.connect(clientTaskList::processTasks);
        postServerTickSignal.connect(serverTaskList::processTasks);
        preGameRenderSignal.connect(preGameRenderTaskList::processTasks);
        
        clientCleanupSignal.connect(() -> {
            if (ClientWorldLoader.getIsInitialized()) {
                clientTaskList.forceClearTasks();
            }
        });
        serverCleanupSignal.connect(serverTaskList::forceClearTasks);
        
        Global.serverTeleportationManager = new ServerTeleportationManager();
        Global.chunkDataSyncManager = new ChunkDataSyncManager();
        
        NewChunkTrackingGraph.init();
        
        WorldInfoSender.init();
        
        GlobalPortalStorage.init();
        
        EntitySync.init();
        
        CollisionHelper.init();
        
        PortalExtension.init();
        
        GcMonitor.initCommon();
    
        IPDimensionAPI.init();
        
    }
    
}
