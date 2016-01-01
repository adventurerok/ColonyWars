package com.ithinkrok.minigames.util.playerstate;

import org.bukkit.entity.Player;

/**
 * Created by paul on 01/01/16.
 */
public class FoodCapture {

    private float exhaustion;
    private float saturation;
    private int foodLevel;

    public FoodCapture(Player capture){
        exhaustion = capture.getExhaustion();
        saturation = capture.getSaturation();
        foodLevel = capture.getFoodLevel();
    }

    public void restore(Player to){
        to.setExhaustion(exhaustion);
        to.setSaturation(saturation);
        to.setFoodLevel(foodLevel);
    }
}
