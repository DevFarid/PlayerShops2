package com.faridkamizi.events;

import com.faridkamizi.system.ShopObject;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerShopViewEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Player viewer;
    private final ShopObject requestedShop;


    public PlayerShopViewEvent(Player player, ShopObject shopObject) {
        this.viewer = player;
        this.requestedShop = shopObject;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public Player getViewer() {
        return this.viewer;
    }

    public ShopObject getRequestedShop() {
        return this.requestedShop;
    }
}