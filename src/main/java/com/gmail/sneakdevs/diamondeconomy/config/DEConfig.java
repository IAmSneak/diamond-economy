package com.gmail.sneakdevs.diamondeconomy.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = "diamond_economy")
public class DEConfig implements ConfigData {
    public boolean transactionHistory = true;
    public boolean chestShops = true;
    public boolean emitRedstoneSignalFromShopPurchase = true;

    @SuppressWarnings("unused")
    public static DEConfig getInstance() {
        return AutoConfig.getConfigHolder(DEConfig.class).getConfig();
    }
}