package com.ithinkrok.minigames.event.game;

import com.ithinkrok.minigames.GameGroup;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Created by paul on 02/01/16.
 */
public class GameEvent<G extends GameGroup> extends Event{

    private final G gameGroup;

    public GameEvent(G gameGroup) {
        this.gameGroup = gameGroup;
    }

    public G getGameGroup() {
        return gameGroup;
    }

    @Override
    public HandlerList getHandlers() {
        return null;
    }
}
