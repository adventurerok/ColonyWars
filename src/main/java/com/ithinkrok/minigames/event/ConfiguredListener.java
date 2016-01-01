package com.ithinkrok.minigames.event;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Listener;

/**
 * Created by paul on 01/01/16.
 *
 * A listener that can be configured with a ConfigurationSection. Useful for listeners attached to maps to allow
 * configuring lobby games etc...
 */
public interface ConfiguredListener extends Listener {

    void configure(ConfigurationSection config);

}
