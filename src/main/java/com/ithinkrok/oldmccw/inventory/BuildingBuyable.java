package com.ithinkrok.oldmccw.inventory;

import com.ithinkrok.minigames.lang.LangFile;
import com.ithinkrok.oldmccw.event.ItemPurchaseEvent;
import com.ithinkrok.oldmccw.util.item.InventoryUtils;
import org.bukkit.Material;

/**
 * Created by paul on 06/11/15.
 * <p>
 * A buyable building block
 */
public class BuildingBuyable extends ItemBuyable {


    private String buildingName;

    public BuildingBuyable(LangFile lang, String buildingName, String purchaseFromBuilding, int cost) {
        this(lang, buildingName, purchaseFromBuilding, cost, 1, true);
    }

    public BuildingBuyable(LangFile lang, String buildingName, String purchaseFromBuilding, int costPerItem, int amount,
                           boolean team) {
        super(InventoryUtils.createItemWithNameAndLore(Material.LAPIS_ORE, amount, 0, buildingName,
                lang.getLocale("building.display.desc", buildingName)), InventoryUtils
                        .createItemWithNameAndLore(Material.LAPIS_ORE, amount, 0, buildingName,
                                lang.getLocale("building.item.desc", buildingName)), purchaseFromBuilding, costPerItem * amount,
                team, true);
        this.buildingName = buildingName;
    }

    @Override
    public boolean canBuy(ItemPurchaseEvent event) {
        return super.canBuy(event);
    }

    @Override
    public void prePurchase(ItemPurchaseEvent event) {
        event.getTeam()
                .messageLocale("building.purchased", event.getUser().getFormattedName(), buildingName);
    }
}
