package com.faridkamizi.events;

import org.bukkit.event.Event;

import javax.annotation.Nullable;
import java.util.UUID;


public class RequestEvent {
    UUID requestOwnerID;
    Event coEvent;

    Object[] objects;

    public RequestEvent(UUID uuid, Event evt, @Nullable Object... aObj) {
        this.requestOwnerID = uuid;
        this.coEvent = evt;

        if(aObj != null) {
            if(aObj.length > 0) {
                objects = aObj;
            }
        }
    }


}
