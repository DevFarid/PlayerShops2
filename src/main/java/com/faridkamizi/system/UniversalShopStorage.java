package com.faridkamizi.system;

import com.faridkamizi.PlayerShops;
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

    public static void create(UUID uuid, Location copyClickedBlock, String name) {
        Location shopLocation = copyClickedBlock.clone().add(0,1,0);
        Location shopLocation2 = shopLocation.clone().add(1, 0, 0);

        Location hologramTitle = shopLocation.clone();
        hologramTitle.add(1, -0.9, 0.5);

        Location hologramView = shopLocation.clone();
        hologramView.add(1, -1.2, 0.5);

        Location particleLoc = shopLocation.clone().add(1, 1, 0.5);

        Location[] locs = {shopLocation, shopLocation2, hologramTitle, hologramView, particleLoc};

        if(!shopLocationDirectory.containsKey(uuid)) {
            ShopObject shopObject = new ShopObject(uuid, name, locs);
            shopLocationDirectory.put(uuid, shopObject);
        } else {
            Player player = Bukkit.getPlayer(uuid);
            player.sendMessage(PlayerShops.colorize("&eYou already have a shop on &lUS-1&e!"));
        }
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
    public static void deleteShop(UUID uuid, boolean remove) {
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

            if(remove) {
                shopLocationDirectory.remove(uuid);
            }
        }
    }

    /**
     * Will remove all shops found in {@code PlayerShopStorage} static storage.
     */
    public static void closeAllShops() {
        for(Map.Entry<UUID, ShopObject> entry : shopLocationDirectory.entrySet()) {
            deleteShop(entry.getKey(), false);
        }
        Hologram.removeAll();
        shopLocationDirectory.clear();
    }
}
