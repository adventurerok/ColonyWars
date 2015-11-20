package com.ithinkrok.mccw.playerclass;

import com.ithinkrok.mccw.WarsPlugin;
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

    public PeasantClass(WarsPlugin plugin, FileConfiguration config) {
        super(new ClassItem(Material.IRON_AXE, plugin.getLocale("items.peasant-axe.name")).withUnlockOnGameStart(true)
                        .withEnchantmentEffects(
                                new ClassItem.EnchantmentEffect(Enchantment.DIG_SPEED, "axe", new LinearCalculator(2, 0))),
                new ClassItem(Material.WOOD_SWORD).withUpgradeBuildings(Buildings.LUMBERMILL)
                        .withUnlockOnBuildingBuild(true).withEnchantmentEffects(
                        new ClassItem.EnchantmentEffect(Enchantment.DAMAGE_ALL, "sharpness",
                                new LinearCalculator(0, 1)),
                        new ClassItem.EnchantmentEffect(Enchantment.KNOCKBACK, "knockback", new LinearCalculator(0, 1)))
                        .withUpgradables(
                                new ClassItem.Upgradable("sharpness", plugin.getLocale("upgrades.sharpness.name"), 2,
                                        configArrayCalculator(config, "costs.peasant.sharpness", 2)),
                                new ClassItem.Upgradable("knockback", plugin.getLocale("upgrades.knockback.name"), 2,
                                        configArrayCalculator(config, "costs.peasant.knockback", 2))));
    }

}
