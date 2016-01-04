package com.ithinkrok.minigames.map;

import com.ithinkrok.minigames.GameGroup;
import com.ithinkrok.minigames.User;
import com.ithinkrok.minigames.event.ListenerEnabledEvent;
import com.ithinkrok.minigames.item.CustomItem;
import com.ithinkrok.minigames.item.IdentifierMap;
import com.ithinkrok.minigames.lang.LanguageLookup;
import com.ithinkrok.minigames.lang.MultipleLanguageLookup;
import com.ithinkrok.minigames.task.GameTask;
import com.ithinkrok.minigames.task.TaskList;
import com.ithinkrok.minigames.util.EventExecutor;
import com.ithinkrok.minigames.util.ListenerLoader;
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
    private MultipleLanguageLookup languageLookup = new MultipleLanguageLookup();
    private List<Listener> listeners;

    private TaskList mapTaskList = new TaskList();
    private IdentifierMap<CustomItem> customItemIdentifierMap = new IdentifierMap<>();

    public GameMap(GameGroup gameGroup, GameMapInfo gameMapInfo) {
        this.gameMapInfo = gameMapInfo;

        loadMap();
        loadLangFiles(gameGroup);
        loadListeners(gameGroup);
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

    private void loadLangFiles(GameGroup gameGroup) {
        if (!gameMapInfo.getConfig().contains("lang_files")) return;

        for (String additional : gameMapInfo.getConfig().getStringList("additional_lang_files")) {
            languageLookup.addLanguageLookup(gameGroup.loadLangFile(additional));
        }
    }

    @SuppressWarnings("unchecked")
    private void loadListeners(GameGroup gameGroup) {
        listeners = new ArrayList<>();

        if (!gameMapInfo.getConfig().contains("listeners")) return;

        ConfigurationSection listenersConfig = gameMapInfo.getConfig().getConfigurationSection("listeners");

        for (String key : listenersConfig.getKeys(false)) {
            ConfigurationSection listenerConfig = listenersConfig.getConfigurationSection(key);

            try {
                listeners.add(ListenerLoader.loadListener(gameGroup, listenerConfig));
            } catch (Exception e) {
                System.out.println("Failed while loading listener for key: " + key);
                e.printStackTrace();
            }
        }
    }

    public void registerCustomItem(CustomItem item) {
        customItemIdentifierMap.put(item.getName(), item);
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
}
