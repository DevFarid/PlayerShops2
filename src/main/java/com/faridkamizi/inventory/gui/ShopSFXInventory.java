package com.faridkamizi.inventory.gui;

import com.faridkamizi.PlayerShops;
import com.faridkamizi.inventory.holders.ShopSFXHolder;
import com.faridkamizi.shops.enhanced.ShopObject;
import com.faridkamizi.util.AsyncParticles;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShopSFXInventory implements ShopSFXHolder {

    private UUID owner;
    private Inventory inventory;

    public ShopSFXInventory(UUID shopOwner, String invName, Integer invSize) {
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

        ItemStack redstone = ShopInventory.createGuiItem(Material.BLAZE_POWDER, PlayerShops.colorize("&6&lSpiral Flame"), PlayerShops.colorize("&fClick to decore your shop."));
        particleItems.put(0, redstone);

        return particleItems;
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        e.setCancelled(true);
        if(e.getRawSlot() == 0) {
            AsyncParticles.spawnParticle(ShopObject.shopLocationDirectory.get(owner).getShopLocation().get(4));
        }
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }
}
