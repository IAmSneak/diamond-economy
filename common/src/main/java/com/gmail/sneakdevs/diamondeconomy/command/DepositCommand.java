package com.gmail.sneakdevs.diamondeconomy.command;

import com.gmail.sneakdevs.diamondeconomy.DiamondEconomy;
import com.gmail.sneakdevs.diamondeconomy.config.DiamondEconomyConfig;
import com.gmail.sneakdevs.diamondeconomy.sql.DatabaseManager;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

public class DepositCommand {
    public static LiteralArgumentBuilder<ServerCommandSource> buildCommand(){
        return CommandManager.literal(DiamondEconomyConfig.getInstance().depositCommandName)
                .executes(DepositCommand::depositCommand);
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
}
