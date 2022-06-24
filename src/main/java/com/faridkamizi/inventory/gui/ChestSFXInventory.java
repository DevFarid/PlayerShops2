package com.faridkamizi.inventory.gui;

import com.faridkamizi.PlayerShops;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChestSFXInventory extends AbstractGUI {

    public ChestSFXInventory(UUID shopOwner, String invName, Integer invSize) {
        super(shopOwner, invName, invSize);
        this.initializeItems(shopOwner);
    }

    private void initializeItems(UUID owner) {
        HashMap<Integer, ItemStack> particleList = this.getParticleItems();

        for (Map.Entry<Integer, ItemStack> entrySetItem: particleList.entrySet()) {
            super.setItem(entrySetItem.getKey(), entrySetItem.getValue());
        }
    }


    private HashMap<Integer, ItemStack> getParticleItems() {
        HashMap<Integer, ItemStack> particleItems = new HashMap<>();

        ItemStack redstone = ShopInventory.createGuiItem(Material.REDSTONE, PlayerShops.colorize("&c&lSpiral Redstone"), PlayerShops.colorize("&fClick to decore your shop."));
        particleItems.put(0, redstone);

        return particleItems;
    }
}
