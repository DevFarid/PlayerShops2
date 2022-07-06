package com.faridkamizi.events;

import com.faridkamizi.PlayerShops;
import com.faridkamizi.currency.Currency;
import com.faridkamizi.system.ShopObject;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class ProcessInputEvent extends Event implements Listener {

    public enum InputType {
        IntegerType, StringType, CUSTOM
    }

    /*
    --------------------------------------------------------------------------------------------------------------------
                                                        EVENT
    --------------------------------------------------------------------------------------------------------------------
     */
    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final RequestEvent requestEvent;
    private final String input;

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public ProcessInputEvent() {
        this.input = null;
        this.player = null;
        this.requestEvent = null;
    }

    public ProcessInputEvent(Player player, String input, RequestEvent associated) {
        this.player = player;
        this.input = input;
        this.requestEvent = associated;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public Player getPlayer() { return this.player; }

    public String getInput() { return this.input; }

    public RequestEvent getAssociatedEvent() { return this.requestEvent; }

    /*
    --------------------------------------------------------------------------------------------------------------------
                                                        LISTENER EVENT
    --------------------------------------------------------------------------------------------------------------------
     */
    @EventHandler
    public static void ProcessComplete(ProcessInputEvent evt) {
        RequestEvent reqEvt = evt.getAssociatedEvent();
        Player player = evt.getPlayer();
        if(reqEvt.coEvent instanceof PrePlayerShopCreation) {
            if(isValidTitle(evt.getInput())) {
                Location shopLocation = ((PrePlayerShopCreation) reqEvt.coEvent).getLocation().clone().add(0,1,0);
                Location shopLocation2 = shopLocation.clone().add(1, 0, 0);

                Location hologramTitle = shopLocation.clone();
                hologramTitle.add(1, -0.9, 0.5);

                Location hologramView = shopLocation.clone();
                hologramView.add(1, -1.2, 0.5);

                Location particleLoc = shopLocation.clone().add(1, 1, 0.5);

                Location[] locs = {shopLocation, shopLocation2, hologramTitle, hologramView, particleLoc};
                ShopObject shopObject = new ShopObject(player.getUniqueId(), evt.getInput(), locs);
            } else {
                player.sendMessage(PlayerShops.colorize("&cA shop name may only contain a letter or a digit with max limit of 16 characters."));
            }
        } else if(reqEvt.coEvent instanceof InventoryClickEvent) {
            if((reqEvt.objects != null) && (reqEvt.objects.length > 0)) {
                Object[] data = reqEvt.objects;

                if(data[0].equals(InputType.CUSTOM)) {
                    boolean repriceEvt = (boolean) data[1];
                    int slot = (int) data[2];

                    if (repriceEvt) {
                        if (isValidPrice(evt.getInput())) {
                            ShopObject.shopLocationDirectory.get(player.getUniqueId()).getShopConfig().setPrice(slot, Integer.parseInt(evt.getInput()));
                        } else {
                            player.sendMessage(PlayerShops.colorize("&c&c'" + evt.getInput() + "' is not a valid number.\n&cItem Pricing - &lCANCELLED"));
                        }
                    } else {
                        ItemStack itemStack = (ItemStack) data[3];

                        if (isValidPrice(evt.getInput())) {
                            ShopObject.shopLocationDirectory.get(player.getUniqueId()).getShopConfig().addItem(itemStack, Integer.parseInt(evt.getInput()), slot);
                        } else {
                            player.sendMessage(PlayerShops.colorize("&c&c'" + evt.getInput() + "' is not a valid number.\n&cItem Pricing - &lCANCELLED"));
                        }
                    }
                } else if(data[0].equals(InputType.IntegerType)) {
                    int slot = (int) data[1];
                    UUID owner = (UUID) data[2];
                    int max = ShopObject.shopLocationDirectory.get(owner).getShopConfig().getAmount(slot);
                    boolean isValidAmount = isValidAmount(evt.getInput(), max);

                    ItemStack shopItem = ((InventoryClickEvent) reqEvt.coEvent).getCurrentItem().clone();

                    if(isValidAmount) {
                        int requestAmount = Integer.parseUnsignedInt(evt.getInput());
                        int singleItemPrice = ShopObject.shopLocationDirectory.get(owner).getShopConfig().getItemPrice(slot, true);
                        int totalItemPrice = requestAmount * singleItemPrice;
                        if (Currency.calculateBalance(player) >= totalItemPrice) {
                            Currency.remove(player, totalItemPrice);
                            ShopObject.shopLocationDirectory.get(owner).getShopConfig().process(owner, player.getUniqueId(), slot, requestAmount);
                            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 2.0F, 1.0F);
                            player.sendMessage(PlayerShops.colorize("&aYou bough an item!"));

                            OfflinePlayer player1 = Bukkit.getOfflinePlayer(owner);
                            if (player1.isOnline()) {
                                player1.getPlayer().sendMessage(PlayerShops.colorize("&a" + ((InventoryClickEvent)reqEvt.coEvent).getWhoClicked().getName() + " bought " + shopItem.getType().name()));
                            }
                        } else {
                            player.sendMessage(PlayerShops.colorize("&cYou do not have enough gems."));
                            player.sendMessage(PlayerShops.colorize("&c&lCOST: &c" + totalItemPrice + "&lG"));
                        }

                    } else {
                        player.sendMessage(PlayerShops.colorize("&cNot a valid amount."));
                    }
                }

            } else {
                if(isValidTitle(evt.getInput())) {
                    ShopObject.shopLocationDirectory.get(player.getUniqueId()).getShopConfig().updateName(evt.getInput());
                } else {
                    player.sendMessage(PlayerShops.colorize("&cA shop name may only contain a letter or a digit with max limit of 16 characters."));
                }
            }
        }

    }

    /*
    --------------------------------------------------------------------------------------------------------------------
                                                        UTILITY FUNCTIONS
    --------------------------------------------------------------------------------------------------------------------
     */

    /**
     * A utility function that checks whether a string message is a valid title, by scanning the characters within the string.
     * @param message
     *              the message to be inspected.
     * @return
     *          a boolean reflecting whether this string is valid or not.
     */
    private static boolean isValidTitle(String message) {
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
    private static boolean isValidPrice(String message) {
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

    private static boolean isValidAmount(String message, int maxAmount) {
        boolean isValidInteger = isValidPrice(message);
        Integer integer = Integer.parseUnsignedInt(message);
        return ((isValidInteger) && ((integer <= maxAmount)));
    }
}
