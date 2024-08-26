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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class SellCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> buildCommand(CommandBuildContext commandBuildContext) {
        return Commands.literal(DiamondEconomyConfig.getInstance().sellCommandName)
                .then(Commands.literal("hand")
                        .executes(ctx -> sellItemCommand(ctx, ctx.getSource().getPlayerOrException().getMainHandItem().getItem(), Integer.MAX_VALUE))
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                .executes(ctx -> {
                                    final int maxAmount = IntegerArgumentType.getInteger(ctx, "amount");
                                    return sellItemCommand(ctx, ctx.getSource().getPlayerOrException().getMainHandItem().getItem(), maxAmount);
                                })
                        )
                )
                .then(Commands.literal("price")
                        .then(Commands.argument("item", ItemArgument.item(commandBuildContext))
                                .executes(ctx -> {
                                    final ItemInput item = ItemArgument.getItem(ctx, "item");
                                    return sellPriceCommand(ctx, item.getItem());
                                })
                        )
                        .then(Commands.literal("hand")
                                .executes(ctx -> sellPriceCommand(ctx, ctx.getSource().getPlayerOrException().getMainHandItem().getItem()))
                        )
                )
                .then(Commands.argument("item", ItemArgument.item(commandBuildContext))
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                .executes(ctx -> {
                                    final int maxAmount = IntegerArgumentType.getInteger(ctx, "amount");
                                    final ItemInput item = ItemArgument.getItem(ctx, "item");
                                    return sellItemCommand(ctx, item.getItem(), maxAmount);
                                })
                        )
                        .executes(ctx -> {
                            final ItemInput item = ItemArgument.getItem(ctx, "item");
                            return sellItemCommand(ctx, item.getItem(), Integer.MAX_VALUE);
                        })
                );
    }

    public static int sellItemCommand(CommandContext<CommandSourceStack> ctx, Item item, int maxAmount) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        DatabaseManager dm = DiamondUtils.getDatabaseManager();
        CurrencyType currency = DiamondUtils.getDatabaseManager().getCurrency(BuiltInRegistries.ITEM.getKey(item).toString());
        if (currency != null && currency.canSell()) {
            int sellCount = 0;
            for (int j = 0; j < player.getInventory().getContainerSize(); j++) {
                if (player.getInventory().getItem(j).getItem().equals(currency.getItem())) {
                    sellCount += player.getInventory().getItem(j).getCount();
                    player.getInventory().setItem(j, new ItemStack(Items.AIR));
                    if (sellCount >= maxAmount) {
                        DiamondUtils.dropItem(sellCount - maxAmount, item, player);
                        sellCount = maxAmount;
                        break;
                    }
                }
            }

            if (dm.changeBalance(player.getStringUUID(), sellCount * currency.getSellValue())) {
                String output = "Added " + DiamondUtils.valueString(sellCount * currency.getSellValue()) + " to your account and sold " + sellCount + " items";
                ctx.getSource().sendSuccess(() -> Component.literal(output), false);
            } else {
                DiamondUtils.dropItem(sellCount, item, player);
                ctx.getSource().sendSuccess(() -> Component.literal("You have too much money in your account"), false);
            }
        } else {
            ctx.getSource().sendSuccess(() -> Component.literal("That item cannot be sold!"), false);
        }
        return 1;
    }

    public static int sellPriceCommand(CommandContext<CommandSourceStack> ctx, Item item) {
        CurrencyType currency = DiamondUtils.getDatabaseManager().getCurrency(BuiltInRegistries.ITEM.getKey(item).toString());
        if (currency != null && currency.canSell()) {
            ctx.getSource().sendSuccess(() -> Component.literal("That item can be sold for " + DiamondUtils.valueString(currency.getSellValue())), false);
        } else {
            ctx.getSource().sendSuccess(() -> Component.literal("That item cannot be sold!"), false);
        }
        return 1;
    }
}
