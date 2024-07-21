package com.gmail.sneakdevs.diamondeconomy.integration;

import com.gmail.sneakdevs.diamondeconomy.DiamondEconomy;
import eu.pb4.common.economy.api.CommonEconomy;
import eu.pb4.common.economy.api.EconomyAccount;
import eu.pb4.common.economy.api.EconomyCurrency;
import eu.pb4.common.economy.api.EconomyProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

public class DiamondEconomyProvider implements EconomyProvider {
    public static final DiamondEconomyProvider INSTANCE = new DiamondEconomyProvider();

    public static void init() {
        CommonEconomy.register(DiamondEconomy.MODID, INSTANCE);
    }

    @Override
    public Component name() {
        return null;
    }

    @Override
    public @Nullable EconomyAccount getAccount(MinecraftServer server, com.mojang.authlib.GameProfile profile, String accountId) {
        if (!accountId.equals(DiamondAccount.ID.getPath())) return null;

        return new DiamondAccount(profile.getId());
    }

    @Override
    public Collection<EconomyAccount> getAccounts(MinecraftServer server, com.mojang.authlib.GameProfile profile) {
        return Collections.singleton(new DiamondAccount(profile.getId()));
    }

    @Override
    public @Nullable EconomyCurrency getCurrency(MinecraftServer server, String currencyId) {
        return null;
    }

    @Override
    public Collection<EconomyCurrency> getCurrencies(MinecraftServer server) {
        return null;
    }

    @Override
    public @Nullable String defaultAccount(MinecraftServer server, com.mojang.authlib.GameProfile profile, EconomyCurrency currency) {
        return null;
    }
}
