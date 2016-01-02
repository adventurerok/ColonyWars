package com.ithinkrok.minigames.item;

import com.ithinkrok.minigames.User;
import com.ithinkrok.minigames.item.event.UserClickItemEvent;
import com.ithinkrok.minigames.item.event.UserViewItemEvent;
import com.ithinkrok.minigames.util.InventoryUtils;
import org.bukkit.inventory.ItemStack;

/**
 * Created by paul on 02/01/16.
 */
public abstract class ClickableItem<U extends User> implements Identifiable {

    private static int clickableItemCount = 0;

    private ItemStack display;
    private int identifier = clickableItemCount++;

    public ClickableItem(ItemStack display) {
        this.display = InventoryUtils.addIdentifier(display, identifier);
    }

    public int getIdentifier() {
        return identifier;
    }

    public ItemStack getDisplayItemStack() {
        return display;
    }

    public abstract boolean isVisible(UserViewItemEvent<U> event);
    public abstract void onClick(UserClickItemEvent<U> event);
}
