package com.gmail.sneakdevs.diamondeconomyfabric;

import com.gmail.sneakdevs.diamondeconomy.DiamondEconomy;
import com.gmail.sneakdevs.diamondeconomy.command.DiamondEconomyCommands;
import com.gmail.sneakdevs.diamondeconomy.config.DiamondEconomyConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;

public class DiamondEconomyFabric implements ModInitializer {
    private static void serverStarting(MinecraftServer server){
        DiamondEconomy.initServer(server);
    }

    @Override
    public void onInitialize() {
        AutoConfig.register(DiamondEconomyConfig.class, JanksonConfigSerializer::new);
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> DiamondEconomyCommands.register(dispatcher));
        ServerLifecycleEvents.SERVER_STARTING.register(DiamondEconomyFabric::serverStarting);
    }
}
