package com.gmail.sneakdevs.diamondeconomy;

import com.gmail.sneakdevs.diamondeconomy.config.DiamondEconomyConfig;
import com.gmail.sneakdevs.diamondeconomy.sql.DatabaseManager;
import com.gmail.sneakdevs.diamondeconomy.sql.SQLiteDatabaseManager;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.LevelResource;

import java.io.File;
import java.util.ArrayList;

public class DiamondEconomy {
    public static final String MODID = "diamondeconomy";
    public static ArrayList<String> tableRegistry;

    public static DatabaseManager getDatabaseManager() {
        return new SQLiteDatabaseManager();
    }

    public static void init() {
        AutoConfig.register(DiamondEconomyConfig.class, JanksonConfigSerializer::new);
    }

    public static void initServer(MinecraftServer server) {
        tableRegistry.add("CREATE TABLE IF NOT EXISTS diamonds (uuid text PRIMARY KEY, name text NOT NULL, money integer DEFAULT 0);");
        SQLiteDatabaseManager.createNewDatabase((DiamondEconomyConfig.getInstance().fileLocation != null) ? (new File(DiamondEconomyConfig.getInstance().fileLocation)) : server.getWorldPath(LevelResource.ROOT).resolve(DiamondEconomy.MODID + ".sqlite").toFile());
    }

    public static void registerTable(String query){
        tableRegistry.add(query);
    }

    public static int dropItem(int amount, ServerPlayer player) {
        for (int i = DiamondEconomyConfig.getCurrencyValues().length - 1; i >= 0 && amount > 0; i--) {
            while (amount >= DiamondEconomyConfig.getCurrencyValues()[i] * DiamondEconomyConfig.getCurrency(i).getMaxStackSize()) {
                ItemEntity itemEntity = player.drop(new ItemStack(DiamondEconomyConfig.getCurrency(i), DiamondEconomyConfig.getCurrency(i).getMaxStackSize()), true);
                assert itemEntity != null;
                itemEntity.setNoPickUpDelay();
                itemEntity.setOwner(player.getUUID());
                amount -= DiamondEconomyConfig.getCurrency(i).getMaxStackSize() * DiamondEconomyConfig.getCurrencyValues()[i];
            }
            while (amount >= DiamondEconomyConfig.getCurrencyValues()[i]) {
                ItemEntity itemEntity = player.drop(new ItemStack(DiamondEconomyConfig.getCurrency(i), amount/DiamondEconomyConfig.getCurrencyValues()[i]), true);
                assert itemEntity != null;
                itemEntity.setNoPickUpDelay();
                itemEntity.setOwner(player.getUUID());
                amount -= amount/DiamondEconomyConfig.getCurrencyValues()[i]*DiamondEconomyConfig.getCurrencyValues()[i];
            }
        }
        if (amount > 0) {
            DatabaseManager dm = DiamondEconomy.getDatabaseManager();
            dm.changeBalance(player.getStringUUID(), amount);
        }
        return amount;
    }
}
