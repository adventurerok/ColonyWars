package com.ithinkrok.mccw.playerclass;

import com.ithinkrok.mccw.WarsPlugin;
import com.ithinkrok.mccw.enumeration.PlayerClass;
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
     * @param playerClass The class the handler is being made for
     * @return A new PlayerClassHandler
     */
    PlayerClassHandler createPlayerClassHandler(WarsPlugin plugin, PlayerClass playerClass);
}
