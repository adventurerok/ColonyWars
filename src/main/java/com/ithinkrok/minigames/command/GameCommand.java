package com.ithinkrok.minigames.command;

import com.ithinkrok.minigames.*;

import java.util.Map;

/**
 * Created by paul on 12/01/16.
 */
public class GameCommand {

    private final String command;
    private final Map<String, Object> params;

    private final GameGroup gameGroup;
    private final User user;
    private final TeamIdentifier teamIdentifier;
    private final Kit kit;

    public GameCommand(String command, Map<String, Object> params, GameGroup gameGroup, User user,
                       TeamIdentifier teamIdentifier, Kit kit) {
        this.command = command;
        this.params = params;
        this.gameGroup = gameGroup;
        this.user = user;
        this.teamIdentifier = teamIdentifier;
        this.kit = kit;
    }

    public boolean hasParameter(String name) {
        return params.containsKey(name);
    }

    public double getDouble(String name, double def) {
        try {
            return ((Number) params.get(name)).doubleValue();
        } catch (Exception e) {
            return def;
        }
    }

    public int getInt(String name, int def) {
        try {
            return ((Number) params.get(name)).intValue();
        } catch (Exception e) {
            return def;
        }
    }

    public boolean getBoolean(String name, boolean def) {
        try{
            return (Boolean) params.get(name);
        } catch (Exception e) {
            return def;
        }
    }

    public String getString(String name, String def) {
        Object o = params.get(name);
        return o != null ? o.toString() : def;
    }

    public String getCommand() {
        return command;
    }

    public GameGroup getGameGroup() {
        return gameGroup;
    }

    public User getUser() {
        return user;
    }

    public TeamIdentifier getTeamIdentifier() {
        return teamIdentifier;
    }

    public Kit getKit() {
        return kit;
    }
}
