package com.gmail.sneakdevs.diamondeconomy.command;

import com.gmail.sneakdevs.diamondeconomy.DiamondEconomy;
import com.gmail.sneakdevs.diamondeconomy.config.DiamondEconomyConfig;
import com.gmail.sneakdevs.diamondeconomy.sql.DatabaseManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

import java.util.Collection;

public class DiamondEconomyCommands {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal(DiamondEconomyConfig.getInstance().commandName)
                        .then(
                                CommandManager.literal("top")
                                        .then(
                                                CommandManager.argument("page", IntegerArgumentType.integer(1))
                                                        .executes(e -> {
                                                            int page = IntegerArgumentType.getInteger(e, "page");
                                                            return DiamondEconomyCommands.topCommand(e, page);
                                                        })
                                        )
                                        .executes(e -> DiamondEconomyCommands.topCommand(e, 5))
                        )
                        .then(
                                CommandManager.literal("balance")
                                        .then(
                                                CommandManager.argument("playerName", StringArgumentType.string())
                                                        .executes(e -> {
                                                            String string = StringArgumentType.getString(e, "playerName");
                                                            return DiamondEconomyCommands.balanceCommand(e, string);
                                                        })
                                        )
                                        .then(
                                                CommandManager.argument("player", EntityArgumentType.player())
                                                        .executes(e -> {
                                                            String player = EntityArgumentType.getPlayer(e, "player").getName().asString();
                                                            return DiamondEconomyCommands.balanceCommand(e, player);
                                                        })
                                        )
                                        .executes(e -> DiamondEconomyCommands.balanceCommand(e, e.getSource().getPlayer().getName().asString()))
                        )

