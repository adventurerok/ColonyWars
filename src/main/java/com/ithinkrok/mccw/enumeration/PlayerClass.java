package com.ithinkrok.mccw.enumeration;

/**
 * Created by paul on 05/11/15.
 *
 * An enum to represent the available classes in the game
 */
public enum PlayerClass {

    CLOAKER("Cloaker"),
    SCOUT("Scout"),
    GENERAL("General"),
    ARCHER("Archer");

    public final String name;

    PlayerClass(String name) {
        this.name = name;
    }
}
