package net.friendlyfire.betterchat;

import net.friendlyfire.betterchat.util.ChatDataStorage;
import net.friendlyfire.betterchat.util.ChatHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

import java.io.File;

@Mod(BetterChatMod.MODID)
public class BetterChatMod {
    public static ChatDataStorage chatDataStorage;
    public static ChatHandler chatHandler;
    public static final String MODID = "betterchat";
    public BetterChatMod(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        NeoForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::addCreative);
        NeoForge.EVENT_BUS.addListener(ChatHandler::onServerChat);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }
    private void commonSetup(final FMLCommonSetupEvent event) {
    }
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
    }
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        File worldDir = event.getServer().getWorldPath(LevelResource.ROOT).toFile();
        chatDataStorage = new ChatDataStorage(worldDir);
        chatHandler = new ChatHandler(chatDataStorage);
    }
    @SubscribeEvent
    public void onServerStopped(ServerStoppedEvent event) {
        chatDataStorage = null; // clear reference
    }

    @EventBusSubscriber(modid = BetterChatMod.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
        }
    }

}
