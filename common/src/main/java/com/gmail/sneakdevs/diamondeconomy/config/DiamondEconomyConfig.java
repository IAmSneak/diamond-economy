package com.gmail.sneakdevs.diamondeconomy.config;

import com.gmail.sneakdevs.diamondeconomy.DiamondEconomy;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

@Config(name = DiamondEconomy.MODID)
public class DiamondEconomyConfig implements ConfigData {

    @Comment("Name of the base command (default: \"diamonds\")")
    public String commandName = "diamonds";

    @Comment("List of items used as currency")
    public String[] currencies = {"minecraft:diamond","minecraft:diamond_block"};

    @Comment("Values of each currency in the same order, decimals not allowed (must be in ascending order)")
    public int[] currencyValues = {1,9};

    @Comment("Enable/disable the withdraw command (default: true)")
    public boolean withdrawCommand = true;

    public static Item getCurrency(int num) {
        return Registry.ITEM.get(Identifier.tryParse(DiamondEconomyConfig.getInstance().currencies[num]));
    }

    public static int[] getCurrencyValues() {
        return DiamondEconomyConfig.getInstance().currencyValues;
    }

    public static DiamondEconomyConfig getInstance() {
        return AutoConfig.getConfigHolder(DiamondEconomyConfig.class).getConfig();
    }
}