package com.faridkamizi.events.input;

import com.faridkamizi.PlayerShops;
import com.faridkamizi.currency.Currency;
import com.faridkamizi.events.PlayerShopCreationEvent;
import com.faridkamizi.inventory.gui.ShopInventory;
import com.faridkamizi.system.ShopObject;
import com.faridkamizi.system.UniversalShopStorage;
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

        if(reqEvt.inputType == Input.InputType.StringType) {

            if(reqEvt.shopEvent == Input.ShopEvent.SHOP_CREATION || reqEvt.shopEvent == Input.ShopEvent.SHOP_RENAME) {

                if (isValidTitle(evt.getInput())) {
                    if (reqEvt.coEvent instanceof PlayerShopCreationEvent) {
                        Location shopLocationCreation = ((PlayerShopCreationEvent) reqEvt.coEvent).getLocation();
                        UniversalShopStorage.create(player.getUniqueId(), shopLocationCreation.clone(), evt.getInput());
                    } else if (reqEvt.coEvent instanceof InventoryClickEvent) {
                        UniversalShopStorage.get(reqEvt.requestOwnerID).getShopConfig().updateName(evt.getInput());
                    }
                } else {
                    player.sendMessage(PlayerShops.colorize("&cA shop name may only contain a letter or a digit with max limit of 16 characters."));
                }

            }
        }
        else if(reqEvt.inputType == Input.InputType.IntegerType) {
            if(reqEvt.coEvent instanceof InventoryClickEvent) {
                if (reqEvt.shopEvent == Input.ShopEvent.OWNER_ADD_ITEM) {
                    ItemStack aItem = (ItemStack) reqEvt.objects[0];
                    int slot = ((InventoryClickEvent) reqEvt.coEvent).getRawSlot();
                    if (isValidPrice(evt.getInput())) {
                        ShopObject.shopLocationDirectory.get(player.getUniqueId()).getShopConfig().addItem(aItem, Integer.parseInt(evt.getInput()), slot);
                    } else {
                        player.sendMessage(PlayerShops.colorize("&c&c'" + evt.getInput() + "' is not a valid number.\n&cItem Pricing - &lCANCELLED"));
                    }
                } else if(reqEvt.shopEvent == Input.ShopEvent.PLAYER_BUY_EVENT) {
                    int clickedSlot = ((InventoryClickEvent) reqEvt.coEvent).getRawSlot();
                    ItemStack shopItem = ((InventoryClickEvent) reqEvt.coEvent).getCurrentItem().clone();
                    ShopInventory gui = (ShopInventory) ((InventoryClickEvent) reqEvt.coEvent).getInventory().getHolder();
                    ShopObject shopObject = gui.getShopObject();
                    UUID owner = gui.owner;


                    int max = shopObject.getShopConfig().getAmount(clickedSlot);
                    boolean isValidAmount = isValidAmount(evt.getInput(), max);

                    if(isValidAmount) {
                        int requestAmount = Integer.parseUnsignedInt(evt.getInput());
                        int singleItemCost = shopObject.getShopConfig().getItemPrice(clickedSlot, true);
                        int totalCost = requestAmount * singleItemCost;
                        if(Currency.calculateBalance(player) >= totalCost) {
                            Currency.remove(player, totalCost);
                            shopObject.getShopConfig().process(owner, player.getUniqueId(), clickedSlot, requestAmount);
                            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 2.0F, 1.0F);
                            player.sendMessage(PlayerShops.colorize("&aYou bough an item!"));

                            OfflinePlayer player1 = Bukkit.getOfflinePlayer(owner);
                            if (player1.isOnline()) {
                                player1.getPlayer().sendMessage(PlayerShops.colorize("&a" + ((InventoryClickEvent)reqEvt.coEvent).getWhoClicked().getName() + " bought " + shopItem.getType().name()));
                            }
                        } else {
                            player.sendMessage(PlayerShops.colorize("&cYou do not have enough gems."));
                            player.sendMessage(PlayerShops.colorize("&c&lCOST: &c" + totalCost + "&lG"));
                        }
                    } else { player.sendMessage(PlayerShops.colorize("&cNot a valid amount.")); }
                } else if(reqEvt.shopEvent == Input.ShopEvent.OWNER_MODIFY_PRICE) {
                    if(isValidPrice(evt.getInput())) {
                        int clickedSlot = ((InventoryClickEvent) reqEvt.coEvent).getRawSlot();
                        ShopInventory gui = (ShopInventory) ((InventoryClickEvent) reqEvt.coEvent).getInventory().getHolder();
                        gui.getShopObject().getShopConfig().setPrice(clickedSlot, Integer.parseUnsignedInt(evt.getInput()));
                    }
                }
            }
        } else if(reqEvt.inputType == Input.InputType.CUSTOM) {}
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
