package com.ithinkrok.cw.inventory;

import com.ithinkrok.cw.metadata.CWTeamStats;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.util.inventory.event.BuyablePurchaseEvent;
import com.ithinkrok.util.config.Config;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Created by paul on 13/01/16.
 */
public class BuildingBuyableWith extends BuildingBuyable {

    private List<String> with;

    public BuildingBuyableWith(ItemStack baseDisplay, int slot) {
        super(baseDisplay, slot);
    }

    @Override
    public void configure(Config config) {
        with = config.getStringList("with_buildings");

        super.configure(config);
    }

    @Override
    public boolean isAvailable(User user) {
        if(!super.isAvailable(user)) return false;
        CWTeamStats teamStats = CWTeamStats.getOrCreate(user.getTeam());

        for(String buildingName : with){
            if(teamStats.getBuildingCount(buildingName) < 1) return false;
        }

        return true;
    }
}
