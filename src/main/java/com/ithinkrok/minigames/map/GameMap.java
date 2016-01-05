package com.ithinkrok.minigames.map;

import com.ithinkrok.minigames.GameGroup;
import com.ithinkrok.minigames.User;
import com.ithinkrok.minigames.item.CustomItem;
import com.ithinkrok.minigames.item.IdentifierMap;
import com.ithinkrok.minigames.lang.LanguageLookup;
import com.ithinkrok.minigames.lang.MultipleLanguageLookup;
import com.ithinkrok.minigames.task.GameTask;
import com.ithinkrok.minigames.task.TaskList;
import com.ithinkrok.minigames.util.io.ConfigHolder;
import com.ithinkrok.minigames.util.io.ConfigParser;
import com.ithinkrok.minigames.util.io.ListenerLoader;
import com.ithinkrok.minigames.util.io.DirectoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by paul on 01/01/16.
 */
public class GameMap implements LanguageLookup, ConfigHolder {

    private static int mapCounter = 0;

    private GameMapInfo gameMapInfo;
    private World world;
    private MultipleLanguageLookup languageLookup = new MultipleLanguageLookup();
    private List<Listener> listeners = new ArrayList<>();
    private Map<String, Listener> listenerMap = new HashMap<>();

    private TaskList mapTaskList = new TaskList();
    private IdentifierMap<CustomItem> customItemIdentifierMap = new IdentifierMap<>();

    public GameMap(GameGroup gameGroup, GameMapInfo gameMapInfo) {
        this.gameMapInfo = gameMapInfo;

        loadMap();
        ConfigParser.parseConfig(gameGroup, this, gameGroup, gameMapInfo.getConfigName(), gameMapInfo.getConfig());
    }

    private void loadMap() {
        ++mapCounter;

        String randomWorldName = gameMapInfo.getName() + "-" + String.format("%04X", mapCounter);
        String copyFrom = "./" + gameMapInfo.getMapFolder() + "/";
        String copyTo = "./" + randomWorldName + "/";

        try {
            DirectoryUtils
                    .copy(Paths.get(copyFrom), Paths.get(copyTo));
        } catch (IOException e) {
            e.printStackTrace();
        }

        File uid = new File(copyTo, "uid.dat");
        if(uid.exists()) {
            boolean deleted = uid.delete();
            if(!deleted) System.out.println("Could not delete uid.dat for world. This could cause errors");
        }

        WorldCreator creator = new WorldCreator(randomWorldName);

        creator.environment(gameMapInfo.getEnvironment());

        world = creator.createWorld();
        world.setAutoSave(false);

    }


    public CustomItem getCustomItem(String name) {
        return customItemIdentifierMap.get(name);
    }

    public CustomItem getCustomItem(int identifier) {
        return customItemIdentifierMap.get(identifier);
    }

    public void bindTaskToMap(GameTask task) {
        mapTaskList.addTask(task);
    }

    public void unloadMap() {
        mapTaskList.cancelAllTasks();

        if (world.getPlayers().size() != 0) System.out.println("There are still players in an unloading map!");

        for (Player player : world.getPlayers()) {
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
        if (user.getLocation().getWorld().equals(world)) return;
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

    @Override
    public void addListener(String name, Listener listener) {
        listeners.add(listener);
        listenerMap.put(name, listener);
    }

    public Map<String, Listener> getListenerMap() {
        return listenerMap;
    }

    @Override
    public void addCustomItem(CustomItem item) {
        customItemIdentifierMap.put(item.getName(), item);
    }

    @Override
    public void addLanguageLookup(LanguageLookup languageLookup) {
        this.languageLookup.addLanguageLookup(languageLookup);
    }
}
