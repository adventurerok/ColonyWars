package com.ithinkrok.mccw.inventory;

import com.ithinkrok.mccw.util.InventoryUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;

/**
 * Created by paul on 06/11/15.
 * <p>
 * A buyable building block
 */
public class BuildingBuyable extends ItemBuyable {


    private String buildingName;

    public BuildingBuyable(String buildingName, String purchaseFromBuilding, int cost) {
        super(InventoryUtils.createItemWithNameAndLore(Material.LAPIS_ORE, 1, 0, buildingName, "Build a " +
                        buildingName + "!"),
                InventoryUtils.createItemWithNameAndLore(Material.LAPIS_ORE, 1, 0, buildingName, "Builds a " +
                        buildingName + " when placed!"), purchaseFromBuilding, cost, true, true);
        this.buildingName = buildingName;
    }

    @Override
    public boolean canBuy(ItemPurchaseEvent event) {
        return super.canBuy(event);
    }

    @Override
    public void prePurchase(ItemPurchaseEvent event) {
        event.getTeamInfo()
                .message(event.getPlayerInfo().getFormattedName() + " purchased " + ChatColor.WHITE + buildingName);
    }
}
