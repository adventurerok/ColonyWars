package com.ithinkrok.minigames;

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

}
