package com.ithinkrok.mccw;

import com.ithinkrok.mccw.enumeration.TeamColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;

/**
 * Created by paul on 02/11/15.
 */
public class TeamInfo {

    private TeamColor teamColor;
    private ArrayList<Player> players = new ArrayList<>();
    private WarsPlugin plugin;
    private int teamCash;

    public TeamInfo(WarsPlugin plugin, TeamColor teamColor) {
        this.plugin = plugin;
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

    public int getTeamCash() {
        return teamCash;
    }

    public void addTeamCash(int cash){
        this.teamCash += cash;

        updatePlayerScoreboards();
    }

    public void updatePlayerScoreboards(){
        for(Player p : players){
            plugin.getPlayerInfo(p).updateScoreboard();
        }
    }
}
