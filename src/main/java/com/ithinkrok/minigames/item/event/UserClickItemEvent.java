package com.ithinkrok.minigames.item.event;

import com.ithinkrok.minigames.User;
import com.ithinkrok.minigames.event.user.UserEvent;
import com.ithinkrok.minigames.item.ClickableInventory;
import com.ithinkrok.minigames.item.ClickableItem;
import org.bukkit.event.inventory.ClickType;

/**
 * Created by paul on 02/01/16.
 *
 * Called when a user clicks on a ClickableItem in an inventory
 */
public class UserClickItemEvent extends UserEvent {

    private final ClickableInventory<User> inventory;
    private final ClickableItem<User> clicked;
    private final ClickType clickType;

    public UserClickItemEvent(User user, ClickableInventory<User> inventory, ClickableItem<User> clicked, ClickType clickType) {
        super(user);
        this.inventory = inventory;
        this.clicked = clicked;
        this.clickType = clickType;
    }

    public ClickableInventory<User> getInventory() {
        return inventory;
    }

    public ClickableItem getClickedItem() {
        return clicked;
    }

    public ClickType getClickType() {
        return clickType;
    }
}
