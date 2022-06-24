package com.faridkamizi.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PostInputProcess extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final String input;
    private final PreInputProcess.BundledEvent bundledEvent;

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public PostInputProcess(Player p, String message, PreInputProcess.BundledEvent event) {
        this.player = p;
        this.input = message;
        this.bundledEvent = event;
    }


    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public Player getPlayer() {
        return this.player;
    }

    public String getInput() {
        return this.input;
    }

    public PreInputProcess.BundledEvent getBundledEvent() {
        return this.bundledEvent;
    }

}