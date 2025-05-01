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

public class SetNicknameCommand {
    public SetNicknameCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("nickname")
                        .requires(source -> {
                            ServerPlayer player = source.getPlayer();
                            return player != null && player.getTags().contains("chat"); // Requires 'chat' tag
                        })
                        .then(Commands.argument("nickname", StringArgumentType.greedyString())
                                .executes(this::execute))
        );
    }
    private int execute(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        String rawNickname = StringArgumentType.getString(context, "nickname");
        Component parsedNickname = ChatHandler.parseFormattedPrefix(rawNickname);
        BetterChatMod.chatHandler.setNickname(player.getUUID(), rawNickname, player);
        context.getSource().sendSuccess(
                () -> Component.literal("Set your nickname to: ").append(parsedNickname),
                false
        );
        return 1;
    }
}
