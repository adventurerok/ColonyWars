package com.ithinkrok.minigames.item;

import com.ithinkrok.minigames.item.event.UserClickItemEvent;
import com.ithinkrok.minigames.item.event.CalculateItemForUserEvent;
import com.ithinkrok.minigames.util.InventoryUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

/**
 * Created by paul on 02/01/16.
 */
public abstract class ClickableItem implements Identifiable {

    private static int clickableItemCount = 0;

    protected ItemStack baseDisplay;
    private int identifier = clickableItemCount++;

    public ClickableItem() {
        this(null);
    }

    public ClickableItem(ItemStack baseDisplay) {
        if(baseDisplay != null) this.baseDisplay = InventoryUtils.addIdentifier(baseDisplay, identifier);
    }

    public void configure(ConfigurationSection config) {}

    public int getIdentifier() {
        return identifier;
    }

    public ItemStack getBaseDisplayStack() {
        return baseDisplay;
    }

    public  void onCalculateItem(CalculateItemForUserEvent event) {
        //Does nothing by default as the event uses item.getBaseDisplayStack() by default
    }

    public abstract void onClick(UserClickItemEvent event);
}
