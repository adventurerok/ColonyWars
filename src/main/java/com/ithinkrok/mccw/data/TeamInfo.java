package com.ithinkrok.mccw.data;

import com.ithinkrok.mccw.WarsPlugin;
import com.ithinkrok.mccw.enumeration.TeamColor;
import org.bukkit.ChatColor;
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

    private int buildingsConstructingNow = 0;

    private HashMap<String, Integer> buildingCounts = new HashMap<>();
    private HashMap<String, Integer> buildingNowCounts = new HashMap<>();

    public TeamInfo(WarsPlugin plugin, TeamColor teamColor) {
        this.plugin = plugin;
        this.teamColor = teamColor;
    }

    public void message(String message){
        for(Player p : players){
            p.sendMessage(WarsPlugin.CHAT_PREFIX + message);
        }
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
        teamCash += cash;

        updatePlayerScoreboards();
    }

    public int getTotalBuildingNowCount() {
        return buildingsConstructingNow;
    }

    public boolean subtractTeamCash(int cash){
        if(cash > teamCash) return false;
        teamCash -= cash;

        updatePlayerScoreboards();

        message(ChatColor.RED + "$" + cash + ChatColor.YELLOW + " were deducted from your Team's Account!");
        message("Your Team's new Balance is: " + ChatColor.GREEN + "$" + teamCash + "!");

        return true;
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

    public int getBuildingNowCount(String buildingType){
        Integer integer = buildingNowCounts.get(buildingType);

        return integer == null ? 0 : integer;
    }

    public HashMap<String, Integer> getBuildingNowCounts() {
        return buildingNowCounts;
    }

    public void buildingStarted(String buildingType){
        buildingsConstructingNow += 1;

        buildingNowCounts.put(buildingType, getBuildingNowCount(buildingType) + 1);

        updatePlayerScoreboards();
    }

    public void buildingFinished(String buildingType){
        buildingsConstructingNow -= 1;

        int buildingNowOfType = getBuildingNowCount(buildingType) - 1;
        if(buildingNowOfType > 0) buildingNowCounts.put(buildingType, buildingNowOfType);
        else buildingNowCounts.remove(buildingType);

        buildingCounts.put(buildingType, getBuildingCount(buildingType) + 1);

        updatePlayerScoreboards();
    }

    public void removeBuilding(String buildingType){
        buildingCounts.put(buildingType, Math.max(getBuildingCount(buildingType) - 1, 0));
    }

    public int getPlayerCount() {
        return players.size();
    }
}
