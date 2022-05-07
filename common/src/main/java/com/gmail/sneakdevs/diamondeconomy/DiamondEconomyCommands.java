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
                CommandManager.literal(AutoConfig.getConfigHolder(DiamondEconomyConfig.class).getConfig().commandName)
                        .then(
                                CommandManager.literal("top")
                                        .then(
                                                CommandManager.argument("amount", IntegerArgumentType.integer(1, 20))
                                                        .executes(e -> {
                                                            int amount = IntegerArgumentType.getInteger(e, "amount");
                                                            return DiamondEconomyCommands.topCommand(e, amount);
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
                                CommandManager.literal("withdraw")
                                        .then(
                                                CommandManager.argument("amount", IntegerArgumentType.integer(1))
                                                        .executes(e -> {
                                                            int amount = IntegerArgumentType.getInteger(e, "amount");
                                                            return DiamondEconomyCommands.withdrawCommand(e, amount);
                                                        })
                                        )
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
                                                CommandManager.literal("take")
                                                        .requires((permission) -> permission.hasPermissionLevel(4))
                                                        .then(
                                                                CommandManager.argument("players", EntityArgumentType.players())
                                                                        .then(
                                                                                CommandManager.argument("amount", IntegerArgumentType.integer(1))
                                                                                        .executes(e -> {
                                                                                            int amount = IntegerArgumentType.getInteger(e, "amount");
                                                                                            return DiamondEconomyCommands.takeCommand(e, EntityArgumentType.getPlayers(e, "players").stream().toList(), amount);
                                                                                        })
                                                                        )
                                                        )
                                        )
                                        .then(
                                                CommandManager.literal("give")
                                                        .requires((permission) -> permission.hasPermissionLevel(4))
                                                        .then(
                                                                CommandManager.argument("players", EntityArgumentType.players())
                                                                        .then(
                                                                                CommandManager.argument("amount", IntegerArgumentType.integer(1))
                                                                                        .executes(e -> {
                                                                                            int amount = IntegerArgumentType.getInteger(e, "amount");
                                                                                            return DiamondEconomyCommands.giveCommand(e, EntityArgumentType.getPlayers(e, "players").stream().toList(), amount);
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

        if (AutoConfig.getConfigHolder(DiamondEconomyConfig.class).getConfig().transactionHistory) {
            dispatcher.register(
                    CommandManager.literal(AutoConfig.getConfigHolder(DiamondEconomyConfig.class).getConfig().commandName)
                            .then(
                                    CommandManager.literal("admin")
                                            .requires((permission) -> permission.hasPermissionLevel(4))
                                            .then(
                                                    CommandManager.literal("history")
                                                            .then(
                                                                    CommandManager.argument("amount", IntegerArgumentType.integer(1))
                                                                            .executes(e -> {
                                                                                int amount = IntegerArgumentType.getInteger(e, "amount");
                                                                                return DiamondEconomyCommands.historyCommand(e, amount);
                                                                            })
                                                            )
                                                            .executes(e -> DiamondEconomyCommands.historyCommand(e, 1))
                                            )
                            )
            );
        }

        if (AutoConfig.getConfigHolder(DiamondEconomyConfig.class).getConfig().withdrawCommand) {
            dispatcher.register(
                    CommandManager.literal(AutoConfig.getConfigHolder(DiamondEconomyConfig.class).getConfig().commandName)
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
            DiamondEconomy.dropItem(DiamondEconomyConfig.getCurrency(), amount, player);
            ctx.getSource().sendFeedback(new LiteralText("Withdrew " + amount + " " + DiamondEconomyConfig.getCurrencyName()), false);
            return 1;
        }

        ctx.getSource().sendFeedback(new LiteralText("You have less than " + amount + " " + DiamondEconomyConfig.getCurrencyName()), false);
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
                if (player.getInventory().getStack(i).getItem().equals(DiamondEconomyConfig.getCurrency())) {
                    currencyCount += player.getInventory().getStack(i).getCount();
                    player.getInventory().setStack(i, new ItemStack(Items.AIR));
                }
            }

            if (amount == 0 || amount == currencyCount) {
                newValue = bal + currencyCount;
                if (newValue < Integer.MAX_VALUE && newValue > 0) {
                    dm.setBalance(player.getUuidAsString(), currencyCount + bal);
                    ctx.getSource().sendFeedback(new LiteralText("Added " + currencyCount + " " + DiamondEconomyConfig.getCurrencyName() + " to your account"), false);
                    return 1;
                }
                DiamondEconomy.dropItem(DiamondEconomyConfig.getCurrency(), currencyCount, player);
                ctx.getSource().sendFeedback(new LiteralText("You do not have enough room in your account"), false);
                return 1;
            }

            if (amount > currencyCount) {
                DiamondEconomy.dropItem(DiamondEconomyConfig.getCurrency(), currencyCount, player);
                ctx.getSource().sendFeedback(new LiteralText("You do not have enough " + DiamondEconomyConfig.getCurrencyName() + " in your inventory"), false);
                return 1;
            }
            DiamondEconomy.dropItem(DiamondEconomyConfig.getCurrency(), currencyCount - amount, player);
            dm.setBalance(player.getUuidAsString(), amount + bal);
            ctx.getSource().sendFeedback(new LiteralText("Added " + amount + " " + DiamondEconomyConfig.getCurrencyName() + " to your account"), false);
            return 1;
        }

        ctx.getSource().sendFeedback(new LiteralText("You do not have enough room in your account"), false);
        return 1;
    }

    public static int takeCommand(CommandContext<ServerCommandSource> ctx, Collection<ServerPlayerEntity> players, int amount) {
        DatabaseManager dm = DiamondEconomy.getDatabaseManager();
        String executerUUID;
        try {
            executerUUID = ctx.getSource().getPlayer().getUuidAsString();
        } catch (CommandSyntaxException e) {
            executerUUID = "@";
        }

        String finalExecuterUUID = executerUUID;
        players.forEach(player -> {
            int bal = dm.getBalanceFromUUID(player.getUuidAsString());
            if (amount > bal) {
                if (AutoConfig.getConfigHolder(DiamondEconomyConfig.class).getConfig().transactionHistory) {
                    dm.createTransaction("take", finalExecuterUUID, player.getUuidAsString(), bal, bal);
                }
                dm.setBalance(player.getUuidAsString(), 0);
            } else {
                if (AutoConfig.getConfigHolder(DiamondEconomyConfig.class).getConfig().transactionHistory) {
                    dm.createTransaction("take", finalExecuterUUID, player.getUuidAsString(), amount, bal);
                }
                dm.setBalance(player.getUuidAsString(), dm.getBalanceFromUUID(player.getUuidAsString()) - amount);
            }
        });

        ctx.getSource().sendFeedback(new LiteralText("Took " + Math.abs(amount) + " " + DiamondEconomyConfig.getCurrencyName() + " from " + players.size() + " players"), true);
        return players.size();
    }

    public static int giveCommand(CommandContext<ServerCommandSource> ctx, Collection<ServerPlayerEntity> players, int amount) {
        DatabaseManager dm = DiamondEconomy.getDatabaseManager();
        String executerUUID;
        try {
            executerUUID = ctx.getSource().getPlayer().getUuidAsString();
        } catch (CommandSyntaxException e) {
            executerUUID = "@";
        }

        String finalExecuterUUID = executerUUID;
        players.forEach(player -> {
            int bal = dm.getBalanceFromUUID(player.getUuidAsString());
            int newValue = bal + amount;
            if (newValue < Integer.MAX_VALUE && newValue > 0) {
                if (AutoConfig.getConfigHolder(DiamondEconomyConfig.class).getConfig().transactionHistory) {
                    dm.createTransaction("give", finalExecuterUUID, player.getUuidAsString(), amount, bal);
                }
                dm.setBalance(player.getUuidAsString(), newValue);
                ctx.getSource().sendFeedback(new LiteralText("Gave " + players.size() + " players " + amount + " " + DiamondEconomyConfig.getCurrencyName()), true);
            } else {
                ctx.getSource().sendFeedback(new LiteralText("That would go over the max value for " + player.getName().asString()), true);
            }
        });
        return players.size();
    }

    public static int setCommand(CommandContext<ServerCommandSource> ctx, Collection<ServerPlayerEntity> players, int amount) {
        DatabaseManager dm = DiamondEconomy.getDatabaseManager();
        String executerUUID;
        try {
            executerUUID = ctx.getSource().getPlayer().getUuidAsString();
        } catch (CommandSyntaxException e) {
            executerUUID = "@";
        }

        String finalExecuterUUID = executerUUID;
        players.forEach(player -> {
            if (AutoConfig.getConfigHolder(DiamondEconomyConfig.class).getConfig().transactionHistory) {
                dm.createTransaction("set", finalExecuterUUID, player.getUuidAsString(), amount, dm.getBalanceFromUUID(player.getUuidAsString()));
            }
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
            ctx.getSource().sendFeedback(new LiteralText(player + " has " + bal + " " + DiamondEconomyConfig.getCurrencyName()), false);
            return 1;
        }
        ctx.getSource().sendFeedback(new LiteralText("No account was found for player with the name \"" + player + "\""), false);
        return 1;
    }

    public static int topCommand(CommandContext<ServerCommandSource> ctx, int topAmount) throws CommandSyntaxException {
        DatabaseManager dm = DiamondEconomy.getDatabaseManager();
        ServerPlayerEntity player1 = ctx.getSource().getPlayer();
        ctx.getSource().sendFeedback(new LiteralText(dm.top(player1.getUuidAsString(), topAmount)), false);
        return 1;
    }

    public static int historyCommand(CommandContext<ServerCommandSource> ctx, int page) {
        DatabaseManager dm = DiamondEconomy.getDatabaseManager();
        ctx.getSource().sendFeedback(new LiteralText(dm.history(page)), false);
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
                    if (AutoConfig.getConfigHolder(DiamondEconomyConfig.class).getConfig().transactionHistory) {
                        dm.createTransaction("send", player1.getUuidAsString(), player.getUuidAsString(), amount, -1);
                    }
                    dm.setBalance(player.getUuidAsString(), newValue);
                    dm.setBalance(player1.getUuidAsString(), bal - amount);

                    player.sendMessage(new LiteralText("You received " + amount + " " + DiamondEconomyConfig.getCurrencyName() + " from " + player1.getName().asString()), false);
                    ctx.getSource().sendFeedback(new LiteralText("Sent " + amount + " " + DiamondEconomyConfig.getCurrencyName() + " to " + player.getName().asString()), false);
                } else {
                    ctx.getSource().sendFeedback(new LiteralText("Failed because that would go over the max value"), false);
                }
            } else {
                ctx.getSource().sendFeedback(new LiteralText("You don't have enough " + DiamondEconomyConfig.getCurrencyName()), false);
            }
        } else {
            ctx.getSource().sendFeedback(new LiteralText("You cant send " + DiamondEconomyConfig.getCurrencyName() + " to yourself"), false);
        }

        return 1;
    }
}