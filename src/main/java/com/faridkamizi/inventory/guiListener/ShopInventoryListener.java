package com.faridkamizi.inventory.guiListener;

import com.faridkamizi.PlayerShops;
import com.faridkamizi.currency.Currency;
import com.faridkamizi.events.PreInputProcess;
import com.faridkamizi.inventory.gui.AbstractGUI;
import com.faridkamizi.inventory.gui.ChestSFXInventory;
import com.faridkamizi.inventory.gui.ShopHistoryInventory;
import com.faridkamizi.inventory.gui.ShopInventory;
import com.faridkamizi.shops.ShopObject;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.faridkamizi.inventory.gui.ShopInventory.*;

public class ShopInventoryListener implements Listener {

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent e) {
        UUID inventoryID = AbstractGUI.getOpenInventories().get(e.getWhoClicked().getUniqueId());
        if(inventoryID != null) {
            e.setCancelled(true);
        }
    }

    /**
     * Inventory management for when a viewer does a certain action in a {@code ShopInventory} inventory.
     * @param e
     *          the event when a player interacts with a {@code ShopInventory} inventory.
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        UUID inventoryID = AbstractGUI.getOpenInventories().get(player.getUniqueId());

//        Bukkit.broadcastMessage("--------------------------------------------------------"
//            + "\nOwner UUID: " + inventoryID
//            + "\nClickType: " + e.getClick()
//            + "\nClickedInv.getType(): " + e.getClickedInventory().getType()
//            + "\nTopInventory: " + e.getView().getTopInventory()
//            + "\nBottomInventory: " + e.getView().getBottomInventory()
//            + "\nCurrentItem: " + e.getCurrentItem()
//            + "\nCursor: " + e.getCursor()
//            + "\n--------------------------------------------------------");

        if(inventoryID != null) {
            boolean isOwner = player.getUniqueId().equals(inventoryID);

            if(e.getClick() != ClickType.LEFT) {
                e.setCancelled(true);
            }
            else {
                if(e.getClickedInventory() != null) {
                    if((e.getClickedInventory().getType() == InventoryType.CHEST) && e.getCurrentItem() == null && e.getCursor() != null) {
                        if (isOwner) {
                            if(!ShopObject.shopOpen(inventoryID)) {
                                player.sendMessage(PlayerShops.colorize("&aEnter the &lGEM&a value of [&l" + e.getCursor().getAmount() + "x&a] of this item."));
                                PreInputProcess.requestPlayer(player, e);
                                e.getWhoClicked().setItemOnCursor(null);
                                player.closeInventory();
                            } else {
                                e.setCancelled(true);
                                player.sendMessage(PlayerShops.colorize("&cYou must close your shop to add an item."));
                            }
                        } else {
                            e.setCancelled(true);
                        }
                    }
                    else if(((e.getClickedInventory().getType() == InventoryType.CHEST)) && e.getCurrentItem() != null && e.getCursor().getType().isAir()) {
                        e.setCancelled(true);
                        // Shop History Function.
                        if (e.getRawSlot() == e.getClickedInventory().getSize() - 9) {
                            ShopHistoryInventory shopHistoryInventory = new ShopHistoryInventory(inventoryID, PlayerShops.colorize("&8" + player.getName() + "'s Shop History"), 9);
                            player.closeInventory();
                            shopHistoryInventory.openInventory(player);
                        }
                        // ChestSFX menu.
                        else if (e.getRawSlot() == e.getClickedInventory().getSize() - 8) {
                            if (isOwner) {
                                ChestSFXInventory sfxInventory = new ChestSFXInventory(inventoryID, PlayerShops.colorize("&8Shop Effect Selector"), 9);
                                player.closeInventory();
                                sfxInventory.open(player);
                            }
                        }
                        // Rename shop function.
                        else if (e.getRawSlot() == e.getClickedInventory().getSize() - 7) {
                            if (isOwner) {
                                player.sendMessage(PlayerShops.colorize("&ePlease enter a &lSHOP NAME&r&e. [max 16 characters]"));
                                player.closeInventory();
                                PreInputProcess.requestPlayer(player, e);
                            }
                        }
                        // Delete shop function.
                        else if (e.getRawSlot() == e.getClickedInventory().getSize() - 2) {
                            if (isOwner) {
                                ShopInventory.closeInventory(inventoryID, false);
                                deleteShop(inventoryID, player.getUniqueId());
                                player.closeInventory();
                                player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 2.0F, 1.0F);
                            }
                        }
                        // Open/Close function.
                        else if (e.getRawSlot() == e.getClickedInventory().getSize() - 1) {
                            if (isOwner) {
                                switchShopStatus(inventoryID, player.getUniqueId());
                                ItemStack openStatus = createGuiItem(Material.LIME_DYE, "&cClick to &lCLOSE &cShop", PlayerShops.colorize("&fClick to &cclose&f shop."));
                                ItemStack closedStatus = createGuiItem(Material.GRAY_DYE, "&aClick to &lOPEN &aShop", PlayerShops.colorize("&fClick to &2open&f shop."));
                                if (ShopObject.shopOpen(inventoryID)) {
                                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 2.0F, 1.0F);
                                    e.getClickedInventory().setItem(e.getRawSlot(), openStatus);
                                } else {
                                    player.playSound(player.getLocation(), Sound.ENTITY_CHICKEN_EGG, 2.0F, 1.0F);
                                    e.getClickedInventory().setItem(e.getRawSlot(), closedStatus);
                                }

                            }
                        }
                        // Otherwise, the owner is trying to add an item.
                        else if (e.getRawSlot() < e.getClickedInventory().getSize() - 9) {
                            if (isOwner) {
                                if (!ShopObject.shopOpen(inventoryID)) {
                                    process(inventoryID, player.getUniqueId(), e.getClickedInventory(), e.getCurrentItem(), e.getRawSlot());
                                } else {
                                    e.getWhoClicked().closeInventory();
                                    player.sendMessage(PlayerShops.colorize("&cYou must close your shop to remove an item first."));
                                }
                            } else {
                                int itemPrice = itemPrice(e.getCurrentItem());
                                int playerMoney = Currency.getTotalMoney(player);

                                if (playerMoney >= itemPrice) {
                                    Currency.removeMoney(player, itemPrice);
                                    ItemStack forSave = e.getCurrentItem();
                                    process(inventoryID, player.getUniqueId(), e.getClickedInventory(), e.getCurrentItem(), e.getRawSlot());
                                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 2.0F, 1.0F);
                                    player.sendMessage(PlayerShops.colorize("&aYou bough an item!"));

                                    OfflinePlayer owner = Bukkit.getOfflinePlayer(inventoryID);
                                    if(owner.isOnline()) {
                                        owner.getPlayer().sendMessage(PlayerShops.colorize("&a"+e.getWhoClicked().getName()+" bought " + forSave.getType().name()));
                                    }
                                } else {
                                    player.sendMessage(PlayerShops.colorize("&cYou do not have enough gems."));
                                    player.sendMessage(PlayerShops.colorize("&c&lCOST: &c" + itemPrice + "&lG"));
                                }
                            }
                        }
                    }
                    else if(((e.getClickedInventory().getType() == InventoryType.CHEST)) && e.getCurrentItem() != null && !(e.getCursor().getType().isAir())) {
                        e.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e){
        Player player = (Player) e.getPlayer();
        UUID playerUUID = player.getUniqueId();

        AbstractGUI.getOpenInventories().remove(playerUUID);

        Bukkit.broadcastMessage("AbstractGUI: " + AbstractGUI.getOpenInventories().toString());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e){
        Player player = e.getPlayer();
        UUID playerUUID = player.getUniqueId();

        AbstractGUI.getOpenInventories().remove(playerUUID);
    }


    public static ItemStack getBook() {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        ItemMeta bookMeta = book.getItemMeta();

        bookMeta.setDisplayName(PlayerShops.colorize("&aPlayer Journal"));

        List<String> lore = new ArrayList<>();
        lore.add(PlayerShops.colorize("&7By The DungeonRealms Team"));
        lore.add(PlayerShops.colorize("&fLeft-Click: &7Invite to party."));
        lore.add(PlayerShops.colorize("&fSneak-Right-Click: &7Setup shop."));

        bookMeta.setLore(lore);
        book.setItemMeta(bookMeta);

        return book;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if(!e.getPlayer().getInventory().contains(getBook())) {
            e.getPlayer().getInventory().addItem(getBook());
        }
    }

}
