package com.ithinkrok.minigames.util.playerstate;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;

/**
 * Created by paul on 01/01/16.
 */
public class ModeCapture {

    float exp;
    float flySpeed;
    float walkSpeed;
    int level;
    boolean allowFlight;
    GameMode gameMode;

    public ModeCapture(Player capture){
        exp = capture.getExp();
        level = capture.getLevel();
        flySpeed = capture.getFlySpeed();
        walkSpeed = capture.getWalkSpeed();
        allowFlight = capture.getAllowFlight();
        gameMode = capture.getGameMode();
    }

    public void restore(Player to){
        to.setExp(exp);
        to.setLevel(level);
        to.setFlySpeed(flySpeed);
        to.setWalkSpeed(walkSpeed);
        to.setAllowFlight(allowFlight);
        to.setGameMode(gameMode);
    }

}
