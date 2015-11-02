package com.ithinkrok.mccw;

import com.ithinkrok.mccw.enumeration.TeamColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

/**
 * Created by paul on 01/11/15.
 *
 * Stores the player's info while they are online
 */
public class PlayerInfo {

    private Player player;
    private TeamColor teamColor;

    private int playerCash = 0;

    public PlayerInfo(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public TeamColor getTeamColor() {
        return teamColor;
    }

    public int getPlayerCash() {
        return playerCash;
    }

    public void addPlayerCash(int cash){
        playerCash += cash;
        updateScoreboard();
    }

    public void setupScoreboard(){
        Scoreboard scoreboard = player.getScoreboard();

        Objective mainObjective = scoreboard.registerNewObjective("main", "dummy");
        mainObjective.setDisplayName("Stats:");
        mainObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
        mainObjective.getScore("Player Money:").setScore(0);
        mainObjective.getScore("Team Money:").setScore(0);
    }

    private void updateScoreboard(){
        Scoreboard scoreboard = player.getScoreboard();

        Objective mainObjective = scoreboard.getObjective("main");
        mainObjective.getScore("Player Money:").setScore(getPlayerCash());
    }
}
