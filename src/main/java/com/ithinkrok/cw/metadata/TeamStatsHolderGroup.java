package com.ithinkrok.cw.metadata;

import com.ithinkrok.minigames.api.User;
import com.ithinkrok.minigames.base.event.game.GameStateChangedEvent;
import com.ithinkrok.minigames.base.event.game.MapChangedEvent;
import com.ithinkrok.minigames.base.metadata.Metadata;
import com.ithinkrok.minigames.api.Team;

import java.util.ArrayList;

/**
 * Created by paul on 17/01/16.
 */
public class TeamStatsHolderGroup extends Metadata {

    private final Team team;
    private ArrayList<StatsHolder> statsHolders = new ArrayList<>();

    public TeamStatsHolderGroup(Team team) {
        this.team = team;
    }

    public static TeamStatsHolderGroup getOrCreate(Team team) {
        TeamStatsHolderGroup teamStatsHolderGroup = team.getMetadata(TeamStatsHolderGroup.class);

        if (teamStatsHolderGroup == null) {
            teamStatsHolderGroup = new TeamStatsHolderGroup(team);
            team.setMetadata(teamStatsHolderGroup);
        }

        return teamStatsHolderGroup;
    }

    public void addUser(User user) {
        StatsHolder statsHolder = StatsHolder.getOrCreate(user);

        if (statsHolders.contains(statsHolder)) return;
        statsHolders.add(statsHolder);
    }

    public void removeUser(User user) {
        if (!team.getGameGroup().getCurrentGameState().getName().equals("lobby")) return;
        StatsHolder statsHolder = StatsHolder.getOrCreate(user);

        statsHolders.remove(statsHolder);
    }

    public StatsHolder getStatsHolder(User user) {
        for (StatsHolder holder : statsHolders) {
            if (holder.getUniqueId().equals(user.getUuid())) return holder;
        }

        return null;
    }

    public void addGameWin() {
        for (StatsHolder statsHolder : statsHolders) {
            statsHolder.addGameWin();
            statsHolder.saveStats();
        }

        statsHolders.clear();
    }

    public void addGameLoss() {
        for (StatsHolder statsHolder : statsHolders) {
            statsHolder.addGameLoss();
            statsHolder.saveStats();
        }
        statsHolders.clear();
    }

    @Override
    public boolean removeOnGameStateChange(GameStateChangedEvent event) {
        return false;
    }

    @Override
    public boolean removeOnMapChange(MapChangedEvent event) {
        return false;
    }
}
