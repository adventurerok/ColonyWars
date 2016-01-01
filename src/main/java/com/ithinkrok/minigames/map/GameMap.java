package com.ithinkrok.minigames.map;

import com.ithinkrok.minigames.GameGroup;
import com.ithinkrok.minigames.lang.LanguageLookup;
import com.ithinkrok.minigames.util.DirectoryUtils;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.event.Listener;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

/**
 * Created by paul on 01/01/16.
 */
public class GameMap implements LanguageLookup {

    private GameMapInfo gameMapInfo;
    private World world;
    private LanguageLookup languageLookup;
    private List<Listener> listeners;

    public GameMap(GameGroup gameGroup, GameMapInfo gameMapInfo) {
        this.gameMapInfo = gameMapInfo;

        loadMap();
    }

    private void loadMap() {
        String randomWorldName = gameMapInfo.getName() + "-" + UUID.randomUUID();

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
