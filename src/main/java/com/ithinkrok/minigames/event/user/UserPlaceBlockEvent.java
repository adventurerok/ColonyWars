package com.ithinkrok.minigames.event.user;

import com.ithinkrok.minigames.User;
import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.BlockPlaceEvent;

/**
 * Created by paul on 31/12/15.
 */
public class UserPlaceBlockEvent<U extends User> extends UserEvent<U> implements Cancellable {

    private final BlockPlaceEvent event;

    public UserPlaceBlockEvent(U user, BlockPlaceEvent event) {
        super(user);
        this.event = event;
    }

    public Block getBlock() {
        return event.getBlock();
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
