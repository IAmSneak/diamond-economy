package com.gmail.sneakdevs.diamondeconomy.command;

import com.gmail.sneakdevs.diamondeconomy.DiamondUtils;
import com.gmail.sneakdevs.diamondeconomy.config.DiamondEconomyConfig;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;

public class ModifyCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> buildCommand(){
        return Commands.literal(DiamondEconomyConfig.getInstance().modifyCommandName)
                .requires((permission) -> permission.hasPermission(DiamondEconomyConfig.getInstance().opCommandsPermissionLevel))
                .then(
                        Commands.argument("players", EntityArgument.players())
                                .then(
                                        Commands.argument("amount", IntegerArgumentType.integer())
                                                .executes(e -> {
                                                    int amount = IntegerArgumentType.getInteger(e, "amount");
                                                    return modifyCommand(e, EntityArgument.getPlayers(e, "players").stream().toList(), amount);
                                                }))
                )
                .then(
                        Commands.argument("amount", IntegerArgumentType.integer())
                                .then(
                                        Commands.argument("shouldModifyAll", BoolArgumentType.bool())
                                                .executes(e -> {
                                                    int amount = IntegerArgumentType.getInteger(e, "amount");
                                                    boolean shouldModifyAll = BoolArgumentType.getBool(e, "shouldModifyAll");
                                                    return modifyCommand(e, amount, shouldModifyAll);
                                                })
                                )
                                .executes(e -> {
                                    int amount = IntegerArgumentType.getInteger(e, "amount");
                                    return modifyCommand(e, amount, false);
                                })
                );
    }

    public static int modifyCommand(CommandContext<CommandSourceStack> ctx, Collection<ServerPlayer> players, int amount) {
        players.forEach(player -> ctx.getSource().sendSuccess(() -> Component.literal((DiamondUtils.getDatabaseManager().changeBalance(player.getStringUUID(), amount)) ? ("Modified " + players.size() + " players money by $" + amount) : ("That would go out of the valid money range for " + player.getName().getString())), true));
        return players.size();
    }

    public static int modifyCommand(CommandContext<CommandSourceStack> ctx, int amount, boolean shouldModifyAll) throws CommandSyntaxException {
        if (shouldModifyAll) {
            DiamondUtils.getDatabaseManager().changeAllBalance(amount);
            ctx.getSource().sendSuccess(() -> Component.literal(("Modified everyones account by " + DiamondUtils.valueString(amount))), true);
        } else {
            String output = (DiamondUtils.getDatabaseManager().changeBalance(ctx.getSource().getPlayerOrException().getStringUUID(), amount)) ? ("Modified your money by " + DiamondUtils.valueString(amount)) : ("That would go out of your valid money range");
            ctx.getSource().sendSuccess(() -> Component.literal(output), true);
        }
        return 1;
    }
}
