package com.gmail.sneakdevs.diamondeconomy.mixin;

import com.gmail.sneakdevs.diamondeconomy.config.DEConfig;
import com.gmail.sneakdevs.diamondeconomy.interfaces.SignBlockEntityInterface;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(SignBlockEntity.class)
public class SignBlockEntityMixin implements SignBlockEntityInterface {
    private String diamondeconomy_owner;
    private UUID diamondeconomy_itemEntity;
    private boolean diamondeconomy_isShop;
    private boolean diamondeconomy_isAdminShop;

    public void diamondeconomy_setOwner(String newOwner)  {
        this.diamondeconomy_owner = newOwner;
    }
    public void diamondeconomy_setItemEntity(UUID newEntity)  {
        this.diamondeconomy_itemEntity = newEntity;
    }
    public void diamondeconomy_setShop(boolean newShop) {
        this.diamondeconomy_isShop = newShop;
    }
    public void diamondeconomy_setAdminShop(boolean newAdminShop) {
        this.diamondeconomy_isAdminShop = newAdminShop;
    }

    public boolean diamondeconomy_getAdminShop() {
        return this.diamondeconomy_isAdminShop;
    }
    public boolean diamondeconomy_getShop() {
        return this.diamondeconomy_isShop;
    }
    public String diamondeconomy_getOwner() {
        return this.diamondeconomy_owner;
    }
    public UUID diamondeconomy_getItemEntity() {
        return this.diamondeconomy_itemEntity;
    }

    @Inject(method = "writeNbt", at = @At("TAIL"))
    private void diamondeconomy_writeNbtMixin(NbtCompound nbt, CallbackInfo ci) {
        if (AutoConfig.getConfigHolder(DEConfig.class).getConfig().chestShops) {
            if (this.diamondeconomy_owner == null) diamondeconomy_owner = "";
            if (this.diamondeconomy_isShop) nbt.putUuid("diamondeconomy_ItemEntity", diamondeconomy_itemEntity);
            nbt.putString("diamondeconomy_ShopOwner", diamondeconomy_owner);
            if (!nbt.contains("diamondeconomy_IsShop")) nbt.putBoolean("diamondeconomy_IsShop", diamondeconomy_isShop);
            if (!nbt.contains("diamondeconomy_IsAdminShop")) nbt.putBoolean("diamondeconomy_IsAdminShop", diamondeconomy_isAdminShop);
        }
    }

    @Inject(method = "readNbt", at = @At("TAIL"))
    private void diamondeconomy_readNbtMixin(NbtCompound nbt, CallbackInfo ci) {
        if (AutoConfig.getConfigHolder(DEConfig.class).getConfig().chestShops) {
            this.diamondeconomy_owner = nbt.getString("diamondeconomy_ShopOwner");
            this.diamondeconomy_isShop = nbt.getBoolean("diamondeconomy_IsShop");
            this.diamondeconomy_isAdminShop = nbt.getBoolean("diamondeconomy_IsAdminShop");
            if (this.diamondeconomy_isShop) this.diamondeconomy_itemEntity = nbt.getUuid("diamondeconomy_ItemEntity");
        }
    }
}