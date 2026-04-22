package net.vova.epicenchantments;

import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.vova.epicenchantments.block.ModBlocks;
import net.vova.epicenchantments.command.ReportCommand;
import net.vova.epicenchantments.command.TimerCommand;
import net.vova.epicenchantments.command.TimerManager;
import net.vova.epicenchantments.enchantments.ModEnchantments;
import net.vova.epicenchantments.entity.ModEntities;
import net.vova.epicenchantments.entity.client.ModModelLayers;
import net.vova.epicenchantments.entity.client.AirModel;
import net.vova.epicenchantments.entity.client.AirRenderer;
import net.vova.epicenchantments.entity.custom.AirEntity;
import net.vova.epicenchantments.item.ModItems;
import net.vova.epicenchantments.sounds.CustomSounds;
import net.vova.epicenchantments.villager.ModProfessions;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Mod(EpicEnchantments.MODID)
public class EpicEnchantments {
    public static final String MODID = "epicenchantments";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final String SERVER_URL = "http://localhost:8080";

    public EpicEnchantments(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        ModEnchantments.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModProfessions.register(modEventBus);
        CustomSounds.register(modEventBus);
        ModEntities.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);

        LOGGER.info("Epic Enchantments mod loaded!");
    }


    // ✅ НОВЫЙ КОД: Регистрация атрибутов сущности
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class CommonModEvents {
        @SubscribeEvent
        public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
            event.put(ModEntities.AIR.get(), AirEntity.createAttributes().build());
            LOGGER.info("Rhino attributes registered!");
        }
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        TimerCommand.register(event.getDispatcher());
        ReportCommand.register(event.getDispatcher());
        LOGGER.info("Timer commands registered!");
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() != null) {
            TimerManager.removeTimer(event.getEntity());
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            try {
                registerPoiTypeBlockStates(ModProfessions.ENCHANTER_POI.get(), Blocks.ENCHANTING_TABLE);
            } catch (Exception e) {
                LOGGER.error("Failed to register PoiType block states: {}", e.getMessage());
            }
        });
        LOGGER.info("Common setup for Epic Enchantments");
    }

    private void registerPoiTypeBlockStates(PoiType poiType, Block block) {
        // Автоматически вызывается Forge
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Epic Enchantments is ready on server!");
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                EntityRenderers.register(ModEntities.AIR.get(), AirRenderer::new);
                createMusicFolder();
            });
        }

        @SubscribeEvent
        public static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
            event.registerLayerDefinition(ModModelLayers.AIR_LAYER, AirModel::createBodyLayer);
        }
    }

    private static void createMusicFolder() {
        try {
            Path musicFolder = Paths.get(System.getProperty("user.home"), "AppData", "Roaming",".minecraft", "sounds");

            System.out.println("🔍 Проверяю папку: " + musicFolder.toAbsolutePath());

            if (!Files.exists(musicFolder)) {
                Files.createDirectories(musicFolder);
                System.out.println("✅ Папка создана: " + musicFolder.toAbsolutePath());
            } else {
                System.out.println("📁 Папка уже существует: " + musicFolder.toAbsolutePath());
            }

            Path readmePath = musicFolder.resolve("README.txt");
            if (!Files.exists(readmePath)) {
                String readme = getReadmeText();
                Files.writeString(readmePath, readme);
                System.out.println("✅ README.txt создан");
            }
        } catch (IOException e) {
            System.err.println("❌ Ошибка при создании папки: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String getReadmeText() {
        return """
        ============================================
        MUSIC MOD - INSTRUCTIONS
        ============================================
        
        📁 How to add music:
        1. Convert your songs to .ogg format
           (Use Audacity or online-converter.com)
        
        2. Copy .ogg files to this folder:
           → .minecraft/sounds/
        
        3. The mod will automatically load all .ogg files
        
        🎵 Supported formats: .ogg only
        
        🎮 Controls (default keys):
           M - Play/Pause
           ↑ - Volume Up
           ↓ - Volume Down
           ← - Previous song
           → - Next song
           Pg Down - Slow down
           Pg Up - Speed up
        
        📝 Requirements:
           - File names can be in any language
           - No spaces recommended
           - Example: my_song.ogg, any.ogg
        
        Enjoy your music! 🎧
        ============================================
        """;
    }
}