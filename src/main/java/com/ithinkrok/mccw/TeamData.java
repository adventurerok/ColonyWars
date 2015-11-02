package com.ithinkrok.mccw;

import com.ithinkrok.mccw.enumeration.TeamColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;

/**
 * Created by paul on 02/11/15.
 */
public class TeamData {

    private TeamColor teamColor;
    private ArrayList<Player> players = new ArrayList<>();

    public TeamData(TeamColor teamColor) {
        this.teamColor = teamColor;
    }

    public TeamColor getTeamColor() {
        return teamColor;
    }

    public void addPlayer(Player player){
        if(players.contains(player)) return;

        players.add(player);
    }

    public void removePlayer(Player player){
        players.remove(player);
    }

    public boolean hasPlayer(Player player){
        return players.contains(player);
    }
}
