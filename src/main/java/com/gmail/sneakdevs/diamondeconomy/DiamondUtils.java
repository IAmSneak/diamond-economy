package com.gmail.sneakdevs.diamondeconomy;

import com.gmail.sneakdevs.diamondeconomy.config.DiamondEconomyConfig;
import com.gmail.sneakdevs.diamondeconomy.sql.DatabaseManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;

public class DiamondUtils {
    public static DatabaseManager databaseManager;

    public static void registerTable(String query){
        DiamondEconomy.tableRegistry.add(query);
    }

    public static DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public static String valueString(int value) {
        return DiamondEconomyConfig.getInstance().currencyPrefix + value + DiamondEconomyConfig.getInstance().currencySuffix;
    }

    public static String valueString(long value) {
        return DiamondEconomyConfig.getInstance().currencyPrefix + value + DiamondEconomyConfig.getInstance().currencySuffix;
    }

    public static ArrayList<CurrencyType> getCurrencyList(){
        return DiamondEconomy.currencyList;
    }

    public static int dropCurrency(int amount, ServerPlayer player) {
        for (int i = 0; i < DiamondUtils.getCurrencyList().size() && amount > 0; i++) {
            CurrencyType currency = DiamondUtils.getCurrencyList().get(i);
            int buyValue = currency.getBuyValue();
            Item currencyItem = currency.getItem();
            int currencyStackSize = currency.getItem().getDefaultMaxStackSize();
            while (amount >= buyValue * currencyStackSize) {
                ItemEntity itemEntity = player.drop(new ItemStack(currencyItem, currencyStackSize), true);
                itemEntity.setNoPickUpDelay();
                amount -= buyValue * currencyStackSize;
            }

            if (amount >= buyValue) {
                int withdrawStackSize = amount/buyValue;
                ItemEntity itemEntity = player.drop(new ItemStack(currencyItem, withdrawStackSize), true);
                itemEntity.setNoPickUpDelay();
                amount -= withdrawStackSize * buyValue;
            }
        }

        DatabaseManager dm = getDatabaseManager();
        dm.changeBalance(player.getStringUUID(), amount);

        return amount;
    }

    public static int dropCurrency(int amount, Item item, ServerPlayer player) {
        CurrencyType currency = DiamondUtils.getDatabaseManager().getCurrency(BuiltInRegistries.ITEM.getKey(item).toString());
        if (currency != null && currency.isInCurrencyList()) {
            int buyValue = currency.getBuyValue();
            Item currencyItem = currency.getItem();
            int currencyStackSize = currency.getItem().getDefaultMaxStackSize();
            while (amount >= buyValue * currencyStackSize) {
                ItemEntity itemEntity = player.drop(new ItemStack(currencyItem, currencyStackSize), true);
                itemEntity.setNoPickUpDelay();
                amount -= buyValue * currencyStackSize;
            }

            if (amount >= buyValue) {
                int withdrawStackSize = amount / buyValue;
                ItemEntity itemEntity = player.drop(new ItemStack(currencyItem, withdrawStackSize), true);
                itemEntity.setNoPickUpDelay();
                amount -= withdrawStackSize * buyValue;
            }
        }

        DatabaseManager dm = getDatabaseManager();
        dm.changeBalance(player.getStringUUID(), amount);

        return amount;
    }

    public static void dropItem(int amount, Item item, ServerPlayer player) {
        int stackSize = item.getDefaultMaxStackSize();
        while (amount >= stackSize) {
            ItemEntity itemEntity = player.drop(new ItemStack(item, stackSize), true);
            itemEntity.setNoPickUpDelay();
            amount -= stackSize;
        }

        if (amount > 0) {
            ItemEntity itemEntity = player.drop(new ItemStack(item, amount), true);
            itemEntity.setNoPickUpDelay();
        }
    }

    public static void createCurrencyList() {
        DiamondEconomy.currencyList.clear();
        int i = 0;
        while (true) {
            CurrencyType currency = DiamondUtils.getDatabaseManager().getCurrency(i);
            if (currency == null) {
                break;
            }
            if (currency.isInCurrencyList()) {
                DiamondEconomy.currencyList.add(currency);
            }
            i++;
        }
    }
}
