package com.gmail.sneakdevs.diamondeconomy;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class CurrencyType {
    private boolean canBuy;
    private boolean canSell;
    private boolean inCurrencyList;
    private int sellValue;
    private int buyValue;
    private final Item item;

    public CurrencyType(Item item, int sellValue, int buyValue, boolean inCurrencyList, boolean canBuy, boolean canSell){
        this.canBuy = canBuy;
        this.canSell = canSell;
        this.inCurrencyList = inCurrencyList;
        this.sellValue = sellValue;
        this.buyValue = buyValue;
        this.item = item;
    }

    public CurrencyType(String item, int sellValue, int buyValue, boolean inCurrencyList, boolean canBuy, boolean canSell){
        this.canBuy = canBuy;
        this.canSell = canSell;
        this.inCurrencyList = inCurrencyList;
        this.sellValue = sellValue;
        this.buyValue = buyValue;
        this.item = BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(item));
    }

    public Item getItem(){
        return item;
    }

    public boolean canBuy(){
        return canBuy;
    }

    public boolean canSell(){
        return canSell;
    }

    public boolean isInCurrencyList(){
        return inCurrencyList;
    }

    public int getSellValue(){
        return sellValue;
    }

    public int getBuyValue() {
        return buyValue;
    }

    public void setCanBuy(boolean canBuy){
        this.canBuy = canBuy;
    }

    public void setCanSell(boolean canSell){
        this.canSell = canSell;
    }

    public void setInCurrencyList(boolean inCurrencyList){
        this.inCurrencyList = inCurrencyList;
    }

    public void setSellValue(int sellValue){
        this.sellValue = sellValue;
    }

    public void setBuyValue(int buyValue) {
        this.buyValue = buyValue;
    }
}
