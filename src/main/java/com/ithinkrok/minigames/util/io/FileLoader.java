package com.ithinkrok.minigames.util.io;

import com.ithinkrok.minigames.lang.LanguageLookup;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Created by paul on 04/01/16.
 */
public interface FileLoader {

    ConfigurationSection loadConfig(String name);
    LanguageLookup loadLangFile(String name);
}
