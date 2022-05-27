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
    public static ArrayList<String> tableRegistry = new ArrayList<>();

    public static DatabaseManager getDatabaseManager() {
        return new SQLiteDatabaseManager();
    }

    public static void init() {
        AutoConfig.register(DiamondEconomyConfig.class, JanksonConfigSerializer::new);
    }

    public static void initServer(MinecraftServer server) {
        registerTable("CREATE TABLE IF NOT EXISTS diamonds (uuid text PRIMARY KEY, name text NOT NULL, money integer DEFAULT 0);");
        SQLiteDatabaseManager.createNewDatabase((DiamondEconomyConfig.getInstance().fileLocation != null) ? (new File(DiamondEconomyConfig.getInstance().fileLocation)) : server.getWorldPath(LevelResource.ROOT).resolve(DiamondEconomy.MODID + ".sqlite").toFile());
    }

    public static void registerTable(String query){
        tableRegistry.add(query);
    }

    public static int dropItem(int amount, ServerPlayer player) {
        if (DiamondEconomyConfig.getInstance().greedyWithdraw) {
            for (int i = DiamondEconomyConfig.getCurrencyValues().length - 1; i >= 0 && amount > 0; i--) {
                while (amount >= DiamondEconomyConfig.getCurrencyValues()[i] * DiamondEconomyConfig.getCurrency(i).getMaxStackSize()) {
                    ItemEntity itemEntity = player.drop(new ItemStack(DiamondEconomyConfig.getCurrency(i), DiamondEconomyConfig.getCurrency(i).getMaxStackSize()), true);
                    assert itemEntity != null;
                    itemEntity.setNoPickUpDelay();
                    itemEntity.setOwner(player.getUUID());
                    amount -= DiamondEconomyConfig.getCurrency(i).getMaxStackSize() * DiamondEconomyConfig.getCurrencyValues()[i];
                }
                if (amount >= DiamondEconomyConfig.getCurrencyValues()[i]) {
                    ItemEntity itemEntity = player.drop(new ItemStack(DiamondEconomyConfig.getCurrency(i), amount/DiamondEconomyConfig.getCurrencyValues()[i]), true);
                    assert itemEntity != null;
                    itemEntity.setNoPickUpDelay();
                    itemEntity.setOwner(player.getUUID());
                    amount -= amount/DiamondEconomyConfig.getCurrencyValues()[i]*DiamondEconomyConfig.getCurrencyValues()[i];
                }
            }
        } else {
            while (amount >= DiamondEconomyConfig.getCurrencyValues()[0] * DiamondEconomyConfig.getCurrency(0).getMaxStackSize()) {
                ItemEntity itemEntity = player.drop(new ItemStack(DiamondEconomyConfig.getCurrency(0), DiamondEconomyConfig.getCurrency(0).getMaxStackSize()), true);
                assert itemEntity != null;
                itemEntity.setNoPickUpDelay();
                itemEntity.setOwner(player.getUUID());
                amount -= DiamondEconomyConfig.getCurrency(0).getMaxStackSize() * DiamondEconomyConfig.getCurrencyValues()[0];
            }
            if (amount >= DiamondEconomyConfig.getCurrencyValues()[0]) {
                ItemEntity itemEntity = player.drop(new ItemStack(DiamondEconomyConfig.getCurrency(0), amount/DiamondEconomyConfig.getCurrencyValues()[0]), true);
                assert itemEntity != null;
                itemEntity.setNoPickUpDelay();
                itemEntity.setOwner(player.getUUID());
                amount -= amount/DiamondEconomyConfig.getCurrencyValues()[0]*DiamondEconomyConfig.getCurrencyValues()[0];
            }
        }
        if (amount > 0) {
            DatabaseManager dm = DiamondEconomy.getDatabaseManager();
            dm.changeBalance(player.getStringUUID(), amount);
        }
        return amount;
    }
}
