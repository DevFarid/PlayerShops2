package com.faridkamizi.util;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;

public class AsyncParticles {

    private static Plugin main;
    private static Map<Location, BukkitTask> currentTasks;

    public AsyncParticles(Plugin p) {
        main = p;
        currentTasks = new HashMap<>();
    }


    public static void spawnParticle(Location location) {
        if(!hasParticle(location)) {
            BukkitTask task = new BukkitRunnable() {
                final Location loc = location.clone();
                final World pWorld = loc.getWorld();

                @Override
                public void run() {
                    for (int i = 0; i < 360; i += 5) {
                        double radians = Math.toRadians(i);
                        double x = Math.cos(radians);
                        double z = Math.sin(radians);
                        loc.add(x, 0, z);
                        pWorld.spawnParticle(Particle.FLAME, loc, 0, x, 0, z, 0.2);
                        loc.subtract(x, 0, z);
                    }
                }
            }.runTaskTimerAsynchronously(main, 1L, 21L);
            currentTasks.put(location, task);
        } else {
            stopTask(location);
        }
    }

    public static boolean hasParticle(Location location) {
        return currentTasks.containsKey(location);
    }

    public static void stopTask(Location particleLocation) {
        BukkitTask task = currentTasks.get(particleLocation);
        if(task != null) {
            task.cancel();
        }
        currentTasks.remove(particleLocation);
    }

    public static void stopAllTasks() {
        for (Map.Entry<Location, BukkitTask> entry: currentTasks.entrySet()) {
            entry.getValue().cancel();
        }
        currentTasks.clear();
    }

}
