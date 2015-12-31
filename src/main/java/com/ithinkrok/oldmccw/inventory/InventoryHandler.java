package com.ithinkrok.oldmccw.inventory;

import com.ithinkrok.oldmccw.data.Building;
import com.ithinkrok.oldmccw.data.Team;
import com.ithinkrok.oldmccw.data.User;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Created by paul on 04/11/15.
 * <p>
 * Interface to handle inventory clicking and creating inventories
 */
public interface InventoryHandler {

    /**
     * Called when a User clicks on an item in an inventory
     * @param item The item the user clicked on
     * @param building The building whose shop the player is in
     * @param user The user who clicked on the item
     * @param team The team of the user
     * @return If the event was handled by this handler
     */
    boolean onInventoryClick(ItemStack item, Building building, User user, Team team);

    /**
     * Called when a shop inventory is created for a player to add the shop items to the inventory
     * @param inventory The list of items to add the new items too. This may already contain items
     * @param building The building whose shop the player is in
     * @param user The user the inventory is being created for
     * @param team The team of the user
     */
    void addInventoryItems(List<ItemStack> inventory, Building building, User user,
                           Team team);

}
