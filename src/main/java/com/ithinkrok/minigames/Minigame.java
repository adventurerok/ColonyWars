package com.ithinkrok.minigames;

import com.ithinkrok.minigames.listeners.MinigameListener;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by paul on 31/12/15.
 */
public abstract class Minigame<U extends User, T extends Team, G extends GameGroup, M extends Minigame> {

    private List<U> usersInServer = new ArrayList<>();
    private List<G> gameGroups = new ArrayList<>();

    private Constructor<G> gameGroupConstructor;
    private Constructor<T> teamConstructor;
    private Constructor<U> userConstructor;

    private Plugin plugin;

    private G spawnGameGroup;

    public Minigame(Plugin plugin, Class<G> gameGroupClass, Class<T> teamClass, Class<U> userClass) {
        this.plugin = plugin;

        try {
            teamConstructor = teamClass.getConstructor(TeamColor.class, gameGroupClass);
            userConstructor =
                    userClass.getConstructor(getClass(), gameGroupClass, teamClass, UUID.class, LivingEntity.class);
            gameGroupConstructor = gameGroupClass.getConstructor(getClass(), teamConstructor.getClass());
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Failed to get constructors for data classes", e);
        }

        spawnGameGroup = createGameGroup();
    }


    private G createGameGroup() {
        try {
            return gameGroupConstructor.newInstance(this, teamConstructor);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to construct GameGroup", e);
        }
    }

    private U createUser(G gameGroup, T team, UUID uuid, LivingEntity entity) {
        try {
            return userConstructor.newInstance(this, gameGroup, team, uuid, entity);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to construct User", e);
        }
    }

    public void registerListeners() {
        MinigameListener<U, T, G> listener = new MinigameListener<>(this);

        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
    }


}
