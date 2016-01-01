package com.ithinkrok.minigames;

import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by paul on 31/12/15.
 */
public class GameState {

    private String name;
    private Collection<Listener> listeners;

    public GameState(String name, Listener listener) {
        this.name = name;
        this.listeners = new ArrayList<>();

        listeners.add(listener);
    }

    public String getName() {
        return name;
    }

    public Collection<Listener> getListeners() {
        return listeners;
    }
}
