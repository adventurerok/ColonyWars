package com.ithinkrok.cw.gamestate;

import com.ithinkrok.cw.metadata.CWTeamStats;
import com.ithinkrok.cw.metadata.StatsHolder;
import com.ithinkrok.minigames.event.map.MapCreatureSpawnEvent;
import com.ithinkrok.minigames.event.map.MapItemSpawnEvent;
import com.ithinkrok.minigames.event.user.game.UserChangeKitEvent;
import com.ithinkrok.minigames.event.user.game.UserChangeTeamEvent;
import com.ithinkrok.minigames.event.user.world.UserDropItemEvent;
import com.ithinkrok.minigames.util.InventoryUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.Random;

/**
 * Created by paul on 14/01/16.
 */
public class BaseGameStateListener implements Listener {

    protected Random random = new Random();

    @EventHandler
    public void onUserDropItem(UserDropItemEvent event) {
        if(InventoryUtils.getIdentifier(event.getItem().getItemStack()) == -1) return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onCreatureSpawn(MapCreatureSpawnEvent event) {
        if(event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM) return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onItemSpawn(MapItemSpawnEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onUserChangeTeam(UserChangeTeamEvent event) {
        if(event.getOldTeam() != null) {
            CWTeamStats oldStats = CWTeamStats.getOrCreate(event.getOldTeam());
            oldStats.removeUser(event.getUser());
        }

        if(event.getNewTeam() != null) {
            CWTeamStats newStats = CWTeamStats.getOrCreate(event.getNewTeam());
            newStats.addUser(event.getUser());

            StatsHolder statsHolder = StatsHolder.getOrCreate(event.getUser());
            statsHolder.setLastTeam(event.getNewTeam().getName());
        }
    }

    @EventHandler
    public void onUserChangeKit(UserChangeKitEvent event) {
        if(event.getNewKit() == null) return;

        StatsHolder statsHolder = StatsHolder.getOrCreate(event.getUser());
        statsHolder.setLastKit(event.getNewKit().getName());
    }
}
