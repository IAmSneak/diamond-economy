package com.gmail.sneakdevs.diamondeconomy.interfaces;

public interface LockableContainerBlockEntityInterface {
    void diamondeconomy_setOwner(String newOwner);
    void diamondeconomy_setItem(String newItem);
    void diamondeconomy_setNbt(String newNbt);
    void diamondeconomy_setShop(boolean newShop);
    String diamondeconomy_getOwner();
    String diamondeconomy_getItem();
    String diamondeconomy_getNbt();
    boolean diamondeconomy_getShop();
}