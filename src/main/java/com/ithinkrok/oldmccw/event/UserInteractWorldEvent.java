package com.ithinkrok.oldmccw.event;

import com.ithinkrok.oldmccw.data.User;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Created by paul on 19/11/15.
 *
 * An event for when the player left clicks or right clicks on air or a block.
 */
public class UserInteractWorldEvent extends UserInteractEvent {

    private PlayerInteractEvent event;

    public UserInteractWorldEvent(User user, PlayerInteractEvent event) {
        super(user);
        this.event = event;
    }

    @Override
    public Block getClickedBlock() {
        return event.getClickedBlock();
    }

    @Override
    public Entity getClickedEntity() {
        return null;
    }

    @Override
    public boolean isRightClick() {
        return event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR;
    }

    @Override
    public BlockFace getBlockFace() {
        return event.getBlockFace();
    }

    @Override
    public ItemStack getItem() {
        return event.getItem();
    }

    @Override
    public boolean isCancelled() {
        return event.isCancelled();
    }

    @Override
    public void setCancelled(boolean b) {
        event.setCancelled(b);
    }
}
