package com.gmail.sneakdevs.diamondeconomy;

import com.gmail.sneakdevs.diamondeconomy.config.DiamondEconomyConfig;
import com.gmail.sneakdevs.diamondeconomy.sql.DatabaseManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import org.jetbrains.annotations.Nullable;

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
                                        .executes(e -> DiamondEconomyCommands.balanceCommand(e, null))
                        )

                        .then(
                                CommandManager.literal("deposit")
                                        .then(
                                                CommandManager.argument("amount", IntegerArgumentType.integer(1))
                                                        .executes(e -> {
                                                            int amount = IntegerArgumentType.getInteger(e, "amount");
                                                            return DiamondEconomyCommands.depositCommand(e, amount);
                                                        })
                                        )
                                        .executes(e -> DiamondEconomyCommands.depositCommand(e, 0))
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
                                                                            return DiamondEconomyCommands.sendCommand(e, player, amount);
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
                                                                                            return DiamondEconomyCommands.takeCommand(e, EntityArgumentType.getPlayers(e, "players").stream().toList(), amount);
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
                                                    CommandManager.argument("amount", IntegerArgumentType.integer(1))
                                                            .executes(e -> {
                                                                int amount = IntegerArgumentType.getInteger(e, "amount");
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
        String uuid = player.getUuidAsString();

        if (dm.changeBalance(uuid, -amount)) {
            DiamondEconomy.dropItem(amount, player);
            ctx.getSource().sendFeedback(new LiteralText("Withdrew $" + amount), false);
            return 1;
        }

        ctx.getSource().sendFeedback(new LiteralText("You have less than $" + amount), false);
        return 1;
    }

    public static int depositCommand(CommandContext<ServerCommandSource> ctx, int amount) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        DatabaseManager dm = DiamondEconomy.getDatabaseManager();
        int currencyCount = 0;
        int bal = dm.getBalanceFromUUID(player.getUuidAsString());
        int newValue = bal + amount;
        if (newValue < Integer.MAX_VALUE && newValue >= 0) {
            for (int i = 0; i < player.getInventory().size(); i++) {
                if (player.getInventory().getStack(i).getItem().equals(item)) {
                    currencyCount += player.getInventory().getStack(i).getCount();
                    player.getInventory().setStack(i, new ItemStack(Items.AIR));
                }
            }

            if (amount == 0 || amount == currencyCount) {
                newValue = bal + currencyCount;
                if (newValue < Integer.MAX_VALUE && newValue > 0) {
                    dm.setBalance(player.getUuidAsString(), currencyCount + bal);
                    ctx.getSource().sendFeedback(new LiteralText("Added " + currencyCount + " " + item.getName().asString() + " to your account"), false);
                    return 1;
                }
                DiamondEconomy.dropItem(currencyCount, player);
                ctx.getSource().sendFeedback(new LiteralText("You do not have enough room in your account"), false);
                return 1;
            }

            if (amount > currencyCount) {
                DiamondEconomy.dropItem(currencyCount, player);
                ctx.getSource().sendFeedback(new LiteralText("You do not have enough " + item.getName().asString() + " in your inventory"), false);
                return 1;
            }
            DiamondEconomy.dropItem(currencyCount - amount, player);
            dm.setBalance(player.getUuidAsString(), amount + bal);
            ctx.getSource().sendFeedback(new LiteralText("Added " + amount + " money to your account"), false);
            return 1;
        }

        ctx.getSource().sendFeedback(new LiteralText("You do not have enough room in your account"), false);
        return 1;
    }

    public static int modifyCommand(CommandContext<ServerCommandSource> ctx, Collection<ServerPlayerEntity> players, int amount) {
        DatabaseManager dm = DiamondEconomy.getDatabaseManager();
        String executerUUID = DiamondEconomy.getExecuterUUID(ctx);

        players.forEach(player -> {
            if (dm.changeBalance(player.getUuidAsString, amount)) {
                ctx.getSource().sendFeedback(new LiteralText("Modified " + players.size() + " players money by $" + amount), true);
            } else {
                ctx.getSource().sendFeedback(new LiteralText("That would go out of the valid money range for " + player.getName().asString()), true);
            }
        });
        return players.size();
    }

    public static int setCommand(CommandContext<ServerCommandSource> ctx, Collection<ServerPlayerEntity> players, int amount) {
        DatabaseManager dm = DiamondEconomy.getDatabaseManager();
        String executerUUID = DiamondEconomy.getExecuterUUID(ctx);

        players.forEach(player -> {
            dm.setBalance(player.getUuidAsString(), amount);
        });

        ctx.getSource().sendFeedback(new LiteralText("Updated balance of " + players.size() + " players"), true);
        return players.size();
    }

    public static int balanceCommand(CommandContext<ServerCommandSource> ctx, @Nullable String player) throws CommandSyntaxException {
        DatabaseManager dm = DiamondEconomy.getDatabaseManager();
        if (player == null) player = ctx.getSource().getPlayer().getName().asString();
        int bal = dm.getBalanceFromName(player);
        if (bal > -1) {
            ctx.getSource().sendFeedback(new LiteralText(player + " has $" + bal), false);
            return 1;
        }
        ctx.getSource().sendFeedback(new LiteralText("No account was found for player with the name \"" + player + "\""), false);
        return 1;
    }

    public static int topCommand(CommandContext<ServerCommandSource> ctx, int page) throws CommandSyntaxException {
        DatabaseManager dm = DiamondEconomy.getDatabaseManager();
        ctx.getSource().sendFeedback(new LiteralText(dm.top(ctx.getSource().getPlayer().getUuidAsString(), page)), false);
        return 1;
    }

    public static int sendCommand(CommandContext<ServerCommandSource> ctx, ServerPlayerEntity player, int amount) throws CommandSyntaxException {
        DatabaseManager dm = DiamondEconomy.getDatabaseManager();
        ServerPlayerEntity player1 = ctx.getSource().getPlayer();

        int bal = dm.getBalanceFromUUID(player1.getUuidAsString());
        int newValue = dm.getBalanceFromUUID(player.getUuidAsString()) + amount;

        if (!player.getUuidAsString().equals(player1.getUuidAsString())) {
            if (bal >= amount) {
                if (newValue < Integer.MAX_VALUE && newValue > 0) {
                    dm.setBalance(player.getUuidAsString(), newValue);
                    dm.setBalance(player1.getUuidAsString(), bal - amount);

                    player.sendMessage(new LiteralText("You received $" + amount + " from " + player1.getName().asString()), false);
                    ctx.getSource().sendFeedback(new LiteralText("Sent $" + amount + " to " + player.getName().asString()), false);
                } else {
                    ctx.getSource().sendFeedback(new LiteralText("Failed because that would go over the max value"), false);
                }
            } else {
                ctx.getSource().sendFeedback(new LiteralText("You don't have enough money"), false);
            }
        } else {
            ctx.getSource().sendFeedback(new LiteralText("You cant send money to yourself"), false);
        }

        return 1;
    }
}