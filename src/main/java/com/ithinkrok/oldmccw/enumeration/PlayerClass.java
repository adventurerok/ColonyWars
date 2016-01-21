package com.ithinkrok.oldmccw.enumeration;

import com.ithinkrok.oldmccw.WarsPlugin;
import com.ithinkrok.oldmccw.playerclass.*;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by paul on 05/11/15.
 * <p>
 * An enum to represent the available classes in the game
 */
public class PlayerClass {

    private static List<PlayerClass> playerClassList = new ArrayList<>();

    static {
        new PlayerClass("inferno", Material.IRON_CHESTPLATE, InfernoClass::new);

        new PlayerClass("vampire", Material.GOLD_SWORD, VampireClass::new);
    }


    private final String name;
    private final String formattedName;
    private final Material chooser;
    private final PlayerClassHandlerFactory classHandlerFactory;

    public PlayerClass(String name, Material chooser, PlayerClassHandlerFactory classHandlerFactory) {
        if (name == null || chooser == null || classHandlerFactory == null) {
            throw new NullPointerException("All constructor parameters for PlayerClass must not be null");
        }

        this.name = name;
        this.formattedName = WordUtils.capitalizeFully(name.replace('_', ' '));
        this.chooser = chooser;
        this.classHandlerFactory = classHandlerFactory;

        if (fromChooserMaterial(chooser) != null) {
            throw new RuntimeException("A player class already exists with the chooser material: " + chooser);
        }
        playerClassList.add(this);
    }

    public static PlayerClass fromChooserMaterial(Material mat) {
        for (PlayerClass playerClass : playerClassList) {
            if (playerClass.getChooser() == mat) return playerClass;
        }

        return null;
    }

    public Material getChooser() {
        return chooser;
    }

    public static PlayerClass fromName(String name) {
        for (PlayerClass playerClass : playerClassList) {
            if (playerClass.name.equalsIgnoreCase(name)) return playerClass;
        }

        return null;
    }

    public static List<PlayerClass> values() {
        return playerClassList;
    }

    public PlayerClassHandler createPlayerClassHandler(WarsPlugin plugin) {
        return classHandlerFactory.createPlayerClassHandler(plugin, this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlayerClass that = (PlayerClass) o;

        return name.equals(that.name) && chooser == that.chooser;

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + chooser.hashCode();
        return result;
    }

    public String getFormattedName() {
        return formattedName;
    }

    @Override
    public String toString() {
        return getName();
    }

    public String getName() {
        return name;
    }
}
