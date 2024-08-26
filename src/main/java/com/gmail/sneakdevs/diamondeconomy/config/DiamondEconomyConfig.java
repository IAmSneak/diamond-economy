package com.gmail.sneakdevs.diamondeconomy.config;

import com.gmail.sneakdevs.diamondeconomy.DiamondEconomy;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

@Config(name = DiamondEconomy.MODID)
public class DiamondEconomyConfig implements ConfigData {

    @Comment("Currency used as an icon in some places")
    public String currencyIcon = "minecraft:diamond";

    @Comment("Currency with a buy and sell value of 1. Other currencies can be added with the currency command.")
    public String mainCurrency = "minecraft:diamond";

    @Comment("Words or symbols that come before and after the balance")
    public String currencyPrefix = "$";
    public String currencySuffix = "";

    @Comment("Type of database to use (only sqlite is supported in the base mod, future database types may be added in addon mods)")
    public String databaseType = "sqlite";

    @Comment("Where the diamondeconomy.sqlite file is located (null to use default location, only used if databaseType is sqlite) (ex: \"C:/Users/example/Desktop/server/world/diamondeconomy.sqlite\")")
    public String fileLocation = null;

    @Comment("Name of the base command (null to disable base command)")
    public String commandName = "diamonds";

    @Comment("Names of the subcommands (null to disable command)")
    public String topCommandName = "top";
    public String balanceCommandName = "balance";
    public String depositCommandName = "deposit";
    public String sendCommandName = "send";
    public String withdrawCommandName = "withdraw";
    public String buyCommandName = "buy";
    public String sellCommandName = "sell";

    @Comment("Names of the admin subcommands (null to disable command)")
    public String setCommandName = "set";
    public String modifyCommandName = "modify";
    public String currencyCommandName = "currency";

    @Comment("Money the player starts with when they first join the server")
    public int startingMoney = 0;

    @Comment("How often to add money to each player, in seconds (0 to disable)")
    public int moneyAddTimer = 0;

    @Comment("Amount of money to add each cycle")
    public int moneyAddAmount = 0;

    @Comment("Permission level (1-4) of the op commands in diamond economy. Set to 2 to allow command blocks to use these commands.")
    public int opCommandsPermissionLevel = 2;

    public static DiamondEconomyConfig getInstance() {
        return AutoConfig.getConfigHolder(DiamondEconomyConfig.class).getConfig();
    }

    public static Item getCurrencyIcon() {
        return BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(DiamondEconomyConfig.getInstance().currencyIcon));
    }
}