package com.faridkamizi.config;

import com.faridkamizi.PlayerShops;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class PlayerConfig extends YamlConfiguration {
//    private static final Cache<UUID, PlayerConfig> configs = CacheBuilder.newBuilder().maximumSize(1000).expireAfterWrite(15, TimeUnit.SECONDS).build();
    private static final Map<UUID, PlayerConfig> configs = new HashMap<>();


    public static PlayerConfig getConfig(Player player) {
        return getConfig(player.getUniqueId());
    }

    public static PlayerConfig getConfig(OfflinePlayer player) {
        UUID uuid = player.getUniqueId();
        if(!new File(com.faridkamizi.PlayerShops.getPlugin(PlayerShops.class).getDataFolder(), "users" + File.separator + uuid.toString() + ".yml").exists()) {
            return null;
        }
        return getConfig(uuid);
    }

    public static PlayerConfig getConfig(UUID uuid) {
        synchronized (configs) {
            if(configs.containsKey(uuid)) {
                return configs.get(uuid);
            }
            PlayerConfig config = new PlayerConfig(uuid);
            configs.put(uuid, config);
            return config;
        }
    }

    public static void removeConfigs() {
        configs.clear();
    }
    private File file = null;
    private final Object saveLock = new Object();
    private final UUID uuid;

    public PlayerConfig(UUID uuid) {
        super();
        file = new File(com.faridkamizi.PlayerShops.getPlugin(com.faridkamizi.PlayerShops.class).getDataFolder(), "users" + File.separator + uuid.toString() + ".yml");
        this.uuid = uuid;
        reload();
    }

    @SuppressWarnings("unused")
    private PlayerConfig() {
        uuid = null;
    }

    private void reload() {
        synchronized (saveLock) {
            try {
                load(file);
            } catch (Exception ignore) {
            }
        }
    }

    public void save() {
        synchronized (saveLock) {
            try {
                save(file);
            } catch (Exception ignore) {
            }
        }
    }

    public void discard() {
        discard(false);
    }

    public void discard(boolean save) {
        if (save) {
            save();
        }
        synchronized (configs) {
            configs.remove(uuid);
        }
    }
}
