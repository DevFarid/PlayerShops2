package com.faridkamizi.shops;

import com.faridkamizi.PlayerShops;
import com.faridkamizi.config.PlayerConfig;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;


public class ShopCMDS implements CommandExecutor {

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
            if(command.getName().equals("saveContents")) {
                PlayerConfig pConfig = PlayerConfig.getConfig(player);

                if(!pConfig.contains("player.UUID") && pConfig.contains("player.shopTier")) {
                    pConfig.set("player.UUID", player.getUniqueId().toString());
                    pConfig.set("player.shopTier", 1);
                }

                ItemStack[] items = player.getInventory().getContents();
                for (int i = 0; i < items.length; i++) {
                    pConfig.set("player.contents." + i, items[i]);
                }

                pConfig.save();
                pConfig.discard();

            } else if(command.getName().equals("banknote")) {
                boolean isDigit = true;
                for (int i = 0; i < args[0].length(); i++) {
                    if(!Character.isDigit(args[0].charAt(i))) {
                        isDigit = false;
                    }
                }
                if(isDigit) {
                    List<String> loreList = new ArrayList<>();
                    loreList.add(PlayerShops.colorize("&f&lValue: &f" + args[0] + " Gems"));
                    loreList.add(PlayerShops.colorize("&7Exchange at any bank for GEM(s)"));

                    ItemStack bankNote = new ItemStack(Material.PAPER);
                    ItemMeta bankMeta = bankNote.getItemMeta();

                    bankMeta.setDisplayName(PlayerShops.colorize("&aGem Note"));
                    bankMeta.setLore(loreList);

                    bankNote.setItemMeta(bankMeta);
                    player.getInventory().addItem(bankNote);
                }
            }
//            else if(command.getName().equals("loadContents")) {
//                PlayerConfig pConfig = PlayerConfig.getConfig(player);
//                if(pConfig.contains("player.contents") && pConfig.getConfigurationSection("player.contents").getKeys(false).size() > 0) {
//                    Set<String> keys = pConfig.getConfigurationSection("player.contents").getKeys(false);
//                    for(String key : keys) {
//                        ItemStack i = pConfig.getItemStack("player.contents."+key);
//                        player.getInventory().addItem(i);
//                    }
//                }
//                pConfig.discard();
//            }
            return true;
        }
        return false;
    }
}
