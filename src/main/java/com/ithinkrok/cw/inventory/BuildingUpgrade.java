package com.ithinkrok.cw.inventory;

import com.ithinkrok.cw.Building;
import com.ithinkrok.cw.metadata.BuildingController;
import com.ithinkrok.minigames.inventory.event.BuyablePurchaseEvent;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

/**
 * Created by paul on 13/01/16.
 */
public class BuildingUpgrade extends BuildingBuyable {

    private String upgradeBuilding;

    public BuildingUpgrade(ItemStack baseDisplay) {
        super(baseDisplay);
    }

    @Override
    public void configure(ConfigurationSection config) {
        upgradeBuilding = config.getString("upgrade_building");

        super.configure(config);
    }

    @Override
    public boolean onPurchase(BuyablePurchaseEvent event) {
        Location buildingLoc = event.getUser().getInventoryTether();

        BuildingController controller = BuildingController.getOrCreate(event.getUserGameGroup());

        Building old = controller.getBuilding(buildingLoc);
        old.remove();

        controller.buildBuilding(upgradeBuilding, old.getTeamIdentifier(), buildingLoc, old.getSchematic()
                .getRotation(), false);

        event.getUser().getTeam().sendLocale(teamPurchaseLocale, event.getUser().getFormattedName(), buildingName);

        return true;
    }
}
