package com.ithinkrok.minigames.item.event;

import com.ithinkrok.minigames.User;
import com.ithinkrok.minigames.event.user.UserEvent;
import com.ithinkrok.minigames.item.ClickableInventory;
import com.ithinkrok.minigames.item.ClickableItem;

/**
 * Created by paul on 02/01/16.
 */
public class UserViewItemEvent<U extends User> extends UserEvent<U> {

    private final ClickableInventory<U> inventory;
    private final ClickableItem<U> item;

    public UserViewItemEvent(U user, ClickableInventory<U> inventory, ClickableItem<U> item) {
        super(user);
        this.inventory = inventory;
        this.item = item;
    }

    public ClickableInventory<U> getInventory() {
        return inventory;
    }

    public ClickableItem getItem() {
        return item;
    }
}
