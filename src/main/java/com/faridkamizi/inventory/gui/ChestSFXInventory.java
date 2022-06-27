package com.faridkamizi.inventory.gui;

import com.faridkamizi.PlayerShops;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChestSFXInventory implements ShopHolder {

    private UUID owner;
    private Inventory inventory;

    public ChestSFXInventory(UUID shopOwner, String invName, Integer invSize) {
        this.owner = shopOwner;
        this.inventory = Bukkit.createInventory(this, invSize, invName);

        this.setUp();
    }

    private void setUp() {
        HashMap<Integer, ItemStack> particleList = this.getParticleItems();

        for (Map.Entry<Integer, ItemStack> entrySetItem: particleList.entrySet()) {
            this.inventory.setItem(entrySetItem.getKey(), entrySetItem.getValue());
        }
    }


    private HashMap<Integer, ItemStack> getParticleItems() {
        HashMap<Integer, ItemStack> particleItems = new HashMap<>();

        ItemStack redstone = ShopInventory.createGuiItem(Material.REDSTONE, PlayerShops.colorize("&c&lSpiral Redstone"), PlayerShops.colorize("&fClick to decore your shop."));
        particleItems.put(0, redstone);

        return particleItems;
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        e.setCancelled(true);
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }
}
