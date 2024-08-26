package com.gmail.sneakdevs.diamondeconomy.integration;

import com.gmail.sneakdevs.diamondeconomy.DiamondEconomy;
import com.gmail.sneakdevs.diamondeconomy.DiamondUtils;
import com.gmail.sneakdevs.diamondeconomy.config.DiamondEconomyConfig;
import eu.pb4.common.economy.api.EconomyCurrency;
import eu.pb4.common.economy.api.EconomyProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class DiamondCurrency implements EconomyCurrency {
    public static final DiamondCurrency INSTANCE = new DiamondCurrency();
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(DiamondEconomy.MODID, "diamonds");

    @Override
    public Component name() {
        return Component.literal("Diamonds");
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String formatValue(long value, boolean precise) {
        return DiamondUtils.valueString(value);
    }

    @Override
    public long parseValue(String value) throws NumberFormatException {
        if (value.startsWith(DiamondEconomyConfig.getInstance().currencyPrefix)) {
            value = value.substring(DiamondEconomyConfig.getInstance().currencyPrefix.length());
            if (value.endsWith(DiamondEconomyConfig.getInstance().currencySuffix)) {
                value = value.substring(0, value.length() - DiamondEconomyConfig.getInstance().currencySuffix.length() - 1);
            }
        } else {
            return 0;
        }

        return Long.parseLong(value);
    }

    @Override
    public EconomyProvider provider() {
        return DiamondEconomyProvider.INSTANCE;
    }

    @Override
    public ItemStack icon() {
        return DiamondEconomyConfig.getCurrencyIcon().getDefaultInstance();
    }
}
