package com.ithinkrok.minigames;

import org.bukkit.event.Listener;

import java.util.Collection;

/**
 * Created by paul on 07/01/16.
 */
public class Kit {

    private final String name;
    private final String formattedName;
    private final Collection<Listener> listeners;

    public Kit(String name, Collection<Listener> listeners) {
        this(name, name, listeners);
    }

    public Kit(String name, String formattedName, Collection<Listener> listeners) {
        this.name = name;
        this.formattedName = formattedName;
        this.listeners = listeners;
    }

    public String getName() {
        return name;
    }

    public String getFormattedName() {
        return formattedName;
    }

    public Collection<Listener> getListeners() {
        return listeners;
    }

    public boolean isKitListener(Listener listener) {
        return listeners.contains(listener);
    }
}
