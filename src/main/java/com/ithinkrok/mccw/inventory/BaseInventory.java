package com.ithinkrok.mccw.inventory;

import com.ithinkrok.mccw.data.PlayerInfo;
import com.ithinkrok.mccw.data.TeamInfo;
import com.ithinkrok.mccw.util.InventoryUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by paul on 04/11/15.
 *
 * InventoryHandler for base shops
 */
public class BaseInventory implements InventoryHandler {

    @Override
    public void onInventoryClick(ItemStack item, PlayerInfo playerInfo, TeamInfo teamInfo) {

    }

    @Override
    public List<ItemStack> getInventoryContents(PlayerInfo playerInfo, TeamInfo teamInfo) {
        ArrayList<ItemStack> result = new ArrayList<>();
        result.add(InventoryUtils.createShopItem(Material.OBSIDIAN, 1, 0, "Farm", "Build a farm!", 3000, true));
        return result;
    }
}
