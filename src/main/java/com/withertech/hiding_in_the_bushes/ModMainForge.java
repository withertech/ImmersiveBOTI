package com.withertech.hiding_in_the_bushes;

import com.withertech.imm_ptl_peripheral.PeripheralModMain;
import com.withertech.tim_wim_holes.Global;
import com.withertech.tim_wim_holes.Helper;
import com.withertech.tim_wim_holes.ModMain;
import com.withertech.tim_wim_holes.ModMainClient;
import com.withertech.tim_wim_holes.portal.LoadingIndicatorEntity;
import com.withertech.tim_wim_holes.portal.Portal;
import com.withertech.tim_wim_holes.portal.global_portals.GlobalPortalStorage;
import com.withertech.tim_wim_holes.render.LoadingIndicatorRenderer;
import com.withertech.tim_wim_holes.render.PortalEntityRenderer;
import com.withertech.tim_wim_holes.render.context_management.RenderDimensionRedirect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.FMLPlayMessages.SpawnEntity;
import net.minecraftforge.registries.IForgeRegistry;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(ModMainForge.MODID)
public class ModMainForge {
    public static final String MODID = "tim_wim_holes";
    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();
    
    public static boolean enableModelDataFix = true;
    
    public ModMainForge() {
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        // Register the enqueueIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        // Register the processIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);
        
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        
        ConfigClient.init();
        ConfigServer.init();
    }
    
    @OnlyIn(Dist.CLIENT)
    private static void initPortalRenderers() {
        EntityRendererManager manager = Minecraft.getInstance().getRenderManager();
        
        Arrays.stream(new EntityType<?>[]{
            Portal.entityType/*,
            NetherPortalEntity.entityType,
            EndPortalEntity.entityType,
            Mirror.entityType,
            BreakableMirror.entityType,
            GlobalTrackedPortal.entityType,
            WorldWrappingPortal.entityType,
            VerticalConnectingPortal.entityType,
            GeneralBreakablePortal.entityType*/
        }).peek(
            Validate::notNull
        ).forEach(
            entityType -> manager.register(
                entityType,
                (EntityRenderer) new PortalEntityRenderer(manager)
            )
        );
        
        manager.register(
            LoadingIndicatorEntity.entityType,
            new LoadingIndicatorRenderer(manager)
        );
    }
    
    private void setup(final FMLCommonSetupEvent event) {
        // concurrent init may have issues
        DeferredWorkQueue.runLater(() -> {
            ModMain.init();
            PeripheralModMain.init();
        });
    }
    
    private void doClientStuff(final FMLClientSetupEvent event) {
        Minecraft.getInstance().execute(() -> {
            ModMainClient.init();
            
            PeripheralModMain.initClient();
            
            ConfigClient instance = ConfigClient.instance;
            if (instance.compatibilityRenderMode.get()) {
                Global.renderMode = Global.RenderMode.compatibility;
                Helper.info("Initially Switched to Compatibility Render Mode");
            }
            Global.doCheckGlError = instance.doCheckGlError.get();
            Helper.info("Do Check Gl Error: " + Global.doCheckGlError);
            Global.renderYourselfInPortal = instance.renderYourselfInPortal.get();
            Global.maxPortalLayer = instance.maxPortalLayer.get();
            Global.correctCrossPortalEntityRendering =
                instance.correctCrossPortalEntityRendering.get();
            Global.reducedPortalRendering = instance.reducedPortalRendering.get();
            Global.pureMirror = instance.pureMirror.get();
            Global.lagAttackProof = instance.lagAttackProof.get();
            enableModelDataFix = instance.modelDataFix.get();
            RenderDimensionRedirect.updateIdMap(
                ConfigClient.listToMap(
                    Arrays.asList(
                        instance.renderDimensionRedirect.get().split("\n")
                    )
                )
            );
            
//            Validate.notNull(PeripheralModMain.portalHelperBlock);
//            RenderTypeLookup.setRenderLayer(
//                PeripheralModMain.portalHelperBlock, RenderType.getCutout()
//            );
        });
        
        initPortalRenderers();
    }
    
    private void enqueueIMC(final InterModEnqueueEvent event) {
    
    }
    
    private void processIMC(final InterModProcessEvent event) {
    
    }
    
    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
    
    }

    public static void applyServerConfigs() {
        ConfigServer instance = ConfigServer.instance;
//        Global.netherPortalFindingRadius = instance.portalSearchingRange.get();
        Global.indirectLoadingRadiusCap = instance.indirectLoadingRadiusCap.get();
        Global.activeLoading = instance.activeLoadRemoteChunks.get();
        Global.teleportationDebugEnabled = instance.teleportationDebug.get();
        Global.multiThreadedNetherPortalSearching = instance.multiThreadedNetherPortalSearching.get();
        Global.looseMovementCheck = instance.looseMovementCheck.get();
//        Global.enableAlternateDimensions = instance.enableAlternateDimensions.get();
//        Global.netherPortalMode = instance.netherPortalMode.get();
//        Global.endPortalMode = instance.endPortalMode.get();
    }
    
    @SubscribeEvent
    public void onModelRegistry(ModelRegistryEvent event) {
    
    }
    
    public static void checkMixinState() {
    }
    
    public static boolean isMixinInClasspath() {
        try {
            Class.forName("org.spongepowered.asm.launch.Phases");
            return true;
        }
        catch (ClassNotFoundException e) {
            return false;
        }
    }
    
    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        checkMixinState();
    }
    
    @SubscribeEvent
    public void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!Global.serverTeleportationManager.isFiringMyChangeDimensionEvent) {
            PlayerEntity player = event.getPlayer();
            if (player instanceof ServerPlayerEntity) {
                GlobalPortalStorage.onPlayerLoggedIn((ServerPlayerEntity) player);
            }
        }
    }
    
    // You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {


