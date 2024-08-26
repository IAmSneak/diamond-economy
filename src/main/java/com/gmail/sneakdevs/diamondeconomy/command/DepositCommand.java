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

public class DepositCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> buildCommand(CommandBuildContext commandBuildContext) {
        return Commands.literal(DiamondEconomyConfig.getInstance().depositCommandName)
                .executes(ctx -> depositAllCommand(ctx, Integer.MAX_VALUE))
                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                        .executes(ctx -> {
                            final int maxAmount = IntegerArgumentType.getInteger(ctx, "amount");
                            return depositAllCommand(ctx, maxAmount);
                        })
                )
                .then(Commands.literal("hand")
                        .executes(ctx -> depositItemCommand(ctx, ctx.getSource().getPlayerOrException().getMainHandItem().getItem(), Integer.MAX_VALUE))
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                .executes(ctx -> {
                                    final int maxAmount = IntegerArgumentType.getInteger(ctx, "amount");
                                    return depositItemCommand(ctx, ctx.getSource().getPlayerOrException().getMainHandItem().getItem(), maxAmount);
                                })
                        )
                )
                .then(Commands.argument("item", ItemArgument.item(commandBuildContext))
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                .executes(ctx -> {
                                    final int maxAmount = IntegerArgumentType.getInteger(ctx, "amount");
                                    final ItemInput item = ItemArgument.getItem(ctx, "item");
                                    return depositItemCommand(ctx, item.getItem(), maxAmount);
                                })
                        )
                        .executes(ctx -> {
                            final ItemInput item = ItemArgument.getItem(ctx, "item");
                            return depositItemCommand(ctx, item.getItem(), Integer.MAX_VALUE);
                        })
                );
    }

    public static int depositAllCommand(CommandContext<CommandSourceStack> ctx, int maxAmount) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        DatabaseManager dm = DiamondUtils.getDatabaseManager();
        int currencyCount = 0;
        currency: for (int i = 0; i < DiamondUtils.getCurrencyList().size(); i++) {
            CurrencyType currency = DiamondUtils.getCurrencyList().get(i);
            for (int j = 0; j < player.getInventory().getContainerSize(); j++) {
                if (player.getInventory().getItem(j).getItem().equals(currency.getItem())) {
                    currencyCount += player.getInventory().getItem(j).getCount() * currency.getSellValue();
                    player.getInventory().setItem(j, new ItemStack(Items.AIR));
                    if (currencyCount >= maxAmount) {
                        currencyCount = maxAmount - DiamondUtils.dropCurrency(currencyCount - maxAmount, player);
                        break currency;
                    }
                }
            }
        }

        if (dm.changeBalance(player.getStringUUID(), currencyCount)) {
            String output = "Added " + DiamondUtils.valueString(currencyCount) + " to your account";
            ctx.getSource().sendSuccess(() -> Component.literal(output), false);
        } else {
            DiamondUtils.dropCurrency(currencyCount, player);
            ctx.getSource().sendSuccess(() -> Component.literal("You have too much money in your account"), false);
        }
        return 1;
    }

    public static int depositItemCommand(CommandContext<CommandSourceStack> ctx, Item item, int maxAmount) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        DatabaseManager dm = DiamondUtils.getDatabaseManager();
        int currencyCount = 0;
        CurrencyType currency = DiamondUtils.getDatabaseManager().getCurrency(BuiltInRegistries.ITEM.getKey(item).toString());
        if (currency != null && currency.isInCurrencyList()) {
            for (int j = 0; j < player.getInventory().getContainerSize(); j++) {
                if (player.getInventory().getItem(j).getItem().equals(currency.getItem())) {
                    currencyCount += player.getInventory().getItem(j).getCount() * currency.getSellValue();
                    player.getInventory().setItem(j, new ItemStack(Items.AIR));
                    if (currencyCount >= maxAmount) {
                        currencyCount = maxAmount - DiamondUtils.dropCurrency(currencyCount - maxAmount, player);
                        break;
                    }
                }
            }

            if (dm.changeBalance(player.getStringUUID(), currencyCount)) {
                String output = "Added " + DiamondUtils.valueString(currencyCount) + " to your account";
                ctx.getSource().sendSuccess(() -> Component.literal(output), false);
            } else {
                DiamondUtils.dropCurrency(currencyCount, player);
                ctx.getSource().sendSuccess(() -> Component.literal("You have too much money in your account"), false);
            }
        } else {
            ctx.getSource().sendSuccess(() -> Component.literal("That item is not a valid currency!"), false);
        }
        return 1;
    }
}
