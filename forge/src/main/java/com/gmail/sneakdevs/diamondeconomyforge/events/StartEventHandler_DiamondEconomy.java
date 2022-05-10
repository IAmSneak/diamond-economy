package com.gmail.sneakdevs.diamondeconomyforge.events;

import com.gmail.sneakdevs.diamondeconomy.DiamondEconomy;
import com.gmail.sneakdevs.diamondeconomy.sql.SQLiteDatabaseManager;
import com.gmail.sneakdevs.diamondeconomy.config.DiamondEconomyConfig;
import net.minecraft.util.WorldSavePath;
import net.minecraftforge.event.server.ServerStartingEvent;

import java.io.File;

public class StartEventHandler_DiamondEconomy {
    public static void diamondeconomy_onServerStartingEvent(ServerStartingEvent event) {
        SQLiteDatabaseManager.createNewDatabase((DiamondEconomyConfig.getInstance().fileLocation != null) ? (new File(DiamondEconomyConfig.getInstance().fileLocation)) : event.getServer().getSavePath(WorldSavePath.ROOT).resolve(DiamondEconomy.MODID + ".sqlite").toFile());
    }
}