//        @SubscribeEvent
//        public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
//            IForgeRegistry<Block> registry = blockRegistryEvent.getRegistry();
//
//            PortalPlaceholderBlock.instance = new PortalPlaceholderBlock(
//                Block.Properties.create(Material.PORTAL)
//                    .doesNotBlockMovement()
//                    .sound(SoundType.GLASS)
//                    .hardnessAndResistance(99999, 0)
//                    .setLightLevel(s -> 15)
//            );
//            PortalPlaceholderBlock.instance.setRegistryName(
//                new ResourceLocation(ModMainForge.MODID, "portal_placeholder")
//            );
//            registry.register(
//                PortalPlaceholderBlock.instance
//            );
//
//            PeripheralModMain.portalHelperBlock = new Block(Block.Properties.create(Material.IRON));
//            PeripheralModMain.portalHelperBlock.setRegistryName(
//                new ResourceLocation(ModMainForge.MODID, "portal_helper")
//            );
//            registry.register(
//                PeripheralModMain.portalHelperBlock
//            );
//        }
        
//        @SubscribeEvent
//        public static void onItemRegistry(final RegistryEvent.Register<Item> event) {
//            IForgeRegistry<Item> registry = event.getRegistry();
//
//            PeripheralModMain.portalHelperBlockItem = new BlockItem(
//                PeripheralModMain.portalHelperBlock,
//                new Item.Properties().group(ItemGroup.MISC)
//            );
//            PeripheralModMain.portalHelperBlockItem.setRegistryName(
//                new ResourceLocation(ModMainForge.MODID, "portal_helper")
//            );
//            registry.register(
//                PeripheralModMain.portalHelperBlockItem
//            );
//        }
        
        private static <T extends Entity> void registerEntity(
            Consumer<EntityType<T>> setEntityType,
            Supplier<EntityType<T>> getEntityType,
            String id,
            EntityType.IFactory<T> constructor,
            IForgeRegistry<EntityType<?>> registry
        ) {
            BiFunction<SpawnEntity, World, T> biFunction = (a, world) -> constructor.create(getEntityType.get(), world);
            EntityType<T> entityType = EntityType.Builder.create(
                constructor, EntityClassification.MISC
            ).size(
                1, 1
            ).immuneToFire().setCustomClientFactory(
                biFunction
            ).setTrackingRange(96).build(
                id
            );
            setEntityType.accept(entityType);
            
            registry.register(entityType.setRegistryName(id));
        }
        
        @SubscribeEvent
        public static void onEntityRegistry(RegistryEvent.Register<EntityType<?>> event) {
            
            IForgeRegistry<EntityType<?>> registry = event.getRegistry();
            registerEntity(
                o -> Portal.entityType = o,
                () -> Portal.entityType,
                ModMainForge.MODID + ":portal",
                Portal::new,
                registry
            );
/*            registerEntity(
                o -> NetherPortalEntity.entityType = o,
                () -> NetherPortalEntity.entityType,
                ModMainForge.MODID + ":nether_portal_new",
                NetherPortalEntity::new,
                registry
            );

            registerEntity(
                o -> EndPortalEntity.entityType = o,
                () -> EndPortalEntity.entityType,
                ModMainForge.MODID + ":end_portal",
                EndPortalEntity::new,
                registry
            );

            registerEntity(
                o -> Mirror.entityType = o,
                () -> Mirror.entityType,
                ModMainForge.MODID + ":mirror",
                Mirror::new,
                registry
            );

            registerEntity(
                o -> BreakableMirror.entityType = o,
                () -> BreakableMirror.entityType,
                ModMainForge.MODID + ":breakable_mirror",
                BreakableMirror::new,
                registry
            );

            registerEntity(
                o -> GlobalTrackedPortal.entityType = o,
                () -> GlobalTrackedPortal.entityType,
                ModMainForge.MODID + ":global_tracked_portal",
                GlobalTrackedPortal::new,
                registry
            );

            registerEntity(
                o -> WorldWrappingPortal.entityType = o,
                () -> WorldWrappingPortal.entityType,
                ModMainForge.MODID + ":border_portal",
                WorldWrappingPortal::new,
                registry
            );

            registerEntity(
                o -> VerticalConnectingPortal.entityType = o,
                () -> VerticalConnectingPortal.entityType,
                ModMainForge.MODID + ":end_floor_portal",
                VerticalConnectingPortal::new,
                registry
            );

            registerEntity(
                o -> GeneralBreakablePortal.entityType = o,
                () -> GeneralBreakablePortal.entityType,
                ModMainForge.MODID + ":general_breakable_portal",
                GeneralBreakablePortal::new,
                registry
            );

            LoadingIndicatorEntity.entityType = EntityType.Builder.create(
                LoadingIndicatorEntity::new, EntityClassification.MISC
            ).size(
                1, 1
            ).immuneToFire().setCustomClientFactory((a, world) ->
                new LoadingIndicatorEntity(LoadingIndicatorEntity.entityType, world)
            ).build(
                ModMainForge.MODID + ":loading_indicator"
            );
            event.getRegistry().register(
                LoadingIndicatorEntity.entityType.setRegistryName(
                    ModMainForge.MODID + ":loading_indicator")
            );*/
        }
    }
    
}
