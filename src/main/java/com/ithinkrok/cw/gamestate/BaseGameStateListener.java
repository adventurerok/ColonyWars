package com.ithinkrok.cw.gamestate;

import com.ithinkrok.minigames.event.map.MapCreatureSpawnEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

/**
 * Created by paul on 14/01/16.
 */
public class BaseGameStateListener implements Listener {

    @EventHandler
    public void onCreatureSpawn(MapCreatureSpawnEvent event) {
        if(event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM) return;

        event.setCancelled(true);
    }
}
