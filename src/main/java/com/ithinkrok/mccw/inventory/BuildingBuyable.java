package com.ithinkrok.mccw.inventory;

import com.ithinkrok.mccw.util.InventoryUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Created by paul on 06/11/15.
 */
public class BuildingBuyable extends ItemBuyable {


    public BuildingBuyable(String buildingName, String purchaseFromBuilding, int cost) {
        super(InventoryUtils.createItemWithNameAndLore(Material.LAPIS_ORE, 1, 0, buildingName, "Build a" +
                        buildingName + "!"),
                InventoryUtils.createItemWithNameAndLore(Material.LAPIS_ORE, 1, 0, buildingName, "Builds a " +
                        buildingName + " when placed!"), purchaseFromBuilding, cost, true, true);
    }
}
