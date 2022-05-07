package com.gmail.sneakdevs.diamondeconomy.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

@Config(name = "diamond_economy")
public class DiamondEconomyConfig implements ConfigData {
    public boolean transactionHistory = true;
    public boolean withdrawCommand = true;
    @Comment("Only ")
    public boolean specificWithdraw = true;
    public String commandName = "diamonds";
    @Comment("List (soon tm) of items used as currency")
    public String currency = "minecraft:diamond";

    public static String getCurrencyName() {
        return Registry.ITEM.get(Identifier.tryParse(AutoConfig.getConfigHolder(DiamondEconomyConfig.class).getConfig().currency)).getName().getString();
    }

    public static Item getCurrency() {
        return Registry.ITEM.get(Identifier.tryParse(AutoConfig.getConfigHolder(DiamondEconomyConfig.class).getConfig().currency));
    }

    @SuppressWarnings("unused")
    public static DiamondEconomyConfig getInstance() {
        return AutoConfig.getConfigHolder(DiamondEconomyConfig.class).getConfig();
    }
}