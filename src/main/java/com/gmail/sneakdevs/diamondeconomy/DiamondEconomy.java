package com.gmail.sneakdevs.diamondeconomy;

import com.gmail.sneakdevs.diamondeconomy.config.DEConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
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

    public static void dropItem(Item item, int amount, ServerPlayerEntity player) {
        while (amount > item.getMaxCount()) {
            ItemEntity itemEntity = player.dropItem(new ItemStack(item, item.getMaxCount()), true);
            assert itemEntity != null;
            itemEntity.resetPickupDelay();
            itemEntity.setOwner(player.getUuid());
            amount -= item.getMaxCount();
        }

        ItemEntity itemEntity2 = player.dropItem(new ItemStack(item, amount), true);
        assert itemEntity2 != null;
        itemEntity2.resetPickupDelay();
        itemEntity2.setOwner(player.getUuid());
    }
}
