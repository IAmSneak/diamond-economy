package com.gmail.sneakdevs.diamondeconomy.integration;

import com.gmail.sneakdevs.diamondeconomy.DiamondUtils;
import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.Placeholders;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import static com.gmail.sneakdevs.diamondeconomy.DiamondEconomy.MODID;

public class DiamondPlaceholders {
    public static void registerPlaceholders() {
        Placeholders.register(ResourceLocation.fromNamespaceAndPath(MODID, "rank_from_player"), (ctx, arg) -> {
            if (ctx.hasPlayer()) {
                return PlaceholderResult.value(Component.literal(DiamondUtils.getDatabaseManager().playerRank(ctx.player().getStringUUID()) + ""));
            } else {
                return PlaceholderResult.invalid();
            }
        });
        Placeholders.register(ResourceLocation.fromNamespaceAndPath(MODID, "rank_from_string_uuid"), (ctx, arg) -> {
            if (arg != null) {
                return PlaceholderResult.value(Component.literal(DiamondUtils.getDatabaseManager().playerRank(arg) + ""));
            } else {
                return PlaceholderResult.invalid();
            }
        });
        Placeholders.register(ResourceLocation.fromNamespaceAndPath(MODID, "balance_from_player"), (ctx, arg) -> {
            if (ctx.hasPlayer()) {
                return PlaceholderResult.value(Component.literal(DiamondUtils.getDatabaseManager().getBalanceFromUUID(ctx.player().getStringUUID()) + ""));
            } else {
                return PlaceholderResult.invalid();
            }
        });
        Placeholders.register(ResourceLocation.fromNamespaceAndPath(MODID, "balance_from_string_uuid"), (ctx, arg) -> {
            if (arg != null) {
                return PlaceholderResult.value(Component.literal(DiamondUtils.getDatabaseManager().getBalanceFromUUID(arg) + ""));
            } else {
                return PlaceholderResult.invalid();
            }
        });
        Placeholders.register(ResourceLocation.fromNamespaceAndPath(MODID, "balance_from_name"), (ctx, arg) -> {
            if (arg != null) {
                return PlaceholderResult.value(Component.literal(DiamondUtils.getDatabaseManager().getBalanceFromName(arg) + ""));
            } else {
                return PlaceholderResult.invalid();
            }
        });
        Placeholders.register(ResourceLocation.fromNamespaceAndPath(MODID, "player_from_rank"), (ctx, arg) -> {
            if (arg != null) {
                return PlaceholderResult.value(Component.literal(DiamondUtils.getDatabaseManager().rank(Integer.parseInt(arg))));
            } else {
                return PlaceholderResult.invalid();
            }
        });
    }
}
