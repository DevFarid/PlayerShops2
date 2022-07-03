package com.faridkamizi.shops;

import com.faridkamizi.PlayerShops;
import com.faridkamizi.config.PlayerConfig;
import com.faridkamizi.inventory.gui.ShopInventory;
import com.faridkamizi.inventory.holders.ShopInventoryHolder;
import com.faridkamizi.util.AsyncParticles;
import com.faridkamizi.util.Hologram;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ShopObject {

    /**
     * The HashMap containing {@code BundledLocation} as its key, and {code ShopObject} as its value
     *  for any entry in the hashTable.
     */
    public final static Map<BundledLocation, UUID> shopLocationDirectory = new HashMap<>();

    /**
     * Bundling two Location Objects together as one.
     */
    public static class BundledLocation {
        public Location[] loc = new Location[2];
        public Location[] armorStandLoc;
        public Location particleLocation;
        BundledLocation(Location location, Location locationTwo, Location[] asLoc) {
            this.loc[0] = location;
            this.loc[1] = locationTwo;
            this.armorStandLoc = asLoc;
            this.particleLocation = null;
        }
        public Location[] getLoc() {
            return loc;
        }
    }

    /**
     * Places a shop at the incoming Location, and creates the ShopObject for the Player.
     * @param location
     *                  the incoming Location object.
     * @param player
     *                  the player object associated with this.
     */
    public static void add(Location location, UUID player, String name) {
        Player p = Bukkit.getPlayer(player);
        if(containsPlayer(player)) {
            p.sendMessage(ChatColor.RED + "You already have a shop on US-1.");
        } else {
            // get config and set shop status to close because of init.
            PlayerConfig pConfig = PlayerConfig.getConfig(player);
            checkConfigDefaults(player);

            pConfig.set("player.shopOpen", false);

            // set the new location of the shop in the config
            pConfig.set("player.location", location);
            pConfig.set("player.shopName", name);
            pConfig.createSection("player.shopViews");
            pConfig.createSection("player.shopHistory");
            pConfig.set("player.shopViews.1", player.toString());

            pConfig.save();
            pConfig.discard();

            addShop(location, player, name);
        }
    }

    /**
     * Generates double chest at the clicked location that will be set to player's shop.
     * @param location
     *                  the location which two chest will be generated.
     * @param player
     *              the shop owner
     */
    private static void addShop(Location location, UUID player, String title) {
        Player p = Bukkit.getPlayer(player);
        if(!contains(location) && !containsPlayer(player)) {
            location = location.add(0, 1, 0);

            boolean isAir = false;
            Block block = location.getBlock();

            for (int x = -2; x < 2; x++) {
                for (int y = 0; y < 1; y++) {
                    for (int z = -2; z < 2; z++) {
                        isAir = block.getRelative(x, y, z).getType().equals(Material.AIR);
                        if(!isAir) {
                            break;
                        }
                    }
                }
            }

            if(!isAir) {
                p.sendMessage(ChatColor.RED + "No free space was available.");
            } else {
                Block block1 = location.getBlock();
                Block block2 = location.add(1, 0,0).getBlock();

                Location hologramTitle = location.clone();
                hologramTitle.add(0,-0.9, 0.5);

                Location hologramView = location.clone();
                hologramView.add(0,-1.2, 0.5);

                Hologram.createHolo(PlayerShops.colorize("&c"+title), hologramTitle);
                Hologram.createHolo(PlayerShops.colorize("&f1 &cview(s)"), hologramView);

                Location[] holos = new Location[2];
                holos[0] = hologramTitle;
                holos[1] = hologramView;

                block1.setType(Material.CHEST);
                block2.setType(Material.CHEST);

                Chest chestBlockState1 = (Chest) block1.getBlockData();
                chestBlockState1.setType(Chest.Type.LEFT);
                block1.setBlockData(chestBlockState1, true);

                Chest chestBlockState2 = (Chest) block2.getBlockData();
                chestBlockState2.setType(Chest.Type.RIGHT);
                block2.setBlockData(chestBlockState2, true);

                BundledLocation shopLocation = new BundledLocation(block1.getLocation(), block2.getLocation(), holos);

                // add the location of the double chest along with player's UUID.
                shopLocationDirectory.put(shopLocation, player);
                p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 2.0F, 1.0F);
                p.sendMessage(PlayerShops.colorize("\n&e&lYOU'VE CREATED A SHOP!\n&eTo stock your shop, simply drag items into your shop's inventory."));
            }
        }
    }

    /**
     *
     * @param player
     *              the player in the interest of the search.
     * @return
     *         whether if the interested player has their shop open or not.
     */
    public static boolean shopOpen(UUID player) {
        boolean isShopOpen = false;
        if(containsPlayer(player)) {
            PlayerConfig pConfig = PlayerConfig.getConfig(player);
            isShopOpen = pConfig.getBoolean("player.shopOpen");
            pConfig.discard();
        }
        return isShopOpen;
    }

    /**
     * Will check to see if the {@code location} is a valid and verified shop location.
     * @param location
     *                  the clicked chest incoming as a location.
     * @return
     *        a boolean reflecting whether that location is a shop location.
     */
    public static boolean contains(Location location) {
        boolean isShop = false;
        for(Map.Entry<BundledLocation, UUID> shopObjectEntry : shopLocationDirectory.entrySet()) {
            if(shopObjectEntry.getKey().loc[0].equals(location) || shopObjectEntry.getKey().loc[1].equals(location)) {
                isShop = true;
                break;
            }
        }
        return isShop;
    }

    /**
     * Checks if a UUID of a player has a shop.
     * @param uuid
     *              the player in the interest of the search in form of an UUID.
     * @return
     *          the boolean reflecting if player has a shop.
     */
    public static boolean containsPlayer(UUID uuid) {
        boolean playerWasFound = false;
        for(Map.Entry<BundledLocation, UUID> shopObjectEntry : shopLocationDirectory.entrySet()) {
            if(shopObjectEntry.getValue().equals(uuid)) {
                playerWasFound = true;
                break;
            }
        }
        return playerWasFound;
    }

    /**
     * Gets the shop owner's uuid from a location.
     * @param location
     *                  the clicked block's location.
     * @return
     *          the uuid of the player who the location of shop belongs to.
     */
    public static UUID getOwner(Location location) {
        UUID id = null;
        if(contains(location)) {
            for(Map.Entry<BundledLocation, UUID> shopObjectEntry : shopLocationDirectory.entrySet()) {
                if(shopObjectEntry.getKey().loc[0].equals(location) || shopObjectEntry.getKey().loc[1].equals(location)) {
                    id = shopObjectEntry.getValue();
                    break;
                }
            }
        }
        return id;
    }

    /**
     * Checks to see if default values of a given player's config is valid, if not proceed to create the defaults for
     * the user.
     * @param uuid
     *              the uuid of a player with the interest of the check validation.
     *
     */
    public static void checkConfigDefaults(UUID uuid) {
        PlayerConfig pConfig = PlayerConfig.getConfig(uuid);

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
        pConfig.discard();
    }

    /**
     * Gets the location of a shop owner's uuid.
     * @param uuid
     *             the uuid who is of the interest in the search.
     * @return
     *          returns a {@code BundledLocation} object which is found to be containing the two-tuple
     *          location of the shop.
     */
    public static BundledLocation getBundledLocation(UUID uuid) {
        BundledLocation loc = null;
        if(containsPlayer(uuid)) {
            for(Map.Entry<BundledLocation, UUID> shopObjectEntry : shopLocationDirectory.entrySet()) {
                if(shopObjectEntry.getValue().equals(uuid)) {
                    loc = shopObjectEntry.getKey();
                    break;
                }
            }
        }
        return loc;
    }

    /**
     * When a player opens a shop, increment the viewer count on the displayed hologram.
     * @param forShop
     *               the shop owner
     * @param player
     *               the viewer being added
     * @requires
     *              forShop != player
     * @ensures
     *              shopViewers = [all unique players who have viewed this shop]
     */
    public static void addView(UUID forShop, UUID player) {
        if(!forShop.equals(player)) {
            PlayerConfig pConfig = PlayerConfig.getConfig(forShop);
            if (pConfig.contains("player.shopViews")) {
                Set<String> keys = pConfig.getConfigurationSection("player.shopViews").getKeys(false);

                boolean exists = false;
                for (String key : keys) {
                    if (pConfig.get("player.shopViews." + key).equals(player.toString())) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    pConfig.set("player.shopViews." + (keys.size() + 1), player.toString());
                }
            }
            pConfig.save();
            pConfig.discard();

            renameShop(forShop, pConfig.getString("player.shopName"));
        }
    }

    /**
     * Returns the amount of times a shop has been viewed.
     * @param forShop
     *                  the shop who we need to look for and see how many views it has
     * @return
     *          getShopViews = [numbers of players who have viewed this shop as an integer]
     */
    public static int getShopViews(UUID forShop) {
        PlayerConfig pConfig = PlayerConfig.getConfig(forShop);
        int size = 1;
        if(pConfig.contains("player.shopViews")) {
            Set<String> keys = pConfig.getConfigurationSection("player.shopViews").getKeys(false);
            size = keys.size();
        }
        pConfig.discard();
        return size;
    }

    /**
     * Rename shop function
     * @param uuid
     *             the player's uuid
     * @param newName
     *              the new name for player's shop.
     */
    public static void renameShop(UUID uuid, String newName) {
        BundledLocation loc = getBundledLocation(uuid);
        Location[] holograms = loc.armorStandLoc;
        String shopStatus = ShopObject.shopOpen(uuid) ? "&a" : "&c";

        PlayerConfig pConfig = PlayerConfig.getConfig(uuid);
        int shopViews = getShopViews(uuid);

        Hologram.deleteHolo(holograms[0]);
        Hologram.deleteHolo(holograms[1]);

        Hologram.createHolo(PlayerShops.colorize(shopStatus + newName), holograms[0]);
        Hologram.createHolo(PlayerShops.colorize("&f" + shopViews + "" + shopStatus + " view(s)"), holograms[1]);

        pConfig.set("player.shopName", newName);
        pConfig.save();
        pConfig.discard();
    }

    /**
     * Upgrades a player's shop.
     * @param uuid
     *              the shop owner who is seeking to upgrade.
     * @param tier
     *              the tier for the designated upgrade.
     */
    public static void upgradeShop(UUID uuid, int tier) {
        PlayerConfig pConfig = PlayerConfig.getConfig(uuid);
        Player player = Bukkit.getPlayer(uuid);
        pConfig.set("player.shopTier", tier);
        pConfig.save();
        pConfig.discard();
        player.sendMessage(PlayerShops.colorize("&a&l*** SHOP UPGRADE TO LEVEL " + tier + " COMPLETE ***"));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 2.0F, 1.0F);
    }

    /**
     * retrieve a specific {@code Player}'s shop tier for number of inventory rows.
     * @param owner
     *              the owner UUID who the shop belongs to
     * @return
     *        the integer which represent the shop owner's shop tier level
     */
    public static int getInventoryRows(UUID owner) {
        PlayerConfig pConfig = PlayerConfig.getConfig(owner);
        int i = pConfig.getInt("player.shopTier");
        pConfig.discard();
        return i;
    }

    /**
     * Adds an {@code ItemStack} item to a Shop for {@code integer} a given price.
     * @param shopOwner
     *                  the shop owner who is adding this {@code ItemStack} item.
     * @param iToAdd
     *                  the {@code ItemStack} item being added.
     * @param price
     *                  the price for the given {@code ItemStack} item.
     */
    public static void addItemToShop(UUID shopOwner, ItemStack iToAdd, int price) {
        Player player = Bukkit.getPlayer(shopOwner);
        List<String> itemLore = new ArrayList<>();
        itemLore.add(PlayerShops.colorize("&aPrice: &f" + price + "g &aeach"));

        ItemMeta itemMeta = iToAdd.getItemMeta();
        itemMeta.setLore(itemLore);
        iToAdd.setItemMeta(itemMeta);

        UUID ID = UUID.randomUUID();

        PlayerConfig pConfig = PlayerConfig.getConfig(shopOwner);
        pConfig.set("player.contents." + ID.toString(), iToAdd);
        player.setItemOnCursor(null);
        player.closeInventory();
        pConfig.save();
        pConfig.discard();
        player.sendMessage(PlayerShops.colorize("&aPrice set. Right-Click item to edit."));
        player.playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT, 2.0F, 1.0F);
    }

    /**
     * Will remove a shop price tag from an {@code ItemStack} in a shop.
     * @param itemStack
     *                  the itemstack in the interest of having their price tag removed.
     */
    public static void deletePriceTag(ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        List<String> lore = itemMeta.getLore();
        lore.set(lore.size()-1, "");
        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);
    }

    /**
     * Given a shop item, this function will report the price of a given item in a shop.
     * @param itemStack
     *                  the clicked item being passed.
     * @return
     *          the integer that represents the price.
     */
    public static int itemPrice(ItemStack itemStack) {
        int price = 0;
        ItemMeta itemMeta = itemStack.getItemMeta();
        List<String> lore = itemMeta.getLore();
        String[] splitLore = lore.get(lore.size()-1).split(" ");

        splitLore[1] = splitLore[1].replaceAll("g", "");

        price = Integer.parseInt(ChatColor.stripColor(splitLore[1]));

        return price;
    }

    /**
     * Toggles the shop's state. if `shopOpen` is true in the yaml file, then flip the boolean value, and
     * save the change in the yaml.
     * @param owner
     *              the player who is the owner of this shop.
     * @param actor
     *              the player that is interacting with this GUI.
     */
    public static void switchShopStatus(UUID owner, UUID actor) {
        if(owner.equals(actor)) {
            PlayerConfig pConfig = PlayerConfig.getConfig(owner);
            boolean shopStatus = pConfig.getBoolean("player.shopOpen");
            if (shopStatus) {
                pConfig.set("player.shopOpen", false);
                ShopObject.renameShop(owner, PlayerShops.colorize("&c" + pConfig.get("player.shopName")));

                /* TO-DO: Close this inventory for players who may have it open. */
                ShopObject.asyncUpdateInventory(owner, true, 0);

            } else {
                pConfig.set("player.shopOpen", true);
                ShopObject.renameShop(owner, PlayerShops.colorize("&a" + pConfig.get("player.shopName")));
            }
            pConfig.save();
            pConfig.discard();
        }
    }

    /**
     * Utility function that will see which current online players have a specific inventory open.
     * @param owner
     *              the shop we are interested to see who may have this shop open.
     * @param shouldClose
     *                      a boolean reflecting whether the update requires the player viewing to close their inventory.
     * @param slot
     *             if {@code shouldClose} is false, then it is a live update and we need the slot to update for.
     */
    public static void asyncUpdateInventory(UUID owner, boolean shouldClose, int slot) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getOpenInventory().getTopInventory().getHolder() instanceof ShopInventoryHolder) {
                ShopInventory gui = (ShopInventory) p.getOpenInventory().getTopInventory().getHolder();
                if (gui.owner.equals(owner)) {
                    if(shouldClose) {
                        p.closeInventory();
                    } else {
                        p.getOpenInventory().getTopInventory().setItem(slot, null);
                        p.updateInventory();
                    }
                }
            }
        }
    }

    /**
     * Deletes a player's (a single unit) shop.
     * @param player
     *              the uuid belonging to the owner of the shop.
     */
    public static void deleteShop(UUID player) {
        if(containsPlayer(player)) {

            PlayerConfig pConfig = PlayerConfig.getConfig(player);
            if(pConfig.getBoolean("player.shopOpen")) {
                pConfig.set("player.shopOpen", false);
            }

            pConfig.set("player.location", null);
            pConfig.set("player.shopName", null);
            pConfig.set("player.shopHistory", null);
            pConfig.set("player.shopViews", null);

            pConfig.save();
            pConfig.discard();

            BundledLocation key = null;
            for(Map.Entry<BundledLocation, UUID> shopObjectEntry : shopLocationDirectory.entrySet()) {
                if(shopObjectEntry.getValue().equals(player)) {
                    for(Location loc : shopObjectEntry.getKey().getLoc()) {
                        loc.getBlock().setType(Material.AIR);
                    }
                    key = shopObjectEntry.getKey();
                    break;
                }
            }
            if(key.particleLocation != null) {
                AsyncParticles.stopTask(key.particleLocation);
            }
            Hologram.deleteHolo(key.armorStandLoc[0]);
            Hologram.deleteHolo(key.armorStandLoc[1]);
            shopLocationDirectory.remove(key);
        }
    }

    /**
     * Called when the server is about to shut down. Will remove all shops, with their holograms.
     * Bulk delete.
     */
    public static void closeAllShop() {
        for(Map.Entry<BundledLocation, UUID> shopObjEntry : shopLocationDirectory.entrySet()) {

            PlayerConfig pConfig = PlayerConfig.getConfig(shopObjEntry.getValue());

            Block placedChest = shopObjEntry.getKey().loc[0].getBlock();
            Block twinChest = shopObjEntry.getKey().loc[1].getBlock();

            placedChest.setType(Material.AIR);
            twinChest.setType(Material.AIR);

            pConfig.set("player.shopOpen", false);
            pConfig.set("player.location", null);
            pConfig.set("player.shopName", null);
            pConfig.set("player.shopHistory", null);
            pConfig.set("player.shopViews", null);

            pConfig.save();
            pConfig.discard();
        }
        Hologram.removeAll();
        shopLocationDirectory.clear();
    }
}
