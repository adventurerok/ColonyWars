package com.ithinkrok.mccw;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.util.HashMap;
import java.util.UUID;

/**
 * Created by paul on 01/11/15.
 *
 * The main plugin class for Colony Wars
 */
public class WarsPlugin extends JavaPlugin {

    private HashMap<UUID, PlayerInfo> playerInfoHashMap = new HashMap<>();

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

        Objective playerMoney = scoreboard.registerNewObjective("player_money", "dummy");
        playerMoney.setDisplayName("Player Money:");
        playerMoney.setDisplaySlot(DisplaySlot.SIDEBAR);
        playerMoney.getScore("test").setScore(1);

    }

    public void setPlayerInfo(Player player, PlayerInfo playerInfo){
        if(playerInfo == null) playerInfoHashMap.remove(player.getUniqueId());
        else playerInfoHashMap.put(player.getUniqueId(), playerInfo);
    }

}
