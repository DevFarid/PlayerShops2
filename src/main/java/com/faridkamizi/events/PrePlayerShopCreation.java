package com.faridkamizi.events;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PrePlayerShopCreation extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final Location loc;

    public PrePlayerShopCreation(Player p, Location sloc) {
        this.player = p;
        this.loc = sloc;
    }

    public PrePlayerShopCreation() {
        this.player = null;
        this.loc = null;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public Player getPlayer() {
        return this.player;
    }

    public Location getLocation() {
        return loc;
    }

    @Override
    public String getEventName() { return "SHOP NAME"; }
}