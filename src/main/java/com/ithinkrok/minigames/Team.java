package com.ithinkrok.minigames;

import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by paul on 31/12/15.
 */
public abstract class Team<U extends User, T extends Team, G extends GameGroup> implements Listener {

    private TeamColor teamColor;
    private List<U> usersInTeam = new ArrayList<>();
    private G gameGroup;

    public Team(TeamColor teamColor, G gameGroup) {
        this.teamColor = teamColor;
        this.gameGroup = gameGroup;
    }
}
