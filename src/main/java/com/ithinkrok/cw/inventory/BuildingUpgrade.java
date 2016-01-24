package com.ithinkrok.cw.inventory;

import com.ithinkrok.cw.Building;
import com.ithinkrok.cw.metadata.BuildingController;
import com.ithinkrok.cw.metadata.CWTeamStats;
import com.ithinkrok.minigames.inventory.event.BuyablePurchaseEvent;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

/**
 * Created by paul on 13/01/16.
 */
public class BuildingUpgrade extends BuildingBuyable {

    public BuildingUpgrade(ItemStack baseDisplay) {
        super(baseDisplay);
    }


    @Override
    public boolean onPurchase(BuyablePurchaseEvent event) {
        event.getUser().closeInventory();

        Location buildingLoc = event.getUser().getInventoryTether();

        BuildingController controller = BuildingController.getOrCreate(event.getUserGameGroup());

        Building old = controller.getBuilding(buildingLoc);

        if(old.getConfig() != null && old.getConfig().getInt("revival_rate") > 0) {
            CWTeamStats teamStats = CWTeamStats.getOrCreate(event.getUser().getTeam());

            //Add the base location as a church while this one is destroyed
            teamStats.addChurchLocation(teamStats.getBaseLocation(), 0);
        }

        old.remove();

        controller.buildBuilding(buildingName, old.getTeamIdentifier(), buildingLoc, old.getSchematic()
                .getRotation(), false);

        event.getUser().getTeam().sendLocale(teamPurchaseLocale, event.getUser().getFormattedName(), buildingName);

        return true;
    }
}
