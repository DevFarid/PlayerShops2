package com.faridkamizi.inventory.gui;

import com.faridkamizi.PlayerShops;
import com.faridkamizi.config.PlayerConfig;
import com.faridkamizi.currency.Currency;
import com.faridkamizi.events.RequestEvent;
import com.faridkamizi.events.RequestInputEvent;
import com.faridkamizi.inventory.holders.ShopInventoryHolder;
import com.faridkamizi.system.ShopObject;
import com.faridkamizi.system.UniversalShopStorage;
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
    public void onClick(InventoryClickEvent invEvt) {
        Player player = (Player) invEvt.getWhoClicked();
        boolean isOwner = player.getUniqueId().equals(this.owner);
        if(invEvt.getClick() == ClickType.LEFT) {
            if (invEvt.getClickedInventory() != null) {
                if ((invEvt.getClickedInventory().getType() == InventoryType.CHEST) && invEvt.getCurrentItem() == null && invEvt.getCursor() != null) {
                    if (isOwner) {
                        if (!shopObject.getShopConfig().getOwnerConfig().getBoolean("player.shopOpen")) {
                            player.sendMessage(PlayerShops.colorize("&aEnter the &lGEM&a value of [&l" + invEvt.getCursor().getAmount() + "x&a] of this item."));
                            boolean reprice = false;
                            Object[] objects = {reprice, invEvt.getRawSlot(), invEvt.getCursor().clone()};

                            RequestEvent evt = new RequestEvent(this.owner, invEvt, objects);
                            RequestInputEvent.request(this.owner, evt);

                            invEvt.getWhoClicked().setItemOnCursor(null);
                            player.closeInventory();
                        } else {
                            invEvt.setCancelled(true);
                            player.sendMessage(PlayerShops.colorize("&cYou must close your shop to add an item."));
                        }
                    } else {
                        invEvt.setCancelled(true);
                    }
                } else if (((invEvt.getClickedInventory().getType() == InventoryType.CHEST)) && invEvt.getCurrentItem() != null && invEvt.getCursor().getType().isAir()) {
                    invEvt.setCancelled(true);
                    // Shop History Function.
                    if (invEvt.getRawSlot() == invEvt.getClickedInventory().getSize() - 9) {
                        OfflinePlayer shopOwner = Bukkit.getOfflinePlayer(this.owner);
                        ShopHistoryInventory shopHistoryInventory = new ShopHistoryInventory(this.owner, PlayerShops.colorize("&8" + shopOwner.getName() + "'s Shop History"), 9);
                        player.closeInventory();
                        player.openInventory(shopHistoryInventory.getInventory());
                    }
                    // ChestSFX menu.
                    else if (invEvt.getRawSlot() == invEvt.getClickedInventory().getSize() - 8) {
                        if (isOwner) {
                            ShopSFXInventory sfxInventory = new ShopSFXInventory(this.owner, PlayerShops.colorize("&8Shop Effect Selector"), 9);
                            player.closeInventory();
                            player.openInventory(sfxInventory.getInventory());
                        }
                    }
                    // Rename shop function.
                    else if (invEvt.getRawSlot() == invEvt.getClickedInventory().getSize() - 7) {
                        if (isOwner) {
                            player.sendMessage(PlayerShops.colorize("&ePlease enter a &lSHOP NAME&r&invEvt. [max 16 characters]"));
                            player.closeInventory();

                            RequestEvent evt = new RequestEvent(this.owner, invEvt, null);
                            RequestInputEvent.request(this.owner, evt);
                        }
                    }
                    // Delete shop function.
                    else if (invEvt.getRawSlot() == invEvt.getClickedInventory().getSize() - 2) {
                        if (isOwner) {
                            // TO-DO: CLOSE this inventory for whoever that may have it open.
                            UniversalShopStorage.deleteShop(this.owner);
                            player.closeInventory();
                            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 2.0F, 1.0F);
                        }
                    }
                    // Open/Close function.
                    else if (invEvt.getRawSlot() == invEvt.getClickedInventory().getSize() - 1) {
                        if (isOwner) {
                            shopObject.getShopConfig().toggleShopStatus();
                            ItemStack openStatus = createGuiItem(Material.LIME_DYE, "&cClick to &lCLOSE &cShop", PlayerShops.colorize("&fClick to &cclose&f shop."));
                            ItemStack closedStatus = createGuiItem(Material.GRAY_DYE, "&aClick to &lOPEN &aShop", PlayerShops.colorize("&fClick to &2open&f shop."));
                            if (shopObject.getShopConfig().getStatus()) {
                                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 2.0F, 1.0F);
                                invEvt.getClickedInventory().setItem(invEvt.getRawSlot(), openStatus);
                            } else {
                                player.playSound(player.getLocation(), Sound.ENTITY_CHICKEN_EGG, 2.0F, 1.0F);
                                invEvt.getClickedInventory().setItem(invEvt.getRawSlot(), closedStatus);
                            }

                        }
                    }
                    // Otherwise, the owner is trying to add an item.
                    else if (invEvt.getRawSlot() < invEvt.getClickedInventory().getSize() - 9) {
                        if (isOwner) {
                            if (!shopObject.getShopConfig().getStatus()) {
                                process(this.owner, player.getUniqueId(), invEvt.getRawSlot());
                            } else {
                                invEvt.getWhoClicked().closeInventory();
                                player.sendMessage(PlayerShops.colorize("&cYou must close your shop to remove an item first."));
                            }
                        } else {
                            ItemStack shopItem = invEvt.getCurrentItem().clone();
                            int slot = invEvt.getRawSlot();
                            int itemPrice = shopObject.getShopConfig().getItemPrice(slot);
                            if (Currency.calculateBalance(player) >= itemPrice) {
                                Currency.remove(player, itemPrice);
                                process(this.owner, player.getUniqueId(), slot);
                                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 2.0F, 1.0F);
                                player.sendMessage(PlayerShops.colorize("&aYou bough an item!"));

                                OfflinePlayer owner = Bukkit.getOfflinePlayer(this.owner);
                                if (owner.isOnline()) {
                                    owner.getPlayer().sendMessage(PlayerShops.colorize("&a" + invEvt.getWhoClicked().getName() + " bought " + shopItem.getType().name()));
                                }
                            } else {
                                player.sendMessage(PlayerShops.colorize("&cYou do not have enough gems."));
                                player.sendMessage(PlayerShops.colorize("&c&lCOST: &c" + itemPrice + "&lG"));
                            }
                        }
                    }
                } else if (((invEvt.getClickedInventory().getType() == InventoryType.CHEST)) && invEvt.getCurrentItem() != null && !(invEvt.getCursor().getType().isAir())) {
                    invEvt.setCancelled(true);
                }
            }
        } else if(invEvt.getClick() == ClickType.RIGHT) {
            // Reprice shop item.
            invEvt.setCancelled(true);
            if(isOwner) {
                if ((invEvt.getClickedInventory().getType() == InventoryType.CHEST) && invEvt.getCurrentItem() != null && invEvt.getCursor() == null) {
                    boolean reprice = true;
                    Object[] objects = {reprice, invEvt.getRawSlot(), invEvt.getCursor().clone()};

                    RequestEvent evt = new RequestEvent(this.owner, invEvt, objects);
                    RequestInputEvent.request(this.owner, evt);

                    player.closeInventory();
                }
            }
        }
        else {
            invEvt.setCancelled(true);
        }
    }

    @Override
    public Inventory getInventory() {
        this.setUp();
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
        this.inventory.clear();
        int rowsLvlSize = 1 + shopObject.getShopConfig().getShopTier();

        Map<Integer, ItemStack> ownerShopItems = getOwnerContents(owner);
        ownerShopItems.putAll(getShopMenuItems(owner, rowsLvlSize));

        for(Map.Entry<Integer, ItemStack> entry : ownerShopItems.entrySet()) {
            this.inventory.setItem(entry.getKey(), entry.getValue());
        }

        for(int i = 1; i < 4; i++) {
            this.inventory.setItem((rowsLvlSize*9-6+i), (ownerShopItems.get(rowsLvlSize*9-6)));
        }
    }

    /**
     * Processes the action done by the {@code UUID} player in {@code UUID} owner's shop,
     * {@code UUID} player can be the {@code UUID} owner, themself, if so remove the item.
     * If not, then attempt to proceed to purchase the item for x price.
     */
    public void process(UUID shopOwner, UUID forPlayer, int slot) {
        Player player = Bukkit.getPlayer(forPlayer);
        PlayerConfig pConfig = PlayerConfig.getConfig(shopOwner);
        if (pConfig.contains("player.contents") && (pConfig.getConfigurationSection("player.contents").getKeys(false).size() > 0)) {
            Set<String> keys = pConfig.getConfigurationSection("player.contents").getKeys(false);
            for (String key : keys) {
                int configSlot = pConfig.getInt("player.contents." + key + ".slot");
                if (configSlot == slot) {
                    ItemStack itemStack = pConfig.getItemStack("player.contents." + key + ".itemstack");

                    if(!shopOwner.equals(forPlayer)) {
                        pConfig.set("player.shopHistory."+key, this.inventory.getItem(slot));
                    }
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
    private static Map<Integer, ItemStack> getOwnerContents(UUID player) {
        Map<Integer, ItemStack> ownerItems = new HashMap<>();
        PlayerConfig pConfig = PlayerConfig.getConfig(player);
        ConfigurationSection cfg = pConfig.getConfigurationSection("player.contents");
        if(pConfig.contains("player.contents")) {
            Set<String> keys = cfg.getKeys(false);
            for (String key : keys) {
                ItemStack itemStack = pConfig.getItemStack("player.contents." + key + ".itemstack");
                int slot = pConfig.getInt("player.contents." + key + ".slot");
                int price = pConfig.getInt("player.contents." + key + ".price");

                // Display price tag
                ItemMeta itemMeta = itemStack.getItemMeta();
                List<String> lore;

                if(itemMeta.getLore() != null) { lore = itemMeta.getLore(); }
                else { lore = new ArrayList<>(); }
                lore.add(0, PlayerShops.colorize("&aPrice: &f" + price + "g &aeach"));

                itemMeta.setLore(lore);
                itemStack.setItemMeta(itemMeta);

                ownerItems.put(slot, itemStack);
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
    public static Map<Integer, ItemStack> getShopMenuItems(UUID owner, int shopTier) {
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
}
