package com.ithinkrok.mccw.enumeration;

import com.ithinkrok.mccw.playerclass.*;
import org.bukkit.Material;

/**
 * Created by paul on 05/11/15.
 *
 * An enum to represent the available classes in the game
 */
public enum PlayerClass {

    CLOAKER("Cloaker", Material.IRON_LEGGINGS, CloakerClass::new),
    SCOUT("Scout", Material.COMPASS, ScoutClass::new),
    GENERAL("General", Material.DIAMOND_SWORD, GeneralClass::new),
    ARCHER("Archer", Material.BOW, ArcherClass::new),
    MAGE("Mage", Material.DIAMOND_LEGGINGS, MageClass::new),
    PEASANT("Peasant", Material.IRON_AXE, PeasantClass::new),
    INFERNO("Inferno", Material.IRON_CHESTPLATE, InfernoClass::new),
    DARK_KNIGHT("Dark Knight", Material.IRON_HELMET, DarkKnightClass::new),
    PRIEST("Priest", Material.GOLD_LEGGINGS, PriestClass::new),
    WARRIOR("Warrior", Material.IRON_SWORD, WarriorClass::new);

    private final String name;
    private final Material chooser;
    private final PlayerClassHandlerFactory classHandlerFactory;

    PlayerClass(String name, Material chooser, PlayerClassHandlerFactory classHandlerFactory) {
        this.name = name;
        this.chooser = chooser;
        this.classHandlerFactory = classHandlerFactory;
    }

    public static PlayerClass fromChooserMaterial(Material mat){
        for(PlayerClass playerClass : values()){
            if(playerClass.getChooser() == mat) return playerClass;
        }

        return null;
    }

    public String getName() {
        return name;
    }

    public Material getChooser() {
        return chooser;
    }

    public PlayerClassHandlerFactory getClassHandlerFactory() {
        return classHandlerFactory;
    }
}
