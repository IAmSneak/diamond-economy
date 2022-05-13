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

import java.util.Collection;

public class SetCommand {
    public static LiteralArgumentBuilder<ServerCommandSource> buildCommand(){
        return CommandManager.literal(DiamondEconomyConfig.getInstance().setCommandName)
                .requires((permission) -> permission.hasPermissionLevel(4))
                .then(
                        CommandManager.argument("players", EntityArgumentType.players())
                                .then(
                                        CommandManager.argument("amount", IntegerArgumentType.integer(0))
                                                .executes(e -> {
                                                    int amount = IntegerArgumentType.getInteger(e, "amount");
                                                    return setCommand(e, EntityArgumentType.getPlayers(e, "players").stream().toList(), amount);
                                                }))
                );
    }

    public static int setCommand(CommandContext<ServerCommandSource> ctx, Collection<ServerPlayerEntity> players, int amount) {
        DatabaseManager dm = DiamondEconomy.getDatabaseManager();
        players.forEach(player -> dm.setBalance(player.getUuidAsString(), amount));
        ctx.getSource().sendFeedback(new LiteralText("Updated balance of " + players.size() + " players"), true);
        return players.size();
    }
}
