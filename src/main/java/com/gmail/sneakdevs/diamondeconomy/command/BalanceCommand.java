package com.gmail.sneakdevs.diamondeconomy.command;

import com.gmail.sneakdevs.diamondeconomy.DiamondEconomy;
import com.gmail.sneakdevs.diamondeconomy.DiamondUtils;
import com.gmail.sneakdevs.diamondeconomy.config.DiamondEconomyConfig;
import com.gmail.sneakdevs.diamondeconomy.sql.DatabaseManager;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.TextComponent;

public class BalanceCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> buildCommand(){
        return Commands.literal(DiamondEconomyConfig.getInstance().balanceCommandName)
                .then(
                        Commands.argument("playerName", StringArgumentType.string())
                                .executes(e -> {
                                    String string = StringArgumentType.getString(e, "playerName");
                                    return balanceCommand(e, string);
                                })
                )
                .then(
                        Commands.argument("player", EntityArgument.player())
                                .executes(e -> {
                                    String player = EntityArgument.getPlayer(e, "player").getName().getString();
                                    return balanceCommand(e, player);
                                })
                )
                .executes(e -> balanceCommand(e, e.getSource().getPlayerOrException().getName().getString()));
    }

    public static int balanceCommand(CommandContext<CommandSourceStack> ctx, String player) {
        DatabaseManager dm = DiamondUtils.getDatabaseManager();
        int bal = dm.getBalanceFromName(player);
        ctx.getSource().sendSuccess(new TextComponent((bal > -1) ? (player + " has $" + bal) : ("No account was found for player with the name \"" + player + "\"")), false);
        return 1;
    }
}
