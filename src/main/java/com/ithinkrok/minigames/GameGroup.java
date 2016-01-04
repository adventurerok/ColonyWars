package com.ithinkrok.minigames;

import com.ithinkrok.minigames.event.game.GameEvent;
import com.ithinkrok.minigames.event.game.GameStateChangedEvent;
import com.ithinkrok.minigames.event.game.MapChangedEvent;
import com.ithinkrok.minigames.event.user.UserEvent;
import com.ithinkrok.minigames.event.user.game.UserJoinEvent;
import com.ithinkrok.minigames.event.user.game.UserQuitEvent;
import com.ithinkrok.minigames.item.CustomItem;
import com.ithinkrok.minigames.lang.LangFile;
import com.ithinkrok.minigames.lang.LanguageLookup;
import com.ithinkrok.minigames.lang.Messagable;
import com.ithinkrok.minigames.map.GameMap;
import com.ithinkrok.minigames.map.GameMapInfo;
import com.ithinkrok.minigames.task.GameRunnable;
import com.ithinkrok.minigames.task.GameTask;
import com.ithinkrok.minigames.task.TaskList;
import com.ithinkrok.minigames.task.TaskScheduler;
import com.ithinkrok.minigames.user.UserResolver;
import com.ithinkrok.minigames.util.EventExecutor;
import com.ithinkrok.minigames.util.io.FileLoader;
import org.bukkit.configuration.ConfigurationSection;
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
        M extends Game<U, T, G, M>> implements LanguageLookup, Messagable, TaskScheduler, UserResolver<U>, FileLoader {

    private ConcurrentMap<UUID, U> usersInGroup = new ConcurrentHashMap<>();
    private Map<TeamColor, T> teamsInGroup = new HashMap<>();
    private M game;

    private Map<String, GameState> gameStates = new HashMap<>();
    private GameState gameState;

    private Constructor<T> teamConstructor;

    private GameMap currentMap;

    private TaskList gameGroupTaskList = new TaskList();
    private TaskList gameStateTaskList = new TaskList();
    private HashMap<String, Listener> defaultListeners = new HashMap<>();

    private List<Listener> defaultAndMapListeners = new ArrayList<>();

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
        GameMap oldMap = currentMap;
        GameMap newMap = new GameMap(this, mapInfo);

        usersInGroup.values().forEach(newMap::teleportUser);

        currentMap = newMap;

        Event event = new MapChangedEvent<>((G) this, oldMap, newMap);

        EventExecutor.executeEvent(event, getListeners(newMap.getListeners()));

        defaultAndMapListeners = createDefaultAndMapListeners(newMap.getListenerMap());

        if (oldMap != null) oldMap.unloadMap();
    }

    @Override
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
        EventExecutor.executeEvent(event, getListeners(event.getUser().getListeners()));
    }

    public void userQuitEvent(UserQuitEvent<U> event) {
        userEvent(event);

        if(event.getRemoveUser()) {
            usersInGroup.remove(event.getUser().getUuid());
            //TODO remove user from team
        }
    }

    @Override
    public ConfigurationSection loadConfig(String name) {
        return game.loadConfig(name);
    }

    @Override
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
        listeners.add(defaultAndMapListeners);
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

    public CustomItem getCustomItem(String name) {
        CustomItem item = null;
        if(currentMap != null) item = currentMap.getCustomItem(name);
        return item != null ? item : game.getCustomItem(name);
    }

    public CustomItem getCustomItem(int identifier) {
        CustomItem item = null;
        if(currentMap != null) item = currentMap.getCustomItem(identifier);
        return item != null ? item : game.getCustomItem(identifier);
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

    public M getGame() {
        return game;
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

    public void setDefaultListeners(HashMap<String, Listener> defaultListeners) {
        this.defaultListeners = defaultListeners;

        if(currentMap != null) defaultAndMapListeners = createDefaultAndMapListeners(currentMap.getListenerMap());
        else defaultAndMapListeners = createDefaultAndMapListeners();
    }

    @SuppressWarnings("unchecked")
    @SafeVarargs
    private final List<Listener> createDefaultAndMapListeners(Map<String, Listener>... extra) {
        HashMap<String, Listener> clone = (HashMap<String, Listener>) defaultListeners.clone();

        for(Map<String, Listener> map : extra) {
            clone.putAll(map);
        }

        return new ArrayList<>(clone.values());
    }
}
