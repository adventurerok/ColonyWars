package com.ithinkrok.mccw.playerclass;

import com.ithinkrok.mccw.WarsPlugin;
import com.ithinkrok.mccw.enumeration.PlayerClass;
import com.ithinkrok.mccw.event.UserAttackEvent;
import com.ithinkrok.mccw.playerclass.items.ClassItem;
import com.ithinkrok.mccw.playerclass.items.LinearCalculator;
import com.ithinkrok.mccw.strings.Buildings;
import com.ithinkrok.mccw.util.Disguises;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import pgDev.bukkit.DisguiseCraft.DisguiseCraft;
import pgDev.bukkit.DisguiseCraft.api.DisguiseCraftAPI;
import pgDev.bukkit.DisguiseCraft.disguise.DisguiseType;

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
            if (event.getFinalDamage() < 1) return;
            PotionEffect effect = new PotionEffect(PotionEffectType.REGENERATION, 20, 1, false, true);
            event.getUser().getPlayer().addPotionEffect(effect);
        });

        vampireSword.withDescriptionLocale("items.vampire-sword.desc");

        vampireSword.withUpgradables(new ClassItem.Upgradable("vampire", "upgrades.vampire.name", 2));

        addExtraClassItems(vampireSword);

        ClassItem batWand = new ClassItem(plugin, playerClass.getName(), Material.STICK, "items.bat-wand.name");

        batWand.withUpgradeBuildings(Buildings.MAGETOWER).withUnlockOnBuildingBuild(true);

        batWand.withRightClickAction(event -> {
            Disguises.disguise(event.getUser(), DisguiseType.Bat);
            return true;
        });

        batWand.withRightClickTimeout(event -> {
            Disguises.undisguise(event.getUser());
            return true;
        }, "bat", "batting", "lore.timeout.bat", "timeouts.batting.finished", new LinearCalculator(5, 5));

        batWand.withRightClickCooldown("bat", "bat", new LinearCalculator(15, 5), "cooldowns.bat.finished");

        batWand.withUpgradables(new ClassItem.Upgradable("bat", "upgrades.bat-wand.name", 2));

        addExtraClassItems(batWand);
    }

    @Override
    public void onUserAttack(UserAttackEvent event) {
        if (event.getUser().isCoolingDown("batting")) {
            event.setCancelled(true);
            return;
        }
        super.onUserAttack(event);
    }
}
