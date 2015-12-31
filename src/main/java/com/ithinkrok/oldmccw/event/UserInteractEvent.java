package com.ithinkrok.oldmccw.event;

import com.ithinkrok.oldmccw.data.User;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.inventory.ItemStack;

/**
 * Created by paul on 13/11/15.
 *
 * An event for when a user interacts by left or right clicking.
 */
public abstract class UserInteractEvent extends UserEvent implements Cancellable {

    public UserInteractEvent(User user) {
        super(user);
    }


    public abstract Block getClickedBlock();
    public abstract Entity getClickedEntity();

    public abstract boolean isRightClick();

    public abstract BlockFace getBlockFace();

    public abstract ItemStack getItem();

    public boolean hasBlock(){
        return getClickedBlock() != null;
    }

    public boolean hasEntity(){
        return getClickedEntity() != null;
    }
}
