package com.ithinkrok.minigames;

import org.bukkit.event.Listener;

/**
 * Created by paul on 31/12/15.
 */
public class GameState {

    private String name;
    private Listener listener;

    public GameState(String name, Listener listener) {
        this.name = name;
        this.listener = listener;
    }

    public String getName() {
        return name;
    }

    public Listener getListener() {
        return listener;
    }
}
