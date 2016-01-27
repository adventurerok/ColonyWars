package com.ithinkrok.cw.gamestate;

import com.ithinkrok.cw.metadata.StatsHolder;
import com.ithinkrok.cw.metadata.TeamStatsHolderGroup;
import com.ithinkrok.minigames.base.GameGroup;
import com.ithinkrok.minigames.base.GameState;
import com.ithinkrok.minigames.base.User;
import com.ithinkrok.minigames.base.event.CommandEvent;
import com.ithinkrok.minigames.base.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.base.event.MinigamesEventHandler;
import com.ithinkrok.minigames.base.event.game.GameStateChangedEvent;
import com.ithinkrok.minigames.base.event.map.MapCreatureSpawnEvent;
import com.ithinkrok.minigames.base.event.map.MapItemSpawnEvent;
import com.ithinkrok.minigames.base.event.user.game.UserChangeKitEvent;
import com.ithinkrok.minigames.base.event.user.game.UserChangeTeamEvent;
import com.ithinkrok.minigames.base.event.user.game.UserJoinEvent;
import com.ithinkrok.minigames.base.event.user.game.UserQuitEvent;
import com.ithinkrok.minigames.base.event.user.world.UserDropItemEvent;
import com.ithinkrok.minigames.base.util.InventoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.Random;

/**
 * Created by paul on 14/01/16.
 */
public class BaseGameStateListener implements Listener {

    protected Random random = new Random();

    protected String quitLocale;
    protected String joinLocale;

    protected GameState gameState;

    @MinigamesEventHandler
    public void onListenerLoaded(ListenerLoadedEvent<GameGroup, GameState> event) {
        gameState = event.getRepresenting();

        ConfigurationSection config = event.getConfig();
        if (config == null) config = new MemoryConfiguration();

        if (quitLocale == null) quitLocale = config.getString("user_quit_locale", "user.quit");
        if (joinLocale == null) joinLocale = config.getString("user_join_locale", "user.join");
    }

    @MinigamesEventHandler
    public void onUserDropItem(UserDropItemEvent event) {
        if (InventoryUtils.getIdentifier(event.getItem().getItemStack()) == -1) return;

        event.setCancelled(true);
    }

    @MinigamesEventHandler
    public void onCommand(CommandEvent event) {
        switch (event.getCommand().getCommand().toLowerCase()) {
            case "kill":
            case "suicide":
                event.setHandled(true);
        }
    }

    @MinigamesEventHandler
    public void onCreatureSpawn(MapCreatureSpawnEvent event) {
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM) return;

        event.setCancelled(true);
    }

    @MinigamesEventHandler
    public void onItemSpawn(MapItemSpawnEvent event) {
        event.setCancelled(true);
    }

    @MinigamesEventHandler
    public void onUserChangeTeam(UserChangeTeamEvent event) {
        if (event.getOldTeam() != null) {
            TeamStatsHolderGroup oldStats = TeamStatsHolderGroup.getOrCreate(event.getOldTeam());
            oldStats.removeUser(event.getUser());
        }

        if (event.getNewTeam() != null) {
            TeamStatsHolderGroup newStats = TeamStatsHolderGroup.getOrCreate(event.getNewTeam());
            newStats.addUser(event.getUser());

            StatsHolder statsHolder = StatsHolder.getOrCreate(event.getUser());
            statsHolder.setLastTeam(event.getNewTeam().getName());
        }
    }

    @MinigamesEventHandler(priority = MinigamesEventHandler.HIGH)
    public void saveStatsOnUserQuit(UserQuitEvent event) {
        StatsHolder statsHolder = StatsHolder.getOrCreate(event.getUser());

        statsHolder.saveStats();

        if (event.getRemoveUser()) {
            statsHolder.setUser(null);
        }
    }

    @MinigamesEventHandler(priority = MinigamesEventHandler.MONITOR)
    public void sendQuitMessageOnUserQuit(UserQuitEvent event) {
        String name = event.getUser().getFormattedName();
        int currentPlayers = event.getUserGameGroup().getUserCount() - 1;
        int maxPlayers = Bukkit.getMaxPlayers();

        event.getUserGameGroup().sendLocale(quitLocale, name, currentPlayers, maxPlayers);
    }

    @MinigamesEventHandler(priority = MinigamesEventHandler.FIRST)
    public void sendJoinMessageOnUserJoin(UserJoinEvent event) {
        String name = event.getUser().getFormattedName();
        int currentPlayers = event.getUserGameGroup().getUserCount();
        int maxPlayers = Bukkit.getMaxPlayers();

        event.getUserGameGroup().sendLocale(joinLocale, name, currentPlayers, maxPlayers);
    }

    @MinigamesEventHandler
    public void onUserChangeKit(UserChangeKitEvent event) {
        if (event.getNewKit() == null) return;

        StatsHolder statsHolder = StatsHolder.getOrCreate(event.getUser());
        statsHolder.setLastKit(event.getNewKit().getName());
    }

    @MinigamesEventHandler
    public void onGameStateChange(GameStateChangedEvent event) {
        for (User user : event.getGameGroup().getUsers()) {
            StatsHolder statsHolder = StatsHolder.getOrCreate(user);
            statsHolder.saveStats();
        }
    }
}
