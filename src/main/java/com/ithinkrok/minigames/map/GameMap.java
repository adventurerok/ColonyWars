package com.ithinkrok.minigames.map;

import com.ithinkrok.minigames.GameGroup;
import com.ithinkrok.minigames.User;
import com.ithinkrok.minigames.event.game.ListenerEnabledEvent;
import com.ithinkrok.minigames.lang.LanguageLookup;
import com.ithinkrok.minigames.util.EventExecutor;
import com.ithinkrok.minigames.util.io.DirectoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by paul on 01/01/16.
 */
public class GameMap implements LanguageLookup {

    private static int mapCounter = 0;

    private GameMapInfo gameMapInfo;
    private World world;
    private LanguageLookup languageLookup;
    private List<Listener> listeners;

    public GameMap(GameGroup gameGroup, GameMapInfo gameMapInfo) {
        this.gameMapInfo = gameMapInfo;

        loadMap();
        loadListeners(gameGroup);
    }

    @SuppressWarnings("unchecked")
    private void loadListeners(GameGroup gameGroup) {
        listeners = new ArrayList<>();

        if(!gameMapInfo.getConfig().contains("listeners")) return;

        ConfigurationSection listenersConfig = gameMapInfo.getConfig().getConfigurationSection("listeners");

        for(String key : listenersConfig.getKeys(false)) {
            ConfigurationSection listenerConfig = listenersConfig.getConfigurationSection(key);

            String className = listenerConfig.getString("class");

            try {
                Class<? extends Listener> clazz = (Class<? extends Listener>) Class.forName(className);

                Listener listener = clazz.newInstance();

                ConfigurationSection config = null;
                if(listenerConfig.contains("config")) config = listenerConfig.getConfigurationSection("config");

                EventExecutor.executeEvent(new ListenerEnabledEvent<>(gameGroup, config), listener);

                listeners.add(listener);
            } catch (Exception e) {
                System.out.println("Failed to load listener for map: " + className);
                e.printStackTrace();
            }
        }
    }

    private void loadMap() {
        ++mapCounter;

        String randomWorldName = gameMapInfo.getName() + "-" + String.format("%04X", mapCounter);

        try {
            DirectoryUtils
                    .copy(Paths.get("./" + gameMapInfo.getMapFolder() + "/"), Paths.get("./" + randomWorldName + "/"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        WorldCreator creator = new WorldCreator(randomWorldName);

        creator.generateStructures(false);
        creator.environment(gameMapInfo.getEnvironment());

        world = creator.createWorld();
        world.setAutoSave(false);

    }

    public void unloadMap() {
        if(world.getPlayers().size() != 0) System.out.println("There are still players in an unloading map!");

        for(Player player : world.getPlayers()) {
            player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
        }

        boolean success = Bukkit.unloadWorld(world, false);

        try {
            DirectoryUtils.delete(world.getWorldFolder().toPath());
        } catch (IOException e) {
            System.out.println("Failed to unload map. When bukkit tried to unload, it returned " + success);
            System.out.println("Please make sure there are no players in the map before deleting?");
            e.printStackTrace();
        }

    }

    public void teleportUser(User user) {
        if(user.getLocation().getWorld().equals(world)) return;
        user.teleport(world.getSpawnLocation());
    }

    @Override
    public String getLocale(String name) {
        return languageLookup.getLocale(name);
    }

    @Override
    public String getLocale(String name, Object... args) {
        return languageLookup.getLocale(name, args);
    }

    @Override
    public boolean hasLocale(String name) {
        return languageLookup.hasLocale(name);
    }

    public List<Listener> getListeners() {
        return listeners;
    }
}
