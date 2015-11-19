package com.ithinkrok.mccw.playerclass;

import com.ithinkrok.mccw.playerclass.items.ClassItem;
import com.ithinkrok.mccw.playerclass.items.LinearCalculator;
import com.ithinkrok.mccw.strings.Buildings;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;

/**
 * Created by paul on 12/11/15.
 * <p>
 * Handles the peasant class
 */
public class PeasantClass extends ClassItemClassHandler {

    public PeasantClass(FileConfiguration config) {
        super(new ClassItem(Material.IRON_AXE, "Peasant Axe").withUnlockOnGameStart(true).withEnchantmentEffects(
                new ClassItem.EnchantmentEffect(Enchantment.DIG_SPEED, "axe", new LinearCalculator(2, 0))),
                new ClassItem(Material.WOOD_SWORD, null).withUpgradeBuildings(Buildings.LUMBERMILL)
                        .withUnlockOnBuildingBuild(true).withEnchantmentEffects(
                        new ClassItem.EnchantmentEffect(Enchantment.DAMAGE_ALL, "sharpness",
                                new LinearCalculator(0, 1)),
                        new ClassItem.EnchantmentEffect(Enchantment.KNOCKBACK, "knockback", new LinearCalculator(0, 1)))
                        .withUpgradables(new ClassItem.Upgradable("sharpness", "Sharpness Upgrade %s", 2,
                                        configArrayCalculator(config, "costs.peasant.sharpness", 2)),
                                new ClassItem.Upgradable("knockback", "Knockback Upgrade %s", 2,
                                        configArrayCalculator(config, "costs.peasant.knockback", 2))));
    }

}
