package com.ithinkrok.minigames;

import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by paul on 31/12/15.
 */
public class GameState {

    private final String name;
    private final Collection<Listener> listeners;

    public GameState(String name, Collection<Listener> listeners) {
        this.name = name;
        this.listeners = listeners;
    }

    public String getName() {
        return name;
    }

    public Collection<Listener> getListeners() {
        return listeners;
    }
}
