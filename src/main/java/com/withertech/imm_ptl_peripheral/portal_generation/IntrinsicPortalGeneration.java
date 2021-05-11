package com.withertech.imm_ptl_peripheral.portal_generation;

import com.google.common.collect.Lists;
import com.withertech.imm_boti.Global;
import com.withertech.imm_boti.portal.custom_portal_gen.CustomPortalGeneration;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import java.util.ArrayList;
import java.util.Random;

public class IntrinsicPortalGeneration {
    public static final IntrinsicNetherPortalForm intrinsicNetherPortalForm =
        new IntrinsicNetherPortalForm();
    public static final DiligentNetherPortalForm diligentNetherPortalForm =
        new DiligentNetherPortalForm();
    public static final PortalHelperForm portalHelperForm =
        new PortalHelperForm();
    
    public static final CustomPortalGeneration intrinsicToNether = new CustomPortalGeneration(
        Lists.newArrayList(World.OVERWORLD), World.THE_NETHER,
        8, 1,
        true,
        intrinsicNetherPortalForm,
        null, new ArrayList<>()
    );
    
    public static final CustomPortalGeneration intrinsicFromNether = intrinsicToNether.getReverse();
    
    public static final CustomPortalGeneration diligentToNether = new CustomPortalGeneration(
        Lists.newArrayList(World.OVERWORLD), World.THE_NETHER,
        8, 1,
        true,
        diligentNetherPortalForm,
        null, new ArrayList<>()
    );
    
    public static final CustomPortalGeneration diligentFromNether = diligentToNether.getReverse();
    
    public static final CustomPortalGeneration portalHelper = new CustomPortalGeneration(
        Lists.newArrayList(CustomPortalGeneration.anyDimension),
        CustomPortalGeneration.theSameDimension,
        1, 1,
        false, portalHelperForm,
        null, new ArrayList<>()
    );
    public final static int randomShiftFactor = 20;
    
    public static void init() {
        intrinsicToNether.identifier = new ResourceLocation("imm_ptl:intrinsic_nether_portal");
        intrinsicFromNether.identifier = new ResourceLocation("imm_ptl:intrinsic_nether_portal");
        
        diligentFromNether.identifier = new ResourceLocation("imm_ptl:intrinsic_diligent_nether_portal");
        diligentToNether.identifier = new ResourceLocation("imm_ptl:intrinsic_diligent_nether_portal");
        
        portalHelper.identifier = new ResourceLocation("imm_ptl:intrinsic_portal_helper");
    }
    
    public static boolean onFireLitOnObsidian(
        ServerWorld fromWorld,
        BlockPos firePos
    ) {
        RegistryKey<World> fromDimension = fromWorld.getDimensionKey();
        
        if (fromDimension == World.OVERWORLD) {
            CustomPortalGeneration gen =
                Global.netherPortalMode == Global.NetherPortalMode.normal ?
                    IntrinsicPortalGeneration.intrinsicToNether :
                    diligentToNether;
            return gen.perform(fromWorld, firePos, null);
        }
        else if (fromDimension == World.THE_NETHER) {
            CustomPortalGeneration gen =
                Global.netherPortalMode == Global.NetherPortalMode.normal ?
                    IntrinsicPortalGeneration.intrinsicFromNether :
                    diligentFromNether;
            return gen.perform(fromWorld, firePos, null);
        }
        
        return false;
    }
    
    public static BlockPos getRandomShift() {
        Random rand = new Random();
        return new BlockPos(
            (rand.nextDouble() * 2 - 1) * randomShiftFactor,
            (rand.nextDouble() * 2 - 1) * randomShiftFactor,
            (rand.nextDouble() * 2 - 1) * randomShiftFactor
        );
    }
    
    public static boolean activatePortalHelper(
        ServerWorld fromWorld,
        BlockPos firePos
    ) {
        return portalHelper.perform(fromWorld, firePos, null);
    }
}
