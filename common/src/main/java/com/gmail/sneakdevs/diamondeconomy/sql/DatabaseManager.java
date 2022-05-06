package com.gmail.sneakdevs.diamondeconomy.sql;

import java.io.File;

public interface DatabaseManager {
    static void createNewDatabase(File file) {}

    void createTransaction(String type, String executerUUID, String victimUUID, int amount, int oldVal);

    void addPlayer(String uuid, String name);
    void updateName(String uuid, String name);
    void setName(String uuid, String name);
    String getNameFromUUID(String uuid);

    int getBalanceFromUUID(String uuid);
    int getBalanceFromName(String name);
    boolean setBalance(String uuid, int money);
    boolean changeBalance(String uuid, int money);

    String top(String uuid, int topAmount);
    String history(int page);
    String getMessage(String type, String executer, String victim, int amount, long time, int oldVal);
}
