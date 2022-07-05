package com.faridkamizi.events.enhanced;

import org.bukkit.event.Event;

import java.util.ArrayList;
import java.util.UUID;


public class RequestEvent {
    UUID requestOwnerID;
    Event coEvent;

    Object[] objects;

    public RequestEvent(UUID uuid, Event evt, Object... aObj) {
        this.requestOwnerID = uuid;
        this.coEvent = evt;

        if(aObj != null) {
            objects = aObj;
        }
    }



}
