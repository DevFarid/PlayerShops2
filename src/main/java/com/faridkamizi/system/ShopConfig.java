package com.faridkamizi.system;

import com.faridkamizi.PlayerShops;
import com.faridkamizi.config.PlayerConfig;
import com.faridkamizi.inventory.gui.ShopInventory;
import com.faridkamizi.util.Hologram;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

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
        asyncInventory(shopOwner);
        pConfig.save();
        pConfig.discard();
    }

    public void asyncInventory(UUID shopOwner) {
        for(Player player : Bukkit.getOnlinePlayers()) {
            if(player.getOpenInventory().getTopInventory().getHolder() instanceof ShopInventory) {
                ShopInventory gui = (ShopInventory) player.getOpenInventory().getTopInventory().getHolder();
                if(gui.owner.equals(shopOwner)) {
                    player.closeInventory();
                }
            }
        }
    }

    public void upgrade() throws InstantiationException, IllegalAccessException {
        PlayerConfig pConfig = PlayerConfig.getConfig(this.shopOwner);
        int currentLevel = getShopTier();
        int nextLevel = currentLevel + 1;
        Player player = Bukkit.getPlayer(shopOwner);
        player.sendMessage(PlayerShops.colorize("&a&l*** SHOP UPGRADE TO LEVEL " + (nextLevel) + " COMPLETE ***"));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 2.0F, 1.0F);
        pConfig.set("player.shopTier", nextLevel);

        pConfig.save();
        pConfig.discard();

        ShopObject shopObject = shopLocationDirectory.get(this.shopOwner);
        ShopInventory shopInventory = new ShopInventory(this.shopOwner, nextLevel, shopObject);
        shopObject.setShopInventory(shopInventory);
    }

    public boolean getStatus() {
        PlayerConfig pConfig = PlayerConfig.getConfig(this.shopOwner);
        boolean isOpen = pConfig.getBoolean("player.shopOpen");
        pConfig.discard();
        return isOpen;
    }

    public int getShopTier() {
        PlayerConfig pConfig = PlayerConfig.getConfig(this.shopOwner);
        int shopTier = pConfig.getInt("player.shopTier");
        pConfig.discard();
        return shopTier;
    }

    public void updateName(String newName) {
        PlayerConfig pConfig = PlayerConfig.getConfig(this.shopOwner);
        pConfig.set("player.shopName", newName);
        pConfig.save();
        pConfig.discard();
        updateHolograms();
    }

    public void addViews(UUID uuid) {
        PlayerConfig pConfig = PlayerConfig.getConfig(this.shopOwner);
        ConfigurationSection pViewCfg = pConfig.getConfigurationSection("player.shopViews");
        Set<String> keys = pViewCfg.getKeys(false);

        boolean alreadyViewed = false;
        for(String key : keys) {
            if(UUID.fromString(pConfig.getString("player.shopViews." + key)).equals(uuid)) {
                alreadyViewed = true;
                break;
            }
        }

        if(!alreadyViewed) {
            pConfig.set("player.shopViews." + (keys.size() + 1), uuid.toString());
        }

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

    public void addItem(ItemStack itemStack, int price, int slot) {
        PlayerConfig pConfig = PlayerConfig.getConfig(this.shopOwner);
        UUID itemID = UUID.randomUUID();

        pConfig.set("player.contents." + itemID + ".itemstack", itemStack);
        pConfig.set("player.contents." + itemID + ".price", price);
        pConfig.set("player.contents." + itemID + ".amount", itemStack.getAmount());
        pConfig.set("player.contents." + itemID + ".slot", slot);

        pConfig.save();
        pConfig.discard();

        Player player = Bukkit.getPlayer(this.shopOwner);
        player.sendMessage(PlayerShops.colorize("&aPrice set. Right-Click item to edit."));
        player.playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT, 2.0F, 1.0F);
    }

    public void setPrice(int clickedSlot, int price) {
        PlayerConfig pConfig = PlayerConfig.getConfig(this.shopOwner);
        ConfigurationSection cfg = pConfig.getConfigurationSection("player.contents");
        Set<String> keys = cfg.getKeys(false);

        for(String key : keys) {
            int configSlot = pConfig.getInt("player.contents." + key + ".slot");
            if(clickedSlot == configSlot) {
                pConfig.set("player.contents." + key + ".price", price);
            }
        }

        pConfig.save();
        pConfig.discard();

        Player player = Bukkit.getPlayer(this.shopOwner);
        player.sendMessage(PlayerShops.colorize("&aPrice set. Right-Click item to edit."));
        player.playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT, 2.0F, 1.0F);
    }

    public int getItemPrice(int clickedSlot, boolean singular) {
        PlayerConfig pConfig = PlayerConfig.getConfig(this.shopOwner);
        ConfigurationSection cfg = pConfig.getConfigurationSection("player.contents");
        Set<String> keys = cfg.getKeys(false);
        int price = 0;
        int amount = 0;

        for(String key : keys) {
            int configSlot = pConfig.getInt("player.contents." + key + ".slot");
            if(clickedSlot == configSlot) {
                price = pConfig.getInt("player.contents." + key + ".price");
                amount = pConfig.getInt("player.contents." + key + ".amount");
                break;
            }
        }

        pConfig.discard();
        if(singular) {
            return price;
        } else {
            return (price * amount);
        }
    }

    public int getAmount(int clickedSlot) {
        PlayerConfig pConfig = PlayerConfig.getConfig(this.shopOwner);
        ConfigurationSection cfg = pConfig.getConfigurationSection("player.contents");
        Set<String> keys = cfg.getKeys(false);
        int amount = 0;

        for(String key : keys) {
            int configSlot = pConfig.getInt("player.contents." + key + ".slot");
            if(clickedSlot == configSlot) {
                amount = pConfig.getInt("player.contents." + key + ".amount");
            }
        }

        pConfig.discard();

        return amount;
    }

    public void process(UUID owner, UUID shopPlayer, int slot, int requestedAmount) {
        Player player = Bukkit.getPlayer(shopPlayer);
        PlayerConfig pConfig = PlayerConfig.getConfig(shopOwner);
        ConfigurationSection itemCfg = pConfig.getConfigurationSection("player.contents");
        Set<String> keys = itemCfg.getKeys(false);

        for(String key : keys) {
            int cfgSlot = pConfig.getInt("player.contents." + key + ".slot");
            if(cfgSlot == slot) {
                int cfgAmount = pConfig.getInt("player.contents." + key + ".amount");
                int cfgDifference = cfgAmount - requestedAmount;

                if(requestedAmount > 0 && requestedAmount <= cfgAmount) {
                    ItemStack rqstedItem = pConfig.getItemStack("player.contents." + key + ".itemstack").clone();
                    rqstedItem.setAmount(requestedAmount);
                    player.getInventory().addItem(rqstedItem);

                    if (!shopOwner.equals(player)) {
                        int price = requestedAmount * getItemPrice(slot, true);
                        ItemStack itemHistory = addPriceTag(rqstedItem.clone(), price);
                        pConfig.set("player.shopHistory." + key, itemHistory);
                    }
                }
                if(cfgDifference == 0) {
                    pConfig.set("player.contents." + key, null);
                    shopLocationDirectory.get(owner).getShopInventory().getInventory().setItem(slot, null);
                } else {
                    pConfig.set("player.contents." + key + ".amount", cfgDifference);

                    ItemStack cfgItem = pConfig.getItemStack("player.contents." + key + ".itemstack");
                    cfgItem.setAmount(cfgDifference);

                    pConfig.set("player.contents." + key + ".itemstack", cfgItem);
                    shopLocationDirectory.get(owner).getShopInventory().getInventory().setItem(slot, cfgItem.clone());
                }
            }
        }
        pConfig.save();
        pConfig.discard();
    }

    public ItemStack addPriceTag(ItemStack itemHistory, int price) {
        ItemMeta historyMeta = itemHistory.getItemMeta();
        List<String> lore = new ArrayList<>();

        if(historyMeta != null && historyMeta.getLore() != null) {
            lore.addAll(historyMeta.getLore());
        }
        lore.add(lore.size(), PlayerShops.colorize("&aSold for &f&l" + price + "g."));
        historyMeta.setLore(lore);
        itemHistory.setItemMeta(historyMeta);
        return itemHistory;
    }

}
