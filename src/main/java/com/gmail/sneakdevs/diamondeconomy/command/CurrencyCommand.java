package com.gmail.sneakdevs.diamondeconomy.command;

import com.gmail.sneakdevs.diamondeconomy.DiamondUtils;
import com.gmail.sneakdevs.diamondeconomy.config.DiamondEconomyConfig;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;

public class CurrencyCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> buildCommand(CommandBuildContext commandBuildContext) {
        return Commands.literal(DiamondEconomyConfig.getInstance().currencyCommandName)
                .requires((permission) -> permission.hasPermission(DiamondEconomyConfig.getInstance().opCommandsPermissionLevel))
                .then(Commands.literal("sortList")
                        .executes(CurrencyCommand::currencyListSort)
                )
                .then(Commands.literal("add")
                        .then(Commands.argument("item", ItemArgument.item(commandBuildContext))
                                .then(Commands.argument("sellValue", IntegerArgumentType.integer(0))
                                        .then(Commands.argument("canSell", BoolArgumentType.bool())
                                                .then(Commands.argument("buyValue", IntegerArgumentType.integer(0))
                                                        .then(Commands.argument("canBuy", BoolArgumentType.bool())
                                                                .then(Commands.argument("inCurrencyList", BoolArgumentType.bool())
                                                                        .executes(ctx -> {
                                                                            final ItemInput item = ItemArgument.getItem(ctx, "item");
                                                                            final int sellValue = IntegerArgumentType.getInteger(ctx, "sellValue");
                                                                            final boolean canSell = BoolArgumentType.getBool(ctx, "canSell");
                                                                            final int buyValue = IntegerArgumentType.getInteger(ctx, "buyValue");
                                                                            final boolean canBuy = BoolArgumentType.getBool(ctx, "canBuy");
                                                                            final boolean inCurrencyList = BoolArgumentType.getBool(ctx, "inCurrencyList");
                                                                            return currencyAdd(ctx, item, sellValue, canSell, buyValue, canBuy, inCurrencyList);
                                                                        }))))))))
                .then(Commands.literal("remove")
                        .then(Commands.argument("item", ItemArgument.item(commandBuildContext))
                                .executes(ctx -> {
                                    final ItemInput item = ItemArgument.getItem(ctx, "item");
                                    return currencyRemove(ctx, item);
                                })))
                .then(Commands.literal("set")
                        .then(Commands.literal("sellValue")
                                .then(Commands.argument("item", ItemArgument.item(commandBuildContext))
                                        .then(Commands.argument("value", IntegerArgumentType.integer(0))
                                                .executes(ctx -> {
                                                    final ItemInput item = ItemArgument.getItem(ctx, "item");
                                                    final int sellValue = IntegerArgumentType.getInteger(ctx, "value");
                                                    return currencySetSellValue(ctx, item, sellValue);
                                                }))))
                        .then(Commands.literal("buyValue")
                                .then(Commands.argument("item", ItemArgument.item(commandBuildContext))
                                        .then(Commands.argument("value", IntegerArgumentType.integer(0))
                                                .executes(ctx -> {
                                                    final ItemInput item = ItemArgument.getItem(ctx, "item");
                                                    final int buyValue = IntegerArgumentType.getInteger(ctx, "value");
                                                    return currencySetBuyValue(ctx, item, buyValue);
                                                }))))
                        .then(Commands.literal("canSell")
                                .then(Commands.argument("item", ItemArgument.item(commandBuildContext))
                                        .then(Commands.argument("value", BoolArgumentType.bool())
                                                .executes(ctx -> {
                                                    final ItemInput item = ItemArgument.getItem(ctx, "item");
                                                    final boolean value = BoolArgumentType.getBool(ctx, "value");
                                                    return currencySetCanSell(ctx, item, value);
                                                }))))
                        .then(Commands.literal("canBuy")
                                .then(Commands.argument("item", ItemArgument.item(commandBuildContext))
                                        .then(Commands.argument("value", BoolArgumentType.bool())
                                                .executes(ctx -> {
                                                    final ItemInput item = ItemArgument.getItem(ctx, "item");
                                                    final boolean value = BoolArgumentType.getBool(ctx, "value");
                                                    return currencySetCanBuy(ctx, item, value);
                                                }))))
                        .then(Commands.literal("inCurrencyList")
                                .then(Commands.argument("item", ItemArgument.item(commandBuildContext))
                                        .then(Commands.argument("value", BoolArgumentType.bool())
                                                .executes(ctx -> {
                                                    final ItemInput item = ItemArgument.getItem(ctx, "item");
                                                    final boolean value = BoolArgumentType.getBool(ctx, "value");
                                                    return currencySetInCurrencyList(ctx, item, value);
                                                })))));
    }

    public static int currencyListSort(CommandContext<CommandSourceStack> ctx) {
        DiamondUtils.createCurrencyList();
        ctx.getSource().sendSuccess(() -> Component.literal("Currency list sorted"), false);
        return 1;
    }

    public static int currencySetSellValue(CommandContext<CommandSourceStack> ctx, ItemInput item, int sellValue) {
        if (DiamondUtils.getDatabaseManager().setSellValue(BuiltInRegistries.ITEM.getKey(item.getItem()).toString(), sellValue)) {
            if (DiamondUtils.getDatabaseManager().getInCurrencyList(BuiltInRegistries.ITEM.getKey(item.getItem()).toString())) {
                DiamondUtils.getDatabaseManager().setBuyValue(BuiltInRegistries.ITEM.getKey(item.getItem()).toString(), sellValue);
                ctx.getSource().sendSuccess(() -> Component.literal("Set buy and sell value (because in currency list)"), false);
            } else {
                ctx.getSource().sendSuccess(() -> Component.literal("Sell value set"), false);
            }
        } else {
            if (BuiltInRegistries.ITEM.getKey(item.getItem()).toString().equals(DiamondEconomyConfig.getInstance().mainCurrency)) {
                ctx.getSource().sendSuccess(() -> Component.literal("Cannot modify the main currency, change to a different item in the config"), false);
            } else {
                ctx.getSource().sendSuccess(() -> Component.literal("That item is not a currency"), false);
            }
        }
        DiamondUtils.createCurrencyList();
        return 1;
    }

    public static int currencySetBuyValue(CommandContext<CommandSourceStack> ctx, ItemInput item, int buyValue) {
        if (DiamondUtils.getDatabaseManager().setBuyValue(BuiltInRegistries.ITEM.getKey(item.getItem()).toString(), buyValue)) {
            if (DiamondUtils.getDatabaseManager().getInCurrencyList(BuiltInRegistries.ITEM.getKey(item.getItem()).toString())) {
                DiamondUtils.getDatabaseManager().setSellValue(BuiltInRegistries.ITEM.getKey(item.getItem()).toString(), buyValue);
                ctx.getSource().sendSuccess(() -> Component.literal("Set buy and sell value (because in currency list)"), false);
            } else {
                ctx.getSource().sendSuccess(() -> Component.literal("Buy value set"), false);
            }
        } else {
            if (BuiltInRegistries.ITEM.getKey(item.getItem()).toString().equals(DiamondEconomyConfig.getInstance().mainCurrency)) {
                ctx.getSource().sendSuccess(() -> Component.literal("Cannot modify the main currency, change to a different item in the config"), false);
            } else {
                ctx.getSource().sendSuccess(() -> Component.literal("That item is not a currency"), false);
            }
        }
        DiamondUtils.createCurrencyList();
        return 1;
    }

    public static int currencySetCanBuy(CommandContext<CommandSourceStack> ctx, ItemInput item, boolean canBuy) {
        if (DiamondUtils.getDatabaseManager().getInCurrencyList(BuiltInRegistries.ITEM.getKey(item.getItem()).toString())) {
            ctx.getSource().sendSuccess(() -> Component.literal("Cannot modify items in currency list"), false);
            return 1;
        }
        if (DiamondUtils.getDatabaseManager().setCanBuy(BuiltInRegistries.ITEM.getKey(item.getItem()).toString(), canBuy)) {
            ctx.getSource().sendSuccess(() -> Component.literal("Set can buy " + canBuy), false);
        } else {
            if (BuiltInRegistries.ITEM.getKey(item.getItem()).toString().equals(DiamondEconomyConfig.getInstance().mainCurrency)) {
                ctx.getSource().sendSuccess(() -> Component.literal("Cannot modify the main currency, change to a different item in the config"), false);
            } else {
                ctx.getSource().sendSuccess(() -> Component.literal("That item is not a currency"), false);
            }
        }
        DiamondUtils.createCurrencyList();
        return 1;
    }

    public static int currencySetCanSell(CommandContext<CommandSourceStack> ctx, ItemInput item, boolean canSell) {
        if (DiamondUtils.getDatabaseManager().getInCurrencyList(BuiltInRegistries.ITEM.getKey(item.getItem()).toString())) {
            ctx.getSource().sendSuccess(() -> Component.literal("Cannot modify items in currency list"), false);
            return 1;
        }
        if (DiamondUtils.getDatabaseManager().setCanSell(BuiltInRegistries.ITEM.getKey(item.getItem()).toString(), canSell)) {
            ctx.getSource().sendSuccess(() -> Component.literal("Set can sell " + canSell), false);
        } else {
            if (BuiltInRegistries.ITEM.getKey(item.getItem()).toString().equals(DiamondEconomyConfig.getInstance().mainCurrency)) {
                ctx.getSource().sendSuccess(() -> Component.literal("Cannot modify the main currency, change to a different item in the config"), false);
            } else {
                ctx.getSource().sendSuccess(() -> Component.literal("That item is not a currency"), false);
            }
        }
        DiamondUtils.createCurrencyList();
        return 1;
    }

    public static int currencySetInCurrencyList(CommandContext<CommandSourceStack> ctx, ItemInput item, boolean inCurrencyList) {
        if (DiamondUtils.getDatabaseManager().setInCurrencyList(BuiltInRegistries.ITEM.getKey(item.getItem()).toString(), inCurrencyList)) {
            if (inCurrencyList) {
                DiamondUtils.getDatabaseManager().setCanSell(BuiltInRegistries.ITEM.getKey(item.getItem()).toString(), true);
                DiamondUtils.getDatabaseManager().setCanBuy(BuiltInRegistries.ITEM.getKey(item.getItem()).toString(), true);
                DiamondUtils.getDatabaseManager().setBuyValue(BuiltInRegistries.ITEM.getKey(item.getItem()).toString(), DiamondUtils.getDatabaseManager().getSellValue(BuiltInRegistries.ITEM.getKey(item.getItem()).toString()));
            }
            ctx.getSource().sendSuccess(() -> Component.literal("Set inCurrencyList " + inCurrencyList), false);
        } else {
            if (BuiltInRegistries.ITEM.getKey(item.getItem()).toString().equals(DiamondEconomyConfig.getInstance().mainCurrency)) {
                ctx.getSource().sendSuccess(() -> Component.literal("Cannot modify the main currency, change to a different item in the config"), false);
            } else {
                ctx.getSource().sendSuccess(() -> Component.literal("That item is not a currency"), false);
            }
        }
        DiamondUtils.createCurrencyList();
        return 1;
    }

    public static int currencyRemove(CommandContext<CommandSourceStack> ctx, ItemInput item) {
        if (DiamondUtils.getDatabaseManager().removeCurrency(BuiltInRegistries.ITEM.getKey(item.getItem()).toString())) {
            ctx.getSource().sendSuccess(() -> Component.literal("Item removed"), false);
        } else {
            if (BuiltInRegistries.ITEM.getKey(item.getItem()).toString().equals(DiamondEconomyConfig.getInstance().mainCurrency)) {
                ctx.getSource().sendSuccess(() -> Component.literal("Cannot remove the main currency, change to a different item in the config"), false);
            } else {
                ctx.getSource().sendSuccess(() -> Component.literal("That item is not a currency"), false);
            }
        }
        DiamondUtils.createCurrencyList();
        return 1;
    }

    public static int currencyAdd(CommandContext<CommandSourceStack> ctx, ItemInput item, int sellValue, boolean canSell, int buyValue, boolean canBuy, boolean inCurrencyList) {
        if (inCurrencyList) {
            canBuy = true;
            canSell = true;
            buyValue = sellValue;
        }
        if (DiamondUtils.getDatabaseManager().addCurrency(BuiltInRegistries.ITEM.getKey(item.getItem()).toString(), sellValue, buyValue, inCurrencyList, canBuy, canSell, false)) {
            ctx.getSource().sendSuccess(() -> Component.literal("Item added"), false);
        } else {
            if (BuiltInRegistries.ITEM.getKey(item.getItem()).toString().equals(DiamondEconomyConfig.getInstance().mainCurrency)) {
                ctx.getSource().sendSuccess(() -> Component.literal("Cannot modify the main currency, change to a different item in the config"), false);
            } else {
                ctx.getSource().sendSuccess(() -> Component.literal("That item is already a currency"), false);
            }
        }
        DiamondUtils.createCurrencyList();
        return 1;
    }
}
