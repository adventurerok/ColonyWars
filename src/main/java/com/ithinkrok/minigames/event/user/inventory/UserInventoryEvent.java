package com.ithinkrok.minigames.event.user.inventory;

import com.ithinkrok.minigames.User;
import com.ithinkrok.minigames.event.user.UserEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

/**
 * Created by paul on 02/01/16.
 */
public abstract class UserInventoryEvent<U extends User> extends UserEvent<U> {


    public UserInventoryEvent(U user) {
        super(user);
    }

    public abstract Inventory getInventory();
    public abstract InventoryView getInventoryView();
}
