package com.gmail.sneakdevs.diamondeconomy.mixin;

import com.gmail.sneakdevs.diamondeconomy.DatabaseManager;
import com.gmail.sneakdevs.diamondeconomy.DiamondEconomy;
import com.gmail.sneakdevs.diamondeconomy.config.DEConfig;
import com.gmail.sneakdevs.diamondeconomy.interfaces.ItemEntityInterface;
import com.gmail.sneakdevs.diamondeconomy.interfaces.LockableContainerBlockEntityInterface;
import com.gmail.sneakdevs.diamondeconomy.interfaces.SignBlockEntityInterface;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.block.AbstractSignBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Objects;

@Mixin(AbstractSignBlock.class)
public abstract class AbstractSignBlockMixin extends BlockWithEntity {

    protected AbstractSignBlockMixin(Settings settings) {
        super(settings);
    }

    //remove shop from chest
    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        System.out.println("onBreak");
        if (!world.isClient() && ((SignBlockEntityInterface) Objects.requireNonNull(world.getBlockEntity(pos))).diamondeconomy_getShop()) {
            System.out.println("in onBreak");
            BlockPos hangingPos = pos.add(state.get(HorizontalFacingBlock.FACING).getOpposite().getOffsetX(), state.get(HorizontalFacingBlock.FACING).getOpposite().getOffsetY(), state.get(HorizontalFacingBlock.FACING).getOpposite().getOffsetZ());
            List<Entity> entities = world.getOtherEntities(player, new Box(hangingPos));
            while(entities.size() > 0) {
                if (entities.get(0).getUuid().equals(((SignBlockEntityInterface) world.getBlockEntity(pos)).diamondeconomy_getItemEntity())) {
                    entities.get(0).kill();
                }
                entities.remove(0);
            }
            if (world.getBlockEntity(hangingPos) instanceof LockableContainerBlockEntity shop) {
                ((LockableContainerBlockEntityInterface) shop).diamondeconomy_setShop(false);
                shop.markDirty();
            }
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

            if (be.toInitialChunkDataNbt().getBoolean("diamondeconomy_IsShop")) {
                //admin shops
                if (item.equals(Items.COMMAND_BLOCK)) {
                    ((SignBlockEntityInterface)be).diamondeconomy_setAdminShop(!((SignBlockEntityInterface)be).diamondeconomy_getAdminShop());
                    be.markDirty();
                    player.sendMessage(new LiteralText((((SignBlockEntityInterface)be).diamondeconomy_getAdminShop()) ? "Created admin shop" : "Removed admin shop"), true);
                    return;
                }
                if (!nbt.getString("diamondeconomy_ShopOwner").equals(player.getUuidAsString()) || ((SignBlockEntityInterface)be).diamondeconomy_getAdminShop()) {
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
                                player.sendMessage(new LiteralText("You dont have enough money"), true);
                                return;
                            }
                            if (dm.getBalanceFromUUID(owner) + money >= Integer.MAX_VALUE && !((SignBlockEntityInterface)be).diamondeconomy_getAdminShop()) {
                                player.sendMessage(new LiteralText("The owner is too rich"), true);
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
                                    player.sendMessage(new LiteralText("The shop is sold out"), true);
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

                            player.sendMessage(new LiteralText("Bought " + quantity + " " + sellItem.getName().getString() + " for " + money + " " + DEConfig.getCurrencyName()), true);
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
                                player.sendMessage(new LiteralText("The owner hasn't got enough money"), true);
                                return;
                            }

                            if (dm.getBalanceFromUUID(player.getUuidAsString()) + money >= Integer.MAX_VALUE) {
                                player.sendMessage(new LiteralText("You are too rich"), true);
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
                                player.sendMessage(new LiteralText("You don't have enough of that item"), true);
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
                                player.sendMessage(new LiteralText("The chest is full"), true);
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

                            player.sendMessage(new LiteralText("Sold " + quantity + " " + buyItem.getName().getString() + " for " + money + " " + DEConfig.getCurrencyName()), true);
                            return;
                        } catch (NumberFormatException ignored) {return;}
                    }
                }
            }

            //create the chest shop
            if (item.equals(DEConfig.getCurrency())) {
                if (nbt.getBoolean("diamondeconomy_IsShop")) {
                    player.sendMessage(new LiteralText("This is already a shop"), true);
                    return;
                }

                BlockPos hangingPos = pos.add(state.get(HorizontalFacingBlock.FACING).getOpposite().getOffsetX(), state.get(HorizontalFacingBlock.FACING).getOpposite().getOffsetY(), state.get(HorizontalFacingBlock.FACING).getOpposite().getOffsetZ());
                if (!(world.getBlockEntity(hangingPos) instanceof LockableContainerBlockEntity shop && world.getBlockEntity(hangingPos) instanceof LootableContainerBlockEntity)) {
                    player.sendMessage(new LiteralText("Sign must be on a valid container"), true);
                    return;
                }

                if (!nbt.getString("diamondeconomy_ShopOwner").equals(player.getUuidAsString()) || !((LockableContainerBlockEntityInterface)shop).diamondeconomy_getOwner().equals(player.getUuidAsString())) {
                    player.sendMessage(new LiteralText("You must have placed down the sign and chest"), true);
                    return;
                }

                if (player.getOffHandStack().getItem().equals(Items.AIR)) {
                    player.sendMessage(new LiteralText("The sell item must be in your offhand"), true);
                    return;
                }

                if (!(DiamondEconomy.signTextToReadable(nbt.getString("Text1")).equals("sell") || DiamondEconomy.signTextToReadable(nbt.getString("Text1")).equals("buy"))) {
                    player.sendMessage(new LiteralText("The first line must be either \"Buy\" or \"Sell\""), true);
                    return;
                }

                if (((LockableContainerBlockEntityInterface)shop).diamondeconomy_getShop()) {
                    player.sendMessage(new LiteralText("That chest already is a shop"), true);
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

                            ItemEntity itemEntity = EntityType.ITEM.create(world);
                            itemEntity.setStack(new ItemStack(player.getOffHandStack().getItem(), Math.min(quantity, player.getOffHandStack().getItem().getMaxCount())));
                            itemEntity.setNeverDespawn();
                            itemEntity.setPickupDelayInfinite();
                            itemEntity.setInvulnerable(true);
                            itemEntity.setNoGravity(true);
                            itemEntity.setPosition(new Vec3d(hangingPos.getX() + 0.5, hangingPos.getY() + 0.95, hangingPos.getZ() + 0.5));
                            ((ItemEntityInterface)itemEntity).diamondeconomy_setShop(true);
                            world.spawnEntity(itemEntity);
                            ((SignBlockEntityInterface) be).diamondeconomy_setItemEntity(itemEntity.getUuid());

                            player.sendMessage(new LiteralText("Created shop with " + quantity + " " + player.getOffHandStack().getItem().getName().getString() + (((nbt.getString("Text1")).contains("sell")) ? " sold for " : " bought for ") + money + " " + DEConfig.getCurrencyName()), true);
                        } else {
                            player.sendMessage(new LiteralText("Positive quantity required"), true);
                        }
                    } else {
                        player.sendMessage(new LiteralText("Negative prices are not allowed"), true);

                    }
                } catch (NumberFormatException ignored) {
                    player.sendMessage(new LiteralText("The second and third lines must be numbers (quantity then money)"), true);
                }
            }
        }
    }
}