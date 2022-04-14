package com.gmail.sneakdevs.diamondeconomy.mixin;

import com.gmail.sneakdevs.diamondeconomy.config.DEConfig;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LockableContainerBlockEntity.class)
public class LockableContainerBlockEntityMixin {
    @Inject(method = "writeNbt", at = @At("TAIL"))
    private void onPlayerConnectMixin(NbtCompound nbt, CallbackInfo ci) {
        if (AutoConfig.getConfigHolder(DEConfig.class).getConfig().chestShops) {
            //todo: add player who placed the block as the owner
            //nbt.putUuid("ShopOwner", );
            nbt.putBoolean("IsShop", false);
        }
    }
}