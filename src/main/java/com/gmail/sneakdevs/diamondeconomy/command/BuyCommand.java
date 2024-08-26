package com.gmail.sneakdevs.diamondeconomy.command;

import com.gmail.sneakdevs.diamondeconomy.CurrencyType;
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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;

public class BuyCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> buildCommand(CommandBuildContext commandBuildContext) {
        return Commands.literal(DiamondEconomyConfig.getInstance().buyCommandName)
                .then(Commands.literal("price")
                        .then(Commands.argument("item", ItemArgument.item(commandBuildContext))
                                .executes(ctx -> {
                                    final ItemInput item = ItemArgument.getItem(ctx, "item");
                                    return buyPriceCommand(ctx, item.getItem());
                                })
                        )
                )
                .then(Commands.argument("item", ItemArgument.item(commandBuildContext))
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                .executes(ctx -> {
                                    final int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                    final ItemInput item = ItemArgument.getItem(ctx, "item");
                                    return buyItemCommand(ctx, item.getItem(), amount);
                                })
                        )
                        .executes(ctx -> {
                            final ItemInput item = ItemArgument.getItem(ctx, "item");
                            return buyItemCommand(ctx, item.getItem(), 1);
                        })
                );
    }

    public static int buyItemCommand(CommandContext<CommandSourceStack> ctx, Item item, int amount) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        DatabaseManager dm = DiamondUtils.getDatabaseManager();
        CurrencyType currency = DiamondUtils.getDatabaseManager().getCurrency(BuiltInRegistries.ITEM.getKey(item).toString());
        if (currency != null && currency.canBuy()) {
            if (dm.changeBalance(player.getStringUUID(), -amount*currency.getBuyValue())) {
                DiamondUtils.dropItem(amount, item, player);
                String output = "You bought " + amount + " items for " + DiamondUtils.valueString(amount*currency.getBuyValue());
                ctx.getSource().sendSuccess(() -> Component.literal(output), false);
            } else {
                ctx.getSource().sendSuccess(() -> Component.literal("Not enough money to buy " + amount + " " + item.getDefaultInstance().getDisplayName()), false);
            }
        } else {
            ctx.getSource().sendSuccess(() -> Component.literal("That item cannot be bought!"), false);
        }
        return 1;
    }

    public static int buyPriceCommand(CommandContext<CommandSourceStack> ctx, Item item) {
        CurrencyType currency = DiamondUtils.getDatabaseManager().getCurrency(BuiltInRegistries.ITEM.getKey(item).toString());
        if (currency != null && currency.canBuy()) {
            ctx.getSource().sendSuccess(() -> Component.literal("That item can be bought for " + DiamondUtils.valueString(currency.getBuyValue())), false);
        } else {
            ctx.getSource().sendSuccess(() -> Component.literal("That item cannot be bought!"), false);
        }
        return 1;
    }
}
