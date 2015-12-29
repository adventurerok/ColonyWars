package com.ithinkrok.mccw.event;

import com.ithinkrok.mccw.data.User;
import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Created by paul on 29/12/15.
 */
public class UserMoveEvent extends UserEvent implements Cancellable{

    private final PlayerMoveEvent event;

    public UserMoveEvent(User user, PlayerMoveEvent event) {
        super(user);
        this.event = event;
    }

    @Override
    public boolean isCancelled() {
        return event.isCancelled();
    }

    @Override
    public void setCancelled(boolean cancel) {
        event.setCancelled(cancel);
    }

    public Location getFrom() {
        return event.getFrom();
    }

    public Location getTo() {
        return event.getTo();
    }
}
