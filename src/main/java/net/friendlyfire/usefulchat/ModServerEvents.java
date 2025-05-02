package net.friendlyfire.usefulchat;

import net.friendlyfire.usefulchat.command.SetChatColorCommand;
import net.friendlyfire.usefulchat.command.SetNicknameCommand;
import net.friendlyfire.usefulchat.command.SetPrefixCommand;
import net.friendlyfire.usefulchat.util.PlayerChatData;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import java.util.UUID;

@EventBusSubscriber(modid = UsefulChat.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
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
        PlayerChatData data = UsefulChat.chatDataStorage.get(uuid);
        if (data.prefix != null && !data.prefix.isEmpty()) {
            UsefulChat.chatHandler.setPrefix(uuid, data.prefix, player);        }
        if (data.nickname != null && !data.nickname.isEmpty()) {
            UsefulChat.chatHandler.setNickname(uuid, data.nickname, player);        }
        if (data.chatColor != null && !data.chatColor.isEmpty()) {
            UsefulChat.chatHandler.setChatColor(uuid, data.chatColor);        }
    }
}