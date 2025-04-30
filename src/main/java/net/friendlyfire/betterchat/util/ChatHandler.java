package net.friendlyfire.betterchat.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.ServerChatEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChatHandler {
    private static final Map<UUID, Component> PREFIXES = new HashMap<>();
    private static final Map<UUID, Component> NICKNAMES = new HashMap<>();
    private static final Map<UUID, Component> COLORS = new HashMap<>();
    private static final int MAX_VISIBLE_LENGTH = 20;
    public static void setPrefix(UUID uuid, String prefix, ServerPlayer player) {
        if (isTooLong(prefix, MAX_VISIBLE_LENGTH)) {
            player.sendSystemMessage(Component.literal("Prefix too long! Max 20 visible characters."));
            return;
        }
        Component prefixComponent = parseFormattedPrefix(prefix);
        PREFIXES.put(uuid, prefixComponent); // ✅ Store it for use in chat rendering
        PlayerChatData data = ChatDataStorage.get(uuid);
        data.prefix = prefix;
        ChatDataStorage.set(uuid, data);  // ✅ Persist to disk
        player.sendSystemMessage(Component.literal("Your prefix has been updated to: ").append(prefixComponent));
    }
    public static void setNickname(UUID uuid, String nickname, ServerPlayer player) {
        Component formattedNickname = parseFormattedPrefix(nickname);
        PlayerChatData data = ChatDataStorage.get(uuid);
        data.nickname = nickname;  // Save the raw nickname (for persistence)
        ChatDataStorage.set(uuid, data);  // Persist to disk
        NICKNAMES.put(uuid, formattedNickname);
        player.sendSystemMessage(Component.literal("Your nickname has been updated to: ").append(formattedNickname));
    }

    public static void setChatColor(UUID uuid, String color) {
        Component colorComponent = parseFormattedPrefix(color);
        PlayerChatData data = ChatDataStorage.get(uuid);
        data.chatColor = color;  // Save the color string (e.g., "&a" or "#ffaa00")
        ChatDataStorage.set(uuid, data);  // Save the updated data
        COLORS.put(uuid, colorComponent);
    }
    @SubscribeEvent
    public static void onServerChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        String message = event.getMessage().getString();
        UUID playerUUID = player.getUUID();
        Component nickname = NICKNAMES.getOrDefault(playerUUID, player.getDisplayName());
        Component prefix = PREFIXES.getOrDefault(playerUUID, Component.empty());
        PlayerChatData data = ChatDataStorage.get(playerUUID);
        Style chatStyle = Style.EMPTY;
        if (data.chatColor != null && !data.chatColor.isEmpty()) {
            Component temp = parseFormattedPrefix(data.chatColor + "msg");
            if (!temp.getSiblings().isEmpty()) {
                chatStyle = temp.getSiblings().get(0).getStyle();
            }
        }
        Component coloredMessage = Component.literal(message).setStyle(chatStyle);
        Component fullMessage = Component.empty()
                .append(prefix)
                .append(nickname)
                .append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
                .append(coloredMessage);
        if (message.startsWith("!staff ")) {
            if (player.getTags().contains("staff")) {
                String staffMsg = message.substring(7);  // Get the message after "!staff "
                Component staffPrefix = Component.literal("[Staff] ").withStyle(ChatFormatting.YELLOW);
                for (ServerPlayer p : player.getServer().getPlayerList().getPlayers()) {
                    if (p.getTags().contains("staff")) {
                        Component staffMessage = Component.empty()
                                .append(staffPrefix)
                                .append(player.getDisplayName())
                                .append(Component.literal(": " + staffMsg));
                        p.sendSystemMessage(staffMessage);
                    }
                }
            } else {
                player.sendSystemMessage(Component.literal("You are not staff.").withStyle(ChatFormatting.YELLOW));
            }
            event.setCanceled(true);
        } else {
            // Send the full message to all players
            player.getServer().getPlayerList().getPlayers().forEach(p -> {
                p.sendSystemMessage(fullMessage);
            });
            event.setCanceled(true);
        }
    }
    public static Component parseFormattedPrefix(String rawPrefix) {
        MutableComponent result = Component.empty();
        StringBuilder buffer = new StringBuilder();
        boolean bold = false, italic = false, underlined = false, strikethrough = false, obfuscated = false;
        ChatFormatting color = null;
        Style currentStyle = Style.EMPTY;
        for (int i = 0; i < rawPrefix.length(); i++) {
            char c = rawPrefix.charAt(i);
            if (c == '&' && i + 1 < rawPrefix.length()) {
                if (!buffer.isEmpty()) {
                    result.append(Component.literal(buffer.toString()).setStyle(currentStyle));
                    buffer.setLength(0);
                }
                char code = Character.toLowerCase(rawPrefix.charAt(++i));
                switch (code) {
                    case 'l' -> bold = true;
                    case 'o' -> italic = true;
                    case 'n' -> underlined = true;
                    case 'm' -> strikethrough = true;
                    case 'k' -> obfuscated = true;
                    case 'r' -> {
                        bold = italic = underlined = strikethrough = obfuscated = false;
                        color = null;
                        currentStyle = Style.EMPTY;
                        continue;
                    }
                    default -> {
                        ChatFormatting parsed = ChatFormatting.getByCode(code);
                        if (parsed != null && parsed.isColor()) {
                            color = parsed;
                        }
                    }
                }
                currentStyle = Style.EMPTY
                        .withBold(bold)
                        .withItalic(italic)
                        .withUnderlined(underlined)
                        .withStrikethrough(strikethrough)
                        .withObfuscated(obfuscated);
                if (color != null) {
                    currentStyle = currentStyle.withColor(color);
                }
            } else if (c == '#' && i + 6 < rawPrefix.length()) {
                String hex = rawPrefix.substring(i + 1, i + 7);
                try {
                    int rgb = Integer.parseInt(hex, 16);
                    if (!buffer.isEmpty()) {
                        result.append(Component.literal(buffer.toString()).setStyle(currentStyle));
                        buffer.setLength(0);
                    }
                    currentStyle = currentStyle.withColor(rgb);
                    i += 6;
                } catch (NumberFormatException ignored) {
                    buffer.append(c);
                }
            } else {
                buffer.append(c);
            }
        }
        if (!buffer.isEmpty()) {
            result.append(Component.literal(buffer.toString()).setStyle(currentStyle));
        }
        return result;
    }
    public static boolean isTooLong(String input, int maxLength) {
        int visibleCharCount = 0;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (c == '&' && i + 1 < input.length()) {
                i++; // skip & and format code
            } else if (c == '#' && i + 6 < input.length()) {
                i += 6; // skip #xxxxxx
            } else {
                visibleCharCount++;
                if (visibleCharCount > maxLength) {
                    return true;
                }
            }
        }
        return false;
    }
}