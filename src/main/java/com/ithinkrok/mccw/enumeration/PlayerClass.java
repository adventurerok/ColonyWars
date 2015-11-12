package com.ithinkrok.mccw.enumeration;

import org.bukkit.Material;

/**
 * Created by paul on 05/11/15.
 *
 * An enum to represent the available classes in the game
 */
public enum PlayerClass {

    CLOAKER("Cloaker", Material.IRON_LEGGINGS),
    SCOUT("Scout", Material.COMPASS),
    GENERAL("General", Material.DIAMOND_SWORD),
    ARCHER("Archer", Material.BOW),
    MAGE("Mage", Material.DIAMOND_LEGGINGS),
    PEASANT("Peasant", Material.IRON_AXE);

    public final String name;
    public final Material chooser;

    PlayerClass(String name, Material chooser) {
        this.name = name;
        this.chooser = chooser;
    }

    public static PlayerClass fromChooserMaterial(Material mat){
        for(PlayerClass playerClass : values()){
            if(playerClass.chooser == mat) return playerClass;
        }

        return null;
    }
}
