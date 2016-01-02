package com.ithinkrok.minigames;

import com.ithinkrok.minigames.event.game.GameEvent;
import com.ithinkrok.minigames.event.game.GameStateChangedEvent;
import com.ithinkrok.minigames.event.game.MapChangedEvent;
import com.ithinkrok.minigames.event.user.UserEvent;
import com.ithinkrok.minigames.event.user.UserJoinEvent;
import com.ithinkrok.minigames.map.GameMap;
import com.ithinkrok.minigames.map.GameMapInfo;
import com.ithinkrok.minigames.util.EventExecutor;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by paul on 31/12/15.
 */
public abstract class GameGroup<U extends User<U, T, G, M>, T extends Team<U, T, G>, G extends GameGroup<U, T, G, M>, M extends Game<U, T, G, M>> {

    private ConcurrentMap<UUID, U> usersInGroup = new ConcurrentHashMap<>();
    private Map<TeamColor, T> teamsInGroup = new HashMap<>();
    private M minigame;

    private Map<String, GameState> gameStates = new HashMap<>();
    private GameState gameState;

    private Constructor<T> teamConstructor;

    private GameMap currentMap;

    public GameGroup(M minigame, Constructor<T> teamConstructor) {
        this.minigame = minigame;
        this.teamConstructor = teamConstructor;

        boolean hasDefault = false;
        for (GameState gs : minigame.getGameStates()) {
            if (!hasDefault) {
                changeGameState(gs);
                hasDefault = true;
            }

            this.gameStates.put(gs.getName(), gs);
        }

        changeMap(minigame.getStartMapInfo());
    }

    @SuppressWarnings("unchecked")
    public void changeMap(GameMapInfo mapInfo) {
        GameMap newMap = new GameMap(this, mapInfo);

        usersInGroup.values().forEach(newMap::teleportUser);

        Event event = new MapChangedEvent<>((G) this, currentMap, newMap);

        EventExecutor.executeEvent(event, getListeners(newMap.getListeners()));

        if (currentMap != null) currentMap.unloadMap();
        currentMap = newMap;
    }

    public U getUser(UUID uuid) {
        return usersInGroup.get(uuid);
    }

    private T createTeam(TeamColor teamColor) {
        try {
            return teamConstructor.newInstance(teamColor, this);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to construct Team", e);
        }
    }

    public void eventUserJoinedAsPlayer(UserJoinEvent<U> event) {
        usersInGroup.put(event.getUser().getUuid(), event.getUser());

        currentMap.teleportUser(event.getUser());

        userEvent(event);
    }

    public void userEvent(UserEvent<U> event) {
        EventExecutor.executeEvent(event, getListeners());
    }

    public void changeGameState(String gameStateName) {
        GameState gameState = gameStates.get(gameStateName);
        if (gameState == null) throw new IllegalArgumentException("Unknown game state name: " + gameStateName);

        changeGameState(gameState);
    }

    @SafeVarargs
    private final Collection<Collection<Listener>> getListeners(Collection<Listener>... extras) {
        Collection<Collection<Listener>> listeners = new ArrayList<>(4);
        if(gameState != null) listeners.add(gameState.getListeners());
        if(currentMap != null) listeners.add(currentMap.getListeners());
        Collections.addAll(listeners, extras);

        return listeners;
    }

    @SuppressWarnings("unchecked")
    public void changeGameState(GameState gameState) {
        if (gameState.equals(this.gameState)) return;

        Event event = new GameStateChangedEvent<>((G) this, this.gameState, gameState);

        EventExecutor.executeEvent(event, getListeners(gameState.getListeners()));

        this.gameState = gameState;
    }

    public void gameEvent(GameEvent<G> event) {
        EventExecutor.executeEvent(event, getListeners());
    }

    public void unload() {
        currentMap.unloadMap();
    }
}
