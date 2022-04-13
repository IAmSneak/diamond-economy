package com.gmail.sneakdevs.diamondeconomy;

import com.gmail.sneakdevs.diamondeconomy.config.DEConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;

public class DiamondEconomy implements ModInitializer {
    public static final String MODID = "diamondeconomy";

    private static void serverStarting(MinecraftServer server){
        DatabaseManager.createNewDatabase(server.getSavePath(WorldSavePath.ROOT).resolve(MODID + ".sqlite").toFile());
    }

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> DECommands.register(dispatcher));
        AutoConfig.register(DEConfig.class, Toml4jConfigSerializer::new);
        ServerLifecycleEvents.SERVER_STARTING.register(DiamondEconomy::serverStarting);
    }
}
