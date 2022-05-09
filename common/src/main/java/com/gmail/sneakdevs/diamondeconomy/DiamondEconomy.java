package com.gmail.sneakdevs.diamondeconomy;

import com.gmail.sneakdevs.diamondeconomy.sql.DatabaseManager;
import com.gmail.sneakdevs.diamondeconomy.sql.SQLiteDatabaseManager;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

public class DiamondEconomy {
    public static final String MODID = "diamondeconomy";

    public static DatabaseManager getDatabaseManager() {
        return new SQLiteDatabaseManager();
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

    public static String getExecuterUUID(CommandContext<ServerCommandSource> ctx) {
        try {
            return ctx.getSource().getPlayer().getUuidAsString();
        } catch (CommandSyntaxException e) {
            return "@";
        }
    }
}
