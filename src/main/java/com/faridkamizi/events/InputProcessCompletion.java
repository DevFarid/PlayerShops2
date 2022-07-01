package com.faridkamizi.events;

import com.faridkamizi.PlayerShops;
import com.faridkamizi.shops.ShopObject;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;


public class InputProcessCompletion implements Listener {

    /**
     * Handles input completion. When an input is completed, we need to know which event it was correlated with.
     * @param e
     *         the {@code PostInputProcess} event.
     */
    @EventHandler
    public void InputProcessComplete(PostInputProcess e) {
        PreInputProcess.BundledEvent correlated = e.getBundledEvent();
        // Shop title handling
        if(correlated.correlatedEvent instanceof PrePlayerShopCreation) {
            Location shopLocation = ((PrePlayerShopCreation) correlated.correlatedEvent).getLocation();
            UUID shopOwner = ((PrePlayerShopCreation) correlated.correlatedEvent).getPlayer().getUniqueId();
            if(isValidTitle(e.getInput())) {
                ShopObject.add(shopLocation, shopOwner, e.getInput());
            } else {
                e.getPlayer().sendMessage(PlayerShops.colorize("&cA shop name may only contain a letter or a digit with max limit of 16 characters."));
            }
        }
        // Shop pricing handling
        else if(correlated.extraInformation != null && (correlated.correlatedEvent instanceof InventoryClickEvent)) {
            ItemStack itemStack = (ItemStack) correlated.extraInformation;
            if(isValidPrice(e.getInput())) {
                ShopObject.addItemToShop(correlated.player, itemStack, Integer.parseInt(e.getInput()));
            } else {
                e.getPlayer().getInventory().addItem(itemStack);
                e.getPlayer().sendMessage(PlayerShops.colorize("&c&c'" + e.getInput() + "' is not a valid number.\n&cItem Pricing - &lCANCELLED"));
            }
        } else if(correlated.extraInformation == null && (correlated.correlatedEvent instanceof InventoryClickEvent)) {
            if(isValidTitle(e.getInput())) {
                ShopObject.renameShop(e.getPlayer().getUniqueId(), e.getInput());
                e.getPlayer().sendMessage(PlayerShops.colorize("&eUpdated shop name."));
            }
        }
        // Unflag the player for input, regardless of success/fail of the correct input type.
        PreInputProcess.removePlayer(e.getPlayer());
    }

    /**
     * A utility function that checks whether a string message is a valid title, by scanning the characters within the string.
     * @param message
     *              the message to be inspected.
     * @return
     *          a boolean reflecting whether this string is valid or not.
     */
    private boolean isValidTitle(String message) {
        boolean isBelowLimit = message.length() < 16;
        boolean isAlphaNumeric = true;

        for (int i = 0; i < message.length(); i++) {
            if( !( Character.isLetter(message.charAt(i)) || Character.isDigit(message.charAt(i)) || Character.isSpaceChar(message.charAt(i)) ) ) {
                isAlphaNumeric = false;
            }
        }
        return isBelowLimit && isAlphaNumeric;
    }

    /**
     * A utility function that checks whether a string message is a valid shop price.
     * @param message
     *               the string to inspect.
     * @return
     *          a boolean reflecting whether this string is all digit character only.
     */
    private boolean isValidPrice(String message) {
        boolean isDigit = true;

        if(message.length() > 8) {
            isDigit = false;
        } else {
            for (int i = 0; i < message.length(); i++) {
                if (!Character.isDigit(message.charAt(i))) {
                    isDigit = false;
                }
            }
        }

        boolean isInRange = false;
        if(isDigit) {
            int digit = Integer.parseInt(message);
            isInRange = digit > 0 && digit <= (Integer.MAX_VALUE - 1);
        }

        return (isDigit && isInRange);
    }
}
