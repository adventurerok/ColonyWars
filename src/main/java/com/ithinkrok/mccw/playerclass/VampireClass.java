package com.ithinkrok.mccw.playerclass;

import com.ithinkrok.mccw.WarsPlugin;
import com.ithinkrok.mccw.enumeration.PlayerClass;
import com.ithinkrok.mccw.playerclass.items.ClassItem;
import com.ithinkrok.mccw.playerclass.items.LinearCalculator;
import com.ithinkrok.mccw.strings.Buildings;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Created by paul on 05/12/15.
 * <p>
 * Handles the vampire class
 */
public class VampireClass extends ClassItemClassHandler {

    public VampireClass(WarsPlugin plugin, PlayerClass playerClass) {

        ClassItem vampireSword =
                new ClassItem(plugin, playerClass.getName(), Material.GOLD_SWORD, "items.vampire-sword.name");

        vampireSword.withUpgradeBuildings(Buildings.MAGETOWER).withUnlockOnBuildingBuild(true);

        vampireSword.withEnchantmentEffects(
                new ClassItem.EnchantmentEffect(Enchantment.DAMAGE_ALL, "vampire", new LinearCalculator(0, 1)));

        vampireSword.withAttackAction(event -> {
            if(event.getUser().isCoolingDown("batting")) {
                event.setCancelled(true);
                return;
            }

            if (event.getFinalDamage() < 1) return;
            PotionEffect effect = new PotionEffect(PotionEffectType.REGENERATION, 20, 1, false, true);
            event.getUser().getPlayer().addPotionEffect(effect);
        });

        vampireSword.withDescriptionLocale("items.vampire-sword.desc");

        vampireSword.withUpgradables(new ClassItem.Upgradable("vampire", "upgrades.vampire.name", 2));

        addExtraClassItems(vampireSword);
    }
}
