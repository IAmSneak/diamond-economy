package com.gmail.sneakdevs.diamondeconomyforge.events;

import com.gmail.sneakdevs.diamondeconomy.DiamondEconomy;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;

public class StartEventHandler_DiamondEconomy {
    public static void diamondeconomy_onServerStartingEvent(FMLServerStartingEvent event) {
        DiamondEconomy.initServer(event.getServer());
    }
}