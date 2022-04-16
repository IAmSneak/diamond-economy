package com.gmail.sneakdevs.diamondeconomy.mixin;

import com.gmail.sneakdevs.diamondeconomy.config.DEConfig;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {
    @Shadow
    protected ServerWorld world;

    @Final
    @Shadow
    protected ServerPlayerEntity player;

    @Inject(method = "tryBreakBlock", at = @At("HEAD"), cancellable = true)
    private void diamondeconomy_tryBreakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> info) {
        if (AutoConfig.getConfigHolder(DEConfig.class).getConfig().chestShops) {
            if (!world.getBlockState(pos).hasBlockEntity()) return;
            if (!NbtHelper.fromBlockPos(pos).getBoolean("diamond_economy_IsShop")) return;
            if (NbtHelper.fromBlockPos(pos).getString("diamond_economy_ShopOwner").equals(player.getUuidAsString())) return;
            info.setReturnValue(false);
        }
    }
}