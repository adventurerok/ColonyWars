package com.ithinkrok.mccw.inventory;

import com.ithinkrok.mccw.data.PlayerInfo;
import com.ithinkrok.mccw.data.TeamInfo;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Created by paul on 04/11/15.
 *
 * Interface to handle inventory clicking and creating inventories
 */
public interface InventoryHandler {

    boolean onInventoryClick(ItemStack item, PlayerInfo playerInfo, TeamInfo teamInfo);
    List<ItemStack> getInventoryContents(PlayerInfo playerInfo, TeamInfo teamInfo);

}
