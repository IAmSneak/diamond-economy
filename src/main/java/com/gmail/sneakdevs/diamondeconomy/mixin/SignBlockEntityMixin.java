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

@Mixin(SignBlockEntity.class)
public class SignBlockEntityMixin implements SignBlockEntityInterface {
    private String diamondeconomy_owner;
    private boolean diamondeconomy_isShop;
    private boolean diamondeconomy_isAdminShop;

    public void diamondeconomy_setOwner(String newOwner) {this.diamondeconomy_owner = newOwner;}
    public void diamondeconomy_setShop(boolean newShop) {this.diamondeconomy_isShop = newShop;}
    public void diamondeconomy_setAdminShop(boolean newAdminShop) {this.diamondeconomy_isAdminShop = newAdminShop;}
    public boolean diamondeconomy_getAdminShop() {return this.diamondeconomy_isAdminShop;}
    public boolean diamondeconomy_getShop() {return this.diamondeconomy_isShop;}
    public String diamondeconomy_getOwner() {return this.diamondeconomy_owner;}

    @Inject(method = "writeNbt", at = @At("TAIL"))
    private void diamondeconomy_writeNbtMixin(NbtCompound nbt, CallbackInfo ci) {
        if (AutoConfig.getConfigHolder(DEConfig.class).getConfig().chestShops) {
            if (diamondeconomy_owner == null) diamondeconomy_owner = "";
            nbt.putString("diamond_economy_ShopOwner", diamondeconomy_owner);
            if (!nbt.contains("diamond_economy_IsShop")) nbt.putBoolean("diamond_economy_IsShop", diamondeconomy_isShop);
            if (!nbt.contains("diamond_economy_IsAdminShop")) nbt.putBoolean("diamond_economy_IsAdminShop", diamondeconomy_isAdminShop);
        }
    }

    @Inject(method = "readNbt", at = @At("TAIL"))
    private void diamondeconomy_readNbtMixin(NbtCompound nbt, CallbackInfo ci) {
        if (AutoConfig.getConfigHolder(DEConfig.class).getConfig().chestShops) {
            this.diamondeconomy_owner = nbt.getString("diamond_economy_ShopOwner");
            this.diamondeconomy_isShop = nbt.getBoolean("diamond_economy_IsShop");
            this.diamondeconomy_isAdminShop = nbt.getBoolean("diamond_economy_IsAdminShop");
        }
    }
}