                        .then(
                                CommandManager.literal("deposit")
                                        .executes(DiamondEconomyCommands::depositCommand)
                        )
                        .then(
                                CommandManager.literal("send")
                                        .then(
                                                CommandManager.argument("player", EntityArgumentType.player())
                                                        .then(
                                                                CommandManager.argument("amount", IntegerArgumentType.integer(1))
                                                                        .executes(e -> {
                                                                            ServerPlayerEntity player = EntityArgumentType.getPlayer(e, "player");
                                                                            int amount = IntegerArgumentType.getInteger(e, "amount");
                                                                            return DiamondEconomyCommands.sendCommand(e, player, e.getSource().getPlayer(), amount);
                                                                        })
                                                        )
                                        )
                        )
                        .then(
                                CommandManager.literal("admin")
                                        .requires((permission) -> permission.hasPermissionLevel(4))
                                        .then(
                                                CommandManager.literal("modify")
                                                        .requires((permission) -> permission.hasPermissionLevel(4))
                                                        .then(
                                                                CommandManager.argument("players", EntityArgumentType.players())
                                                                        .then(
                                                                                CommandManager.argument("amount", IntegerArgumentType.integer())
                                                                                        .executes(e -> {
                                                                                            int amount = IntegerArgumentType.getInteger(e, "amount");
                                                                                            return DiamondEconomyCommands.modifyCommand(e, EntityArgumentType.getPlayers(e, "players").stream().toList(), amount);
                                                                                        })
                                                                        )
                                                        )
                                        )
                                        .then(
                                                CommandManager.literal("set")
                                                        .requires((permission) -> permission.hasPermissionLevel(4))
                                                        .then(
                                                                CommandManager.argument("players", EntityArgumentType.players())
                                                                        .then(
                                                                                CommandManager.argument("amount", IntegerArgumentType.integer(0))
                                                                                        .executes(e -> {
                                                                                            int amount = IntegerArgumentType.getInteger(e, "amount");
                                                                                            return DiamondEconomyCommands.setCommand(e, EntityArgumentType.getPlayers(e, "players").stream().toList(), amount);
                                                                                        })
                                                                        )
                                                        )
                                        )
                        )
        );
        if (DiamondEconomyConfig.getInstance().withdrawCommand) {
            dispatcher.register(
                    CommandManager.literal(DiamondEconomyConfig.getInstance().commandName)
                            .then(
                                    CommandManager.literal("withdraw")
                                                .then(
                                                    CommandManager.argument("maxAmount", IntegerArgumentType.integer(1))
                                                            .executes(e -> {
                                                                int amount = IntegerArgumentType.getInteger(e, "maxAmount");
                                                                return DiamondEconomyCommands.withdrawCommand(e, amount);
                                                            })
                                            )
                            )
            );
        }
    }

    public static int withdrawCommand(CommandContext<ServerCommandSource> ctx, int amount) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        DatabaseManager dm = DiamondEconomy.getDatabaseManager();
        if (dm.changeBalance(player.getUuidAsString(), -amount)) {
            ctx.getSource().sendFeedback(new LiteralText("Withdrew $" + (amount - DiamondEconomy.dropItem(amount, player))), false);
        } else {
            ctx.getSource().sendFeedback(new LiteralText("You have less than $" + amount), false);
        }
        return 1;
    }

    public static int depositCommand(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        DatabaseManager dm = DiamondEconomy.getDatabaseManager();
        int currencyCount = 0;
        for (int i = DiamondEconomyConfig.getCurrencyValues().length - 1; i >= 0; i--) {
            for (int j = 0; j < player.getInventory().size(); j++) {
                if (player.getInventory().getStack(j).getItem().equals(DiamondEconomyConfig.getCurrency(i))) {
                    currencyCount += player.getInventory().getStack(j).getCount() * DiamondEconomyConfig.getCurrencyValues()[i];
                    player.getInventory().setStack(j, new ItemStack(Items.AIR));
                }
            }
        }
        if (dm.changeBalance(player.getUuidAsString(), currencyCount)) {
            ctx.getSource().sendFeedback(new LiteralText("Added $" + currencyCount + " to your account"), false);
        } else {
            DiamondEconomy.dropItem(currencyCount, player);
        }
        return 1;
    }

    public static int modifyCommand(CommandContext<ServerCommandSource> ctx, Collection<ServerPlayerEntity> players, int amount) {
        DatabaseManager dm = DiamondEconomy.getDatabaseManager();
        players.forEach(player -> ctx.getSource().sendFeedback(new LiteralText((dm.changeBalance(player.getUuidAsString(), amount)) ? ("Modified " + players.size() + " players money by $" + amount) : ("That would go out of the valid money range for " + player.getName().asString())), true));
        return players.size();
    }

    public static int setCommand(CommandContext<ServerCommandSource> ctx, Collection<ServerPlayerEntity> players, int amount) {
        DatabaseManager dm = DiamondEconomy.getDatabaseManager();
        players.forEach(player -> dm.setBalance(player.getUuidAsString(), amount));
        ctx.getSource().sendFeedback(new LiteralText("Updated balance of " + players.size() + " players"), true);
        return players.size();
    }

    public static int balanceCommand(CommandContext<ServerCommandSource> ctx, String player) {
        DatabaseManager dm = DiamondEconomy.getDatabaseManager();
        int bal = dm.getBalanceFromName(player);
        ctx.getSource().sendFeedback(new LiteralText((bal > -1) ? (player + " has $" + bal) : ("No account was found for player with the name \"" + player + "\"")), false);
        return 1;
    }

    public static int topCommand(CommandContext<ServerCommandSource> ctx, int page) throws CommandSyntaxException {
        DatabaseManager dm = DiamondEconomy.getDatabaseManager();
        ctx.getSource().sendFeedback(new LiteralText(dm.top(ctx.getSource().getPlayer().getUuidAsString(), page)), false);
        return 1;
    }

    public static int sendCommand(CommandContext<ServerCommandSource> ctx, ServerPlayerEntity player, ServerPlayerEntity player1, int amount) throws CommandSyntaxException {
        DatabaseManager dm = DiamondEconomy.getDatabaseManager();
        long newValue = dm.getBalanceFromUUID(player.getUuidAsString()) + amount;
        if (newValue < Integer.MAX_VALUE && dm.changeBalance(player1.getUuidAsString(), -amount)) {
            dm.changeBalance(player.getUuidAsString(), amount);
            player.sendMessage(new LiteralText("You received $" + amount + " from " + player1.getName().asString()), false);
            ctx.getSource().sendFeedback(new LiteralText("Sent $" + amount + " to " + player.getName().asString()), false);
        } else {
            ctx.getSource().sendFeedback(new LiteralText("Failed because that would go over the max value"), false);
        }
        return 1;
    }
}