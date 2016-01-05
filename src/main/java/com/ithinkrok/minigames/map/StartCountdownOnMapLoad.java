package com.ithinkrok.minigames.map;

import com.ithinkrok.minigames.GameGroup;
import com.ithinkrok.minigames.event.ListenerLoadedEvent;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Created by paul on 04/01/16.
 */
public class StartCountdownOnMapLoad implements Listener {

    @EventHandler
    public void onListenerEnabled(ListenerLoadedEvent<GameGroup> event) {
        ConfigurationSection config = event.getConfig();

        String name = config.getString("name");
        String localeStub = config.getString("locale_stub");
        int seconds = config.getInt("seconds");

        event.getCreator().startCountdown(name, localeStub, seconds);
    }
}