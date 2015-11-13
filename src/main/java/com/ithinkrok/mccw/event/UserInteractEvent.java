package com.ithinkrok.mccw.event;

import com.ithinkrok.mccw.data.User;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.PlayerInventory;

/**
 * Created by paul on 13/11/15.
 */
public class UserInteractEvent extends PlayerInteractEvent{

    private User user;

    public UserInteractEvent(User who, PlayerInteractEvent event) {
        super(who.getPlayer(), event.getAction(), event.getItem(), event.getClickedBlock(), event.getBlockFace());
        user = who;
    }

    public User getUserClicked() {
        return user;
    }

    public PlayerInventory getUserInventory(){
        return user.getPlayer().getInventory();
    }
}
