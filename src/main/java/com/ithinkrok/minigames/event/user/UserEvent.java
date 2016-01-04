package com.ithinkrok.minigames.event.user;

import com.ithinkrok.minigames.User;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Created by paul on 31/12/15.
 */
public class UserEvent extends Event {

    private User user;

    public UserEvent(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    @Override
    public HandlerList getHandlers() {
        return null;
    }
}
