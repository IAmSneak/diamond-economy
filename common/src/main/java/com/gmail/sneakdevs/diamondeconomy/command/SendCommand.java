package com.gmail.sneakdevs.diamondeconomy.command;

import com.gmail.sneakdevs.diamondeconomy.DiamondEconomy;
import com.gmail.sneakdevs.diamondeconomy.config.DiamondEconomyConfig;
import com.gmail.sneakdevs.diamondeconomy.sql.DatabaseManager;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;

public class SendCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> buildCommand(){
        return Commands.literal(DiamondEconomyConfig.getInstance().sendCommandName)
                .then(
                        Commands.argument("player", EntityArgument.player())
                                .then(
                                        Commands.argument("amount", IntegerArgumentType.integer(1))
                                                .executes(e -> {
                                                    ServerPlayer player = EntityArgument.getPlayer(e, "player");
                                                    int amount = IntegerArgumentType.getInteger(e, "amount");
                                                    return sendCommand(e, player, e.getSource().getPlayerOrException(), amount);
                                                })));
    }

    public static int sendCommand(CommandContext<CommandSourceStack> ctx, ServerPlayer player, ServerPlayer player1, int amount) throws CommandSyntaxException {
        DatabaseManager dm = DiamondEconomy.getDatabaseManager();
        long newValue = dm.getBalanceFromUUID(player.getStringUUID()) + amount;
        if (newValue < Integer.MAX_VALUE && dm.changeBalance(player1.getStringUUID(), -amount)) {
            dm.changeBalance(player.getStringUUID(), amount);
            player.displayClientMessage(new TextComponent("You received $" + amount + " from " + player1.getName().getString()), false);
            ctx.getSource().sendSuccess(new TextComponent("Sent $" + amount + " to " + player.getName().getString()), false);
        } else {
            ctx.getSource().sendSuccess(new TextComponent("Failed because that would go over the max value"), false);
        }
        return 1;
    }
}
