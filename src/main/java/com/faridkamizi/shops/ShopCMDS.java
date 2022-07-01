package com.faridkamizi.shops;

import com.faridkamizi.PlayerShops;
import com.faridkamizi.config.PlayerConfig;
import com.faridkamizi.inventory.gui.ShopInventory;
import com.faridkamizi.util.AsyncParticles;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;


public class ShopCMDS implements CommandExecutor {

    private final Plugin main;

    public ShopCMDS(Plugin playerShops) {
        this.main = playerShops;
    }

    /**
     * Executes the given command, returning its success.
     * <br>
     * If false is returned, then the "usage" plugin.yml entry for this command
     * (if defined) will be sent to the player.
     *
     * @param sender  Source of the command
     * @param command Command which was executed
     * @param label   Alias of the command which was used
     * @param args    Passed command arguments
     * @return true if a valid command, otherwise false
     */
    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        if(sender instanceof Player player) {
            Bukkit.getServer().getConsoleSender().sendMessage("command = " + command.getDescription());
            if(command.getName().equals("banknote")) {
                boolean isDigit = true;
                for (int i = 0; i < args[0].length(); i++) {
                    if(!Character.isDigit(args[0].charAt(i))) {
                        isDigit = false;
                    }
                }
                if(isDigit) {
                    ItemStack bankNote = ShopInventory.createGuiItem(Material.PAPER, PlayerShops.colorize("&aGem Note"),
                            PlayerShops.colorize("&f&lValue: &f" + args[0] + " Gems"),
                            PlayerShops.colorize("&7Exchange at any bank for GEM(s)"));

                    player.getInventory().addItem(bankNote);
                }
            }
            return true;
        }
        return false;
    }
}
