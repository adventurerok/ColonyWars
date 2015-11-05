package com.ithinkrok.mccw.inventory;

import com.ithinkrok.mccw.WarsPlugin;
import com.ithinkrok.mccw.data.BuildingInfo;
import com.ithinkrok.mccw.data.PlayerInfo;
import com.ithinkrok.mccw.data.TeamInfo;
import com.ithinkrok.mccw.util.InventoryUtils;
import com.ithinkrok.mccw.util.SchematicBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by paul on 05/11/15.
 */
public class ChurchInventory implements InventoryHandler {

    private int cathedralCost = 8000;

    private WarsPlugin plugin;

    public ChurchInventory(WarsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onInventoryClick(ItemStack item, BuildingInfo buildingInfo, PlayerInfo playerInfo,
                                    TeamInfo teamInfo) {

        if (playerInfo.getPlayer().getInventory().firstEmpty() == -1) {
            playerInfo.getPlayer().sendMessage("Please ensure you have one free slot in your inventory.");
            return true;
        }

        switch (item.getType()) {
            case LAPIS_ORE:
                int cost = cathedralCost;

                if (!InventoryUtils.hasTeamCash(cost, teamInfo, playerInfo)) {
                    playerInfo.getPlayer().sendMessage("You don't have that amount of money!");
                    return true;
                }

                InventoryUtils.payWithTeamCash(cost, teamInfo, playerInfo);
                InventoryUtils.playBuySound(playerInfo.getPlayer());

                buildingInfo.remove();

                if (!SchematicBuilder
                        .buildSchematic(plugin, plugin.getSchematicData("Cathedral"), buildingInfo.getCenterBlock(),
                                buildingInfo.getTeamColor())) {
                    playerInfo.getPlayer().sendMessage("We failed to build a cathedral here. Have the block yourself " +
                            "to find a better place!");

                    playerInfo.getPlayer().getInventory().addItem(InventoryUtils.createItemWithNameAndLore(Material
                            .LAPIS_ORE, 1, 0, "Cathedral", "Builds a cathedral when placed!"));
                }
                return true;
        }

        return false;
    }

    @Override
    public List<ItemStack> getInventoryContents(BuildingInfo buildingInfo, PlayerInfo playerInfo, TeamInfo teamInfo) {
        List<ItemStack> result = new ArrayList<>();

        result.add(InventoryUtils
                .createShopItem(Material.LAPIS_ORE, 1, 0, "Cathedral", "Replace this church with a cathedral",
                        cathedralCost, true));

        return result;
    }
}
