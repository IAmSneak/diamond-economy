package com.gmail.sneakdevs.diamondeconomy.mixin;

import com.gmail.sneakdevs.diamondeconomy.DiamondEconomy;
import com.gmail.sneakdevs.diamondeconomy.sql.DatabaseManager;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin_DiamondEconomy {
    @Inject(method = "onPlayerConnect", at = @At("TAIL"))
    private void diamondeconomy_onPlayerConnectMixin(ClientConnection connection, ServerPlayer player, CallbackInfo ci) {
        DatabaseManager dm = DiamondEconomy.getDatabaseManager();
        String uuid = player.getStringUUID();
        String name = player.getName().getString();
        dm.addPlayer(uuid, name);
        dm.setName(uuid, name);
    }
}