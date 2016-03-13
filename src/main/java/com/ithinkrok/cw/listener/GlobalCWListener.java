package com.ithinkrok.cw.listener;

import com.ithinkrok.cw.metadata.StatsHolder;
import com.ithinkrok.cw.metadata.TeamStatsHolderGroup;
import com.ithinkrok.minigames.api.event.game.GameStateChangedEvent;
import com.ithinkrok.minigames.api.event.user.game.UserChangeKitEvent;
import com.ithinkrok.minigames.api.event.user.game.UserChangeTeamEvent;
import com.ithinkrok.minigames.api.event.user.game.UserQuitEvent;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.util.event.CustomListener;

/**
 * Created by paul on 30/01/16.
 */
public class GlobalCWListener implements CustomListener {

    @CustomEventHandler(priority = CustomEventHandler.HIGH)
    public void saveStatsOnUserQuit(UserQuitEvent event) {
        StatsHolder statsHolder = StatsHolder.getOrCreate(event.getUser());

        statsHolder.saveStats();

        if (event.getRemoveUser()) {
            statsHolder.setUser(null);
        }
    }

    @CustomEventHandler
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

    @CustomEventHandler
    public void onUserChangeKit(UserChangeKitEvent event) {
        if (event.getNewKit() == null) return;

        StatsHolder statsHolder = StatsHolder.getOrCreate(event.getUser());
        statsHolder.setLastKit(event.getNewKit().getName());
    }

    @CustomEventHandler
    public void onGameStateChange(GameStateChangedEvent event) {
        for (User user : event.getGameGroup().getUsers()) {
            StatsHolder statsHolder = StatsHolder.getOrCreate(user);
            statsHolder.saveStats();
        }
    }
}
