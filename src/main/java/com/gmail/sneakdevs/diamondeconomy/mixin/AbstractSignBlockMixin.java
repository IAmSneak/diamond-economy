package com.gmail.sneakdevs.diamondeconomy.mixin;

import com.gmail.sneakdevs.diamondeconomy.DatabaseManager;
import com.gmail.sneakdevs.diamondeconomy.DiamondEconomy;
import com.gmail.sneakdevs.diamondeconomy.config.DEConfig;
import com.gmail.sneakdevs.diamondeconomy.interfaces.LockableContainerBlockEntityInterface;
import com.gmail.sneakdevs.diamondeconomy.interfaces.SignBlockEntityInterface;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.block.AbstractSignBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(AbstractSignBlock.class)
public abstract class AbstractSignBlockMixin extends BlockWithEntity {
    protected AbstractSignBlockMixin(Settings settings) {
        super(settings);
    }

    //remove shop from chest
    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient() && ((SignBlockEntityInterface) Objects.requireNonNull(world.getBlockEntity(pos))).diamondeconomy_getShop()) {
            BlockPos hangingPos = pos.add(state.get(HorizontalFacingBlock.FACING).getOpposite().getOffsetX(), state.get(HorizontalFacingBlock.FACING).getOpposite().getOffsetY(), state.get(HorizontalFacingBlock.FACING).getOpposite().getOffsetZ());
            if (!(world.getBlockEntity(hangingPos) instanceof LockableContainerBlockEntity shop)) return;
            ((LockableContainerBlockEntityInterface)shop).diamondeconomy_setShop(false);
            shop.markDirty();
        }
    }

    @Inject(method = "onUse", at = @At("HEAD"))
    private void diamondeconomy_onUseMixin(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        if (AutoConfig.getConfigHolder(DEConfig.class).getConfig().chestShops && !world.isClient) {
            ItemStack itemStack = player.getStackInHand(hand);
            Item item = itemStack.getItem();
            BlockEntity be = world.getBlockEntity(pos);
            if (be == null) return;
            NbtCompound nbt = be.toInitialChunkDataNbt();

            if (be.toInitialChunkDataNbt().getBoolean("diamond_economy_IsShop")) {
                //admin shops
                if (item.equals(Items.COMMAND_BLOCK)) {
                    ((SignBlockEntityInterface)be).diamondeconomy_setAdminShop(!((SignBlockEntityInterface)be).diamondeconomy_getAdminShop());
                    be.markDirty();
                    Text text = new LiteralText((((SignBlockEntityInterface)be).diamondeconomy_getAdminShop()) ? "Created admin shop" : "Removed admin shop");
                    player.sendMessage(text, true);
                    return;
                }
                if (!nbt.getString("diamond_economy_ShopOwner").equals(player.getUuidAsString()) || ((SignBlockEntityInterface)be).diamondeconomy_getAdminShop()) {
                    //sell shops
                    if (DiamondEconomy.signTextToReadable(nbt.getString("Text1")).contains("sell")) {
                        try {
                            int quantity = Integer.parseInt(DiamondEconomy.signTextToReadable(nbt.getString("Text2")));
                            int money = Integer.parseInt(DiamondEconomy.signTextToReadable(nbt.getString("Text3")));
                            DatabaseManager dm = new DatabaseManager();
                            BlockPos hangingPos = pos.add(state.get(HorizontalFacingBlock.FACING).getOpposite().getOffsetX(), state.get(HorizontalFacingBlock.FACING).getOpposite().getOffsetY(), state.get(HorizontalFacingBlock.FACING).getOpposite().getOffsetZ());
                            LootableContainerBlockEntity shop = (LootableContainerBlockEntity) world.getBlockEntity(hangingPos);
                            assert shop != null;
                            String owner = ((LockableContainerBlockEntityInterface) shop).diamondeconomy_getOwner();
                            Item sellItem = Registry.ITEM.get(Identifier.tryParse(((LockableContainerBlockEntityInterface) shop).diamondeconomy_getItem()));

                            if (dm.getBalanceFromUUID(player.getUuidAsString()) < money) {
                                Text text = new LiteralText("You dont have enough money");
                                player.sendMessage(text, true);
                                return;
                            }
                            if (dm.getBalanceFromUUID(owner) + money >= Integer.MAX_VALUE && !((SignBlockEntityInterface)be).diamondeconomy_getAdminShop()) {
                                Text text = new LiteralText("The owner is too rich");
                                player.sendMessage(text, true);
                                return;
                            }

                            //check shop has item in proper quantity
                            if (!((SignBlockEntityInterface)be).diamondeconomy_getAdminShop()) {
                                int itemCount = 0;
                                for (int i = 0; i < shop.size(); i++) {
                                    if (shop.getStack(i).getItem().equals(sellItem)) {
                                        itemCount += shop.getStack(i).getCount();
                                    }
                                }
                                if (itemCount < quantity) {
                                    Text text = new LiteralText("The shop is sold out");
                                    player.sendMessage(text, true);
                                    return;
                                }

                                //take items from chest
                                itemCount = quantity;
                                for (int i = 0; i < shop.size(); i++) {
                                    if (shop.getStack(i).getItem().equals(sellItem)) {
                                        itemCount -= shop.getStack(i).getCount();
                                        shop.setStack(i, new ItemStack(Items.AIR));
                                        if (itemCount < 0) {
                                            shop.setStack(i, new ItemStack(sellItem, Math.abs(itemCount)));
                                            break;
                                        }
                                    }
                                }
                            }

                            //give the player the items
                            DiamondEconomy.dropItem(sellItem, quantity, (ServerPlayerEntity) player);

                            //make the transaction
                            if (AutoConfig.getConfigHolder(DEConfig.class).getConfig().transactionHistory) {
                                dm.createTransaction("send", player.getUuidAsString(), owner, money, -1);
                            }
                            dm.setBalance(player.getUuidAsString(), dm.getBalanceFromUUID(player.getUuidAsString()) - money);
                            if (!((SignBlockEntityInterface)be).diamondeconomy_getAdminShop()) {
                                dm.setBalance(owner, dm.getBalanceFromUUID(owner) + money);
                            }

                            Text text = new LiteralText("Bought " + quantity + " " + sellItem.getName().getString() + " for " + money + " " + DEConfig.getCurrencyName());
                            player.sendMessage(text, true);
                            return;
                        } catch (NumberFormatException ignored) {return;}
                    }

                    //buy shops
                    if (DiamondEconomy.signTextToReadable(nbt.getString("Text1")).contains("buy")) {
                        try {
                            int quantity = Integer.parseInt(DiamondEconomy.signTextToReadable(nbt.getString("Text2")));
                            int money = Integer.parseInt(DiamondEconomy.signTextToReadable(nbt.getString("Text3")));
                            DatabaseManager dm = new DatabaseManager();
                            BlockPos hangingPos = pos.add(state.get(HorizontalFacingBlock.FACING).getOpposite().getOffsetX(), state.get(HorizontalFacingBlock.FACING).getOpposite().getOffsetY(), state.get(HorizontalFacingBlock.FACING).getOpposite().getOffsetZ());
                            LootableContainerBlockEntity shop = (LootableContainerBlockEntity) world.getBlockEntity(hangingPos);
                            assert shop != null;
                            String owner = ((LockableContainerBlockEntityInterface)shop).diamondeconomy_getOwner();
                            Item buyItem = Registry.ITEM.get(Identifier.tryParse(((LockableContainerBlockEntityInterface)shop).diamondeconomy_getItem()));

                            if (dm.getBalanceFromUUID(owner) < money && !((SignBlockEntityInterface)be).diamondeconomy_getAdminShop()) {
                                Text text = new LiteralText("The owner hasn't got enough money");
                                player.sendMessage(text, true);
                                return;
                            }

                            if (dm.getBalanceFromUUID(player.getUuidAsString()) + money >= Integer.MAX_VALUE) {
                                Text text = new LiteralText("You are too rich");
                                player.sendMessage(text, true);
                                return;
                            }

                            //check player has item in proper quantity
                            int itemCount = 0;
                            for (int i = 0; i < player.getInventory().size(); i++) {
                                if (player.getInventory().getStack(i).getItem().equals(buyItem)) {
                                    itemCount += player.getInventory().getStack(i).getCount();
                                }
                            }
                            if (itemCount < quantity) {
                                Text text = new LiteralText("You don't have enough of that item");
                                player.sendMessage(text, true);
                                return;
                            }
                            int emptyspaces = 0;
                            for (int i = 0; i < shop.size(); i++) {
                                if (shop.getStack(i).getItem().equals(Items.AIR)) {
                                    emptyspaces += buyItem.getMaxCount();
                                    continue;
                                }
                                if (shop.getStack(i).getItem().equals(buyItem)) {
                                    emptyspaces += buyItem.getMaxCount() - shop.getStack(i).getCount();
                                }
                            }
                            if (emptyspaces < quantity) {
                                Text text = new LiteralText("The chest is full");
                                player.sendMessage(text, true);
                                return;
                            }

                            //take items from player
                            itemCount = quantity;
                            for (int i = 0; i < player.getInventory().size(); i++) {
                                if (player.getInventory().getStack(i).getItem().equals(buyItem)) {
                                    itemCount -= player.getInventory().getStack(i).getCount();
                                    player.getInventory().setStack(i, new ItemStack(Items.AIR));
                                    if (itemCount < 0) {
                                        player.getInventory().setStack(i, new ItemStack(buyItem, Math.abs(itemCount)));
                                        break;
                                    }
                                }
                            }

                            //give the chest the items
                            if (!((SignBlockEntityInterface)be).diamondeconomy_getAdminShop()) {
                                int itemsToAdd = quantity;
                                for (int i = 0; i < shop.size(); i++) {
                                    if (shop.getStack(i).getItem().equals(buyItem)) {
                                        itemsToAdd += shop.getStack(i).getCount();
                                        itemsToAdd -= buyItem.getMaxCount();
                                        shop.setStack(i, new ItemStack(buyItem, buyItem.getMaxCount()));
                                    }
                                    if (shop.getStack(i).getItem().equals(Items.AIR)) {
                                        itemsToAdd -= buyItem.getMaxCount();
                                        shop.setStack(i, new ItemStack(buyItem, buyItem.getMaxCount()));
                                    }
                                    if (itemsToAdd < 0) {
                                        shop.setStack(i, new ItemStack(buyItem, buyItem.getMaxCount() + itemsToAdd));
                                        break;
                                    }
                                }
                            }

                            //make the transaction
                            if (AutoConfig.getConfigHolder(DEConfig.class).getConfig().transactionHistory) {
                                dm.createTransaction("send", owner, player.getUuidAsString(), money, -1);
                            }
                            if (!((SignBlockEntityInterface)be).diamondeconomy_getAdminShop()) {
                                dm.setBalance(owner, dm.getBalanceFromUUID(owner) - money);
                            }
                            dm.setBalance(player.getUuidAsString(), dm.getBalanceFromUUID(player.getUuidAsString()) + money);

                            Text text = new LiteralText("Sold " + quantity + " " + buyItem.getName().getString() + " for " + money + " " + DEConfig.getCurrencyName());
                            player.sendMessage(text, true);
                            return;
                        } catch (NumberFormatException ignored) {return;}
                    }
                }
            }

            //create the chest shop
            if (item.equals(DEConfig.getCurrency())) {
                if (nbt.getBoolean("diamond_economy_IsShop")) {
                    Text text = new LiteralText("This is already a shop");
                    player.sendMessage(text, true);
                    return;
                }

                BlockPos hangingPos = pos.add(state.get(HorizontalFacingBlock.FACING).getOpposite().getOffsetX(), state.get(HorizontalFacingBlock.FACING).getOpposite().getOffsetY(), state.get(HorizontalFacingBlock.FACING).getOpposite().getOffsetZ());
                if (!(world.getBlockEntity(hangingPos) instanceof LockableContainerBlockEntity shop)) {
                    Text text = new LiteralText("Sign must be on a container");
                    player.sendMessage(text, true);
                    return;
                }

                if (!nbt.getString("diamond_economy_ShopOwner").equals(player.getUuidAsString()) || !((LockableContainerBlockEntityInterface)shop).diamondeconomy_getOwner().equals(player.getUuidAsString())) {
                    Text text = new LiteralText("You must have placed down the sign and chest");
                    player.sendMessage(text, true);
                    return;
                }

                if (player.getOffHandStack().getItem().equals(Items.AIR)) {
                    Text text = new LiteralText("The sell item must be in your offhand");
                    player.sendMessage(text, true);
                    return;
                }

                if (!(DiamondEconomy.signTextToReadable(nbt.getString("Text1")).equals("sell") || DiamondEconomy.signTextToReadable(nbt.getString("Text1")).equals("buy"))) {
                    Text text = new LiteralText("The first line must be either \"Buy\" or \"Sell\"");
                    player.sendMessage(text, true);
                    return;
                }

                if (((LockableContainerBlockEntityInterface)shop).diamondeconomy_getShop()) {
                    Text text = new LiteralText("That chest already is a shop");
                    player.sendMessage(text, true);
                    return;
                }

                try {
                    int quantity = Integer.parseInt(DiamondEconomy.signTextToReadable(nbt.getString("Text2")));
                    int money = Integer.parseInt(DiamondEconomy.signTextToReadable(nbt.getString("Text3")));
                    if (quantity >= 1) {
                        if (money >= 0) {
                            ((SignBlockEntityInterface) be).diamondeconomy_setShop(true);
                            ((LockableContainerBlockEntityInterface) shop).diamondeconomy_setShop(true);
                            ((LockableContainerBlockEntityInterface) shop).diamondeconomy_setItem(Registry.ITEM.getId(player.getOffHandStack().getItem()).toString());
                            be.markDirty();
                            shop.markDirty();
                            Text text = new LiteralText("Created shop with " + quantity + " " + player.getOffHandStack().getItem().getName().getString() + (((nbt.getString("Text1")).contains("sell")) ? " sold for " : " bought for ") + money + " " + DEConfig.getCurrencyName());
                            player.sendMessage(text, true);
                        } else {
                            Text text = new LiteralText("Positive quantity required");
                            player.sendMessage(text, true);
                        }
                    } else {
                        Text text = new LiteralText("Negative prices are not allowed");
                        player.sendMessage(text, true);

                    }
                } catch (NumberFormatException ignored) {
                    Text text = new LiteralText("The second and third lines must be numbers (quantity then money)");
                    player.sendMessage(text, true);
                }
            }
        }
    }
}