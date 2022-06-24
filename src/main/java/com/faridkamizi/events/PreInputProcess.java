package com.faridkamizi.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PreInputProcess implements Listener {

    public static class BundledEvent {
        UUID player;
        Event correlatedEvent;
        Object extraInformation;
        BundledEvent(UUID p, Event e, Object extra) {
            this.player = p;
            this.correlatedEvent = e;
            this.extraInformation = extra;
        }
    }

    /**
     * {@code allRequestPlayers} is a HashMap<Player, String> where key represents a requested player, and the value for that key is used for storing the
     * input given by the player.
     */
    private static final Map<BundledEvent, String> allRequestedPlayers = new HashMap<>();
    private static final Plugin mainPlugin = Bukkit.getServer().getPluginManager().getPlugin("PlayerShops");

    /**
     * Request a player for chat input for a correlated event that may need that input back.
     * @param p
     *          the player to be examined for an input
     * @param e
     *          the event that is correlated with this even for a callback about the input.
     */
    public static void requestPlayer(Player p, Event e) {
        if(!containsPlayer(p)) {
            BundledEvent bundledEvent;
            if(e instanceof PrePlayerShopCreation event) {
                bundledEvent = new BundledEvent(p.getUniqueId(), e, event.getLocation());
                allRequestedPlayers.put(bundledEvent, "");
            } else if(e instanceof InventoryClickEvent) {
                if(((InventoryClickEvent) e).getCursor().getType().isAir()) {
                    bundledEvent = new BundledEvent(p.getUniqueId(), e, null);
                } else {
                    bundledEvent = new BundledEvent(p.getUniqueId(), e, ((InventoryClickEvent) e).getCursor());
                }
                allRequestedPlayers.put(bundledEvent, "");
            }
        }
    }

    /**
     * Retrieve the entry for the player that is bundled with a correlated event for an input callback.
     * @param p
     *          the player associated with an event.
     * @return
     *          the {@code BundledEvent} object that contains the information of an event associated with this input request.
     */
    public static Map.Entry<BundledEvent, String> getPlayerEntrySet(Player p) {
        for (Map.Entry<BundledEvent, String> requestPlayerEntrySet: allRequestedPlayers.entrySet()) {
            if (requestPlayerEntrySet.getKey().player.equals(p.getUniqueId())) {
                return requestPlayerEntrySet;
            }
        }
        return null;
    }

    /**
     * Will check to see if a player is already requested for an input.
     * @param p
     *          the player to see if already request for input.
     * @return
     *          a boolean that reflects whether the player is requested or not.
     */
    public static boolean containsPlayer(Player p) {
        return getPlayerEntrySet(p) != null;
    }

    /**
     * Removes a player from the requested directory.
     * @param p
     *          the player to remove from request input mode.
     */
    public static void removePlayer(Player p) {
        for (Map.Entry<BundledEvent, String> requestPlayerEntrySet: allRequestedPlayers.entrySet()) {
            if (requestPlayerEntrySet.getKey().player.equals(p.getUniqueId())) {
                allRequestedPlayers.remove(requestPlayerEntrySet.getKey());
            }
        }
    }

    /**
     * Handling the input from {@code AsyncPlayerChatEvent}.
     * @param e
     *          the event for when a chat is sent.
     */
    @EventHandler
    public void preInputRequestEvent(AsyncPlayerChatEvent e) {
        if(containsPlayer(e.getPlayer())) {
            e.setCancelled(true);
            Map.Entry<PreInputProcess.BundledEvent, String> playerEntrySet = PreInputProcess.getPlayerEntrySet(e.getPlayer());

            PostInputProcess postInputProcess = new PostInputProcess(e.getPlayer(), e.getMessage(), playerEntrySet.getKey());
            Bukkit.getScheduler().runTask(mainPlugin, () -> Bukkit.getPluginManager().callEvent(postInputProcess));
        }
    }

}
