package com.gmail.sneakdevs.diamondeconomy;

import com.gmail.sneakdevs.diamondeconomy.config.DiamondEconomyConfig;
import com.gmail.sneakdevs.diamondeconomy.sql.DatabaseManager;
import com.gmail.sneakdevs.diamondeconomy.sql.SQLiteDatabaseManager;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

public class DiamondEconomy {
    public static final String MODID = "diamondeconomy";

    public static DatabaseManager getDatabaseManager() {
        return new SQLiteDatabaseManager();
    }

    public static int dropItem(int amount, ServerPlayerEntity player) {
        for (int i = DiamondEconomyConfig.getCurrencyValues().length - 1; i >= 0 && amount > 0; i--) {
            while (amount >= DiamondEconomyConfig.getCurrencyValues()[i] * DiamondEconomyConfig.getCurrency(i).getMaxCount()) {
                ItemEntity itemEntity = player.dropItem(new ItemStack(DiamondEconomyConfig.getCurrency(i), DiamondEconomyConfig.getCurrency(i).getMaxCount()), true);
                assert itemEntity != null;
                itemEntity.resetPickupDelay();
                itemEntity.setOwner(player.getUuid());
                amount -= DiamondEconomyConfig.getCurrency(i).getMaxCount() * DiamondEconomyConfig.getCurrencyValues()[i];
            }
            while (amount >= DiamondEconomyConfig.getCurrencyValues()[i]) {
                ItemEntity itemEntity = player.dropItem(new ItemStack(DiamondEconomyConfig.getCurrency(i), amount/DiamondEconomyConfig.getCurrencyValues()[i]), true);
                assert itemEntity != null;
                itemEntity.resetPickupDelay();
                itemEntity.setOwner(player.getUuid());
                amount -= amount/DiamondEconomyConfig.getCurrencyValues()[i]*DiamondEconomyConfig.getCurrencyValues()[i];
            }
        }
        if (amount > 0) {
            DatabaseManager dm = DiamondEconomy.getDatabaseManager();
            dm.changeBalance(player.getUuidAsString(), amount);
        }
        return amount;
    }
}
