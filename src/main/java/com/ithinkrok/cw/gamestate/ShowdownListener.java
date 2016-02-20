package com.ithinkrok.cw.gamestate;

import com.ithinkrok.cw.metadata.CWTeamStats;
import com.ithinkrok.cw.metadata.ShowdownArena;
import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.User;
import com.ithinkrok.minigames.base.event.game.GameStateChangedEvent;
import com.ithinkrok.util.event.CustomEventHandler;
import org.bukkit.Location;

/**
 * Created by paul on 16/01/16.
 */
public class ShowdownListener extends BaseGameListener {


    @Override
    protected void checkShowdownStart(GameGroup gameGroup, int teamsInGame, int nonZombieUsersInGame) {

    }

    @Override
    public boolean shouldRespawnUser(User user, CWTeamStats teamStats) {
        return false;
    }

    @CustomEventHandler
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
