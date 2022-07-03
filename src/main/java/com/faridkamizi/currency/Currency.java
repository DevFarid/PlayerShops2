package com.faridkamizi.currency;

import com.faridkamizi.PlayerShops;
import com.faridkamizi.inventory.gui.ShopInventory;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class Currency
{

    /**
     * Given an {@code ItemStack}, this function will search the lore to see how much the bank note is worth.
     * @param itemStack
     *                  the item stack that is a banknote.
     * @return
     *          the integer reflecting how much this banknote is worth.
     */
    private static int retrieveValue(ItemStack itemStack) {
        String[] splitLore = itemStack.getItemMeta().getLore().get(0).split(" ");
        int amount = 0;
        if(splitLore[1] != null) {
            amount = Integer.parseInt(ChatColor.stripColor(splitLore[1]));
        }
        return amount;
    }

    /**
     * Maps a bank note to its value.
     * @param player
     *              the player who is in the interest of the search.
     * @return
     *          a Map reflecting the item as the key, and its worth as the value.
     */
    public static Map<ItemStack, Integer> mapBankNotesToValue(Player player) {

        HashMap<ItemStack, Integer> totalValue = new HashMap<>();
        int inventorySize = player.getInventory().getContents().length;
        ItemStack[] allItems = player.getInventory().getContents();


        for (int i = 0; i < inventorySize; i++) {
            if(allItems[i] != null) {
                ItemMeta itemStackMeta = allItems[i].getItemMeta();

                if (itemStackMeta.getDisplayName().equals(PlayerShops.colorize("&aGem Note"))) {
                    totalValue.put(allItems[i], retrieveValue(allItems[i]));
                }
            }
        }

        return totalValue;
    }

    /**
     * Calculates the the balance of bank note(s) in player's inventory.
     * @param player
     *              the player who is to undergo a search for balance.
     * @return
     *          an int reflecting the player's total balance contained in their inventory.
     */
    public static int calculateBalance(Player player) {
        Map<ItemStack, Integer> bankNoteWorth = mapBankNotesToValue(player);
        int balance = 0;

        for (Map.Entry<ItemStack, Integer> bankNoteEntry: bankNoteWorth.entrySet()) {
            balance += bankNoteEntry.getValue();
        }

        return balance;
    }

    /**
     *
     * @param itemStack
     *                  the {@code ItemStack} item that will be modified.
     * @param newBalance
     *                  the new balance for the update.
     */
    public static void modifyNote(ItemStack itemStack, int newBalance) {

        ItemMeta itemMeta = itemStack.getItemMeta();
        List<String> itemLore = itemMeta.getLore();

        itemLore.set(0, PlayerShops.colorize("&f&lValue: &f" + newBalance + " Gems"));
        itemMeta.setLore(itemLore);
        itemStack.setItemMeta(itemMeta);
    }


    public static void remove(Player player, int amountToRemove) {
        Map<ItemStack, Integer> mappedBankNoteWorth = mapBankNotesToValue(player);
        int totalBalance = calculateBalance(player);
        int balanceDifference = totalBalance - amountToRemove;

        if(balanceDifference > 0) {
            for(Map.Entry<ItemStack, Integer> entry : mappedBankNoteWorth.entrySet()) {
                player.getInventory().remove(entry.getKey());
            }
            ItemStack bankNote = ShopInventory.createGuiItem(Material.PAPER, PlayerShops.colorize("&aGem Note"),
                    PlayerShops.colorize("&f&lValue: &f" + balanceDifference + " Gems"),
                    PlayerShops.colorize("&7Exchange at any bank for GEM(s)"));
            player.getInventory().addItem(bankNote);
        } else if(balanceDifference == 0) {
            for(Map.Entry<ItemStack, Integer> entry : mappedBankNoteWorth.entrySet()) {
                player.getInventory().remove(entry.getKey());
            }
        } else {
        }
    }
}
