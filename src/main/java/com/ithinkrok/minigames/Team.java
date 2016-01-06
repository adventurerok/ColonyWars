package com.ithinkrok.minigames;

import com.ithinkrok.oldmccw.data.TeamColor;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by paul on 31/12/15.
 */
public class Team implements Listener {

    private TeamColor teamColor;
    private List<User> usersInTeam = new ArrayList<>();
    private GameGroup gameGroup;

    public Team(TeamColor teamColor, GameGroup gameGroup) {
        this.teamColor = teamColor;
        this.gameGroup = gameGroup;
    }
}
