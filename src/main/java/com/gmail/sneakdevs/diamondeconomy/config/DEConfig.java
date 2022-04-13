package com.gmail.sneakdevs.diamondeconomy.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = "diamond_economy")
public class DEConfig implements ConfigData {
    public boolean transactionHistory = true;

    public static DEConfig getInstance() {
        return AutoConfig.getConfigHolder(DEConfig.class).getConfig();
    }


}