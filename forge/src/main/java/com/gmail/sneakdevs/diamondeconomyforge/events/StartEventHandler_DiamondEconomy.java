package com.gmail.sneakdevs.diamondeconomyforge.events;

import com.gmail.sneakdevs.diamondeconomy.DiamondEconomy;
import net.minecraftforge.event.server.ServerStartingEvent;

public class StartEventHandler_DiamondEconomy {
    public static void diamondeconomy_onServerStartingEvent(ServerStartingEvent event) {
        DiamondEconomy.initServer(event.getServer());
    }
}