package com.ithinkrok.mccw.enumeration;

/**
 * Created by paul on 09/11/15.
 *
 * An enum for the three countdown types.
 */
public enum CountdownType {

    GAME("game"),
    SHOWDOWN("showdown"),
    LOBBY("lobby");

    public final String name;

    CountdownType(String name) {
        this.name = name;
    }
}
