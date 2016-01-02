package com.ithinkrok.minigames.item;

import com.ithinkrok.minigames.User;
import com.ithinkrok.minigames.item.event.UserViewItemEvent;
import com.ithinkrok.minigames.util.InventoryUtils;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by paul on 02/01/16.
 */
public class ClickableInventory<U extends User> {

    private static int clickableInventoryCount = 0;

    private final int clickableInventoryId = clickableInventoryCount++;
    private final String title;
    private List<ClickableItem<U>> items = new ArrayList<>();

    public ClickableInventory(String title) {
        this.title = InventoryUtils.generateIdentifierString(clickableInventoryId) + title;
    }

    public void addItem(ClickableItem<U> item) {
        items.add(item);
    }

    public Inventory createInventory(U user) {
        Inventory inventory = user.createInventory(items.size(), title);

        for(ClickableItem<U> item : items) {
            if(!item.isVisible(new UserViewItemEvent<>(user, item))) continue;

            inventory.addItem(item.getDisplayItemStack());
        }

        return inventory;
    }
}
