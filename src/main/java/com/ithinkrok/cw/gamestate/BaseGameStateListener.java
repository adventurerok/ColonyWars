package com.ithinkrok.cw.gamestate;

import com.ithinkrok.minigames.event.map.MapCreatureSpawnEvent;
import com.ithinkrok.minigames.event.map.MapItemSpawnEvent;
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
}
