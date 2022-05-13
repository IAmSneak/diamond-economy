package com.gmail.sneakdevs.diamondeconomy.command;

import com.gmail.sneakdevs.diamondeconomy.DiamondEconomy;
import com.gmail.sneakdevs.diamondeconomy.config.DiamondEconomyConfig;
import com.gmail.sneakdevs.diamondeconomy.sql.DatabaseManager;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

public class SendCommand {
    public static LiteralArgumentBuilder<ServerCommandSource> buildCommand(){
        return CommandManager.literal(DiamondEconomyConfig.getInstance().sendCommandName)
                .then(
                        CommandManager.argument("player", EntityArgumentType.player())
                                .then(
                                        CommandManager.argument("amount", IntegerArgumentType.integer(1))
                                                .executes(e -> {
                                                    ServerPlayerEntity player = EntityArgumentType.getPlayer(e, "player");
                                                    int amount = IntegerArgumentType.getInteger(e, "amount");
                                                    return sendCommand(e, player, e.getSource().getPlayer(), amount);
                                                })));
    }

    public static int sendCommand(CommandContext<ServerCommandSource> ctx, ServerPlayerEntity player, ServerPlayerEntity player1, int amount) throws CommandSyntaxException {
        DatabaseManager dm = DiamondEconomy.getDatabaseManager();
        long newValue = dm.getBalanceFromUUID(player.getUuidAsString()) + amount;
        if (newValue < Integer.MAX_VALUE && dm.changeBalance(player1.getUuidAsString(), -amount)) {
            dm.changeBalance(player.getUuidAsString(), amount);
            player.sendMessage(new LiteralText("You received $" + amount + " from " + player1.getName().asString()), false);
            ctx.getSource().sendFeedback(new LiteralText("Sent $" + amount + " to " + player.getName().asString()), false);
        } else {
            ctx.getSource().sendFeedback(new LiteralText("Failed because that would go over the max value"), false);
        }
        return 1;
    }
}
