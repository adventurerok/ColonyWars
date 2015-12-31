package com.ithinkrok.oldmccw.event;

import com.ithinkrok.oldmccw.data.User;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Created by paul on 19/11/15.
 *
 * An event for when a user right clicks on an entity
 */
public class UserRightClickEntityEvent extends UserInteractEvent {

    private PlayerInteractEntityEvent event;

    public UserRightClickEntityEvent(User user, PlayerInteractEntityEvent event) {
        super(user);
        this.event = event;
    }

    @Override
    public Block getClickedBlock() {
        return null;
    }

    @Override
    public Entity getClickedEntity() {
        return event.getRightClicked();
    }

    @Override
    public boolean isRightClick() {
        return true;
    }

    @Override
    public BlockFace getBlockFace() {
        return null;
    }

    @Override
    public ItemStack getItem() {
        return user.getPlayerInventory().getItemInHand();
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
