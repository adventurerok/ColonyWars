package com.ithinkrok.mccw.inventory;

import com.ithinkrok.mccw.event.ItemPurchaseEvent;
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

    public BuildingBuyable(String buildingName, String purchaseFromBuilding, int cost){
        this(buildingName, purchaseFromBuilding, cost, 1, true);
    }

    public BuildingBuyable(String buildingName, String purchaseFromBuilding, int costPerItem, int amount, boolean
            team) {
        super(InventoryUtils.createItemWithNameAndLore(Material.LAPIS_ORE, amount, 0, buildingName, "Build a " +
                        buildingName + "!"),
                InventoryUtils.createItemWithNameAndLore(Material.LAPIS_ORE, amount, 0, buildingName, "Builds a " +
                        buildingName + " when placed!"), purchaseFromBuilding, costPerItem * amount, team, true);
        this.buildingName = buildingName;
    }

    @Override
    public boolean canBuy(ItemPurchaseEvent event) {
        return super.canBuy(event);
    }

    @Override
    public void prePurchase(ItemPurchaseEvent event) {
        event.getTeam().message(
                event.getUser().getFormattedName() + ChatColor.DARK_AQUA + " purchased " + ChatColor.WHITE +
                        buildingName);
    }
}
