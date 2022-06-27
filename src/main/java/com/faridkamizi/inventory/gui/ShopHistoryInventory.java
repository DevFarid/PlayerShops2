package com.faridkamizi.inventory.gui;

import com.faridkamizi.config.PlayerConfig;
import com.faridkamizi.inventory.holders.ShopHistoryHolder;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Set;
import java.util.UUID;

public class ShopHistoryInventory implements ShopHistoryHolder {

    private UUID owner;
    private Inventory inventory;

    public ShopHistoryInventory(UUID shopOwner, String invName, int invSize) {
        this.owner = shopOwner;
        this.inventory = Bukkit.createInventory(this, invSize, invName);
        this.setUp();
    }

    private void setUp() {
        PlayerConfig pConfig = PlayerConfig.getConfig(this.owner);

        if(pConfig.contains("player.shopHistory")) {
            ConfigurationSection cfg = pConfig.getConfigurationSection("player.shopHistory");
            Set<String> keys = cfg.getKeys(false);
            if(keys.size() > 0) {
                int slot = 0;
                for (String key : keys) {
                    ItemStack itemStack = pConfig.getItemStack("player.shopHistory." + key);
                    this.inventory.setItem(slot, itemStack);
                    slot++;
                }
            }
        }
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
