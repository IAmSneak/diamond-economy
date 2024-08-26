package com.gmail.sneakdevs.diamondeconomy;

import com.gmail.sneakdevs.diamondeconomy.command.DiamondEconomyCommands;
import com.gmail.sneakdevs.diamondeconomy.config.DiamondEconomyConfig;
import com.gmail.sneakdevs.diamondeconomy.integration.DiamondPlaceholders;
import com.gmail.sneakdevs.diamondeconomy.sql.SQLiteDatabaseManager;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.File;
import java.util.ArrayList;

public class DiamondEconomy implements ModInitializer {
    public static final String MODID = "diamondeconomy";
    public static ArrayList<String> tableRegistry = new ArrayList<>();
    public static ArrayList<CurrencyType> currencyList = new ArrayList<>();

    public static void initServer(MinecraftServer server) {
        if (DiamondEconomyConfig.getInstance().databaseType.equals("sqlite")) {
            DiamondUtils.databaseManager = new SQLiteDatabaseManager();
            DiamondUtils.registerTable("CREATE TABLE IF NOT EXISTS diamonds (uuid text PRIMARY KEY, name text NOT NULL, money integer DEFAULT 0);");
            DiamondUtils.registerTable("CREATE TABLE IF NOT EXISTS currencies (item text PRIMARY KEY, sellvalue integer, buyvalue integer, incurrencylist bit, canbuy bit, cansell bit);");
            SQLiteDatabaseManager.createNewDatabase((DiamondEconomyConfig.getInstance().fileLocation != null) ? (new File(DiamondEconomyConfig.getInstance().fileLocation)) : server.getWorldPath(LevelResource.ROOT).resolve(DiamondEconomy.MODID + ".sqlite").toFile());
        }
        DiamondUtils.getDatabaseManager().addCurrency(DiamondEconomyConfig.getInstance().mainCurrency, 1, 1, true, true, true, true);
        DiamondUtils.createCurrencyList();
    }

    @Override
    public void onInitialize() {
        AutoConfig.register(DiamondEconomyConfig.class, JanksonConfigSerializer::new);
        DiamondPlaceholders.registerPlaceholders();
        CommandRegistrationCallback.EVENT.register((dispatcher, commandBuildContext, environment) -> DiamondEconomyCommands.register(dispatcher, commandBuildContext));
        ServerLifecycleEvents.SERVER_STARTING.register(DiamondEconomy::initServer);
    }
}
