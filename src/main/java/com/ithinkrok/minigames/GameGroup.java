package com.ithinkrok.minigames;

import com.ithinkrok.minigames.event.game.GameEvent;
import com.ithinkrok.minigames.event.game.GameStateChangedEvent;
import com.ithinkrok.minigames.event.game.MapChangedEvent;
import com.ithinkrok.minigames.event.user.UserEvent;
import com.ithinkrok.minigames.event.user.game.UserJoinEvent;
import com.ithinkrok.minigames.event.user.game.UserQuitEvent;
import com.ithinkrok.minigames.lang.LangFile;
import com.ithinkrok.minigames.lang.LanguageLookup;
import com.ithinkrok.minigames.lang.Messagable;
import com.ithinkrok.minigames.map.GameMap;
import com.ithinkrok.minigames.map.GameMapInfo;
import com.ithinkrok.minigames.task.GameRunnable;
import com.ithinkrok.minigames.task.GameTask;
import com.ithinkrok.minigames.task.TaskList;
import com.ithinkrok.minigames.task.TaskScheduler;
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
public abstract class GameGroup<U extends User<U, T, G, M>, T extends Team<U, T, G>, G extends GameGroup<U, T, G, M>,
        M extends Game<U, T, G, M>> implements LanguageLookup, Messagable, TaskScheduler {

    private ConcurrentMap<UUID, U> usersInGroup = new ConcurrentHashMap<>();
    private Map<TeamColor, T> teamsInGroup = new HashMap<>();
    private M game;

    private Map<String, GameState> gameStates = new HashMap<>();
    private GameState gameState;

    private Constructor<T> teamConstructor;

    private GameMap currentMap;

    private TaskList gameGroupTaskList = new TaskList();
    private TaskList gameStateTaskList = new TaskList();

    public GameGroup(M game, Constructor<T> teamConstructor) {
        this.game = game;
        this.teamConstructor = teamConstructor;

        boolean hasDefault = false;
        for (GameState gs : game.getGameStates()) {
            if (!hasDefault) {
                changeGameState(gs);
                hasDefault = true;
            }

            this.gameStates.put(gs.getName(), gs);
        }

        changeMap(game.getStartMapInfo());
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

    public void userQuitEvent(UserQuitEvent<U> event) {
        userEvent(event);

        if(event.getRemoveUser()) {
            usersInGroup.remove(event.getUser().getUuid());
            //TODO remove user from team
        }
    }

    public LangFile loadLangFile(String path) {
        return game.loadLangFile(path);
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

        gameStateTaskList.cancelAllTasks();

        this.gameState = gameState;
    }

    public void gameEvent(GameEvent<G> event) {
        EventExecutor.executeEvent(event, getListeners());
    }

    public void unload() {
        currentMap.unloadMap();
    }

    public void bindTaskToCurrentGameState(GameTask task) {
        gameStateTaskList.addTask(task);
    }

    public void bindTaskToCurrentMap(GameTask task) {
        if(currentMap == null) throw new RuntimeException("No GameMap to bind task to");
        currentMap.bindTaskToMap(task);
    }

    @Override
    public String getLocale(String name) {
        if(currentMap != null && currentMap.hasLocale(name)) return currentMap.getLocale(name);
        else return game.getLocale(name);
    }

    @Override
    public boolean hasLocale(String name) {
        return (currentMap != null && currentMap.hasLocale(name) || game.hasLocale(name));
    }

    @Override
    public String getLocale(String name, Object...args) {
        if(currentMap != null && currentMap.hasLocale(name)) return currentMap.getLocale(name, args);
        else return game.getLocale(name, args);
    }

    public String getChatPrefix() {
        return game.getChatPrefix();
    }

    @Override
    public void sendMessage(String message) {
        sendMessageNoPrefix(getChatPrefix() + message);
    }

    @Override
    public void sendMessageNoPrefix(String message) {
        for(U user : usersInGroup.values()) {
            user.sendMessageNoPrefix(message);
        }
    }

    @Override
    public void sendLocale(String locale, Object...args) {
        sendMessage(getLocale(locale, args));
    }

    @Override
    public void sendLocaleNoPrefix(String locale, Object...args) {
        sendMessageNoPrefix(getLocale(locale, args));
    }

    @Override
    public GameTask doInFuture(GameRunnable task) {
        GameTask gameTask = game.doInFuture(task);

        gameGroupTaskList.addTask(gameTask);
        return gameTask;
    }

    @Override
    public GameTask doInFuture(GameRunnable task, int delay) {
        GameTask gameTask = game.doInFuture(task, delay);

        gameGroupTaskList.addTask(gameTask);
        return gameTask;
    }

    @Override
    public GameTask repeatInFuture(GameRunnable task, int delay, int period) {
        GameTask gameTask = game.repeatInFuture(task, delay, period);

        gameGroupTaskList.addTask(gameTask);
        return gameTask;
    }

    @Override
    public void cancelAllTasks() {
        gameGroupTaskList.cancelAllTasks();
    }
}
