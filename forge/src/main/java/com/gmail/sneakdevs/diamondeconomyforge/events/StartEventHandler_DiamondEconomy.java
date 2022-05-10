package com.gmail.sneakdevs.diamondeconomyforge.events;

import com.gmail.sneakdevs.diamondeconomy.DiamondEconomy;
import com.gmail.sneakdevs.diamondeconomy.sql.SQLiteDatabaseManager;
import com.gmail.sneakdevs.diamondeconomy.config.DiamondEconomyConfig;
import net.minecraft.util.WorldSavePath;
import net.minecraftforge.event.server.ServerStartingEvent;

public class StartEventHandler_DiamondEconomy {
    public static void diamondeconomy_onServerStartingEvent(ServerStartingEvent event) {
        AutoConfig.setPath(event.getServer().getSavePath(WorldSavePath.ROOT).resolve(DiamondEconomy.MODID + ".sqlite"))
        SQLiteDatabaseManager.createNewDatabase(DiamondEconomyConfig.getInstance().filePath.toFile());
    }
}