package com.faridkamizi.system;

import com.faridkamizi.PlayerShops;
import com.faridkamizi.config.PlayerConfig;
import com.faridkamizi.util.Hologram;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ShopConfig implements UniversalShopStorage {

    UUID shopOwner;
    List<Location> shopLocation;
    String name;

    public ShopConfig(UUID player, String shopName, List<Location> shopLocation) {
        this.shopOwner = player;
        this.shopLocation = shopLocation;
        this.name = shopName;

        this.initShopConfig();
    }

    public void initShopConfig() {
        PlayerConfig pConfig = PlayerConfig.getConfig(this.shopOwner);
        checkConfigDefaults(this.shopOwner, pConfig);

        pConfig.set("player.shopOpen", false);
        pConfig.set("player.location", this.shopLocation);
        pConfig.set("player.shopName", this.name);
        pConfig.createSection("player.shopViews");
        pConfig.createSection("player.shopHistory");
        pConfig.set("player.shopViews.1", this.shopOwner.toString());

        pConfig.save();
        pConfig.discard();
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
        PlayerConfig pConfig = PlayerConfig.getConfig(this.shopOwner);
        pConfig.set("player.shopOpen", false);
        pConfig.set("player.location", null);
        pConfig.set("player.shopName", null);
        pConfig.set("player.shopHistory", null);
        pConfig.set("player.shopViews", null);
        
        pConfig.save();
        pConfig.discard();
    }

    public PlayerConfig getOwnerConfig() {
        PlayerConfig pConfig = PlayerConfig.getConfig(this.shopOwner);
        return pConfig;
    }

    public void toggleShopStatus() {
        PlayerConfig pConfig = PlayerConfig.getConfig(this.shopOwner);
        if(pConfig.getBoolean("player.shopOpen")) {
            pConfig.set("player.shopOpen", false);
        } else {
            pConfig.set("player.shopOpen", true);
        }
        updateHolograms();
        pConfig.save();
        pConfig.discard();
    }

    public void upgrade() throws InstantiationException, IllegalAccessException {
        PlayerConfig pConfig = PlayerConfig.getConfig(this.shopOwner);
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
        PlayerConfig pConfig = PlayerConfig.getConfig(this.shopOwner);
        boolean isOpen = pConfig.getBoolean("player.shopOpen");
        return isOpen;
    }

    public int getShopTier() {
        PlayerConfig pConfig = PlayerConfig.getConfig(this.shopOwner);
        int shopTier = pConfig.getInt("player.shopTier");
        return shopTier;
    }

    public void updateName(String newName) {
        PlayerConfig pConfig = PlayerConfig.getConfig(this.shopOwner);
        pConfig.set("player.shopName", newName);
        pConfig.save();
        pConfig.discard();
        updateHolograms();
    }

    public int getViews() {
        PlayerConfig pConfig = PlayerConfig.getConfig(this.shopOwner);
        int views = 0;
        if(pConfig.contains("player.shopViews")) {
            views = pConfig.getConfigurationSection("player.shopViews").getKeys(false).size();
        }
        pConfig.discard();
        return views;
    }

    public void updateHolograms() {
        PlayerConfig pConfig = PlayerConfig.getConfig(this.shopOwner);
        String shopStatus = getStatus() ? "&a" : "&c";
        Hologram.rename((shopStatus + pConfig.get("player.shopName")), shopLocation.get(2));
        Hologram.rename(("&f"+ getViews() + shopStatus + " view(s)"), shopLocation.get(3));
        pConfig.discard();
    }

    public void addItem(ItemStack itemStack, int price) {
        PlayerConfig pConfig = PlayerConfig.getConfig(this.shopOwner);
        ItemMeta itemMeta = itemStack.getItemMeta();
        List<String> itemLore;

        if(itemMeta.getLore() != null) {
            itemLore = itemMeta.getLore();
        } else {
            itemLore = new ArrayList<>();
        }

        itemLore.add(PlayerShops.colorize("&aPrice: &f" + price + "g &aeach"));
        itemMeta.setLore(itemLore);

        itemStack.setItemMeta(itemMeta);

        UUID itemID = UUID.randomUUID();

        pConfig.set("player.contents." + itemID, itemStack);

        pConfig.save();
        pConfig.discard();

        Player player = Bukkit.getPlayer(this.shopOwner);
        player.sendMessage(PlayerShops.colorize("&aPrice set. Right-Click item to edit."));
        player.playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT, 2.0F, 1.0F);
    }

    @Override
    public void add(UUID uuid, ShopObject shopObject) {
        if(!shopLocationDirectory.containsKey(uuid)) {
            shopLocationDirectory.put(uuid, shopObject);
        }
    }
}
