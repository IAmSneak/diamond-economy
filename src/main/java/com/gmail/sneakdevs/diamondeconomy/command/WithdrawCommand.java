package com.gmail.sneakdevs.diamondeconomy.command;

import com.gmail.sneakdevs.diamondeconomy.DiamondUtils;
import com.gmail.sneakdevs.diamondeconomy.config.DiamondEconomyConfig;
import com.gmail.sneakdevs.diamondeconomy.sql.DatabaseManager;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class WithdrawCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> buildCommand(CommandBuildContext commandBuildContext){
        return Commands.literal(DiamondEconomyConfig.getInstance().withdrawCommandName)
                .then(
                        Commands.argument("amount", IntegerArgumentType.integer(1))
                                .executes(e -> {
                                    int amount = IntegerArgumentType.getInteger(e, "amount");
                                    return withdrawCommand(e, amount);
                                })
                )
                .then(Commands.argument("item", ItemArgument.item(commandBuildContext))
                        .then(
                                Commands.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(e -> {
                                            int amount = IntegerArgumentType.getInteger(e, "amount");
                                            final ItemInput item = ItemArgument.getItem(e, "item");
                                            return withdrawCommand(e, item, amount);
                                        })
                        )
                );
    }

    public static int withdrawCommand(CommandContext<CommandSourceStack> ctx, int amount) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        DatabaseManager dm = DiamondUtils.getDatabaseManager();
        if (dm.changeBalance(player.getStringUUID(), -amount)) {
            ctx.getSource().sendSuccess(() -> Component.literal("Withdrew " + DiamondUtils.valueString(amount - DiamondUtils.dropCurrency(amount, player))), false);
        } else {
            ctx.getSource().sendSuccess(() -> Component.literal("You have less than " + DiamondUtils.valueString(amount)), false);
        }
        return 1;
    }

    public static int withdrawCommand(CommandContext<CommandSourceStack> ctx, ItemInput item, int amount) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        DatabaseManager dm = DiamondUtils.getDatabaseManager();
        if (dm.changeBalance(player.getStringUUID(), -amount)) {
            ctx.getSource().sendSuccess(() -> Component.literal("Withdrew " + DiamondUtils.valueString(amount - DiamondUtils.dropCurrency(amount, item.getItem(), player))), false);
        } else {
            ctx.getSource().sendSuccess(() -> Component.literal("You have less than " + DiamondUtils.valueString(amount)), false);
        }
        return 1;
    }

}
