package com.ithinkrok.minigames.item;

import com.ithinkrok.minigames.User;
import com.ithinkrok.minigames.event.user.inventory.UserInventoryClickEvent;
import com.ithinkrok.minigames.item.event.UserClickItemEvent;
import com.ithinkrok.minigames.item.event.CalculateItemForUserEvent;
import com.ithinkrok.minigames.util.InventoryUtils;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by paul on 02/01/16.
 */
public class ClickableInventory {


    private final String title;
    private Map<Integer, ClickableItem> items = new HashMap<>();

    public ClickableInventory(String title) {
        this.title = title;
    }

    public void addItem(ClickableItem item) {
        items.put(item.getIdentifier(), item);
    }

    public Inventory createInventory(User user) {
        Inventory inventory = user.createInventory(items.size(), title);

        for(ClickableItem item : items.values()) {
            CalculateItemForUserEvent event = new CalculateItemForUserEvent(user, this, item);

            item.onCalculateItem(event);
            if(event.getDisplay() == null) continue;
            if(InventoryUtils.getIdentifier(event.getDisplay()) == -1) {
                event.setDisplay(InventoryUtils.addIdentifier(event.getDisplay().clone(), item.getIdentifier()));
            }

            inventory.addItem(event.getDisplay());
        }

        return inventory;
    }

    public void inventoryClick(UserInventoryClickEvent event) {
        event.setCancelled(true);

        if(InventoryUtils.isEmpty(event.getItemInSlot())) return;
        int identifier = InventoryUtils.getIdentifier(event.getItemInSlot());

        ClickableItem item = items.get(identifier);

        item.onClick(new UserClickItemEvent(event.getUser(), this, item, event.getClickType()));
    }
}
