package com.ithinkrok.minigames.event.game;

import com.ithinkrok.minigames.GameGroup;
import com.ithinkrok.minigames.event.MinigamesEvent;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Created by paul on 02/01/16.
 */
public class GameEvent implements MinigamesEvent {

    private final GameGroup gameGroup;

    public GameEvent(GameGroup gameGroup) {
        this.gameGroup = gameGroup;
    }

    public GameGroup getGameGroup() {
        return gameGroup;
    }
}
