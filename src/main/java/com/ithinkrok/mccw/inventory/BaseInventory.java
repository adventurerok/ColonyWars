package com.ithinkrok.mccw.inventory;

import com.ithinkrok.mccw.data.PlayerInfo;
import com.ithinkrok.mccw.data.TeamInfo;
import com.ithinkrok.mccw.util.InventoryUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by paul on 04/11/15.
 *
 * InventoryHandler for base shops
 */
public class BaseInventory implements InventoryHandler {

    @Override
    public void onInventoryClick(ItemStack item, PlayerInfo playerInfo, TeamInfo teamInfo) {
        PlayerInventory inv = playerInfo.getPlayer().getInventory();

        switch(item.getItemMeta().getDisplayName()){
            case "Farm":
                if(!InventoryUtils.hasTeamCash(3000, teamInfo, playerInfo)){
                    playerInfo.getPlayer().sendMessage("You don't have that amount of money!");
                    break;
                }

                HashMap<Integer, ItemStack> leftOver = inv.addItem(InventoryUtils.createItemWithNameAndLore(Material
                        .OBSIDIAN, 1, 0, "Farm", "Builds a farm when placed!"));

                if(!leftOver.isEmpty()) break;

                InventoryUtils.payWithTeamCash(3000, teamInfo, playerInfo);
                InventoryUtils.playBuySound(playerInfo.getPlayer());

                break;
        }
    }

    @Override
    public List<ItemStack> getInventoryContents(PlayerInfo playerInfo, TeamInfo teamInfo) {
        ArrayList<ItemStack> result = new ArrayList<>();
        result.add(InventoryUtils.createShopItem(Material.LAPIS_ORE, 1, 0, "Farm", "Build a farm!", 3000, true));
        return result;
    }
}
