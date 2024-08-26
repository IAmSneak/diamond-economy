package com.gmail.sneakdevs.diamondeconomy.sql;

import com.gmail.sneakdevs.diamondeconomy.CurrencyType;

import java.io.File;

public interface DatabaseManager {
    static void createNewDatabase(File file) {}

    void addPlayer(String uuid, String name);
    void updateName(String uuid, String name);
    void setName(String uuid, String name);
    String getNameFromUUID(String uuid);

    int getBalanceFromUUID(String uuid);
    int getBalanceFromName(String name);
    boolean setBalance(String uuid, int money);
    void setAllBalance(int money);
    boolean changeBalance(String uuid, int money);
    void changeAllBalance(int money);

    String top(String uuid, int topAmount);
    String rank(int rank);
    int playerRank(String uuid);

    boolean addCurrency(String item, int sellValue, int buyValue, boolean inCurrencyList, boolean canBuy, boolean canSell, boolean init);
    boolean setSellValue(String item, int sellValue);
    boolean setBuyValue(String item, int buyValue);
    boolean setInCurrencyList(String item, boolean inCurrencyList);
    boolean setCanBuy(String item, boolean canBuy);
    boolean setCanSell(String item, boolean canSell);

    boolean isCurrency(String item);
    int getSellValue(String item);
    int getBuyValue(String item);
    boolean getInCurrencyList(String item);
    boolean getCanBuy(String item);
    boolean getCanSell(String item);

    boolean removeCurrency(String item);
    CurrencyType getCurrency(String item);
    CurrencyType getCurrency(int i);

}
