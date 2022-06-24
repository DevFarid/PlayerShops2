package com.faridkamizi.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Hologram implements Listener
{

    private final static Map<UUID, Location> armorStandDirectory = new HashMap<>();

    public static void createHolo(String name, Location location) {

        ArmorStand as = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND); //Spawn the ArmorStand

        as.setGravity(false); //Make sure it doesn't fall
        as.setCanPickupItems(false); //I'm not sure what happens if you leave this as it is, but you might as well disable it
        as.setCustomName(name); //Set this to the text you want
        as.setCustomNameVisible(true); //This makes the text appear no matter if your looking at the entity or not
        as.setVisible(false); //Makes the ArmorStand invisible

        armorStandDirectory.put(as.getUniqueId(), location);
    }

    public static void deleteHolo(Location location) {
        for(Map.Entry<UUID, Location> asEntry : armorStandDirectory.entrySet()) {
            if(asEntry.getValue().equals(location)) {
                Bukkit.getEntity(asEntry.getKey()).remove();
                armorStandDirectory.remove(asEntry.getKey());
                break;
            }
        }
    }

    public static void removeAll() {
        for(Map.Entry<UUID, Location> asEntry : armorStandDirectory.entrySet()) {
            Bukkit.getEntity(asEntry.getKey()).remove();
        }
        armorStandDirectory.clear();
    }

    /**
     * Armor stand interaction cancellation.
     *   Should cancel interaction events with custom invisible spawn armor stand for displaying texts.
     * @param e
     *          the event which is taking place at manipulating the armor stand by a player.
     */
    @EventHandler
    public void manipulate(PlayerArmorStandManipulateEvent e)
    {
        if(!e.getRightClicked().isVisible())
        {
            e.setCancelled(true);
        }
    }
}
