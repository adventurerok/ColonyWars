package com.ithinkrok.oldmccw.event;

import com.ithinkrok.oldmccw.data.User;
import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.BlockBreakEvent;

/**
 * Created by paul on 28/12/15.
 */
public class UserBreakBlockEvent extends UserEvent implements Cancellable {

    private BlockBreakEvent event;

    public UserBreakBlockEvent(User user, BlockBreakEvent event) {
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

    public Block getBlock() {
        return event.getBlock();
    }
}
