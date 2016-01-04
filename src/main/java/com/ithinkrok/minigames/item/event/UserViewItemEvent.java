package com.ithinkrok.minigames.item.event;

import com.ithinkrok.minigames.User;
import com.ithinkrok.minigames.event.user.UserEvent;
import com.ithinkrok.minigames.item.ClickableInventory;
import com.ithinkrok.minigames.item.ClickableItem;

/**
 * Created by paul on 02/01/16.
 */
public class UserViewItemEvent extends UserEvent {

    private final ClickableInventory inventory;
    private final ClickableItem item;

    public UserViewItemEvent(User user, ClickableInventory inventory, ClickableItem item) {
        super(user);
        this.inventory = inventory;
        this.item = item;
    }

    public ClickableInventory getInventory() {
        return inventory;
    }

    public ClickableItem getItem() {
        return item;
    }
}
