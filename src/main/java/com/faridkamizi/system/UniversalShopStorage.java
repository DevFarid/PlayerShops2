package com.faridkamizi.system;

import com.faridkamizi.util.AsyncParticles;
import com.faridkamizi.util.Hologram;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface UniversalShopStorage {
    final static Map<UUID, ShopObject> shopLocationDirectory = new HashMap<>();

    void add(UUID uuid, ShopObject shopObject);

    public static ShopConfig get(UUID uuid, ShopObject shopObject) {
        if (shopLocationDirectory.containsKey(uuid))
            return null;

        return shopLocationDirectory.get(uuid).getShopConfig();
    }

    public static ShopObject get(UUID uuid) {
        if (shopLocationDirectory.containsKey(uuid))
            return null;

        return shopLocationDirectory.get(uuid);
    }

    /**
     * Deletes a shop, along with its physical properties and session-saved config values.
     * @param uuid
     *              the shop to be removed.
     */
    public static void deleteShop(UUID uuid) {
        if(shopLocationDirectory.containsKey(uuid)) {
            ShopObject shopOfPlayer = shopLocationDirectory.get(uuid);
            ShopConfig shopCfg = shopOfPlayer.getShopConfig();
            List<Location> shopLocation = shopOfPlayer.getShopLocation();

            if(shopLocation.get(4) != null) {
                AsyncParticles.stopTask(shopLocation.get(4));
            }

            shopCfg.asyncInventory(uuid);

            Hologram.deleteHolo(shopLocation.get(2));
            Hologram.deleteHolo(shopLocation.get(3));

            shopLocation.get(0).getBlock().setType(Material.AIR);
            shopLocation.get(1).getBlock().setType(Material.AIR);

            shopCfg.uninitialize();

            shopLocationDirectory.remove(uuid);
        }
    }

    /**
     * Will remove all shops found in {@code PlayerShopStorage} static storage.
     */
    public static void closeAllShops() {
        for(Map.Entry<UUID, ShopObject> playerShopEntry : shopLocationDirectory.entrySet()) {
            deleteShop(playerShopEntry.getKey());
        }
        Hologram.removeAll();
        shopLocationDirectory.clear();
    }
}
