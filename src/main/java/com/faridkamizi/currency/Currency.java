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
     * Removes a certain X gems from player's inventory (if applicable)
     * @param player
     *              the player whose balance needs modification
     * @param amountToRemove
     *              the amount to remove from player's balance
     */
    public static boolean removeMoney(Player player, int amountToRemove) {
        boolean operationSucess = false;
        Map<ItemStack, Integer> totalBalance = getItemAndMoney(player);
        int totalPlayerMoney = calculateTotalMoney(totalBalance);
        if(totalPlayerMoney >= amountToRemove) {
            modify(player, totalBalance, amountToRemove);
            operationSucess = true;
        }
        return operationSucess;
    }

    /**
     * Maps {@code ItemStack} items that are treated as banknotes to their appropriate value they're worth.
     * @param player
     *              the player whose inventory must be searched and mapped.
     * @return
     *          a Map that reflects each ItemStack to its own value.
     */
    public static Map<ItemStack, Integer> getItemAndMoney(Player player) {
        HashMap<ItemStack, Integer> invMoneyMapping = new HashMap<>();
        ItemStack[] playerContents = player.getInventory().getContents();
        for(ItemStack itemStack : playerContents) {
            if(itemStack != null && itemStack.getType().equals(Material.PAPER)) {
                ItemMeta banknoteMeta = itemStack.getItemMeta();
                if(banknoteMeta.getDisplayName().equals(PlayerShops.colorize("&aGem Note"))) {
                    int value = retrieveValue(itemStack);
                    invMoneyMapping.put(itemStack, value);
                }
            }
        }
        return invMoneyMapping;
    }

    /**
     * Calculates the total money presented in a Mapping that maps the banknotes to their value.
     * @param invToMoneyMap
     *                  the Map that points each ItemStack to its value.
     * @return
     *          an integer reflecting the total value for all bank notes.
     */
    public static int calculateTotalMoney(Map<ItemStack, Integer> invToMoneyMap) {
        int total = 0;
        for(Map.Entry<ItemStack, Integer> invToMoneyMapEntry : invToMoneyMap.entrySet()) {
            total += invToMoneyMapEntry.getValue();
        }

        return total;
    }

    /**
     *
     * @param player
     * @param totalBalance
     * @param amountToRemove
     */
    public static void modify(Player player, Map<ItemStack, Integer> totalBalance, int amountToRemove) {
        Set<ItemStack> bulkToRemove = new HashSet<>();
        int bulkBankNoteValue = 0;
        int difference = 0;
        for (Map.Entry<ItemStack, Integer> moneyEntry: totalBalance.entrySet()) {
            if(moneyEntry.getValue() >= amountToRemove) {
                difference = moneyEntry.getValue() - amountToRemove;
                player.getInventory().remove(moneyEntry.getKey());
                break;
            } else if(bulkBankNoteValue >= amountToRemove) {
                for (ItemStack i: bulkToRemove) {
                    difference = bulkBankNoteValue - amountToRemove;
                    player.getInventory().remove(i);
                    totalBalance.remove(i);
                }
            } else {
                bulkBankNoteValue += moneyEntry.getValue();
                bulkToRemove.add(moneyEntry.getKey());
            }
        }
        if(difference > 0) {
            ItemStack bankNote = ShopInventory.createGuiItem(Material.PAPER, PlayerShops.colorize("&aGem Note"),
                    PlayerShops.colorize("&f&lValue: &f" + difference + " Gems"),
                    PlayerShops.colorize("&7Exchange at any bank for GEM(s)"));

            player.getInventory().addItem(bankNote);
        }
    }


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



}
