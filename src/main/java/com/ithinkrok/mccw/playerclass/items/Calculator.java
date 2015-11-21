package com.ithinkrok.mccw.playerclass.items;

/**
 * Created by paul on 18/11/15.
 * <p>
 * Calculates a number based on an input of a player level
 */
public interface Calculator {

    /**
     * Calculates a number based on an input of a player level
     *
     * @param playerLevel The level of the player in a certain skill
     * @return The calculated double
     */
    double calculate(int playerLevel);
}
