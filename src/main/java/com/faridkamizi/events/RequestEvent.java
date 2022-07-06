package com.faridkamizi.events;

import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.UUID;



public class RequestEvent {
    UUID requestOwnerID;
    Event coEvent;

    Input.InputType inputType;
    Input.ShopEvent shopEvent;

    Object[] objects = new Object[4];

    /**
     *
     * @param uuid
     * @param correlatedEvt
     */
    public RequestEvent(UUID uuid, Event correlatedEvt, Input.InputType iType, Input.ShopEvent eventName) {
        this.requestOwnerID = uuid;
        this.coEvent = correlatedEvt;
        this.inputType = iType;
        this.shopEvent = eventName;

        if(eventName == Input.ShopEvent.OWNER_ADD_ITEM) {
            this.objects[0] = ((InventoryClickEvent) correlatedEvt).getCursor().clone();
        }
    }

}
