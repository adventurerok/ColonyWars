package com.ithinkrok.minigames.item;

import com.ithinkrok.minigames.User;
import com.ithinkrok.minigames.event.user.inventory.UserInventoryClickEvent;
import com.ithinkrok.minigames.item.event.UserClickItemEvent;
import com.ithinkrok.minigames.item.event.UserViewItemEvent;
import com.ithinkrok.minigames.util.InventoryUtils;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by paul on 02/01/16.
 */
public class ClickableInventory<U extends User> {

    private static int clickableInventoryCount = 0;

    private final int clickableInventoryId = clickableInventoryCount++;
    private final String title;
    private Map<Integer, ClickableItem<U>> items = new HashMap<>();

    public ClickableInventory(String title) {
        this.title = InventoryUtils.generateIdentifierString(clickableInventoryId) + title;
    }

    public void addItem(ClickableItem<U> item) {
        items.put(item.getIdentifier(), item);
    }

    public int getIdentifier() {
        return clickableInventoryId;
    }

    public Inventory createInventory(U user) {
        Inventory inventory = user.createInventory(items.size(), title);

        for(ClickableItem<U> item : items.values()) {
            if(!item.isVisible(new UserViewItemEvent<>(user, item))) continue;

            inventory.addItem(item.getDisplayItemStack());
        }

        return inventory;
    }

    public void inventoryClick(UserInventoryClickEvent<U> event) {
        event.setCancelled(true);

        if(InventoryUtils.isEmpty(event.getItemInSlot())) return;
        int identifier = InventoryUtils.getIdentifier(event.getItemInSlot());

        ClickableItem<U> item = items.get(identifier);

        item.onClick(new UserClickItemEvent<>(event.getUser(), item, event.getClickType()));
    }
}
