package com.faridkamizi.events.enhanced;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;

import java.util.ArrayList;
import java.util.UUID;


public class RequestEvent {
    UUID requestOwnerID;
    Event coEvent;

    ArrayList<Object> objects;

    public RequestEvent(UUID uuid, Event evt, Object... aObj) {
        this.requestOwnerID = uuid;
        this.coEvent = evt;

        if(aObj != null) {
            objects = new ArrayList<>();
            for (int i = 0; i < aObj.length; i++) {
                objects.add(i, aObj[i]);
            }
        }
    }



}
