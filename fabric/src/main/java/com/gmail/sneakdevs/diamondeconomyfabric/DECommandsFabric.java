package com.gmail.sneakdevs.diamondeconomyfabric;

import com.gmail.sneakdevs.diamondeconomy.DECommands;
import com.gmail.sneakdevs.diamondeconomy.config.DEConfig;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class DECommandsFabric {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal(AutoConfig.getConfigHolder(DEConfig.class).getConfig().commandName)
                        .then(
                                CommandManager.literal("top")
                                        .then(
                                                CommandManager.argument("amount", IntegerArgumentType.integer(1, 20))
                                                        .executes(e -> {
                                                            int amount = IntegerArgumentType.getInteger(e, "amount");
                                                            return DECommands.topCommand(e, amount);
                                                        })
                                        )
                                        .executes(e -> DECommands.topCommand(e, 5))
                        )
                        .then(
                                CommandManager.literal("balance")
                                        .then(
                                                CommandManager.argument("playerName", StringArgumentType.string())
                                                        .executes(e -> {
                                                            String string = StringArgumentType.getString(e, "playerName");
                                                            return DECommands.balanceCommand(e, string);
                                                        })
                                        )
                                        .then(
                                                CommandManager.argument("player", EntityArgumentType.player())
                                                        .executes(e -> {
                                                            String player = EntityArgumentType.getPlayer(e, "player").getName().asString();
                                                            return DECommands.balanceCommand(e, player);
                                                        })
                                        )
                                        .executes(e -> DECommands.balanceCommand(e, null))
                        )

                        .then(
                                CommandManager.literal("deposit")
                                        .then(
                                                CommandManager.argument("amount", IntegerArgumentType.integer(1))
                                                        .executes(e -> {
                                                            int amount = IntegerArgumentType.getInteger(e, "amount");
                                                            return DECommands.depositCommand(e, amount);
                                                        })
                                        )
                                        .executes(e -> DECommands.depositCommand(e, 0))
                        )
                        .then(
                                CommandManager.literal("withdraw")
                                        .then(
                                                CommandManager.argument("amount", IntegerArgumentType.integer(1))
                                                        .executes(e -> {
                                                            int amount = IntegerArgumentType.getInteger(e, "amount");
                                                            return DECommands.withdrawCommand(e, amount);
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
                                                                            return DECommands.sendCommand(e, player, amount);
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
                                                                                            return DECommands.takeCommand(e, EntityArgumentType.getPlayers(e, "players").stream().toList(), amount);
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
                                                                                            return DECommands.giveCommand(e, EntityArgumentType.getPlayers(e, "players").stream().toList(), amount);
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
                                                                                            return DECommands.setCommand(e, EntityArgumentType.getPlayers(e, "players").stream().toList(), amount);
                                                                                        })
                                                                        )
                                                        )
                                        )
                        )
        );

        if (AutoConfig.getConfigHolder(DEConfig.class).getConfig().transactionHistory) {
            dispatcher.register(
                    CommandManager.literal(AutoConfig.getConfigHolder(DEConfig.class).getConfig().commandName)
                            .then(
                                    CommandManager.literal("admin")
                                            .requires((permission) -> permission.hasPermissionLevel(4))
                                            .then(
                                                    CommandManager.literal("history")
                                                            .then(
                                                                    CommandManager.argument("amount", IntegerArgumentType.integer(1))
                                                                            .executes(e -> {
                                                                                int amount = IntegerArgumentType.getInteger(e, "amount");
                                                                                return DECommands.historyCommand(e, amount);
                                                                            })
                                                            )
                                                            .executes(e -> DECommands.historyCommand(e, 1))
                                            )
                            )
            );
        }

        if (AutoConfig.getConfigHolder(DEConfig.class).getConfig().withdrawCommand) {
            dispatcher.register(
                    CommandManager.literal(AutoConfig.getConfigHolder(DEConfig.class).getConfig().commandName)
                            .then(
                                    CommandManager.literal("withdraw")
                                            .then(
                                                    CommandManager.argument("amount", IntegerArgumentType.integer(1))
                                                            .executes(e -> {
                                                                int amount = IntegerArgumentType.getInteger(e, "amount");
                                                                return DECommands.withdrawCommand(e, amount);
                                                            })
                                            )
                            )
            );
        }
    }
}