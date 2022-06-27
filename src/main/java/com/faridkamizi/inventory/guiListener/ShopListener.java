package com.faridkamizi.inventory.guiListener;

import com.faridkamizi.PlayerShops;
import com.faridkamizi.inventory.gui.ShopInventory;
import com.faridkamizi.inventory.holders.ShopHistoryHolder;
import com.faridkamizi.inventory.holders.ShopInventoryHolder;
import com.faridkamizi.inventory.holders.ShopSFXHolder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ShopListener implements Listener {

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent e) {
        if(e.getInventory().getHolder() instanceof ShopInventoryHolder) {
            e.setCancelled(true);
        } else if(e.getInventory().getHolder() instanceof ShopHistoryHolder) {
            e.setCancelled(true);
        } else if(e.getInventory().getHolder() instanceof ShopSFXHolder) {
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
        if(e.getInventory().getHolder() instanceof ShopInventoryHolder) {
            ShopInventory gui = (ShopInventory) e.getInventory().getHolder();
            gui.onClick(e);
        } else if(e.getInventory().getHolder() instanceof ShopHistoryHolder) {
            ShopHistoryHolder gui = (ShopHistoryHolder) e.getInventory().getHolder();
            gui.onClick(e);
        } else if(e.getInventory().getHolder() instanceof ShopSFXHolder) {
            ShopSFXHolder gui = (ShopSFXHolder) e.getInventory().getHolder();
            gui.onClick(e);
        }
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
