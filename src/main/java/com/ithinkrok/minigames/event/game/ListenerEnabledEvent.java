package com.ithinkrok.minigames.event.game;

import com.ithinkrok.minigames.GameGroup;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Created by paul on 02/01/16.
 *
 * Called on a listener when it is enabled (ready and going to start receiving events)
 */
public class ListenerEnabledEvent<G extends GameGroup> extends GameEvent<G> {

    private final ConfigurationSection config;

    public ListenerEnabledEvent(G gameGroup, ConfigurationSection config) {
        super(gameGroup);
        this.config = config;
    }

    public ConfigurationSection getConfig() {
        return config;
    }

    public boolean hasConfig() {
        return getConfig() != null;
    }
}
