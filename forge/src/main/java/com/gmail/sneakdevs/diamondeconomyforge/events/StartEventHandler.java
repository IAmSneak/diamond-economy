package com.gmail.sneakdevs.diamondeconomyforge.events;

import com.gmail.sneakdevs.diamondeconomy.DiamondEconomy;
import com.gmail.sneakdevs.diamondeconomy.sql.SQLiteDatabaseManager;
import net.minecraft.util.WorldSavePath;
import net.minecraftforge.event.server.ServerStartingEvent;

public class StartEventHandler {
    public static void onServerStartingEvent(ServerStartingEvent event) {
        SQLiteDatabaseManager.createNewDatabase(event.getServer().getSavePath(WorldSavePath.ROOT).resolve(DiamondEconomy.MODID + ".sqlite").toFile());
    }
}
