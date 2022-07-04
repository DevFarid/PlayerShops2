package com.faridkamizi.shops.enhanced;

import com.faridkamizi.PlayerShops;
import com.faridkamizi.config.PlayerConfig;
import com.faridkamizi.util.Hologram;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.UUID;

public class ShopConfig implements UniversalShopStorage {

    UUID shopOwner;
    List<Location> shopLocation;
    String name;
    PlayerConfig pConfig;

    public ShopConfig(UUID player, String shopName, List<Location> shopLocation) {
        this.shopOwner = player;
        this.shopLocation = shopLocation;
        this.name = shopName;
        this.pConfig = PlayerConfig.getConfig(this.shopOwner);

        this.initShopConfig();
    }

    public void initShopConfig() {
        checkConfigDefaults(this.shopOwner, pConfig);

        pConfig.set("player.shopOpen", false);
        pConfig.set("player.location", this.shopLocation);
        pConfig.set("player.shopName", this.name);
        pConfig.createSection("player.shopViews");
        pConfig.createSection("player.shopHistory");
        pConfig.set("player.shopViews.1", this.shopOwner.toString());

        pConfig.save();
    }

    private void checkConfigDefaults(UUID uuid, PlayerConfig pConfig) {
        if(!pConfig.contains("player.shopOpen")) {
            pConfig.set("player.shopOpen", false);
        }
        if(!pConfig.contains("player.shopTier")) {
            pConfig.set("player.UUID", uuid.toString());
        }
        if(!pConfig.contains("player.shopTier")) {
            pConfig.set("player.shopTier", 1);
        }
        if(!pConfig.contains("player.contents")) {
            pConfig.createSection("player.contents");
        }
        pConfig.save();
    }

    public void uninitialize() {
        pConfig.set("player.shopOpen", false);
        pConfig.set("player.location", null);
        pConfig.set("player.shopName", null);
        pConfig.set("player.shopHistory", null);
        pConfig.set("player.shopViews", null);
        
        pConfig.save();
        pConfig.discard();
    }

    public PlayerConfig getOwnerConfig() {
        return this.pConfig;
    }

    public void toggleShopStatus() {
        if(pConfig.getBoolean("player.shopOpen")) {
            pConfig.set("player.shopOpen", false);
        } else {
            pConfig.set("player.shopOpen", true);
        }
        updateHolograms();
        pConfig.save();
    }

    public void upgrade() throws InstantiationException, IllegalAccessException {
        int currentLevel = getShopTier();
        pConfig.set("player.shopTier", (currentLevel + 1));
        pConfig.save();

        Player player = Bukkit.getPlayer(shopOwner);
        player.sendMessage(PlayerShops.colorize("&a&l*** SHOP UPGRADE TO LEVEL " + (currentLevel+1) + " COMPLETE ***"));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 2.0F, 1.0F);

        ShopObject shopObject = shopLocationDirectory.get(this.shopOwner);

        List<Location> locationList = shopObject.getShopLocation();
        Location[] arrayLocation = new Location[]{locationList.get(0), locationList.get(1), locationList.get(2), locationList.get(3), locationList.get(4)};


        ShopObject newInstance = new ShopObject(this.shopOwner, this.name, arrayLocation);
        pConfig.save();
        pConfig.discard();

        ShopConfig newConfig = new ShopConfig(this.shopOwner, this.name, locationList);
        shopObject.setShopConfig(newConfig);

        shopLocationDirectory.remove(this.shopOwner);
        shopLocationDirectory.put(this.shopOwner, newInstance);
    }

    public boolean getStatus() {
        return pConfig.getBoolean("player.shopOpen");
    }

    public int getShopTier() {
        return pConfig.getInt("player.shopTier");
    }

    public void updateName(String newName) {
        pConfig.set("player.shopName", newName);
        updateHolograms();
    }

    public int getViews() {
        int views = 0;
        if(pConfig.contains("player.shopViews")) {
            views = pConfig.getConfigurationSection("player.shopViews").getKeys(false).size();
        }
        return views;
    }

    public void updateHolograms() {
        String shopStatus = getStatus() ? "&a" : "&c";
        Hologram.rename((shopStatus + pConfig.get("player.shopName")), shopLocation.get(2));
        Hologram.rename(("&f"+ getViews() + shopStatus + " view(s)"), shopLocation.get(3));
    }

    public void addItem(ItemStack itemStack, int price) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        List<String> itemLore = itemMeta.getLore();

        itemLore.add(PlayerShops.colorize("&aPrice: &f" + price + "g &aeach"));
        itemMeta.setLore(itemLore);

        itemStack.setItemMeta(itemMeta);

        UUID itemID = UUID.randomUUID();
        pConfig.set("player.contents." + itemID, itemStack);
        itemStack.setType(Material.AIR);
    }

    @Override
    public void add(UUID uuid, ShopObject shopObject) {
        if(!shopLocationDirectory.containsKey(uuid)) {
            shopLocationDirectory.put(uuid, shopObject);
        }
    }
}
