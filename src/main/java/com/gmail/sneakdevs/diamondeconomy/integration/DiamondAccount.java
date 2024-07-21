package com.gmail.sneakdevs.diamondeconomy.integration;

import com.gmail.sneakdevs.diamondeconomy.DiamondEconomy;
import com.gmail.sneakdevs.diamondeconomy.DiamondUtils;
import com.gmail.sneakdevs.diamondeconomy.config.DiamondEconomyConfig;
import eu.pb4.common.economy.api.EconomyAccount;
import eu.pb4.common.economy.api.EconomyCurrency;
import eu.pb4.common.economy.api.EconomyProvider;
import eu.pb4.common.economy.api.EconomyTransaction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.UUID;

public class DiamondAccount implements EconomyAccount {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(DiamondEconomy.MODID, "deaccount");
    private final UUID uuid;
    private final String uuidString;

    public DiamondAccount(UUID uuid) {
        this.uuid = uuid;
        this.uuidString = uuid.toString();
    }

    @Override
    public Component name() {
        return Component.literal("DEAccount");
    }

    @Override
    public UUID owner() {
        return uuid;
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public long balance() {
        return DiamondUtils.getDatabaseManager().getBalanceFromUUID(uuidString);
    }

    @Override
    public EconomyTransaction canIncreaseBalance(long value) {
        int currentBal = DiamondUtils.getDatabaseManager().getBalanceFromUUID(uuidString);
        long newBal = (long)currentBal+value;
        if (newBal >= Integer.MAX_VALUE) {
            return new EconomyTransaction.Simple(
                    false,
                    Component.literal("Integer overflow ($" + newBal + " > $" + Integer.MAX_VALUE + ")"),
                    currentBal,
                    currentBal,
                    0,
                    this
            );
        }

        return new EconomyTransaction.Simple(
                true,
                Component.literal("Added $" + value + " to the account"),
                newBal,
                currentBal,
                value,
                this
        );
    }

    @Override
    public EconomyTransaction canDecreaseBalance(long value) {
        int currentBal = DiamondUtils.getDatabaseManager().getBalanceFromUUID(uuidString);
        long newBal = (long)currentBal-value;
        if (newBal < 0) {
            return new EconomyTransaction.Simple(
                    false,
                    Component.literal("Not enough money to take $" + value + "from your account of $" + currentBal),
                    currentBal,
                    currentBal,
                    0,
                    this
            );
        }

        return new EconomyTransaction.Simple(
                true,
                Component.literal("Added $" + value + " to your account"),
                newBal,
                currentBal,
                value,
                this
        );
    }

    @Override
    public void setBalance(long value) {
        DiamondUtils.getDatabaseManager().setBalance(uuidString, Math.toIntExact(value));
    }

    @Override
    public EconomyProvider provider() {
        return DiamondEconomyProvider.INSTANCE;
    }

    @Override
    public EconomyCurrency currency() {
        return DiamondCurrency.INSTANCE;
    }

    @Override
    public ItemStack accountIcon() {
        return DiamondEconomyConfig.getCurrency(0).getDefaultInstance();
    }
}
