package com.faridkamizi.shops.enhanced;

import com.faridkamizi.config.PlayerConfig;
import com.faridkamizi.util.Hologram;
import org.bukkit.Location;

import java.util.List;
import java.util.UUID;

public class ShopConfig {

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
}
