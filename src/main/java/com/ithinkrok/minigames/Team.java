package com.ithinkrok.minigames;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;
import com.ithinkrok.minigames.lang.LanguageLookup;
import com.ithinkrok.minigames.lang.Messagable;
import com.ithinkrok.minigames.metadata.Metadata;
import com.ithinkrok.minigames.metadata.MetadataHolder;
import com.ithinkrok.minigames.task.GameRunnable;
import com.ithinkrok.minigames.task.GameTask;
import com.ithinkrok.minigames.task.TaskList;
import com.ithinkrok.minigames.task.TaskScheduler;
import com.ithinkrok.minigames.user.UserResolver;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by paul on 31/12/15.
 */
public class Team implements Listener, Messagable, LanguageLookup, SharedObjectAccessor, TaskScheduler, UserResolver,
        MetadataHolder<Metadata> {

    private TeamIdentifier teamIdentifier;
    private ConcurrentMap<UUID, User> usersInTeam = new ConcurrentHashMap<>();
    private GameGroup gameGroup;

    private ClassToInstanceMap<Metadata> metadataMap = MutableClassToInstanceMap.create();

    private TaskList teamTaskList = new TaskList();

    public Team(TeamIdentifier teamIdentifier, GameGroup gameGroup) {
        this.teamIdentifier = teamIdentifier;
        this.gameGroup = gameGroup;
    }

    public TeamIdentifier getTeamIdentifier() {
        return teamIdentifier;
    }

    @Override
    public String getLocale(String name) {
        return gameGroup.getLocale(name);
    }

    void addUser(User user) {
        usersInTeam.put(user.getUuid(), user);
    }

    void removeUser(User user) {
        usersInTeam.remove(user.getUuid());
    }

    @Override
    public boolean hasLocale(String name) {
        return gameGroup.hasLocale(name);
    }

    @Override
    public void sendLocale(String locale, Object... args) {
        sendMessage(getLocale(locale, args));
    }

    @Override
    public void sendMessage(String message) {
        sendMessageNoPrefix(gameGroup.getChatPrefix() + message);
    }

    @Override
    public String getLocale(String name, Object... args) {
        return gameGroup.getLocale(name, args);
    }

    @Override
    public void sendMessageNoPrefix(String message) {
        for (User user : getUsers()) {
            user.sendMessageNoPrefix(message);
        }
    }

    public Collection<User> getUsers() {
        return usersInTeam.values();
    }

    @Override
    public void sendLocaleNoPrefix(String locale, Object... args) {
        sendMessageNoPrefix(getLocale(locale, args));
    }

    @Override
    public LanguageLookup getLanguageLookup() {
        return gameGroup.getLanguageLookup();
    }

    @Override
    public <B extends Metadata> B getMetadata(Class<? extends B> clazz) {
        return metadataMap.getInstance(clazz);
    }

    @Override
    public <B extends Metadata> void setMetadata(B metadata) {
        metadataMap.put(metadata.getMetadataClass(), metadata);
    }

    @Override
    public boolean hasMetadata(Class<? extends Metadata> clazz) {
        return metadataMap.containsKey(clazz);
    }

    @Override
    public ConfigurationSection getSharedObject(String name) {
        return gameGroup.getSharedObject(name);
    }

    @Override
    public GameTask doInFuture(GameRunnable task) {
        GameTask gameTask = gameGroup.doInFuture(task);

        teamTaskList.addTask(gameTask);
        return gameTask;
    }

    @Override
    public GameTask doInFuture(GameRunnable task, int delay) {
        GameTask gameTask = gameGroup.doInFuture(task, delay);

        teamTaskList.addTask(gameTask);
        return gameTask;
    }

    @Override
    public GameTask repeatInFuture(GameRunnable task, int delay, int period) {
        GameTask gameTask = gameGroup.repeatInFuture(task, delay, period);

        teamTaskList.addTask(gameTask);
        return gameTask;
    }

    @Override
    public void cancelAllTasks() {
        teamTaskList.cancelAllTasks();
    }

    @Override
    public User getUser(UUID uuid) {
        if (uuid == null) return null;
        return usersInTeam.get(uuid);
    }
}
