package com.ithinkrok.cw.gamestate;

import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.GameState;
import com.ithinkrok.minigames.api.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.api.event.MinigamesCommandEvent;
import com.ithinkrok.minigames.api.event.map.MapCreatureSpawnEvent;
import com.ithinkrok.minigames.api.event.map.MapItemSpawnEvent;
import com.ithinkrok.minigames.api.event.user.game.UserJoinEvent;
import com.ithinkrok.minigames.api.event.user.game.UserQuitEvent;
import com.ithinkrok.minigames.api.event.user.world.UserDropItemEvent;
import com.ithinkrok.minigames.base.gamestate.SimpleInGameListener;
import com.ithinkrok.minigames.api.util.InventoryUtils;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.event.CustomEventHandler;
import org.bukkit.Bukkit;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.Random;

/**
 * Created by paul on 14/01/16.
 */
public class BaseGameStateListener extends SimpleInGameListener {

    protected Random random = new Random();

    protected String quitLocale;
    protected String joinLocale;

    @CustomEventHandler
    public void onListenerLoaded(ListenerLoadedEvent<GameGroup, GameState> event) {
        super.onListenerLoaded(event);

        Config config = event.getConfigOrEmpty();

        if (quitLocale == null) quitLocale = config.getString("user_quit_locale", "user.quit");
        if (joinLocale == null) joinLocale = config.getString("user_join_locale", "user.join");
    }

    @CustomEventHandler
    public void onUserDropItem(UserDropItemEvent event) {
        if (InventoryUtils.getIdentifier(event.getItem().getItemStack()) == -1) return;

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

    @CustomEventHandler(priority = CustomEventHandler.MONITOR)
    public void sendQuitMessageOnUserQuit(UserQuitEvent event) {
        if(event.getReason() == UserQuitEvent.QuitReason.NON_PLAYER_REMOVED) return;

        String name = event.getUser().getFormattedName();
        int currentPlayers = event.getUserGameGroup().getUserCount() - 1;
        int maxPlayers = Bukkit.getMaxPlayers();

        event.getUserGameGroup().sendLocale(quitLocale, name, currentPlayers, maxPlayers);
    }

    @CustomEventHandler(priority = CustomEventHandler.FIRST)
    public void sendJoinMessageOnUserJoin(UserJoinEvent event) {
        String name = event.getUser().getFormattedName();
        int currentPlayers = event.getUserGameGroup().getUserCount();
        int maxPlayers = Bukkit.getMaxPlayers();

        event.getUserGameGroup().sendLocale(joinLocale, name, currentPlayers, maxPlayers);
    }
}
