package com.gmail.sneakdevs.diamondeconomy.mixin;

import com.gmail.sneakdevs.diamondeconomy.config.DEConfig;
import com.gmail.sneakdevs.diamondeconomy.interfaces.ItemEntityInterface;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemEntity.class)
public class ItemEntityMixin implements ItemEntityInterface {
    private boolean diamondeconomy_isShop;
    private boolean canTick = true;

    public void diamondeconomy_setShop(boolean newVal) {
        this.diamondeconomy_isShop = newVal;
    }


    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void diamondeconomy_tickMixin(CallbackInfo ci) {
        if (AutoConfig.getConfigHolder(DEConfig.class).getConfig().chestShops && diamondeconomy_isShop) {
            if (((ItemEntity)(Object)this).world.getBlockEntity(((ItemEntity)(Object)this).getBlockPos().down()) instanceof LockableContainerBlockEntity ||
                    ((ItemEntity)(Object)this).world.getBlockEntity(((ItemEntity)(Object)this).getBlockPos().down(2)) instanceof LockableContainerBlockEntity) {
                ((ItemEntity)(Object) this).setVelocity(0, 0, 0);
                if (!canTick) {
                    ci.cancel();
                }
                canTick = false;
            } else {
                ((ItemEntity)(Object)this).kill();
            }
        }
    }

    @Inject(method = "canMerge()Z", at = @At("HEAD"), cancellable = true)
    private void diamondeconomy_canMergeMixin(CallbackInfoReturnable<Boolean> cir) {
        if (AutoConfig.getConfigHolder(DEConfig.class).getConfig().chestShops && diamondeconomy_isShop) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void diamondeconomy_writeCustomDataToNbtMixin(NbtCompound nbt, CallbackInfo ci) {
        if (AutoConfig.getConfigHolder(DEConfig.class).getConfig().chestShops) {
            nbt.putBoolean("diamondeconomy_IsShop", this.diamondeconomy_isShop);
        }
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void diamondeconomy_readCustomDataFromNbtMixin(NbtCompound nbt, CallbackInfo ci) {
        if (AutoConfig.getConfigHolder(DEConfig.class).getConfig().chestShops) {
            this.diamondeconomy_isShop = nbt.getBoolean("diamondeconomy_IsShop");
        }
    }
}