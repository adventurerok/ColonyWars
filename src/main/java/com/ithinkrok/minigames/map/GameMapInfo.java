package com.ithinkrok.minigames.map;

import org.bukkit.configuration.ConfigurationSection;

/**
 * Created by paul on 01/01/16.
 */
public class GameMapInfo {

    private String name;
    private ConfigurationSection config;

    public GameMapInfo(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public ConfigurationSection getConfig() {
        return config;
    }

    public String getMapFolder() {
        return config.getString("folder");
    }
}
