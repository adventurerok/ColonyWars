package com.ithinkrok.minigames.item;

import com.ithinkrok.minigames.User;
import com.ithinkrok.minigames.item.event.UserClickItemEvent;
import com.ithinkrok.minigames.item.event.UserViewItemEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Created by paul on 02/01/16.
 */
public abstract class ClickableItem<U extends User> {

    private static int clickableItemCount = 0;

    private ItemStack display;
    private int clickableItemId = clickableItemCount++;

    public ClickableItem(ItemStack display) {
        this.display = display;
    }

    public ItemStack getDisplayItemStack() {
        return display;
    }

    public abstract boolean isVisible(UserViewItemEvent<U> event);
    public abstract void onClick(UserClickItemEvent<U> event);
}
