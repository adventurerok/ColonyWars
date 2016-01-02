package com.ithinkrok.minigames.event.game;

import com.ithinkrok.minigames.GameGroup;
import com.ithinkrok.minigames.map.GameMap;

/**
 * Created by paul on 02/01/16.
 */
public class MapChangedEvent<G extends GameGroup> extends GameEvent<G> {

    private final GameMap oldMap;
    private final GameMap newMap;

    public MapChangedEvent(G gameGroup, GameMap oldMap, GameMap newMap) {
        super(gameGroup);
        this.oldMap = oldMap;
        this.newMap = newMap;
    }

    public GameMap getOldMap() {
        return oldMap;
    }

    public GameMap getNewMap() {
        return newMap;
    }
}
