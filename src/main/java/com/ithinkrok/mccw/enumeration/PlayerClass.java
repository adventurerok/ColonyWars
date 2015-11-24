package com.ithinkrok.mccw.enumeration;

import com.ithinkrok.mccw.playerclass.*;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by paul on 05/11/15.
 *
 * An enum to represent the available classes in the game
 */
public class PlayerClass {

    private static List<PlayerClass> playerClassList = new ArrayList<>();

    public static final PlayerClass CLOAKER = new PlayerClass("cloaker", Material.IRON_LEGGINGS, CloakerClass::new);
    public static final PlayerClass SCOUT = new PlayerClass("scout", Material.COMPASS, ScoutClass::new);
    public static final PlayerClass GENERAL = new PlayerClass("general", Material.DIAMOND_SWORD, GeneralClass::new);
    public static final PlayerClass ARCHER = new PlayerClass("archer", Material.BOW, ArcherClass::new);
    public static final PlayerClass MAGE = new PlayerClass("mage", Material.DIAMOND_LEGGINGS, MageClass::new);
    public static final PlayerClass PEASANT = new PlayerClass("peasant", Material.IRON_AXE, PeasantClass::new);
    public static final PlayerClass INFERNO = new PlayerClass("inferno", Material.IRON_CHESTPLATE, InfernoClass::new);
    public static final PlayerClass DARK_KNIGHT = new PlayerClass("dark_knight", Material.IRON_HELMET,
            DarkKnightClass::new);

    public static final PlayerClass PRIEST = new PlayerClass("priest", Material.GOLD_LEGGINGS, PriestClass::new);
    public static final PlayerClass WARRIOR = new PlayerClass("warrior", Material.IRON_SWORD, WarriorClass::new);


    private final String name;
    private final String formattedName;
    private final Material chooser;
    private final PlayerClassHandlerFactory classHandlerFactory;

    public PlayerClass(String name, Material chooser, PlayerClassHandlerFactory classHandlerFactory) {
        if(name == null || chooser == null || classHandlerFactory == null ) {
            throw new NullPointerException("All constructor parameters for PlayerClass must not be null");
        }

        this.name = name;
        this.formattedName = WordUtils.capitalizeFully(name.replace('_', ' '));
        this.chooser = chooser;
        this.classHandlerFactory = classHandlerFactory;

        if(fromChooserMaterial(chooser) != null){
            throw new RuntimeException("A player class already exists with the chooser material: " + chooser);
        }
        playerClassList.add(this);
    }

    public static PlayerClass fromChooserMaterial(Material mat){
        for(PlayerClass playerClass : playerClassList){
            if(playerClass.getChooser() == mat) return playerClass;
        }

        return null;
    }

    public static PlayerClass fromName(String name) {
        for(PlayerClass playerClass : playerClassList) {
            if(playerClass.name.equalsIgnoreCase(name)) return playerClass;
        }

        return null;
    }

    public static List<PlayerClass> values(){
        return playerClassList;
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

    public Material getChooser() {
        return chooser;
    }

    public PlayerClassHandlerFactory getClassHandlerFactory() {
        return classHandlerFactory;
    }

    @Override
    public String toString() {
        return getName();
    }

    public String getName() {
        return name;
    }
}
