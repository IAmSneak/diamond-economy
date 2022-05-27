package com.gmail.sneakdevs.diamondeconomy.config;

import com.gmail.sneakdevs.diamondeconomy.DiamondEconomy;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

@Config(name = DiamondEconomy.MODID)
public class DiamondEconomyConfig implements ConfigData {

    @Comment("List of items used as currency")
    public String[] currencies = {"minecraft:diamond","minecraft:diamond_block"};

    @Comment("Values of each currency in the same order, decimals not allowed (must be in ascending order unless greedyWithdraw is disabled)")
    public int[] currencyValues = {1,9};

    @Comment("Where the diamondeconomy.sqlite file is located (ex: \"C:/Users/example/Desktop/server/world/diamondeconomy.sqlite\")")
    public String fileLocation = null;

    @Comment("Name of the base command (null to disable base command)")
    public String commandName = "diamonds";

    @Comment("Names of the subcommands (null to disable command)")
    public String topCommandName = "top";
    public String balanceCommandName = "balance";
    public String depositCommandName = "deposit";
    public String sendCommandName = "send";
    public String withdrawCommandName = "withdraw";

    @Comment("Names of the admin subcommands (null to disable command)")
    public String setCommandName = "set";
    public String modifyCommandName = "modify";

    @Comment("Try to withdraw items using the most high value items possible (ex. diamond blocks then diamonds) \n If disabled withdraw will give player the first item in the list")
    public boolean greedyWithdraw = true;

    public static Item getCurrency(int num) {
        return Registry.ITEM.get(ResourceLocation.tryParse(DiamondEconomyConfig.getInstance().currencies[num]));
    }

    public static String getCurrencyName(int num) {
        return Registry.ITEM.get(ResourceLocation.tryParse(DiamondEconomyConfig.getInstance().currencies[num])).getDescription().getString();
    }

    public static int[] getCurrencyValues() {
        return DiamondEconomyConfig.getInstance().currencyValues;
    }

    public static DiamondEconomyConfig getInstance() {
        return AutoConfig.getConfigHolder(DiamondEconomyConfig.class).getConfig();
    }
}