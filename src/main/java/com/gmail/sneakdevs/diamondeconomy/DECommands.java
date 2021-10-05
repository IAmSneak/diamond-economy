package com.gmail.sneakdevs.diamondeconomy;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class DECommands {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("diamonds")
                        .then(
                                CommandManager.literal("top")
                                        .then(
                                                CommandManager.argument("amount", IntegerArgumentType.integer(1))
                                                        .executes(e -> {
                                                            int amount = IntegerArgumentType.getInteger(e, "amount");
                                                            return topCommand(e, amount);
                                                        })
                                        )
                                        .executes(e -> topCommand(e, 5))
                        )
                        .then(
                                CommandManager.literal("balance")
                                        .then(
                                                CommandManager.argument("player", EntityArgumentType.player())
                                                        .executes(e -> {
                                                            ServerPlayerEntity player = EntityArgumentType.getPlayer(e, "player");
                                                            return balanceCommand(e, player);
                                                        })
                                        )
                                        .executes(e -> balanceCommand(e, null))
                        )

                        .then(
                                CommandManager.literal("deposit")
                                        .then(
                                                CommandManager.argument("amount", IntegerArgumentType.integer(1))
                                                        .executes(e -> {
                                                            int amount = IntegerArgumentType.getInteger(e, "amount");
                                                            return depositCommand(e, amount);
                                                        })
                                        )
                                        .executes(e -> depositCommand(e, 0))
                        )
                        .then(
                                CommandManager.literal("withdraw")
                                        .then(
                                                CommandManager.argument("amount", IntegerArgumentType.integer(1))
                                                        .executes(e -> {
                                                            int amount = IntegerArgumentType.getInteger(e, "amount");
                                                            return withdrawCommand(e, amount);
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
                                                                            return sendCommand(e, player, amount);
                                                                        })
                                                )
                                        )
                        )
                        .then(
                                CommandManager.literal("take")
                                        .requires((permission) -> permission.hasPermissionLevel(4))
                                        .then(
                                                CommandManager.argument("players", EntityArgumentType.players())
                                                        .then(
                                                                CommandManager.argument("amount", IntegerArgumentType.integer(1))
                                                                        .executes(e -> {
                                                                            int amount = IntegerArgumentType.getInteger(e, "amount");
                                                                            return takeCommand(e, EntityArgumentType.getPlayers(e, "players").stream().toList(), amount);
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
                                                                            return giveCommand(e, EntityArgumentType.getPlayers(e, "players").stream().toList(), amount);
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
                                                                            return setCommand(e, EntityArgumentType.getPlayers(e, "players").stream().toList(), amount);
                                                                        })
                                                        )
                                        )
                        )
        );
    }

    private static int withdrawCommand(CommandContext<ServerCommandSource> ctx, int amount) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        DatabaseManager dm = new DatabaseManager();
        String uuid = player.getUuidAsString();
        String name = player.getName().asString();

        if (dm.getBalance(uuid) >= amount) {
            dropDiamonds(amount, player);
            dm.setBalance(uuid, name, dm.getBalance(uuid) - amount);
            ctx.getSource().sendFeedback(new LiteralText("Withdrew " + amount + " diamonds"), false);

            return 1;
        }

        ctx.getSource().sendFeedback(new LiteralText("You have less than " + amount + " diamonds"), false);

        return 1;
    }

    private static int depositCommand(CommandContext<ServerCommandSource> ctx, int amount) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        DatabaseManager dm = new DatabaseManager();
        int diamondCount = 0;
        int bal = dm.getBalance(player.getUuidAsString());

        int newValue = bal + amount;
        if (newValue < Integer.MAX_VALUE && newValue >= 0) {
            for (int i = 0; i < 36; i++) {
                System.out.println("repeat");
                if (player.getInventory().getStack(i).getItem().equals(Items.DIAMOND)) {
                    diamondCount += player.getInventory().getStack(i).getCount();
                    player.getInventory().setStack(i, new ItemStack(Items.AIR));
                } else if (player.getInventory().getStack(i).getItem().equals(Items.DIAMOND_BLOCK)){
                    diamondCount += player.getInventory().getStack(i).getCount() * 9;
                    player.getInventory().setStack(i, new ItemStack(Items.AIR));
                }
            }

            if (amount == 0) {
                newValue = bal + diamondCount;
                if (newValue < Integer.MAX_VALUE && newValue > 0) {
                    dm.setBalance(player.getUuidAsString(), player.getName().asString(), diamondCount + bal);
                    ctx.getSource().sendFeedback(new LiteralText("Added " + diamondCount + " diamonds to your account"), false);
                    return 1;
                }
                dropDiamonds(diamondCount, player);
                ctx.getSource().sendFeedback(new LiteralText("You do not have enough room in your account"), false);
                return 0;
            }

            if (amount > diamondCount) {
                dropDiamonds(diamondCount, player);
                ctx.getSource().sendFeedback(new LiteralText("You do not have enough diamonds in your inventory"), false);
                return 0;
            }
            dropDiamonds(diamondCount - amount, player);
            dm.setBalance(player.getUuidAsString(), player.getName().asString(), amount + bal);
            ctx.getSource().sendFeedback(new LiteralText("Added " + amount + " diamonds to your account"), false);
            return 1;
        }

        ctx.getSource().sendFeedback(new LiteralText("You do not have enough room in your account"), false);
        return 0;
    }

    private static int takeCommand(CommandContext<ServerCommandSource> ctx, Collection<ServerPlayerEntity> players, int amount) {
        DatabaseManager dm = new DatabaseManager();

        players.forEach(player -> {
            if (amount > dm.getBalance(player.getUuidAsString())) {
                dm.setBalance(player.getUuidAsString(), player.getName().asString(), 0);
            } else {
                dm.setBalance(player.getUuidAsString(), player.getName().asString(), dm.getBalance(player.getUuidAsString()) - amount);
            }
        });

        ctx.getSource().sendFeedback(new LiteralText("Took " + Math.abs(amount) + " diamonds from " + players.size() + " players"), false);
        return players.size();
    }

    private static int giveCommand(CommandContext<ServerCommandSource> ctx, Collection<ServerPlayerEntity> players, int amount) {
        DatabaseManager dm = new DatabaseManager();

        players.forEach(player -> {
            int newValue = dm.getBalance(player.getUuidAsString()) + amount;
            if (newValue < Integer.MAX_VALUE && newValue > 0) {
                dm.setBalance(player.getUuidAsString(), player.getName().asString(), dm.getBalance(player.getUuidAsString()) + amount);
                ctx.getSource().sendFeedback(new LiteralText("Gave " + players.size() + " players " + amount + " diamonds"), false);
            } else {
                ctx.getSource().sendFeedback(new LiteralText("That would go over the max value for " + player.getName().asString()), false);
            }
        });
        return players.size();
    }

    private static int setCommand(CommandContext<ServerCommandSource> ctx, Collection<ServerPlayerEntity> players, int amount) {
        DatabaseManager dm = new DatabaseManager();

        players.forEach(player -> dm.setBalance(player.getUuidAsString(), player.getName().asString(), amount));

        ctx.getSource().sendFeedback(new LiteralText("Updated balance of " + players.size() + " players"), false);
        return players.size();
    }

    private static int balanceCommand(CommandContext<ServerCommandSource> ctx, @Nullable ServerPlayerEntity player) throws CommandSyntaxException {
        DatabaseManager dm = new DatabaseManager();
        if(player == null) {
            player = ctx.getSource().getPlayer();
        }
        int bal = dm.getBalance(player.getUuidAsString());
        if (bal > -1) {
            ctx.getSource().sendFeedback(new LiteralText(player.getName().asString() + " has " + bal + " diamonds"), false);
            return 0;
        }
        ctx.getSource().sendFeedback(new LiteralText("No account was found for player with the name \"" + player.getName().asString() + "\""), false);
        return 1;
    }

    private static int topCommand(CommandContext<ServerCommandSource> ctx, int topAmount) throws CommandSyntaxException {
        DatabaseManager dm = new DatabaseManager();
        ServerPlayerEntity player1 = ctx.getSource().getPlayer();
        ctx.getSource().sendFeedback(new LiteralText(dm.top(player1.getUuidAsString(), topAmount)), false);
        return 1;
    }

    private static int sendCommand(CommandContext<ServerCommandSource> ctx, ServerPlayerEntity player, int amount) throws CommandSyntaxException {
        DatabaseManager dm = new DatabaseManager();
        ServerPlayerEntity player1 = ctx.getSource().getPlayer();

        int bal = dm.getBalance(player1.getUuidAsString());
        int newValue = dm.getBalance(player.getUuidAsString());

        if (!player.getUuidAsString().equals(player1.getUuidAsString())) {
            if (bal >= amount) {
                if (newValue < Integer.MAX_VALUE && newValue > 0) {
                    dm.setBalance(player.getUuidAsString(), player.getName().asString(), dm.getBalance(player.getUuidAsString()) + amount);
                    dm.setBalance(player1.getUuidAsString(), player1.getName().asString(), bal - amount);

                    player.sendMessage(new LiteralText("You received " + amount + " diamonds from " + player1.getName().asString()), false);
                    ctx.getSource().sendFeedback(new LiteralText("Sent " + amount + " diamonds to " + player.getName().asString()), false);
                } else {
                    ctx.getSource().sendFeedback(new LiteralText("Failed because that would go over the max value"), false);
                }
            } else {
                ctx.getSource().sendFeedback(new LiteralText("You don't have enough diamonds"), false);
            }
        } else {
            ctx.getSource().sendFeedback(new LiteralText("You cant send diamonds to yourself"), false);
        }

        return 1;
    }

    private static void dropDiamonds(int amount, ServerPlayerEntity player) {
        int blockAmount = amount / 9;

        while (blockAmount > 64) {
            ItemEntity itemEntity = player.dropItem(new ItemStack(Items.DIAMOND_BLOCK, 64), true);
            itemEntity.resetPickupDelay();
            itemEntity.setOwner(player.getUuid());
            blockAmount -= 64;
        }

        if (blockAmount > 0) {
            ItemEntity itemEntity1 = player.dropItem(new ItemStack(Items.DIAMOND_BLOCK, blockAmount), true);
            itemEntity1.resetPickupDelay();
            itemEntity1.setOwner(player.getUuid());
        }

        if (amount % 9 > 0) {
            ItemEntity itemEntity2 = player.dropItem(new ItemStack(Items.DIAMOND, amount % 9), true);
            itemEntity2.resetPickupDelay();
            itemEntity2.setOwner(player.getUuid());
        }
    }
}