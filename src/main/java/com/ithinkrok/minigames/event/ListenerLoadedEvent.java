package com.ithinkrok.minigames.event;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Created by paul on 02/01/16.
 *
 * Called on a listener when it is enabled (but not necessarily before it starts receiving events)
 */
public class ListenerLoadedEvent<C> extends Event{

    private final C creator;
    private final ConfigurationSection config;

    public ListenerLoadedEvent(C creator, ConfigurationSection config) {
        this.creator = creator;
        this.config = config;
    }

    public C getCreator() {
        return creator;
    }

    public ConfigurationSection getConfig() {
        return config;
    }

    public boolean hasConfig() {
        return getConfig() != null;
    }

    @Override
    public HandlerList getHandlers() {
        return null;
    }
}
