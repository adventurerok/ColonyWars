package com.ithinkrok.minigames.event.team;

import com.ithinkrok.minigames.Team;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Created by paul on 08/01/16.
 */
public class TeamEvent extends Event {

    private final Team team;

    public TeamEvent(Team team) {
        this.team = team;
    }

    public Team getTeam() {
        return team;
    }

    @Override
    public HandlerList getHandlers() {
        return null;
    }
}
