package com.ithinkrok.mccw.playerclass;

import com.ithinkrok.mccw.WarsPlugin;
import com.ithinkrok.mccw.data.PlayerInfo;
import com.ithinkrok.mccw.data.TeamInfo;
import com.ithinkrok.mccw.enumeration.TeamColor;
import com.ithinkrok.mccw.util.InventoryUtils;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

/**
 * Created by paul on 05/11/15.
 */
public class ScoutClass implements PlayerClassHandler {

    private WarsPlugin plugin;

    public ScoutClass(WarsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void addExtraInventoryItems(List<ItemStack> inventory, String buildingName, PlayerInfo playerInfo,
                                       TeamInfo teamInfo) {

    }

    @Override
    public boolean onInventoryClick(ItemStack item, String buildingName, PlayerInfo playerInfo, TeamInfo teamInfo) {
        return false;
    }

    @Override
    public void onBuildingBuilt(String buildingName, PlayerInfo playerInfo, TeamInfo teamInfo) {
        PlayerInventory inv = playerInfo.getPlayer().getInventory();

        switch(buildingName){
            case "Lumbermill":
                inv.addItem(new ItemStack(Material.WOOD_SWORD));
                inv.addItem(InventoryUtils.createItemWithNameAndLore(Material.COMPASS, 1, 0, "Player Compass",
                        "Oriented at: No One"));

                playerInfo.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2), true);
                break;
        }
    }

    @Override
    public void onGameBegin(PlayerInfo playerInfo, TeamInfo teamInfo) {

    }

    @Override
    public void onInteractWorld(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if(item == null) return;

        switch(item.getType()){
            case COMPASS:
                TeamColor exclude = plugin.getPlayerInfo(event.getPlayer()).getTeamColor();
                plugin.updateScoutCompass(item, event.getPlayer(), exclude);
                break;
        }


    }
}
