package com.ithinkrok.mccw.inventory;

import com.ithinkrok.mccw.data.Building;
import com.ithinkrok.mccw.data.Team;
import com.ithinkrok.mccw.data.User;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Created by paul on 04/11/15.
 * <p>
 * Interface to handle inventory clicking and creating inventories
 */
public interface InventoryHandler {

    boolean onInventoryClick(ItemStack item, Building building, User user, Team team);

    void addInventoryItems(List<ItemStack> inventory, Building building, User user,
                           Team team);

}
