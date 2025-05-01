package net.friendlyfire.betterchat;

import net.friendlyfire.betterchat.command.SetChatColorCommand;
import net.friendlyfire.betterchat.command.SetNicknameCommand;
import net.friendlyfire.betterchat.command.SetPrefixCommand;
import net.friendlyfire.betterchat.util.ChatDataStorage;
import net.friendlyfire.betterchat.util.ChatHandler;
import net.friendlyfire.betterchat.util.PlayerChatData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.UUID;

@EventBusSubscriber(modid = BetterChatMod.MODID, bus = EventBusSubscriber.Bus.GAME)
public class ModServerEvents {
    @SubscribeEvent
    private static void onCommandRegister(RegisterCommandsEvent event) {
        new SetPrefixCommand(event.getDispatcher());
        new SetChatColorCommand(event.getDispatcher());
        new SetNicknameCommand(event.getDispatcher());
    }
    @SubscribeEvent
    private static void playerJoined(PlayerEvent.PlayerLoggedInEvent event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();
        UUID uuid = player.getUUID();
        PlayerChatData data = BetterChatMod.chatDataStorage.get(uuid);
        if (data.prefix != null && !data.prefix.isEmpty()) {
            BetterChatMod.chatHandler.setPrefix(uuid, data.prefix, player);        }
        if (data.nickname != null && !data.nickname.isEmpty()) {
            BetterChatMod.chatHandler.setNickname(uuid, data.nickname, player);        }
        if (data.chatColor != null && !data.chatColor.isEmpty()) {
            BetterChatMod.chatHandler.setChatColor(uuid, data.chatColor);        }
    }
}