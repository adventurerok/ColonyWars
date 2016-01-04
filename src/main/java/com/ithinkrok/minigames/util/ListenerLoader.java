package com.ithinkrok.minigames.util;

import com.ithinkrok.minigames.event.ListenerEnabledEvent;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Listener;

/**
 * Created by paul on 04/01/16.
 */
public class ListenerLoader {

    @SuppressWarnings("unchecked")
    public static Listener loadListener(Object creator, ConfigurationSection listenerConfig) throws Exception {
        String className = listenerConfig.getString("class");

        Class<? extends Listener> clazz = (Class<? extends Listener>) Class.forName(className);

        Listener listener = clazz.newInstance();

        ConfigurationSection config = null;
        if (listenerConfig.contains("config")) config = listenerConfig.getConfigurationSection("config");

        EventExecutor.executeEvent(new ListenerEnabledEvent<>(creator, config), listener);

        return listener;

    }
}
