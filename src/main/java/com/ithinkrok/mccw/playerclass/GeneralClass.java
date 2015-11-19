package com.ithinkrok.mccw.playerclass;

import com.ithinkrok.mccw.playerclass.items.ClassItem;
import com.ithinkrok.mccw.playerclass.items.LinearCalculator;
import com.ithinkrok.mccw.strings.Buildings;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;

/**
 * Created by paul on 05/11/15.
 * <p>
 * Handles the General class
 */
public class GeneralClass extends ClassItemClassHandler {

    public GeneralClass(FileConfiguration config) {
        super(new ClassItem(Material.DIAMOND_SWORD).withUpgradeBuildings(Buildings.BLACKSMITH)
                .withUnlockOnBuildingBuild(true).
                        withEnchantmentEffects(new ClassItem.EnchantmentEffect(Enchantment.DAMAGE_ALL, "sword",
                                        new LinearCalculator(0, 1)),
                                new ClassItem.EnchantmentEffect(Enchantment.KNOCKBACK, "sword",
                                        new LinearCalculator(5, 0))).withUpgradables(
                        new ClassItem.Upgradable("sword", "Sword Upgrade %s", 2,
                                configArrayCalculator(config, "costs.general.sword", 2))));
    }

}