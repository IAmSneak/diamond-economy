package com.gmail.sneakdevs.diamondeconomyfabric;

import com.gmail.sneakdevs.diamondeconomy.DiamondEconomy;
import com.gmail.sneakdevs.diamondeconomy.config.DEConfig;
import com.gmail.sneakdevs.diamondeconomy.sql.SQLiteDatabaseManager;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;

public class DiamondEconomyFabric implements ModInitializer {
    private static void serverStarting(MinecraftServer server){
        SQLiteDatabaseManager.createNewDatabase(server.getSavePath(WorldSavePath.ROOT).resolve(DiamondEconomy.MODID + ".sqlite").toFile());
    }

    @Override
    public void onInitialize() {
        AutoConfig.register(DEConfig.class, Toml4jConfigSerializer::new);
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> DECommandsFabric.register(dispatcher));
        ServerLifecycleEvents.SERVER_STARTING.register(DiamondEconomyFabric::serverStarting);
    }
}
