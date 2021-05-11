package com.withertech.hiding_in_the_bushes;


import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigClient {
    public static final ConfigClient instance;
    public static final ForgeConfigSpec spec;
    public final ForgeConfigSpec.BooleanValue compatibilityRenderMode;
    public final ForgeConfigSpec.BooleanValue doCheckGlError;
    public final ForgeConfigSpec.IntValue maxPortalLayer;
    public final ForgeConfigSpec.BooleanValue renderYourselfInPortal;
    public final ForgeConfigSpec.BooleanValue correctCrossPortalEntityRendering;
    public final ForgeConfigSpec.BooleanValue reducedPortalRendering;
    public final ForgeConfigSpec.BooleanValue pureMirror;
    public final ForgeConfigSpec.BooleanValue lagAttackProof;
    public final ForgeConfigSpec.BooleanValue modelDataFix;
    public final ForgeConfigSpec.ConfigValue<String> renderDimensionRedirect;
    
    public final String defaultDimRedirect = ModMainForge.MODID + ":alternate1->minecraft:overworld\n" +
        ModMainForge.MODID + ":alternate2->minecraft:overworld\n" +
        ModMainForge.MODID + ":alternate3->minecraft:overworld\n" +
        ModMainForge.MODID + ":alternate4->minecraft:overworld\n" +
        ModMainForge.MODID + ":alternate5->minecraft:overworld\n";
    
    public ConfigClient(ForgeConfigSpec.Builder builder) {
        compatibilityRenderMode = builder
            .comment("Used for debugging")
            .define("compatibility_render_mode", false);
        reducedPortalRendering = builder.
            comment("Reduced Portal Rendering")
            .define("reduced_portal_rendering", false);
        doCheckGlError = builder
            .comment("Used for debugging")
            .define("check_gl_error", false);
        maxPortalLayer = builder
            .comment("Max Portal-in-portal Render Layer")
            .defineInRange("max_portal_layer", 5, 1, 10);
        renderYourselfInPortal = builder
            .comment("Render Yourself In Portal")
            .define("render_yourself_in_portal", true);
        correctCrossPortalEntityRendering = builder
            .comment("...")
            .define("correct_cross_portal_entity_rendering", true);
        pureMirror = builder
            .comment("Remove the glass texture on mirrors")
            .define("pure_mirror", false);
        lagAttackProof = builder
            .comment("Render Fewer Portals When Laggy")
            .define("lag_attack_proof", true);
        modelDataFix = builder
            .comment("New model data fix. May Decrease FPS")
            .define("model_data_fix_new", true);
        renderDimensionRedirect = builder.comment(
            "..."
        ).define(
            "dimension_render_redirect",
            defaultDimRedirect
        );
    }
    
    static {
        Pair<ConfigClient, ForgeConfigSpec> pair =
            new ForgeConfigSpec.Builder().configure(ConfigClient::new);
        instance = pair.getKey();
        spec = pair.getValue();
    }
    
    public static void init() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, spec);
    }
    
    public static final String splitter = "->";
    
    public static Map<String, String> listToMap(List<String> redirectList) {
        Map<String, String> result = new HashMap<>();
        for (String s : redirectList) {
            int i = s.indexOf(splitter);
            if (i != -1) {
                result.put(
                    s.substring(0, i),
                    s.substring(i + 2)
                );
            }
            else {
                result.put(s, "???");
            }
        }
        return result;
    }
}

