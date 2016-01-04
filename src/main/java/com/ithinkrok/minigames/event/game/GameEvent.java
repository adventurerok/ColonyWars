package com.ithinkrok.minigames.event.game;

import com.ithinkrok.minigames.GameGroup;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Created by paul on 02/01/16.
 */
public class GameEvent extends Event{

    private final GameGroup gameGroup;

    public GameEvent(GameGroup gameGroup) {
        this.gameGroup = gameGroup;
    }

    public GameGroup getGameGroup() {
        return gameGroup;
    }

    @Override
    public HandlerList getHandlers() {
        return null;
    }
}
