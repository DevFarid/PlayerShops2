package com.faridkamizi.currency;

import com.faridkamizi.PlayerShops;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class Currency
{

    public static int getTotalMoney(Player player) {
        int totalMoney = 0;
        ItemStack[] itemContents = player.getInventory().getContents();
        for (ItemStack itemContent : itemContents) {
            if (itemContent != null && itemContent.getType().equals(Material.PAPER)) {
                ItemMeta bankMeta = itemContent.getItemMeta();
                if (bankMeta.getDisplayName().equals(PlayerShops.colorize("&aGem Note"))) {
                    totalMoney += retrieveMoney(itemContent);
                }
            }
        }
        return totalMoney;
    }

    private static int retrieveMoney(ItemStack itemStack) {
        String[] splitLore = itemStack.getItemMeta().getLore().get(0).split(" ");
        int amount = 0;
        if(splitLore[1] != null) {
            amount = Integer.parseInt(ChatColor.stripColor(splitLore[1]));
        }
        return amount;
    }

    private static void modify(Player player, ItemStack itemStack, int toRemove) {
        int itemStackAmount = retrieveMoney(itemStack);
        if(itemStackAmount >= toRemove) {

            int deducted = itemStackAmount - toRemove;

            if(deducted == 0) {
                player.getInventory().remove(itemStack);
            } else {
                ItemMeta bankMeta = itemStack.getItemMeta();
                List<String> lore = bankMeta.getLore();

                lore.set(0, PlayerShops.colorize("&f&lValue: &f" + deducted + " Gems"));
                bankMeta.setLore(lore);
                itemStack.setItemMeta(bankMeta);
            }
        }
    }

    public static void removeMoney(Player fromPlayer, int amntToRemove) {
        int totalMoney = getTotalMoney(fromPlayer);
        if(totalMoney >= amntToRemove) {

            ItemStack[] itemContents = fromPlayer.getInventory().getContents();
            for (int i = 0; i < itemContents.length; i++) {
                if(itemContents[i] != null && itemContents[i].getType().equals(Material.PAPER)) {
                    ItemMeta bankMeta = itemContents[i].getItemMeta();
                    if(bankMeta.getDisplayName().equals(PlayerShops.colorize("&aGem Note"))) {
                        modify(fromPlayer, itemContents[i], amntToRemove);
                    }
                }
            }
        } else {
            fromPlayer.sendMessage(PlayerShops.colorize("&cYou do not have enough money."));
        }
    }

}
