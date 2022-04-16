package com.gmail.sneakdevs.diamondeconomy.interfaces;

import java.util.UUID;

public interface SignBlockEntityInterface {
    void diamondeconomy_setOwner(String newOwner);
    void diamondeconomy_setItemEntity(UUID newEntity);
    void diamondeconomy_setShop(boolean newShop);
    void diamondeconomy_setAdminShop(boolean newAdminShop);
    String diamondeconomy_getOwner();
    UUID diamondeconomy_getItemEntity();
    boolean diamondeconomy_getAdminShop();
    boolean diamondeconomy_getShop();
}