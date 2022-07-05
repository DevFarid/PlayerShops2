package com.faridkamizi.system;

import com.faridkamizi.PlayerShops;
import com.faridkamizi.inventory.gui.ShopInventory;
import com.faridkamizi.util.Hologram;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Chest;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class ShopObject extends ShopLocation implements UniversalShopStorage {
    private final UUID shopOwner;
    private final String shopName;
    private ShopConfig shopConfig;
    private final ShopInventory shopInventory;

    /**
     * Constructor for when a shop is to be created.
     * @param player
     *              the shop owner
     * @param name
     *              the name associated with the shop
     * @param locations
     *              the location(s) associated with shop such as chest locations, hologram locations, and particle location.
     */
    public ShopObject(UUID player, String name, Location... locations) {
        super(locations);
        this.shopOwner = player;
        this.shopName = name;
        this.shopConfig = new ShopConfig(player, this.shopName, super.getShopLocation());
        int shopSize = ((1 + shopConfig.getShopTier()) * 9);

        this.shopInventory = new ShopInventory(this.shopOwner, shopSize, this);
        this.createPhysicalProperties();

        add(shopOwner, this);
    }

    /**
     * Create physical representation in the world using the locations associated with the shop.
     */
    private void createPhysicalProperties() {
        if(!shopLocationDirectory.containsKey(this.shopOwner)) {
            Player player = Bukkit.getPlayer(this.shopOwner);

            Block block1 = getShopLocation().get(0).clone().getBlock();
            Block block2 = getShopLocation().get(1).clone().getBlock();

            Hologram.createHolo(PlayerShops.colorize("&c"+shopName), getShopLocation().get(2));
            Hologram.createHolo(PlayerShops.colorize("&f1 &cview(s)"), getShopLocation().get(3));

            block1.setType(Material.CHEST);
            block2.setType(Material.CHEST);

            org.bukkit.block.data.type.Chest chestBlockState1 = (org.bukkit.block.data.type.Chest) block1.getBlockData();
            chestBlockState1.setType(org.bukkit.block.data.type.Chest.Type.LEFT);
            block1.setBlockData(chestBlockState1, true);

            org.bukkit.block.data.type.Chest chestBlockState2 = (org.bukkit.block.data.type.Chest) block2.getBlockData();
            chestBlockState2.setType(Chest.Type.RIGHT);
            block2.setBlockData(chestBlockState2, true);

            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 2.0F, 1.0F);
            player.sendMessage(PlayerShops.colorize("\n&e&lYOU'VE CREATED A SHOP!\n&eTo stock your shop, simply drag items into your shop's inventory."));
        }
    }

    /**
     * Get the owner of {@code this} shop.
     * @return
     *          the UUID that belongs to the owner
     */
    public UUID getShopOwnerID() {
        return this.shopOwner;
    }

    /**
     * Get the {@code ShopConfig} config associated with this {@code ShopObject} shop.
     * @return
     *         the {@code ShopConfig} object associated with this {@code ShopObject}
     */
    public ShopConfig getShopConfig() {
        return this.shopConfig;
    }

    public void setShopConfig(ShopConfig newConfig) {
        this.shopConfig = newConfig;
    }

    /**
     * Get the {@code ShopInventory} associated with this {@code ShopObject} shop.
     * @return
     *         the {@code ShopInventory} object associated with this {@code ShopObject}
     */
    public ShopInventory getShopInventory() {
        return this.shopInventory;
    }

    /**
     * Given a location, find the {@code ShopObject} shop associated with the clicked location.
     * @param clickedBlock
     *                  the clicked block incoming as a location.
     * @return
     *          the {@code ShopObject} associated to the clicked block
     */
    public static ShopObject getShop(Location clickedBlock) {
        ShopObject shopObject = null;
        for(Map.Entry<UUID, ShopObject> entry : shopLocationDirectory.entrySet()) {
            if(clickedBlock.equals(entry.getValue().getShopLocation().get(0)) || clickedBlock.equals(entry.getValue().getShopLocation().get(1))) {
                shopObject = entry.getValue();
                break;
            }
        }
        return shopObject;
    }

    /**
     * Add this shop to the shop directory.
     * @param uuid
     *              the player who owns {@code this} ShopObject.
     * @param shopObject
     *                  The shop object that was just created, a.k.a {@code this}.
     */
    @Override
    public void add(UUID uuid, ShopObject shopObject) {
        if(!shopLocationDirectory.containsKey(uuid)) {
            shopLocationDirectory.put(uuid, shopObject);
        }
    }
}
