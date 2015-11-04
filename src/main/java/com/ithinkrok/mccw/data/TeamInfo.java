package com.ithinkrok.mccw.data;

import com.ithinkrok.mccw.WarsPlugin;
import com.ithinkrok.mccw.enumeration.TeamColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by paul on 02/11/15.
 */
public class TeamInfo {

    private TeamColor teamColor;
    private ArrayList<Player> players = new ArrayList<>();
    private WarsPlugin plugin;
    private int teamCash;

    private HashMap<String, Integer> buildingCounts = new HashMap<>();

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

    public int getBuildingCount(String buildingType){
        Integer integer = buildingCounts.get(buildingType);

        return integer == null ? 0 : integer;
    }

    public void addBuilding(String buildingType){
        buildingCounts.put(buildingType, getBuildingCount(buildingType) + 1);
    }

    public void removeBuilding(String buildingType){
        buildingCounts.put(buildingType, Math.max(getBuildingCount(buildingType) - 1, 0));
    }
}
