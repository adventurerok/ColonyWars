package com.ithinkrok.minigames;

import org.bukkit.World;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by paul on 31/12/15.
 */
public class GameGroup<U extends User, T extends Team, G extends GameGroup, M extends Minigame> {

    private ConcurrentMap<UUID, U> usersInGroup = new ConcurrentHashMap<>();
    private Map<TeamColor, T> teamsInGroup = new HashMap<>();
    private M minigame;

    private Constructor<T> teamConstructor;

    private World currentWorld;

    public GameGroup(M minigame, Constructor<T> teamConstructor) {
        this.minigame = minigame;
        this.teamConstructor = teamConstructor;
    }

    private T createTeam(TeamColor teamColor) {
        try {
            return teamConstructor.newInstance(teamColor, this);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to construct Team", e);
        }
    }

}
