package com.faridkamizi;

import com.faridkamizi.config.PlayerConfig;
import com.faridkamizi.events.input.ProcessInputEvent;
import com.faridkamizi.events.input.RequestInputEvent;
import com.faridkamizi.inventory.guiListener.ShopListener;
import com.faridkamizi.system.UniversalShopStorage;
import com.faridkamizi.system.commands.ShopCMDS;
import com.faridkamizi.events.ShopEventRedirectors;
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
        this.getServer().getPluginManager().registerEvents(new ShopEventRedirectors(), this);
        this.getServer().getPluginManager().registerEvents(new ShopListener(), this);

        this.getServer().getPluginManager().registerEvents(new RequestInputEvent(), this);
        this.getServer().getPluginManager().registerEvents(new ProcessInputEvent(), this);

        this.getCommand("banknote").setExecutor(shopCMDS);
        this.getCommand("test").setExecutor(shopCMDS);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        UniversalShopStorage.closeAllShops();
        PlayerConfig.removeConfigs();
        Hologram.removeAll();
        AsyncParticles.stopAllTasks();
        this.getServer().getConsoleSender().sendMessage(ChatColor.RED + "PlayerShops disabled");
    }

}
