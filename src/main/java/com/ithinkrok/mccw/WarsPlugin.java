package com.ithinkrok.mccw;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

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
    private Random random = new Random();

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public void onEnable() {

        WarsListener pluginListener = new WarsListener(this);
        getServer().getPluginManager().registerEvents(pluginListener, this);
    }

    public PlayerInfo getPlayerInfo(Player player){
        return playerInfoHashMap.get(player.getUniqueId());
    }

    public void setupScoreboard(Player player){
        Scoreboard scoreboard = player.getScoreboard();

        Objective mainObjective = scoreboard.registerNewObjective("main", "dummy");
        mainObjective.setDisplayName("Stats:");
        mainObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
        mainObjective.getScore("Player Money:").setScore(0);
        mainObjective.getScore("Team Money:").setScore(0);

    }

    public void updateScoreboard(Player player){
        PlayerInfo playerInfo = getPlayerInfo(player);
        Scoreboard scoreboard = player.getScoreboard();

        Objective mainObjective = scoreboard.getObjective("main");
        mainObjective.getScore("Player Money:").setScore(playerInfo.getPlayerCash());
    }

    public void setPlayerInfo(Player player, PlayerInfo playerInfo){
        if(playerInfo == null) playerInfoHashMap.remove(player.getUniqueId());
        else playerInfoHashMap.put(player.getUniqueId(), playerInfo);
    }

    public Random getRandom() {
        return random;
    }
}
