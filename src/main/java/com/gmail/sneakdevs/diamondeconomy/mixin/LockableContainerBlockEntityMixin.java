package com.gmail.sneakdevs.diamondeconomy.mixin;

import com.gmail.sneakdevs.diamondeconomy.config.DEConfig;
import com.gmail.sneakdevs.diamondeconomy.interfaces.LockableContainerBlockEntityInterface;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.text.LiteralText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LockableContainerBlockEntity.class)
public class LockableContainerBlockEntityMixin implements LockableContainerBlockEntityInterface {
    private String diamondeconomy_owner;
    private String diamondeconomy_item;
    private String diamondeconomy_nbt;
    private boolean diamondeconomy_isShop;

    public void diamondeconomy_setOwner(String newOwner) {
        this.diamondeconomy_owner = newOwner;
    }
    public void diamondeconomy_setItem(String newItem) {
        this.diamondeconomy_item = newItem;
    }
    public void diamondeconomy_setNbt(String newNbt) {
        this.diamondeconomy_nbt = newNbt;
    }
    public void diamondeconomy_setShop(boolean newShop) {
        this.diamondeconomy_isShop = newShop;
    }

    public String diamondeconomy_getOwner() {
        return this.diamondeconomy_owner;
    }
    public String diamondeconomy_getItem() {
        return this.diamondeconomy_item;
    }
    public String diamondeconomy_getNbt() {
        return this.diamondeconomy_nbt;
    }
    public boolean diamondeconomy_getShop() {
        return this.diamondeconomy_isShop;
    }

    @Inject(method = "writeNbt", at = @At("TAIL"))
    private void diamondeconomy_writeNbtMixin(NbtCompound nbt, CallbackInfo ci) {
        if (AutoConfig.getConfigHolder(DEConfig.class).getConfig().chestShops) {
            if (diamondeconomy_owner == null) diamondeconomy_owner = "";
            if (diamondeconomy_item == null) diamondeconomy_item = "";
            if (diamondeconomy_nbt == null) diamondeconomy_nbt = "";
            if (!nbt.contains("diamondeconomy_ShopOwner")) nbt.putString("diamondeconomy_ShopOwner", diamondeconomy_owner);
            if (!nbt.contains("diamondeconomy_ShopItem")) nbt.putString("diamondeconomy_ShopItem", diamondeconomy_item);
            if (!nbt.contains("diamondeconomy_NBT")) nbt.putString("diamondeconomy_NBT", diamondeconomy_nbt);
            if (!nbt.contains("diamondeconomy_IsShop")) nbt.putBoolean("diamondeconomy_IsShop", diamondeconomy_isShop);
        }
    }

    @Inject(method = "readNbt", at = @At("TAIL"))
    private void diamondeconomy_readNbtMixin(NbtCompound nbt, CallbackInfo ci) {
        if (AutoConfig.getConfigHolder(DEConfig.class).getConfig().chestShops) {
            this.diamondeconomy_owner = nbt.getString("diamondeconomy_ShopOwner");
            this.diamondeconomy_item = nbt.getString("diamondeconomy_ShopItem");
            this.diamondeconomy_nbt = nbt.getString("diamondeconomy_NBT");
            this.diamondeconomy_isShop = nbt.getBoolean("diamondeconomy_IsShop");
        }
    }

    @Inject(method = "checkUnlocked(Lnet/minecraft/entity/player/PlayerEntity;)Z", at = @At("RETURN"), cancellable = true)
    private void diamondeconomy_checkUnlockedMixin(PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        if (AutoConfig.getConfigHolder(DEConfig.class).getConfig().chestShops) {
            if (!cir.getReturnValue()) return;
            if (player.isCreative()) return;
            if (diamondeconomy_isShop) {
                if (diamondeconomy_owner.equals(player.getUuidAsString())) {
                    cir.setReturnValue(true);
                    return;
                }
                System.out.println(diamondeconomy_owner + " : " + player.getUuidAsString() + " : " + diamondeconomy_owner.equals(player.getUuidAsString()));
                player.sendMessage(new LiteralText("Cannot open another player's shop"), true);
                cir.setReturnValue(false);
            }
        }
    }
}