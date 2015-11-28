package com.ithinkrok.mccw.playerclass;

import com.ithinkrok.mccw.WarsPlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Created by paul on 19/11/15.
 * <p>
 * A interface for a factory to create PlayerClassHandlers
 */
public interface PlayerClassHandlerFactory {

    /**
     * Creates a PlayerClassHandler
     *
     * @param plugin The Colony Wars plugin instance
     * @param config The configuration to use while creating the PlayerClassHandler
     * @return A new PlayerClassHandler
     */
    PlayerClassHandler createPlayerClassHandler(WarsPlugin plugin, ConfigurationSection config);
}
