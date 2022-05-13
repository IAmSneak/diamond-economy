package com.gmail.sneakdevs.diamondeconomy.command;

import com.gmail.sneakdevs.diamondeconomy.config.DiamondEconomyConfig;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class DiamondEconomyCommands {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        if (DiamondEconomyConfig.getInstance().commandName == null) {
            if (DiamondEconomyConfig.getInstance().modifyCommandName != null) {
                dispatcher.register(ModifyCommand.buildCommand());
            }
            if (DiamondEconomyConfig.getInstance().balanceCommandName != null) {
                dispatcher.register(BalanceCommand.buildCommand());
            }
            if (DiamondEconomyConfig.getInstance().topCommandName != null) {
                dispatcher.register(TopCommand.buildCommand());
            }
            if (DiamondEconomyConfig.getInstance().depositCommandName != null) {
                dispatcher.register(DepositCommand.buildCommand());
            }
            if (DiamondEconomyConfig.getInstance().sendCommandName != null) {
                dispatcher.register(SendCommand.buildCommand());
            }
            if (DiamondEconomyConfig.getInstance().setCommandName != null) {
                dispatcher.register(SetCommand.buildCommand());
            }
            if (DiamondEconomyConfig.getInstance().withdrawCommandName != null) {
                dispatcher.register(WithdrawCommand.buildCommand());
            }
        } else {
            if (DiamondEconomyConfig.getInstance().modifyCommandName != null) {
                dispatcher.register(CommandManager.literal(DiamondEconomyConfig.getInstance().commandName).then(ModifyCommand.buildCommand()));
            }
            if (DiamondEconomyConfig.getInstance().balanceCommandName != null) {
                dispatcher.register(CommandManager.literal(DiamondEconomyConfig.getInstance().commandName).then(BalanceCommand.buildCommand()));
            }
            if (DiamondEconomyConfig.getInstance().topCommandName != null) {
                dispatcher.register(CommandManager.literal(DiamondEconomyConfig.getInstance().commandName).then(TopCommand.buildCommand()));
            }
            if (DiamondEconomyConfig.getInstance().depositCommandName != null) {
                dispatcher.register(CommandManager.literal(DiamondEconomyConfig.getInstance().commandName).then(DepositCommand.buildCommand()));
            }
            if (DiamondEconomyConfig.getInstance().sendCommandName != null) {
                dispatcher.register(CommandManager.literal(DiamondEconomyConfig.getInstance().commandName).then(SendCommand.buildCommand()));
            }
            if (DiamondEconomyConfig.getInstance().setCommandName != null) {
                dispatcher.register(CommandManager.literal(DiamondEconomyConfig.getInstance().commandName).then(SetCommand.buildCommand()));
            }
            if (DiamondEconomyConfig.getInstance().withdrawCommandName != null) {
                dispatcher.register(CommandManager.literal(DiamondEconomyConfig.getInstance().commandName).then(WithdrawCommand.buildCommand()));
            }
        }
    }
}