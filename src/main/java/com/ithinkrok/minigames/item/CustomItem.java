package com.ithinkrok.minigames.item;

import com.ithinkrok.minigames.User;
import com.ithinkrok.minigames.event.user.world.UserInteractEvent;
import org.bukkit.Material;

/**
 * Created by paul on 02/01/16.
 * <p>
 * An item with custom use or inventory click listeners
 */
public class CustomItem<U extends User> implements Identifiable{

    private static int customItemCount = 0;

    private int customItemId = customItemCount++;

    private UserInteractEvent.InteractAction<U> rightClickAction;
    private UserInteractEvent.InteractAction<U> leftClickAction;

    private String itemDisplayName;
    private Material itemMaterial;
    private String rightClickCooldownUpgrade;
    private String rightClickCooldownFinished;

    public CustomItem() {

    }


    @Override
    public int getIdentifier() {
        return customItemId;
    }

}
