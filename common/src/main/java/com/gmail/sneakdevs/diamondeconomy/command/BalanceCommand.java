package com.gmail.sneakdevs.diamondeconomy.command;

import com.gmail.sneakdevs.diamondeconomy.DiamondEconomy;
import com.gmail.sneakdevs.diamondeconomy.config.DiamondEconomyConfig;
import com.gmail.sneakdevs.diamondeconomy.sql.DatabaseManager;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

public class BalanceCommand {
    public static LiteralArgumentBuilder<ServerCommandSource> buildCommand(){
        return CommandManager.literal(DiamondEconomyConfig.getInstance().balanceCommandName)
                .then(
                        CommandManager.argument("playerName", StringArgumentType.string())
                                .executes(e -> {
                                    String string = StringArgumentType.getString(e, "playerName");
                                    return balanceCommand(e, string);
                                })
                )
                .then(
                        CommandManager.argument("player", EntityArgumentType.player())
                                .executes(e -> {
                                    String player = EntityArgumentType.getPlayer(e, "player").getName().asString();
                                    return balanceCommand(e, player);
                                })
                )
                .executes(e -> balanceCommand(e, e.getSource().getPlayer().getName().asString()));
    }

    public static int balanceCommand(CommandContext<ServerCommandSource> ctx, String player) {
        DatabaseManager dm = DiamondEconomy.getDatabaseManager();
        int bal = dm.getBalanceFromName(player);
        ctx.getSource().sendFeedback(new LiteralText((bal > -1) ? (player + " has $" + bal) : ("No account was found for player with the name \"" + player + "\"")), false);
        return 1;
    }
}
