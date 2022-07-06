package com.faridkamizi.system;

import com.faridkamizi.util.AsyncParticles;
import com.faridkamizi.util.Hologram;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface UniversalShopStorage {
    final static Map<UUID, ShopObject> shopLocationDirectory = new HashMap<>();

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
     * Add this shop to the shop directory.
     * @param uuid
     *              the player who owns {@code this} ShopObject.
     * @param shopObject
     *                  The shop object that was just created, a.k.a {@code this}.
     */
    public static void add(UUID uuid, ShopObject shopObject) {
        if(!shopLocationDirectory.containsKey(uuid)) {
            shopLocationDirectory.put(uuid, shopObject);
        }
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

            shopLocation.get(0).getBlock().setType(Material.AIR);
            shopLocation.get(1).getBlock().setType(Material.AIR);

            if(shopLocation.get(4) != null) {
                AsyncParticles.stopTask(shopLocation.get(4));
            }

            shopCfg.asyncInventory(uuid);

            Hologram.deleteHolo(shopLocation.get(2));
            Hologram.deleteHolo(shopLocation.get(3));


            shopCfg.uninitialize();

            shopLocationDirectory.remove(uuid);
        }
    }

    /**
     * Will remove all shops found in {@code PlayerShopStorage} static storage.
     */
    public static void closeAllShops() {
        for(Player player : Bukkit.getOnlinePlayers()) {
            if(shopLocationDirectory.get(player.getUniqueId()) != null) {
                deleteShop(player.getUniqueId());
            }
            Hologram.removeAll();
        }
        Hologram.removeAll();
        shopLocationDirectory.clear();
    }
}
