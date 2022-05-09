package com.gmail.sneakdevs.diamondeconomy;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.command.argument.ItemStringReader;
import net.minecraft.util.registry.Registry;

public class CurrencyArgumentType implements ArgumentType<ItemStackArgument> {
    private static final Collection<String> EXAMPLES = Arrays.asList("stick", "minecraft:stick", "stick{foo=bar}");

    public CurrencyArgumentType() {
    }

    public static net.minecraft.command.argument.ItemStackArgumentType itemStack() {
        return new net.minecraft.command.argument.ItemStackArgumentType();
    }

    public ItemStackArgument parse(StringReader stringReader) throws CommandSyntaxException {
        ItemStringReader itemStringReader = (new ItemStringReader(stringReader, false)).consume();
        return new ItemStackArgument(itemStringReader.getItem(), itemStringReader.getNbt());
    }

    public static <S> ItemStackArgument getItemStackArgument(CommandContext<S> context, String name) {
        return (ItemStackArgument)context.getArgument(name, ItemStackArgument.class);
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        StringReader stringReader = new StringReader(builder.getInput());
        stringReader.setCursor(builder.getStart());
        ItemStringReader itemStringReader = new ItemStringReader(stringReader, false);

        try {
            itemStringReader.consume();
        } catch (CommandSyntaxException var6) {
        }

        //todo somehow create a registry for currencies
        return itemStringReader.getSuggestions(builder, Registry.ITEM);
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
