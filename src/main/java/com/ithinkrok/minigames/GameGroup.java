package com.ithinkrok.minigames;

import com.ithinkrok.minigames.event.game.CountdownFinishedEvent;
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
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by paul on 31/12/15.
 */
public class GameGroup implements LanguageLookup, Messagable, TaskScheduler, UserResolver, FileLoader {

    private ConcurrentMap<UUID, User> usersInGroup = new ConcurrentHashMap<>();
    private Map<TeamColor, Team> teamsInGroup = new HashMap<>();
    private Game game;

    private Map<String, GameState> gameStates = new HashMap<>();
    private GameState gameState;

    private GameMap currentMap;

    private TaskList gameGroupTaskList = new TaskList();
    private TaskList gameStateTaskList = new TaskList();
    private HashMap<String, Listener> defaultListeners = new HashMap<>();

    private List<Listener> defaultAndMapListeners = new ArrayList<>();

    private Countdown countdown;

    public GameGroup(Game game) {
        this.game = game;

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

    public Collection<User> getUsers() {
        return usersInGroup.values();
    }


    public void changeMap(String mapName) {
        GameMapInfo mapInfo = game.getMapInfo(mapName);
        Validate.notNull(mapInfo, "The map " + mapName + " does not exist");

        changeMap(mapInfo);
    }

    public GameMap getCurrentMap() {
        return currentMap;
    }

    public ConfigurationSection getSharedObject(String name) {
        ConfigurationSection result = null;
        if(currentMap != null) result = currentMap.getSharedObject(name);
        return result != null ? result : game.getSharedObject(name);
    }

    public void changeMap(GameMapInfo mapInfo) {
        GameMap oldMap = currentMap;
        GameMap newMap = new GameMap(this, mapInfo);

        usersInGroup.values().forEach(newMap::teleportUser);

        currentMap = newMap;

        game.setGameGroupForMap(this, newMap.getWorld().getName());

        Event event = new MapChangedEvent(this, oldMap, newMap);

        EventExecutor.executeEvent(event, getListeners(getAllUserListeners(), newMap.getListeners()));

        defaultAndMapListeners = createDefaultAndMapListeners(newMap.getListenerMap());

        if (oldMap != null) oldMap.unloadMap();
    }

    public Countdown getCountdown() {
        return countdown;
    }

    private Collection<Listener> getAllUserListeners() {
        ArrayList<Listener> result = new ArrayList<>(usersInGroup.size());

        for(User user : usersInGroup.values()) {
            result.addAll(user.getListeners());
        }

        return result;
    }

    @Override
    public User getUser(UUID uuid) {
        return usersInGroup.get(uuid);
    }

    private Team createTeam(TeamColor teamColor) {
        return new Team(teamColor, this);
    }

    public void eventUserJoinedAsPlayer(UserJoinEvent event) {
        usersInGroup.put(event.getUser().getUuid(), event.getUser());

        currentMap.teleportUser(event.getUser());

        userEvent(event);
    }

    public void userEvent(UserEvent event) {
        EventExecutor.executeEvent(event, getListeners(event.getUser().getListeners()));
    }

    public void userQuitEvent(UserQuitEvent event) {
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

        Event event = new GameStateChangedEvent(this, this.gameState, gameState);

        EventExecutor.executeEvent(event, getListeners(getAllUserListeners(), gameState.getListeners()));

        gameStateTaskList.cancelAllTasks();

        this.gameState = gameState;
    }

    public void startCountdown(String name, String localeStub, int seconds) {
        if(countdown != null) countdown.cancel();

        countdown = new Countdown(name, localeStub, seconds);
        countdown.start(this);
    }

    public void stopCountdown() {
        countdown.cancel();
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

    public void gameEvent(GameEvent event) {
        EventExecutor.executeEvent(event, getListeners());
    }

    public void countdownFinishedEvent(CountdownFinishedEvent event) {
        gameEvent(event);

        if(event.getCountdown().getSecondsRemaining() > 0) return;
        if(event.getCountdown() != countdown) return;

        countdown = null;
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

    public Game getGame() {
        return game;
    }

    @Override
    public void sendMessage(String message) {
        sendMessageNoPrefix(getChatPrefix() + message);
    }

    @Override
    public void sendMessageNoPrefix(String message) {
        for(User user : usersInGroup.values()) {
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

    public boolean hasActiveCountdown() {
        return countdown != null;
    }

    public boolean hasActiveCountdown(String name) {
        return countdown != null && countdown.getName().equals(name);
    }

    public int getUserCount() {
        return getUsers().size();
    }
}
