package com.ithinkrok.mccw;

import com.ithinkrok.mccw.enumeration.TeamColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

/**
 * Created by paul on 01/11/15.
 *
 * The main plugin class for Colony Wars
 */
public class WarsPlugin extends JavaPlugin {

    private HashMap<UUID, PlayerInfo> playerInfoHashMap = new HashMap<>();
    private EnumMap<TeamColor, TeamData> teamDataEnumMap = new EnumMap<>(TeamColor.class);
    private Random random = new Random();

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public void onEnable() {

        WarsListener pluginListener = new WarsListener(this);
        getServer().getPluginManager().registerEvents(pluginListener, this);

        for(TeamColor c : TeamColor.values()){
            teamDataEnumMap.put(c, new TeamData(c));
        }
    }

    public PlayerInfo getPlayerInfo(Player player){
        return playerInfoHashMap.get(player.getUniqueId());
    }

    public void setPlayerInfo(Player player, PlayerInfo playerInfo){
        if(playerInfo == null) playerInfoHashMap.remove(player.getUniqueId());
        else playerInfoHashMap.put(player.getUniqueId(), playerInfo);
    }

    public Random getRandom() {
        return random;
    }

    public TeamData getTeamData(TeamColor teamColor){
        return teamDataEnumMap.get(teamColor);
    }

    public void setPlayerTeam(Player player, TeamColor teamColor){
        PlayerInfo playerInfo = getPlayerInfo(player);

        if(playerInfo.getTeamColor() != null){
            getTeamData(playerInfo.getTeamColor()).removePlayer(player);
        }

        playerInfo.setTeamColor(teamColor);
        getTeamData(teamColor).addPlayer(player);
    }
}
