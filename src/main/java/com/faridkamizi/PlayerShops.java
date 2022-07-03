package com.faridkamizi;

import com.faridkamizi.config.PlayerConfig;
import com.faridkamizi.events.InputProcessCompletion;
import com.faridkamizi.events.PreInputProcess;
import com.faridkamizi.inventory.guiListener.ShopListener;
import com.faridkamizi.shops.ShopCMDS;
import com.faridkamizi.shops.ShopEvent;
import com.faridkamizi.shops.ShopObject;
import com.faridkamizi.shops.enhanced.EnhancedShopObject;
import com.faridkamizi.util.AsyncParticles;
import com.faridkamizi.util.Hologram;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.units.qual.A;


public class PlayerShops extends JavaPlugin {
    private final ShopCMDS shopCMDS = new ShopCMDS(this);
    private final AsyncParticles particles = new AsyncParticles(this);
    private final EnhancedShopObject shopObjects = new EnhancedShopObject();

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
        ShopObject.closeAllShop();
        PlayerConfig.removeConfigs();
        Hologram.removeAll();
        AsyncParticles.stopAllTasks();
        shopObjects.closeAllShops();
        this.getServer().getConsoleSender().sendMessage(ChatColor.RED + "PlayerShops disabled");
    }

}