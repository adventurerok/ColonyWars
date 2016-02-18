package com.ithinkrok.cw.inventory;

import com.ithinkrok.cw.metadata.CWTeamStats;
import com.ithinkrok.minigames.base.inventory.event.BuyablePurchaseEvent;
import com.ithinkrok.util.config.Config;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Created by paul on 15/01/16.
 */
public class BuildingUpgradeWith extends BuildingUpgrade {

    private List<String> with;

    public BuildingUpgradeWith(ItemStack baseDisplay) {
        super(baseDisplay);
    }

    @Override
    public void configure(Config config) {
        with = config.getStringList("with_buildings");

        super.configure(config);
    }

    @Override
    public boolean canBuy(BuyablePurchaseEvent event) {
        if(!super.canBuy(event)) return false;
        CWTeamStats teamStats = CWTeamStats.getOrCreate(event.getUser().getTeam());

        for(String buildingName : with){
            if(teamStats.getBuildingCount(buildingName) < 1) return false;
        }

        return true;
    }
}
