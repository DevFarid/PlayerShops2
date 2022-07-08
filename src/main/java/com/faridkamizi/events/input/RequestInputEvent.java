package com.faridkamizi.events.input;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RequestInputEvent implements Listener {

    private final static Map<UUID, RequestEvent> requestPlayers = new HashMap<>();
    private static final Plugin mainPlugin = Bukkit.getServer().getPluginManager().getPlugin("PlayerShops");


    public static void request(UUID uuid, RequestEvent evt) {
        if(!requestPlayers.containsKey(uuid)) {
            requestPlayers.put(uuid, evt);
            Player player = Bukkit.getPlayer(uuid);
            player.playSound(player.getLocation(), Sound.BLOCK_WOODEN_BUTTON_CLICK_ON, 2F, 1F);
        }
    }

    @EventHandler
    public void call(AsyncPlayerChatEvent chatInputEvt) {
        Player player = chatInputEvt.getPlayer();
        if(requestPlayers.containsKey(player.getUniqueId())) {
            chatInputEvt.setCancelled(true);
            String input = chatInputEvt.getMessage();
            RequestEvent associated = requestPlayers.get(player.getUniqueId());

            ProcessInputEvent fireEvt = new ProcessInputEvent(player, input, associated);
            Bukkit.getScheduler().runTask(mainPlugin, ()-> { Bukkit.getPluginManager().callEvent(fireEvt); });

            requestPlayers.remove(player.getUniqueId());
        }
    }

}
