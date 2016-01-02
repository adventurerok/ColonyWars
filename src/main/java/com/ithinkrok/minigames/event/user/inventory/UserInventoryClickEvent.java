package com.ithinkrok.minigames.event.user.inventory;

import com.ithinkrok.minigames.User;
import com.ithinkrok.minigames.event.user.UserEvent;
import org.bukkit.event.Cancellable;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Created by paul on 02/01/16.
 */
public class UserInventoryClickEvent<U extends User> extends UserEvent<U> implements Cancellable{

    private final InventoryClickEvent event;

    public UserInventoryClickEvent(U user, InventoryClickEvent event) {
        super(user);
        this.event = event;
    }

    public ItemStack getItemOnCursor() {
        return event.getCursor();
    }

    public ItemStack getItemInSlot() {
        return event.getCurrentItem();
    }

    public InventoryType.SlotType getSlotType() {
        return event.getSlotType();
    }

    public ClickType getClickType() {
        return event.getClick();
    }

    public InventoryAction getAction() {
        return event.getAction();
    }

    public Inventory getInventory() {
        return event.getInventory();
    }

    public int getSlot() {
        return event.getSlot();
    }

    public int getRawSlot() {
        return event.getRawSlot();
    }

    @Override
    public boolean isCancelled() {
        return event.isCancelled();
    }

    @Override
    public void setCancelled(boolean cancel) {
        event.setCancelled(cancel);
    }
}
