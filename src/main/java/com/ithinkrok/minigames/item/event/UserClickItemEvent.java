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
public class UserClickItemEvent<U extends User> extends UserEvent<U> {

    private final ClickableInventory<U> inventory;
    private final ClickableItem<U> clicked;
    private final ClickType clickType;

    public UserClickItemEvent(U user, ClickableInventory<U> inventory, ClickableItem<U> clicked, ClickType clickType) {
        super(user);
        this.inventory = inventory;
        this.clicked = clicked;
        this.clickType = clickType;
    }

    public ClickableInventory<U> getInventory() {
        return inventory;
    }

    public ClickableItem getClickedItem() {
        return clicked;
    }

    public ClickType getClickType() {
        return clickType;
    }
}
