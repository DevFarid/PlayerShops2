package com.faridkamizi.inventory.gui;

import com.faridkamizi.PlayerShops;
import com.faridkamizi.config.PlayerConfig;
import com.faridkamizi.currency.Currency;
import com.faridkamizi.events.PreInputProcess;
import com.faridkamizi.inventory.holders.ShopInventoryHolder;
import com.faridkamizi.shops.enhanced.ShopObject;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;


public class ShopInventory implements ShopInventoryHolder {

    public UUID owner;
    private final Inventory inventory;
    private final ShopObject shopObject;
    /*
    --------------------------------------------------------------------------------------------------------------------
    Constructor
    --------------------------------------------------------------------------------------------------------------------
     */
    public ShopInventory(UUID shopOwner, int inventorySize, ShopObject shopObjectHolder) {
        String name = Bukkit.getOfflinePlayer(shopOwner).getName()+ "'s Shop";
        this.owner = shopOwner;
        this.inventory = Bukkit.createInventory(this, inventorySize, name);
        this.shopObject = shopObjectHolder;
        this.setUp();
    }

        /*
    --------------------------------------------------------------------------------------------------------------------
    Inventory Handling
    --------------------------------------------------------------------------------------------------------------------
     */

    @Override
    public void onClick(InventoryClickEvent e) {
        if(e.getClick() == ClickType.LEFT) {
            Player player = (Player) e.getWhoClicked();
            boolean isOwner = player.getUniqueId().equals(this.owner);
            if (e.getClickedInventory() != null) {
                if ((e.getClickedInventory().getType() == InventoryType.CHEST) && e.getCurrentItem() == null && e.getCursor() != null) {
                    if (isOwner) {
                        if (!shopObject.getShopConfig().getOwnerConfig().getBoolean("player.shopOpen")) {
                            player.sendMessage(PlayerShops.colorize("&aEnter the &lGEM&a value of [&l" + e.getCursor().getAmount() + "x&a] of this item."));
                            PreInputProcess.requestPlayer(player, e);
                            e.getWhoClicked().setItemOnCursor(null);
                            player.closeInventory();
                        } else {
                            e.setCancelled(true);
                            player.sendMessage(PlayerShops.colorize("&cYou must close your shop to add an item."));
                        }
                    } else {
                        e.setCancelled(true);
                    }
                } else if (((e.getClickedInventory().getType() == InventoryType.CHEST)) && e.getCurrentItem() != null && e.getCursor().getType().isAir()) {
                    e.setCancelled(true);
                    // Shop History Function.
                    if (e.getRawSlot() == e.getClickedInventory().getSize() - 9) {
                        OfflinePlayer shopOwner = Bukkit.getOfflinePlayer(this.owner);
                        ShopHistoryInventory shopHistoryInventory = new ShopHistoryInventory(this.owner, PlayerShops.colorize("&8" + shopOwner.getName() + "'s Shop History"), 9);
                        player.closeInventory();
                        player.openInventory(shopHistoryInventory.getInventory());
                    }
                    // ChestSFX menu.
                    else if (e.getRawSlot() == e.getClickedInventory().getSize() - 8) {
                        if (isOwner) {
                            ShopSFXInventory sfxInventory = new ShopSFXInventory(this.owner, PlayerShops.colorize("&8Shop Effect Selector"), 9);
                            player.closeInventory();
                            player.openInventory(sfxInventory.getInventory());
                        }
                    }
                    // Rename shop function.
                    else if (e.getRawSlot() == e.getClickedInventory().getSize() - 7) {
                        if (isOwner) {
                            player.sendMessage(PlayerShops.colorize("&ePlease enter a &lSHOP NAME&r&e. [max 16 characters]"));
                            player.closeInventory();
                            PreInputProcess.requestPlayer(player, e);
                        }
                    }
                    // Delete shop function.
                    else if (e.getRawSlot() == e.getClickedInventory().getSize() - 2) {
                        if (isOwner) {

                            // TO-DO: CLOSE this inventory for whoever that may have it open.
                            shopObject.deleteShop(this.owner);
                            player.closeInventory();
                            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 2.0F, 1.0F);
                        }
                    }
                    // Open/Close function.
                    else if (e.getRawSlot() == e.getClickedInventory().getSize() - 1) {
                        if (isOwner) {
                            shopObject.getShopConfig().toggleShopStatus();
                            ItemStack openStatus = createGuiItem(Material.LIME_DYE, "&cClick to &lCLOSE &cShop", PlayerShops.colorize("&fClick to &cclose&f shop."));
                            ItemStack closedStatus = createGuiItem(Material.GRAY_DYE, "&aClick to &lOPEN &aShop", PlayerShops.colorize("&fClick to &2open&f shop."));
                            if (shopObject.getShopConfig().getStatus()) {
                                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 2.0F, 1.0F);
                                e.getClickedInventory().setItem(e.getRawSlot(), openStatus);
                            } else {
                                player.playSound(player.getLocation(), Sound.ENTITY_CHICKEN_EGG, 2.0F, 1.0F);
                                e.getClickedInventory().setItem(e.getRawSlot(), closedStatus);
                            }

                        }
                    }
                    // Otherwise, the owner is trying to add an item.
                    else if (e.getRawSlot() < e.getClickedInventory().getSize() - 9) {
                        if (isOwner) {
                            if (!shopObject.getShopConfig().getStatus()) {
                                process(this.owner, player.getUniqueId(), e.getCurrentItem(), e.getRawSlot());
                            } else {
                                e.getWhoClicked().closeInventory();
                                player.sendMessage(PlayerShops.colorize("&cYou must close your shop to remove an item first."));
                            }
                        } else {
                            int itemPrice = itemPrice(e.getCurrentItem());
                            if (Currency.calculateBalance(player) >= itemPrice) {
                                Currency.remove(player, itemPrice);
                                ItemStack forSave = e.getCurrentItem();
                                process(this.owner, player.getUniqueId(), e.getCurrentItem(), e.getRawSlot());
                                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 2.0F, 1.0F);
                                player.sendMessage(PlayerShops.colorize("&aYou bough an item!"));

                                OfflinePlayer owner = Bukkit.getOfflinePlayer(this.owner);
                                if (owner.isOnline()) {
                                    owner.getPlayer().sendMessage(PlayerShops.colorize("&a" + e.getWhoClicked().getName() + " bought " + forSave.getType().name()));
                                }
                            } else {
                                player.sendMessage(PlayerShops.colorize("&cYou do not have enough gems."));
                                player.sendMessage(PlayerShops.colorize("&c&lCOST: &c" + itemPrice + "&lG"));
                            }
                        }
                    }
                } else if (((e.getClickedInventory().getType() == InventoryType.CHEST)) && e.getCurrentItem() != null && !(e.getCursor().getType().isAir())) {
                    e.setCancelled(true);
                }
            }
        } else {
            e.setCancelled(true);
        }
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    /*
    --------------------------------------------------------------------------------------------------------------------
    Member Functions
    --------------------------------------------------------------------------------------------------------------------
     */

    /**
     * Set up the shop items.
     */
    private void setUp() {
        int rowsLvlSize = 1 + shopObject.getShopConfig().getShopTier();
        this.inventory.clear();

        HashMap<Integer, ItemStack> bottomRow = getShopMenuItems(owner, rowsLvlSize);
        List<ItemStack> ownerShopItems = getOwnerContents(owner);

        int currentRow = 0;
        int maxSlot = 9*rowsLvlSize;
        int counter = 0;
        int slot = 0;
        for(ItemStack i : ownerShopItems) {
            if(currentRow < maxSlot) {
                this.inventory.setItem(slot, i);

                counter++;
                if(counter > 8) {
                    currentRow++;
                    counter = 0;
                }
            } else {
                break;
            }
            slot++;
        }

        for(Map.Entry<Integer, ItemStack> rowEntry : bottomRow.entrySet()) {
            this.inventory.setItem(rowEntry.getKey(), rowEntry.getValue());
        }
        for(int i = 1; i < 4; i++) {
            this.inventory.setItem((rowsLvlSize*9-6+i), (bottomRow.get(rowsLvlSize*9-6)));
        }
    }

    /**
     * Processes the action done by the {@code UUID} player in {@code UUID} owner's shop,
     * {@code UUID} player can be the {@code UUID} owner, themself, if so remove the item.
     * If not, then attempt to proceed to purchase the item for x price.
     */
    public void process(UUID shopOwner, UUID forPlayer, ItemStack itemSlot, int slot) {
        Player player = Bukkit.getPlayer(forPlayer);
        PlayerConfig pConfig = PlayerConfig.getConfig(shopOwner);
        if (pConfig.contains("player.contents") && (pConfig.getConfigurationSection("player.contents").getKeys(false).size() > 0)) {
            Set<String> keys = pConfig.getConfigurationSection("player.contents").getKeys(false);
            for (String key : keys) {
                ItemStack itemStack = pConfig.getItemStack("player.contents." + key);
                ItemStack historyStack = itemStack.clone();
                if (itemSlot.equals(itemStack)) {

                    if(!shopOwner.equals(forPlayer)) {
                        pConfig.set("player.shopHistory."+key, historyStack);
                    }

                    deletePriceTag(itemStack);

                    this.inventory.setItem(slot, null);


                    player.getInventory().addItem(itemStack);
                    pConfig.set("player.contents." + key, null);

                    // TO-DO: CLOSE this inventory for whoever that may have it open.
                    this.shopObject.getShopInventory().getInventory().setItem(slot, null);

                    break;
                }
            }
            pConfig.save();
            pConfig.discard();
        } else {
            player.closeInventory();
            player.sendMessage(PlayerShops.colorize("&cYou must close your shop to remove an item first."));
        }
    }

    /*
    --------------------------------------------------------------------------------------------------------------------
    Static Functions
    --------------------------------------------------------------------------------------------------------------------
     */

    /**
     * Retrieves the owner content-item of a shop.
     * @param player
     *              the player who we are in the interest of the content-item search.
     * @return
     *        the list containing the contents of a shop.
     */
    private static List<ItemStack> getOwnerContents(UUID player) {
        List<ItemStack> ownerItems = new ArrayList<>();
        PlayerConfig pConfig = PlayerConfig.getConfig(player);
        ConfigurationSection cfg = pConfig.getConfigurationSection("player.contents");
        if(pConfig.contains("player.contents")) {
            Set<String> keys = cfg.getKeys(false);
            for (String key : keys) {
                ItemStack i = pConfig.getItemStack("player.contents." + key);
                ownerItems.add(ownerItems.size(), i);
            }
        }
        pConfig.discard();
        return ownerItems;
    }

    /**
     * Returns the default items that are placed within the button row as a {@code List<ItemStack>}
     * @param owner
     *              the owner UUID items requested for.
     * @return
     *          the string of ItemStack that will be returned for GUI placement and information.
     */
    public static HashMap<Integer, ItemStack> getShopMenuItems(UUID owner, int shopTier) {
        HashMap<Integer, ItemStack> shopMenuItems = new HashMap<>();
        PlayerConfig pConfig = PlayerConfig.getConfig(owner);
        boolean shopStatus = pConfig.getBoolean("player.shopOpen");
        int slotIndex = shopTier * 9;
        shopMenuItems.put(slotIndex-6, createGuiItem(Material.BLACK_STAINED_GLASS_PANE, " ", ""));

        /*
            Player skull ItemStack for shop history menu.
         */
        ItemStack pHead = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta pHeadMeta = (SkullMeta) pHead.getItemMeta();
        assert pHeadMeta != null;
        pHeadMeta.setOwningPlayer(Bukkit.getOfflinePlayer(owner));
        pHeadMeta.setDisplayName(PlayerShops.colorize("&a" + Bukkit.getOfflinePlayer(owner).getName() + "'s Shop"));
        pHead.setItemMeta(pHeadMeta);
        shopMenuItems.put(slotIndex-9, pHead);
        /*
            Chest SFX ItemStack for sfx menu.
         */
        ItemStack chestSFX = createGuiItem(Material.FIREWORK_ROCKET, "&aChest SFX", PlayerShops.colorize("&7More on this later."));
        shopMenuItems.put(slotIndex-8, chestSFX);
        /*
            Name tag ItemStack for Rename function.
         */
        ItemStack nameTag = createGuiItem(Material.NAME_TAG, "&aRename Shop", PlayerShops.colorize("&7More on this later."));
        shopMenuItems.put(slotIndex-7, nameTag);
        /*
            Name tag ItemStack for Rename function.
         */
        ItemStack barrier = createGuiItem(Material.BARRIER, "&cDelete Shop", PlayerShops.colorize("&7More on this later."));
        shopMenuItems.put(slotIndex-2, barrier);
        /*
            Name tag ItemStack for Rename function.
         */
        ItemStack openStatus = createGuiItem(Material.LIME_DYE, "&cClick to &lCLOSE &cShop", PlayerShops.colorize("&fClick to &cclose&f shop."));
        ItemStack closedStatus = createGuiItem(Material.GRAY_DYE, "&aClick to &lOPEN &aShop", PlayerShops.colorize("&fClick to &2open&f shop."));

        if(shopStatus) {
            shopMenuItems.put(slotIndex-1, openStatus);
        } else {
            shopMenuItems.put(slotIndex-1, closedStatus);
        }

        pConfig.discard();

        return shopMenuItems;
    }

    /**
     * Syntactic Sugar for creating an ItemStack with ItemMeta with shorter amount of lines.
     * @param material
     *                 the {@code Material} material type for this {@code ItemStack} item.
     * @param name
     *              the string name for the {@code ItemStack} item.
     * @param lore
     *              the array string containing the lore of the {@code ItemStack} item.
     * @return
     *          the ItemStack with given properties from the parameter.
     */
    public static ItemStack createGuiItem(final Material material, final String name, final String... lore) {
        final ItemStack item = new ItemStack(material, 1);
        final ItemMeta meta = item.getItemMeta();

        // Set the name of the item
        meta.setDisplayName(PlayerShops.colorize(name));

        // Set the lore of the item
        for(int i = 0; i < lore.length; i++) {
            lore[i] = PlayerShops.colorize(lore[i]);
        }

        meta.setLore(Arrays.asList(lore));

        item.setItemMeta(meta);

        return item;
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

}
