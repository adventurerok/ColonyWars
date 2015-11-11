package com.ithinkrok.mccw.enumeration;

/**
 * Created by paul on 09/11/15.
 */
public enum CountdownType {

    GAME_START("start"),
    SHOWDOWN_START("showdown"),
    GAME_END("end");

    public final String name;

    CountdownType(String name) {
        this.name = name;
    }
}
