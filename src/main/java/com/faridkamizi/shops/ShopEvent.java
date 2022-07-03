package com.faridkamizi.shops;

import com.faridkamizi.PlayerShops;
import com.faridkamizi.currency.Currency;
import com.faridkamizi.events.PlayerShopViewEvent;
import com.faridkamizi.events.PreInputProcess;
import com.faridkamizi.events.PrePlayerShopCreation;
import com.faridkamizi.inventory.gui.ShopInventory;
import com.faridkamizi.inventory.guiListener.ShopListener;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.io.Serializable;
import java.util.UUID;

public class ShopEvent implements Listener, Serializable {

    /**
     * Manage shop action handling such as creating a shop or retrieve the shop from where it was clicked.
     * @param e
     *          the event that is from when a player is interacting with the world.
     */
    @EventHandler
    public static void onPlayerInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if(p.isSneaking() && e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            ItemStack pJournal = ShopListener.getBook();
            if(p.getInventory().getItemInMainHand().equals(pJournal)) {
                e.setCancelled(true);

                PrePlayerShopCreation shopCreationEvent = new PrePlayerShopCreation(p, e.getClickedBlock().getLocation());
                Bukkit.getServer().getPluginManager().callEvent(shopCreationEvent);

            } else if(e.getClickedBlock().getType().equals(Material.CHEST)) {
                e.setCancelled(true);
                if(ShopObject.getOwner(e.getClickedBlock().getLocation()).equals(e.getPlayer().getUniqueId())) {
                    if(ShopObject.shopOpen(e.getPlayer().getUniqueId())) {
                        p.sendMessage(PlayerShops.colorize("&cYou must close your shop to upgrade it."));
                    } else {
                        int currentRows = ShopObject.getInventoryRows((e.getPlayer().getUniqueId()));
                        int upgradeCost = 200 * currentRows;
                        if(currentRows < 5) {
                            if(Currency.calculateBalance(p) >= upgradeCost) {
                                Currency.remove(p, upgradeCost);
                                ShopObject.upgradeShop(e.getPlayer().getUniqueId(), currentRows+1);
                            } else {
                                p.sendMessage("You required " + upgradeCost + " gems to upgrade.");
                            }
                        } else {
                            p.sendMessage(PlayerShops.colorize("&cMax shop level reached."));
                        }
                    }
                }
            }
        } else if(e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock().getType() == Material.CHEST) {
            if(ShopObject.contains(e.getClickedBlock().getLocation())) {
                e.setCancelled(true);

                PlayerShopViewEvent shopViewEvent = new PlayerShopViewEvent(p, e.getClickedBlock().getLocation());
                Bukkit.getServer().getPluginManager().callEvent(shopViewEvent);
            }
        }
    }

    /**
     * Prepares to create a shop for a player
     * @param e
     *          the custom even for when a player is about to create a shop.
     */
    @EventHandler
    public void shopCreationEvent(PrePlayerShopCreation e) {
        Player p = e.getPlayer();

        p.sendMessage(PlayerShops.colorize("&ePlease enter a &lSHOP NAME&r&e. [max 16 characters]"));
        p.playSound(p.getLocation(), Sound.BLOCK_WOODEN_BUTTON_CLICK_ON, 2F, 1F);
        PreInputProcess.requestPlayer(p, e);
    }

    /**
     * Will open an inventory revealing the contents of the clicked chest that is treated as a shop.
     * @param e
     *         the custom event for when a player clicks a chest that is a verified shop location.
     */
    @EventHandler
    public void shopViewEvent(PlayerShopViewEvent e) {
        Player p = e.getPlayer();
        UUID shopOwnerUUID = ShopObject.getOwner(e.getLocation());

        if(ShopObject.shopOpen(shopOwnerUUID) || p.getUniqueId().equals(shopOwnerUUID) || p.isOp()) {
            String name = Bukkit.getOfflinePlayer(shopOwnerUUID).getName()+ "'s Shop";
            int size = ((1 + ShopObject.getInventoryRows(shopOwnerUUID)) * 9);

            ShopInventory gui = new ShopInventory(shopOwnerUUID, name, size);
            ShopObject.addView(shopOwnerUUID, e.getPlayer().getUniqueId());

            p.openInventory(gui.getInventory());
            p.playSound(e.getPlayer().getLocation(), Sound.BLOCK_CHEST_OPEN, 2.0F, 1.0F);
        } else {
            p.sendMessage(PlayerShops.colorize("&cThat shop is not open."));
        }
    }
}
