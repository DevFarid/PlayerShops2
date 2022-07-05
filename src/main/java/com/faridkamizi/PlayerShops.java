package com.faridkamizi;

import com.faridkamizi.config.PlayerConfig;
import com.faridkamizi.events.InputProcessCompletion;
import com.faridkamizi.events.PreInputProcess;
import com.faridkamizi.inventory.guiListener.ShopListener;
import com.faridkamizi.system.UniversalShopStorage;
import com.faridkamizi.system.commands.ShopCMDS;
import com.faridkamizi.events.ShopEvent;
import com.faridkamizi.system.ShopObject;
import com.faridkamizi.util.AsyncParticles;
import com.faridkamizi.util.Hologram;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;


public class PlayerShops extends JavaPlugin {
    private final ShopCMDS shopCMDS = new ShopCMDS(this);

    @SuppressWarnings("InstantiationOfUtilityClass")
    private final AsyncParticles particles = new AsyncParticles(this);


    public static String colorize(String message)
    {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    @Override
    public void onEnable() {
        super.onEnable();

        this.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "PlayerShops enabled");
        this.getServer().getPluginManager().registerEvents(new Hologram(), this);
        this.getServer().getPluginManager().registerEvents(new ShopEvent(), this);
        this.getServer().getPluginManager().registerEvents(new ShopListener(), this);
        this.getServer().getPluginManager().registerEvents(new PreInputProcess(), this);
        this.getServer().getPluginManager().registerEvents(new InputProcessCompletion(), this);

        this.getCommand("banknote").setExecutor(shopCMDS);
        this.getCommand("test").setExecutor(shopCMDS);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        PlayerConfig.removeConfigs();
        Hologram.removeAll();
        AsyncParticles.stopAllTasks();
        UniversalShopStorage.closeAllShops();
        this.getServer().getConsoleSender().sendMessage(ChatColor.RED + "PlayerShops disabled");
    }

}
