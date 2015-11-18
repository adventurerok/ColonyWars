package com.ithinkrok.mccw.event;

import com.ithinkrok.mccw.data.User;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * Created by paul on 13/11/15.
 *
 * An event for when a user interacts by left or right clicking.
 * Excludes left clicking on Entities.
 */
public class UserInteractEvent extends UserEvent implements Cancellable {

    private PlayerInteractEvent event;

    public UserInteractEvent(User user, PlayerInteractEvent event) {
        super(user);
        this.event = event;
    }



    @Override
    public void setCancelled(boolean cancel) {
        event.setCancelled(cancel);
    }

    @Override
    public boolean isCancelled() {
        return event.isCancelled();
    }

    public Block getClickedBlock(){
        return event.getClickedBlock();
    }

    public Action getAction(){
        return event.getAction();
    }

    public BlockFace getBlockFace(){
        return event.getBlockFace();
    }

    public ItemStack getItem(){
        return event.getItem();
    }
}
