package com.gmail.sneakdevs.diamondeconomyforge.events;

import com.gmail.sneakdevs.diamondeconomy.command.DiamondEconomyCommands;
import net.minecraftforge.event.RegisterCommandsEvent;

public class RegisterCommandEventHandler_DiamondEconomy {
    public static void diamondeconomy_registerCommandsEvent(RegisterCommandsEvent event) {
        DiamondEconomyCommands.register(event.getDispatcher());
    }
}
