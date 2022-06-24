package com.faridkamizi.inventory.gui;

import com.faridkamizi.config.PlayerConfig;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Set;
import java.util.UUID;

public class ShopHistoryInventory extends AbstractGUI {

    public ShopHistoryInventory(UUID shopOwner, String invName, Integer invSize) {
        super(shopOwner, invName, invSize);
        this.initializeItems(shopOwner);
    }

    private void initializeItems(UUID owner) {
        PlayerConfig pConfig = PlayerConfig.getConfig(owner);

        if(pConfig.contains("player.shopHistory")) {
            ConfigurationSection cfg = pConfig.getConfigurationSection("player.shopHistory");
            Set<String> keys = cfg.getKeys(false);
            if(keys.size() > 0) {
                for (String key : keys) {
                    ItemStack itemStack = pConfig.getItemStack("player.shopHistory." + key);
                    super.setItem(itemStack);
                }
            }
        }
    }

    public void openInventory(final Player player) {
        super.open(player);
    }
}
