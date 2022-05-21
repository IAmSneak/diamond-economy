package com.gmail.sneakdevs.diamondeconomyforge;

import com.gmail.sneakdevs.diamondeconomy.DiamondEconomy;
import com.gmail.sneakdevs.diamondeconomyforge.events.RegisterCommandEventHandler_DiamondEconomy;
import com.gmail.sneakdevs.diamondeconomyforge.events.StartEventHandler_DiamondEconomy;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;

@Mod(DiamondEconomy.MODID)
public class DiamondEconomyForge {
    public DiamondEconomyForge() {
        MinecraftForge.EVENT_BUS.addListener(StartEventHandler_DiamondEconomy::diamondeconomy_onServerStartingEvent);
        MinecraftForge.EVENT_BUS.addListener(RegisterCommandEventHandler_DiamondEconomy::diamondeconomy_registerCommandsEvent);
        DiamondEconomy.init();
    }
}