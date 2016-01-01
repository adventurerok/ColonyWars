package com.ithinkrok.minigames.event;

import com.ithinkrok.minigames.User;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Created by paul on 31/12/15.
 */
public class UserEvent<U extends User> extends Event {

    private U user;

    public UserEvent(U user) {
        this.user = user;
    }

    public U getUser() {
        return user;
    }

    @Override
    public HandlerList getHandlers() {
        return null;
    }
}
