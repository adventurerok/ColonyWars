package com.ithinkrok.cw.inventory;

import com.ithinkrok.cw.metadata.CWTeamStats;
import com.ithinkrok.minigames.inventory.event.BuyablePurchaseEvent;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Created by paul on 13/01/16.
 */
public class BuildingBuyableWith extends BuildingBuyable {

    private List<String> with;

    public BuildingBuyableWith(ItemStack baseDisplay) {
        super(baseDisplay);
    }

    @Override
    public void configure(ConfigurationSection config) {
        with = config.getStringList("with_buildings");

        super.configure(config);
    }

    @Override
    public boolean canBuy(BuyablePurchaseEvent event) {
        CWTeamStats teamStats = CWTeamStats.getOrCreate(event.getUser().getTeam());

        for(String buildingName : with){
            if(teamStats.getBuildingCount(buildingName) < 1) return false;
        }

        return true;
    }
}
