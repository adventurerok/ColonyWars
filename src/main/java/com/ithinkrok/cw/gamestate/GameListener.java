package com.ithinkrok.cw.gamestate;

import com.ithinkrok.minigames.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.event.map.MapBlockBreakNaturallyEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Created by paul on 05/01/16.
 */
public class GameListener implements Listener {

    @EventHandler
    public void onListenerLoaded(ListenerLoadedEvent<?> event) {

    }

    @EventHandler
    public void onBlockBreakNaturally(MapBlockBreakNaturallyEvent event) {
        //event.getMap().
    }
}
