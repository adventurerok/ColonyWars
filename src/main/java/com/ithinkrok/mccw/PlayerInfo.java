package com.ithinkrok.mccw;

import com.ithinkrok.mccw.enumeration.TeamColor;
import org.bukkit.entity.Player;

/**
 * Created by paul on 01/11/15.
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
    }
}
