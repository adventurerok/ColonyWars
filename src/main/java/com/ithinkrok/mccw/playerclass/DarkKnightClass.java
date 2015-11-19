package com.ithinkrok.mccw.playerclass;

import com.ithinkrok.mccw.event.UserBeginGameEvent;
import com.ithinkrok.mccw.playerclass.items.ArrayCalculator;
import com.ithinkrok.mccw.playerclass.items.ClassItem;
import com.ithinkrok.mccw.strings.Buildings;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Created by paul on 14/11/15.
 * <p>
 * Handles the dark knight class
 */
public class DarkKnightClass extends ClassItemClassHandler {

    public DarkKnightClass(FileConfiguration config) {
        super(new ClassItem(Material.IRON_HELMET, "Darkness Sword").withUpgradeBuildings(Buildings.MAGETOWER)
                .withUnlockOnBuildingBuild(true).withWeaponModifier(
                        new ClassItem.WeaponModifier("sword").withDamageCalculator(new ArrayCalculator(4, 6, 10))
                                .withWitherCalculator(new ArrayCalculator(3, 6, 10))
                                .withNauseaCalculator(new ArrayCalculator(5, 7, 8))).withUpgradables(
                        new ClassItem.Upgradable("sword", "Darkness Sword Upgrade %s", 2,
                                configArrayCalculator(config, "costs.dark_knight.sword", 2))));
    }

    @Override
    public void onUserBeginGame(UserBeginGameEvent event) {
        super.onUserBeginGame(event);

        event.getPlayer()
                .addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 1, false, false),
                        true);
    }
}
