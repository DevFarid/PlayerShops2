package com.faridkamizi.events;

import com.faridkamizi.PlayerShops;
import com.faridkamizi.currency.Currency;
import com.faridkamizi.inventory.guiListener.ShopListener;
import com.faridkamizi.system.ShopObject;
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

public class ShopEvent implements Listener, Serializable {

    /**
     * Manage shop action handling such as creating a shop or retrieve the shop from where it was clicked.
     * @param e
     *          the event that is from when a player is interacting with the world.
     */
    @EventHandler
    public static void onPlayerInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        if(player.isSneaking() && e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            ItemStack pJournal = ShopListener.getBook();
            if(player.getInventory().getItemInMainHand().equals(pJournal)) {
                e.setCancelled(true);

                PrePlayerShopCreation shopCreationEvent = new PrePlayerShopCreation(player, e.getClickedBlock().getLocation());

                RequestEvent evt = new RequestEvent(player.getUniqueId(), shopCreationEvent, (Object) null);
                RequestInputEvent.request(player.getUniqueId(), evt);


            } else if(e.getClickedBlock().getType().equals(Material.CHEST)) {
                ShopObject shopObject = ShopObject.getShop(e.getClickedBlock().getLocation());
                if(shopObject != null && shopObject.getShopOwnerID().equals(player.getUniqueId())) {
                    e.setCancelled(true);

                    if(shopObject.getShopConfig().getStatus()) {
                        player.sendMessage(PlayerShops.colorize("&cYou must close your shop to upgrade it."));
                    } else {
                        PlayerShopUpgrade shopUpgradeEvent = new PlayerShopUpgrade(player, shopObject);
                        Bukkit.getServer().getPluginManager().callEvent(shopUpgradeEvent);
                    }
                }
            }
        } else if(e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock().getType() == Material.CHEST) {
            ShopObject shopObject = ShopObject.getShop(e.getClickedBlock().getLocation());
            if(shopObject != null) {
                e.setCancelled(true);

                PlayerShopViewEvent shopViewEvent = new PlayerShopViewEvent(player, shopObject);
                Bukkit.getServer().getPluginManager().callEvent(shopViewEvent);

            }
        }
    }

//    /**
//     * Prepares to create a shop for a player
//     * @param e
//     *          the custom even for when a player is about to create a shop.
//     */
//    @EventHandler
//    public void shopCreationEvent(PrePlayerShopCreation e) {
//        Player p = e.getPlayer();
//
//        p.sendMessage(PlayerShops.colorize("&ePlease enter a &lSHOP NAME&r&e. [max 16 characters]"));
//        p.playSound(p.getLocation(), Sound.BLOCK_WOODEN_BUTTON_CLICK_ON, 2F, 1F);
//        PreInputProcess.requestPlayer(p, e);
//    }

    /**
     * Will open an inventory revealing the contents of the clicked chest that is treated as a shop.
     * @param evt
     *         the custom event for when a player clicks a chest that is a verified shop location.
     */
    @EventHandler
    public void shopViewEvent(PlayerShopViewEvent evt) {
        Player viewer = evt.getViewer();
        if(evt.getRequestedShop().getShopConfig().getOwnerConfig().getBoolean("player.shopOpen") || evt.getRequestedShop().getShopOwnerID().equals(viewer.getUniqueId())) {
            viewer.openInventory(evt.getRequestedShop().getShopInventory().getInventory());
            viewer.playSound(viewer.getLocation(), Sound.BLOCK_CHEST_OPEN, 2.0F, 1.0F);
        } else {
            viewer.sendMessage(PlayerShops.colorize("&cThat shop is not open."));
        }
    }

    @EventHandler
    public void shopUpgradeEvent(PlayerShopUpgrade evt) throws InstantiationException, IllegalAccessException {
        int currentRows = evt.getRequestedShop().getShopConfig().getShopTier();
        int upgradeCost = 200 * currentRows;
        if(currentRows < 5) {
            if(Currency.calculateBalance(evt.getOwner()) >= upgradeCost) {
                Currency.remove(evt.getOwner(), upgradeCost);
                evt.getRequestedShop().getShopConfig().upgrade();
            } else {
                evt.getOwner().sendMessage("You required " + upgradeCost + " gems to upgrade.");
            }
        } else {
            evt.getOwner().sendMessage(PlayerShops.colorize("&cMax shop level reached."));
        }
    }
}
