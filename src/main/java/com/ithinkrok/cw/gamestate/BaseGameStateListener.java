package com.ithinkrok.cw.gamestate;

import com.ithinkrok.cw.metadata.CWTeamStats;
import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.GameState;
import com.ithinkrok.minigames.api.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.api.event.MinigamesCommandEvent;
import com.ithinkrok.minigames.api.event.map.MapCreatureSpawnEvent;
import com.ithinkrok.minigames.api.event.map.MapItemSpawnEvent;
import com.ithinkrok.minigames.api.event.user.world.UserDropItemEvent;
import com.ithinkrok.minigames.api.util.InventoryUtils;
import com.ithinkrok.minigames.util.gamestate.SimpleInGameListener;
import com.ithinkrok.util.event.CustomEventHandler;
import org.bukkit.Material;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

/**
 * Created by paul on 14/01/16.
 */
public class BaseGameStateListener extends SimpleInGameListener {

    protected Random random = new Random();

    @CustomEventHandler
    public void onListenerLoaded(ListenerLoadedEvent<GameGroup, GameState> event) {
        super.onListenerLoaded(event);
    }

    @CustomEventHandler
    public void onUserDropItem(UserDropItemEvent event) {
        ItemStack itemStack = event.getItem().getItemStack();
        if (InventoryUtils.getIdentifier(itemStack) == -1) {
            //If the user is in a team and the dropped item was a building, remove it from the team count
            if (itemStack != null && event.getUser().getTeam() != null && itemStack.getType() == Material.LAPIS_ORE) {
                CWTeamStats teamStats = CWTeamStats.getOrCreate(event.getUser().getTeam());

                teamStats.addBuildingInventoryCount(itemStack.getItemMeta().getDisplayName(), -itemStack.getAmount());
            }

            return;
        }

        event.setCancelled(true);
    }

    @CustomEventHandler
    public void onCommand(MinigamesCommandEvent event) {
        switch (event.getCommand().getCommand().toLowerCase()) {
            case "kill":
            case "suicide":
                event.setHandled(true);
        }
    }

    @CustomEventHandler
    public void onCreatureSpawn(MapCreatureSpawnEvent event) {
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM) return;

        event.setCancelled(true);
    }

    @CustomEventHandler
    public void onItemSpawn(MapItemSpawnEvent event) {
        event.setCancelled(true);
    }
}
