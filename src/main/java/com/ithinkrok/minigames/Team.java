package com.ithinkrok.minigames;

import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by paul on 31/12/15.
 */
public class Team implements Listener {

    private TeamIdentifier teamIdentifier;
    private List<User> usersInTeam = new ArrayList<>();
    private GameGroup gameGroup;

    public Team(TeamIdentifier teamIdentifier, GameGroup gameGroup) {
        this.teamIdentifier = teamIdentifier;
        this.gameGroup = gameGroup;
    }
}
