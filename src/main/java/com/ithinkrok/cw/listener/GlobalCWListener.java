package com.ithinkrok.cw.listener;

import com.ithinkrok.cw.metadata.StatsHolder;
import com.ithinkrok.cw.metadata.TeamStatsHolderGroup;
import com.ithinkrok.minigames.base.User;
import com.ithinkrok.minigames.base.event.MinigamesEventHandler;
import com.ithinkrok.minigames.base.event.game.GameStateChangedEvent;
import com.ithinkrok.minigames.base.event.user.game.UserChangeKitEvent;
import com.ithinkrok.minigames.base.event.user.game.UserChangeTeamEvent;
import com.ithinkrok.minigames.base.event.user.game.UserQuitEvent;
import org.bukkit.event.Listener;

/**
 * Created by paul on 30/01/16.
 */
public class GlobalCWListener implements Listener {

    @MinigamesEventHandler(priority = MinigamesEventHandler.HIGH)
    public void saveStatsOnUserQuit(UserQuitEvent event) {
        StatsHolder statsHolder = StatsHolder.getOrCreate(event.getUser());

        statsHolder.saveStats();

        if (event.getRemoveUser()) {
            statsHolder.setUser(null);
        }
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
