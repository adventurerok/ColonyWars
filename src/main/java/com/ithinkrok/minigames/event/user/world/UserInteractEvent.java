package com.ithinkrok.minigames.event.user.world;

import com.ithinkrok.minigames.User;
import com.ithinkrok.minigames.event.user.UserEvent;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.inventory.ItemStack;

/**
 * Created by paul on 01/01/16.
 */
public abstract class UserInteractEvent<U extends User> extends UserEvent<U> implements Cancellable{

    private boolean cooldown = false;

    public UserInteractEvent(U user) {
        super(user);
    }

    public abstract Block getClickedBlock();
    public abstract Entity getClickedEntity();

    public abstract InteractType getInteractType();

    public abstract BlockFace getBlockFace();
    public abstract ItemStack getItem();

    public boolean hasBlock() {
        return getClickedBlock() != null;
    }

    public boolean hasEntity() {
        return getClickedEntity() != null;
    }

    public boolean hasItem() {
        return getItem() != null;
    }

    public enum InteractType {

        /**
         * If the interaction was not done by the User, but by an Entity that is representing the User.
         */
        REPRESENTING,

        /**
         * If the interaction was via left click (e.g. block breaking or attacking)
         */
        LEFT_CLICK,

        /**
         * If the interaction was via right click
         */
        RIGHT_CLICK,

        /**
         * If the interaction was physical (e.g. standing on a pressure plate)
         */
        PHYSICAL
    }

    public boolean getStartCooldownAfterAction() {
        return cooldown;
    }

    public void setStartCooldownAfterAction(boolean cooldown) {
        this.cooldown = cooldown;
    }
}
