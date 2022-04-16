package com.gmail.sneakdevs.diamondeconomy.mixin;

import com.gmail.sneakdevs.diamondeconomy.config.DEConfig;
import com.gmail.sneakdevs.diamondeconomy.interfaces.LockableContainerBlockEntityInterface;
import com.gmail.sneakdevs.diamondeconomy.interfaces.SignBlockEntityInterface;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
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
            BlockEntity be = world.getBlockEntity(pos);
            if (!(be instanceof LockableContainerBlockEntity || be instanceof SignBlockEntity)) return;
            if (be instanceof LockableContainerBlockEntity) {
                if (!((LockableContainerBlockEntityInterface)be).diamondeconomy_getShop()) return;
                if (((LockableContainerBlockEntityInterface)be).diamondeconomy_getOwner().equals(player.getUuidAsString())) return;
            } else {
                if (!((SignBlockEntityInterface)be).diamondeconomy_getShop()) return;
                if (((SignBlockEntityInterface)be).diamondeconomy_getOwner().equals(player.getUuidAsString())) return;
            }
            player.sendMessage(new LiteralText("Cannot break another player's shop"), true);
            info.setReturnValue(false);
        }
    }
}