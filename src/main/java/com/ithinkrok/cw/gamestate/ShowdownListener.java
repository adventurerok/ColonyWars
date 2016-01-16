package com.ithinkrok.cw.gamestate;

import com.ithinkrok.cw.metadata.ShowdownArena;
import com.ithinkrok.minigames.User;
import com.ithinkrok.minigames.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.event.game.GameStateChangedEvent;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.event.EventHandler;

/**
 * Created by paul on 16/01/16.
 */
public class ShowdownListener extends BaseGameListener {

    private String showdownGameState;

    @EventHandler
    public void onListenerLoaded(ListenerLoadedEvent<?> event) {
        super.onListenerLoaded(event);

        ConfigurationSection config = event.getConfig();
        if(config == null) config = new MemoryConfiguration();

        showdownGameState = config.getString("showdown_gamestate", "showdown");

    }

    @EventHandler
    public void onGameStateChanged(GameStateChangedEvent event) {
        if(!event.getNewGameState().getName().equals(showdownGameState)) return;

        ShowdownArena arena = new ShowdownArena(event.getGameGroup());
        event.getGameGroup().setMetadata(arena);

        int x = arena.getRadiusX();
        int z = arena.getRadiusZ();

        for(User user : event.getGameGroup().getUsers()) {
            int offsetX = (-x/2) + random.nextInt(x);
            int offsetZ = (-z/2) + random.nextInt(z);
            int offsetY = 2;

            Location teleport = arena.getCenter().clone();
            teleport.setX(teleport.getX() + offsetX);
            teleport.setY(teleport.getY() + offsetY);
            teleport.setZ(teleport.getZ() + offsetZ);

            user.teleport(teleport);
        }

        arena.startShrinkTask();
        arena.startCheckTasks();
    }
}
