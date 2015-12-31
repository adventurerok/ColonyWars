package com.ithinkrok.minigames;

/**
 * Created by paul on 31/12/15.
 */
public class GameState<U extends User> {

    private String name;
    private GameStateHandler<U> gameStateHandler;

    public GameState(String name, GameStateHandler<U> gameStateHandler) {
        this.name = name;
        this.gameStateHandler = gameStateHandler;
    }

    public String getName() {
        return name;
    }

    public GameStateHandler<U> getHandler() {
        return gameStateHandler;
    }
}
