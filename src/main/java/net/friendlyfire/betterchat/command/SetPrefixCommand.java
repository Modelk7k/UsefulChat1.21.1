package net.friendlyfire.betterchat.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.friendlyfire.betterchat.BetterChatMod;
import net.friendlyfire.betterchat.util.ChatHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;


public class SetPrefixCommand {
    public SetPrefixCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("prefix")
                        .requires(source -> {
                            ServerPlayer player = source.getPlayer();
                            return player != null && player.getTags().contains("chat"); // Requires 'chat' tag
                        })
                        .then(Commands.argument("prefix", StringArgumentType.greedyString())
                                .executes(this::execute))
        );
    }
    private int execute(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return 0; // Ensure that the player exists
        String rawPrefix = StringArgumentType.getString(context, "prefix");
        if (!rawPrefix.startsWith("[")) rawPrefix = "&r[" + rawPrefix;
        if (!rawPrefix.endsWith("]")) rawPrefix = rawPrefix + "&r]";
        BetterChatMod.chatHandler.setPrefix(player.getUUID(), rawPrefix, player);
        Component parsedPrefix = ChatHandler.parseFormattedPrefix(rawPrefix);
        context.getSource().sendSuccess(
                () -> Component.literal("Set your prefix to: ").append(parsedPrefix),
                false
        );
        return 1;
    }
}