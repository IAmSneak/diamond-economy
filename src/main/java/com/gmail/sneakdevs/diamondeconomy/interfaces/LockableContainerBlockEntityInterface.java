package com.gmail.sneakdevs.diamondeconomy.interfaces;

public interface LockableContainerBlockEntityInterface {
    void diamondeconomy_setOwner(String newOwner);
    void diamondeconomy_setShop(boolean newShop);
    void diamondeconomy_setItem(String newItem);
    String diamondeconomy_getOwner();
    String diamondeconomy_getItem();
    boolean diamondeconomy_getShop();
}