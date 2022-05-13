package com.gmail.sneakdevs.diamondeconomy.command;

import com.gmail.sneakdevs.diamondeconomy.DiamondEconomy;
import com.gmail.sneakdevs.diamondeconomy.config.DiamondEconomyConfig;
import com.gmail.sneakdevs.diamondeconomy.sql.DatabaseManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

import java.util.Collection;

public class ModifyCommand {
    public static LiteralArgumentBuilder<ServerCommandSource> buildCommand(){
        return CommandManager.literal(DiamondEconomyConfig.getInstance().modifyCommandName)
                .requires((permission) -> permission.hasPermissionLevel(4))
                .then(
                        CommandManager.argument("players", EntityArgumentType.players())
                                .then(
                                        CommandManager.argument("amount", IntegerArgumentType.integer())
                                                .executes(e -> {
                                                    int amount = IntegerArgumentType.getInteger(e, "amount");
                                                    return modifyCommand(e, EntityArgumentType.getPlayers(e, "players").stream().toList(), amount);
                                                }))
                );
    }

    public static int modifyCommand(CommandContext<ServerCommandSource> ctx, Collection<ServerPlayerEntity> players, int amount) {
        DatabaseManager dm = DiamondEconomy.getDatabaseManager();
        players.forEach(player -> ctx.getSource().sendFeedback(new LiteralText((dm.changeBalance(player.getUuidAsString(), amount)) ? ("Modified " + players.size() + " players money by $" + amount) : ("That would go out of the valid money range for " + player.getName().asString())), true));
        return players.size();
    }

}
