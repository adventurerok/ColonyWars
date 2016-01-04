package com.ithinkrok.minigames.item.event;

import com.ithinkrok.minigames.User;
import com.ithinkrok.minigames.event.user.UserEvent;
import com.ithinkrok.minigames.item.ClickableInventory;
import com.ithinkrok.minigames.item.ClickableItem;

/**
 * Created by paul on 02/01/16.
 */
public class UserViewItemEvent extends UserEvent {

    private final ClickableInventory<User> inventory;
    private final ClickableItem<User> item;

    public UserViewItemEvent(User user, ClickableInventory<User> inventory, ClickableItem<User> item) {
        super(user);
        this.inventory = inventory;
        this.item = item;
    }

    public ClickableInventory<User> getInventory() {
        return inventory;
    }

    public ClickableItem getItem() {
        return item;
    }
}
