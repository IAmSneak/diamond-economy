package com.gmail.sneakdevs.diamondeconomy;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;

public class DiamondEconomy implements DedicatedServerModInitializer {
    public static final String MODID = "diamondeconomy";

    private static void serverStarting(MinecraftServer server){
        DatabaseManager.createNewDatabase(server.getSavePath(WorldSavePath.ROOT).resolve("diamondeconomy.sqlite").toFile());
    }

    @Override
    public void onInitializeServer() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            if (dedicated) {
                DECommands.register(dispatcher);
            }
        });
        ServerLifecycleEvents.SERVER_STARTING.register(DiamondEconomy::serverStarting);
    }
}
