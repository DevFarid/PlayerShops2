package com.faridkamizi.inventory.gui;

import com.faridkamizi.PlayerShops;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class AbstractGUI {

    /**
     * KEY: A PLAYER'S UUID, VALUE: THIS
     * Represents an {@code AbstractGUI} class that belongs to {@code UUID} player's UUID.
     * If a player exists as a key, then it is said to be that the key (player) has a shop created.
     */
    private static final Map<UUID, AbstractGUI> ownerInventoryDirectory = new HashMap<>();

    /**
     *  KEY: A PLAYER'S UUD, VALUE: UUID OF SHOP OWNER
     *  This stores what a player is currently viewing that is a custom GUI.
     */
    private static final Map<UUID, UUID> openInventories = new HashMap<>();

    private final UUID owner;
    private final Inventory guiInventory;

    public AbstractGUI(UUID shopOwner, String invName, Integer invSize) {
        owner = shopOwner;
        guiInventory = Bukkit.createInventory(null, invSize, PlayerShops.colorize(invName));
        ownerInventoryDirectory.put(getShopUUID(), this);
    }

    public UUID getShopUUID() {
        return owner;
    }

    public Inventory getAbstractGUI() {
        return guiInventory;
    }

    public void open(Player p) {
        openInventories.put(p.getUniqueId(), getShopUUID());
        p.openInventory(guiInventory);

        Bukkit.broadcastMessage("AbstractGUI: " + AbstractGUI.getOpenInventories().toString());
    }

    public static Map<UUID, AbstractGUI> getShopOwnerInventories() {
        return ownerInventoryDirectory;
    }

    public static Map<UUID, UUID> getOpenInventories() {
        return openInventories;
    }

    public void setItem(int slot, ItemStack itemStack) {
        guiInventory.setItem(slot, itemStack);
    }

    public void setItem(ItemStack itemStack) {
        guiInventory.addItem(itemStack);
    }

    public void delete() {
        for (Player p : Bukkit.getOnlinePlayers()){
            UUID u = getOpenInventories().get(p.getUniqueId());
            if (u.equals(getShopUUID())){
                p.closeInventory();
            }
            openInventories.remove(u);
            ownerInventoryDirectory.remove(u);
        }
    }
}
