package com.ithinkrok.oldmccw.playerclass;

import com.ithinkrok.oldmccw.WarsPlugin;
import com.ithinkrok.oldmccw.enumeration.PlayerClass;
import com.ithinkrok.oldmccw.event.UserBeginGameEvent;
import com.ithinkrok.oldmccw.playerclass.items.ArrayCalculator;
import com.ithinkrok.oldmccw.playerclass.items.ClassItem;
import com.ithinkrok.oldmccw.strings.Buildings;
import com.ithinkrok.oldmccw.util.item.TeamCompass;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Created by paul on 14/11/15.
 * <p>
 * Handles the dark knight class
 */
public class DarkKnightClass extends ClassItemClassHandler {

    public DarkKnightClass(WarsPlugin plugin, PlayerClass playerClass) {
        super(new ClassItem(plugin, playerClass.getName(), Material.IRON_HELMET, "items.darkness-sword.name")
                .withUpgradeBuildings(Buildings.MAGETOWER).withUnlockOnBuildingBuild(true).withWeaponModifier(
                        new ClassItem.WeaponModifier("sword").withDamageCalculator(new ArrayCalculator(1, 2.5, 4))
                                .withWitherCalculator(new ArrayCalculator(3, 6, 10))
                                .withNauseaCalculator(new ArrayCalculator(5, 7, 8))).withUpgradables(
                        new ClassItem.Upgradable("sword", "upgrades.darkness-sword.name", 2)),
                TeamCompass.createTeamCompass(plugin));
    }

    @Override
    public void onUserBeginGame(UserBeginGameEvent event) {
        super.onUserBeginGame(event);

        event.getUser()
                .addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 1, false, false),
                        true);
    }
}
