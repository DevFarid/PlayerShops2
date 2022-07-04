package com.faridkamizi.events;

import com.faridkamizi.shops.enhanced.ShopObject;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerShopUpgrade extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Player shopOwner;
    private final ShopObject requestedShop;

    public PlayerShopUpgrade(Player player, ShopObject shopObject) {
        this.shopOwner = player;
        this.requestedShop = shopObject;
    }

    public static HandlerList getHandlerList() { return HANDLERS; }

    @Override
    public HandlerList getHandlers() { return HANDLERS; }

    public Player getOwner() { return this.shopOwner; }

    public ShopObject getRequestedShop() { return this.requestedShop; }
}
