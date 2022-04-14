package com.gmail.sneakdevs.diamondeconomy.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

@Config(name = "diamond_economy")
public class DEConfig implements ConfigData {
    public boolean transactionHistory = true;
    public String commandName = "diamonds";
    public String currency = "minecraft:diamond";
    public boolean chestShops = true;
    public boolean emitRedstoneSignalFromShopPurchase = true;

    public static String getCurrencyName() {
        return Registry.ITEM.get(Identifier.tryParse(AutoConfig.getConfigHolder(DEConfig.class).getConfig().currency)).getName().getString();
    }

    public static Item getCurrency() {
        return Registry.ITEM.get(Identifier.tryParse(AutoConfig.getConfigHolder(DEConfig.class).getConfig().currency));
    }

    @SuppressWarnings("unused")
    public static DEConfig getInstance() {
        return AutoConfig.getConfigHolder(DEConfig.class).getConfig();
    }
}