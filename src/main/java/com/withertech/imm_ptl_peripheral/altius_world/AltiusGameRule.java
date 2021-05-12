package com.withertech.imm_ptl_peripheral.altius_world;

import com.withertech.imm_boti.Helper;
import com.withertech.imm_boti.McHelper;
import com.withertech.imm_boti.ModMain;
import net.minecraft.world.GameRules;

public class AltiusGameRule {
    public static GameRules.RuleKey<GameRules.BooleanValue> dimensionStackKey;
    
    
    private static boolean doUpgradeOldDimensionStack = false;
    
    public static void init() {
        dimensionStackKey = GameRules.register(
            "ipDimensionStack",
            GameRules.Category.MISC,
            GameRules.BooleanValue.create(false)
        );
        
        ModMain.postServerTickSignal.connect(AltiusGameRule::serverTick);
    }
    
    private static void serverTick() {
        if (doUpgradeOldDimensionStack) {
            setIsDimensionStack(true);
            doUpgradeOldDimensionStack = false;
            Helper.info("Upgraded old dimension stack info");
        }
    }
    
    public static boolean getIsDimensionStack() {
        return McHelper.getServer().getGameRules().get(dimensionStackKey).get();
    }
    
    public static void setIsDimensionStack(boolean cond) {
        McHelper.getServer().getGameRules()
            .get(dimensionStackKey).set(cond, McHelper.getServer());
    }
    
    public static void upgradeOldDimensionStack() {
        doUpgradeOldDimensionStack = true;
    }
}
