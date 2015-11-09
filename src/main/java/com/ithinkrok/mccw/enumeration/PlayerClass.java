package com.ithinkrok.mccw.enumeration;

/**
 * Created by paul on 05/11/15.
 */
public enum PlayerClass {

    CLOAKER("Cloaker"),
    SCOUT("Scout"),
    GENERAL("General");

    public final String name;

    PlayerClass(String name) {
        this.name = name;
    }
}
