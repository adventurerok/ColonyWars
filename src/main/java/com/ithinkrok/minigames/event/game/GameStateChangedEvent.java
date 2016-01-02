package com.ithinkrok.minigames.event.game;

import com.ithinkrok.minigames.GameGroup;
import com.ithinkrok.minigames.GameState;

/**
 * Created by paul on 02/01/16.
 */
public class GameStateChangedEvent<G extends GameGroup> extends GameEvent<G> {

    private final GameState oldGameState;
    private final GameState newGameState;

    public GameStateChangedEvent(G gameGroup, GameState oldGameState, GameState newGameState) {
        super(gameGroup);
        this.oldGameState = oldGameState;
        this.newGameState = newGameState;
    }

    public GameState getOldGameState() {
        return oldGameState;
    }

    public GameState getNewGameState() {
        return newGameState;
    }
}
