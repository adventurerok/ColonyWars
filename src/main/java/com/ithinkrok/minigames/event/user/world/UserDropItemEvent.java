package com.ithinkrok.minigames.event.user.world;

import com.ithinkrok.minigames.User;
import com.ithinkrok.minigames.event.user.UserEvent;
import org.bukkit.entity.Item;
import org.bukkit.event.Cancellable;
import org.bukkit.event.player.PlayerDropItemEvent;

/**
 * Created by paul on 02/01/16.
 */
public class UserDropItemEvent<U extends User> extends UserEvent<U> implements Cancellable{

    private final PlayerDropItemEvent event;

    public UserDropItemEvent(U user, PlayerDropItemEvent event) {
        super(user);
        this.event = event;
    }

    public Item getItem() {
        return event.getItemDrop();
    }

    @Override
    public boolean isCancelled() {
        return event.isCancelled();
    }

    @Override
    public void setCancelled(boolean cancel) {
        event.setCancelled(cancel);
    }
}
