package com.gmail.sneakdevs.diamondeconomy.mixin;

import com.gmail.sneakdevs.diamondeconomy.DiamondUtils;
import com.gmail.sneakdevs.diamondeconomy.sql.DatabaseManager;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public class PlayerListMixin_DiamondEconomy {
    @Inject(method = "placeNewPlayer", at = @At("TAIL"))
    private void diamondeconomy_placeNewPlayerMixin(Connection connection, ServerPlayer serverPlayer, CommonListenerCookie commonListenerCookie, CallbackInfo ci) {
        DatabaseManager dm = DiamondUtils.getDatabaseManager();
        String uuid = serverPlayer.getStringUUID();
        String name = serverPlayer.getName().getString();
        dm.addPlayer(uuid, name);
        dm.setName(uuid, name);
    }
}