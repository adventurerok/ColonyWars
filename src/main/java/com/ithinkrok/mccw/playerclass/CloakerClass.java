package com.ithinkrok.mccw.playerclass;

import com.ithinkrok.mccw.WarsPlugin;
import com.ithinkrok.mccw.enumeration.PlayerClass;
import com.ithinkrok.mccw.event.*;
import com.ithinkrok.mccw.playerclass.items.ArrayCalculator;
import com.ithinkrok.mccw.playerclass.items.Calculator;
import com.ithinkrok.mccw.playerclass.items.ClassItem;
import com.ithinkrok.mccw.playerclass.items.LinearCalculator;
import com.ithinkrok.mccw.strings.Buildings;
import com.ithinkrok.mccw.util.item.TeamCompass;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Created by paul on 07/11/15.
 * <p>
 * Handles the Cloaker class.
 */
public class CloakerClass extends ClassItemClassHandler {


    public CloakerClass(WarsPlugin plugin, PlayerClass playerClass) {
        super(new ClassItem(plugin, playerClass, Material.IRON_LEGGINGS, "items.cloak.name")
                .withUpgradeBuildings(Buildings.MAGETOWER).withUnlockOnBuildingBuild(true)
                .withRightClickAction(new Cloak(plugin, cloakDurationCalculator()))
                .withRightClickCooldown("cloak", "cloak", new LinearCalculator(25, 10), "cooldowns.cloak.finished")
                .withRightClickTimeout(new Decloak(plugin), "cloak", "cloaking", "lore.timeout.cloak",
                        "timeouts.cloaking.finished", cloakDurationCalculator()).withUpgradables(
                        new ClassItem.Upgradable("cloak", "upgrades.cloak.name", 2)),
                TeamCompass.createTeamCompass(plugin));
    }

    private static Calculator cloakDurationCalculator() {
        return new ArrayCalculator(10, 15, 25);
    }


    @Override
    public void onUserBeginGame(UserBeginGameEvent event) {
        super.onUserBeginGame(event);
        event.getPlayer()
                .addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, false, false), false);
        event.getPlayer()
                .addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 3, false, false), false);
    }

    private static class Cloak implements ClassItem.InteractAction {

        private Calculator cloakDuration;
        private WarsPlugin plugin;

        public Cloak(WarsPlugin plugin, Calculator cloakDuration) {
            this.plugin = plugin;
            this.cloakDuration = cloakDuration;
        }

        @Override
        public boolean onInteractWorld(UserInteractEvent event) {
            event.getUser().cloak();

            int cloakLevel = event.getUser().getUpgradeLevel("cloak");
            int cloakDuration = (int) this.cloakDuration.calculate(cloakLevel);

            event.getPlayer()
                    .addPotionEffect(new PotionEffect(PotionEffectType.SPEED, cloakDuration * 20, 1, false, true),
                            true);


            int swirlTask = plugin.getGameInstance().scheduleRepeatingTask(
                    () -> event.getPlayer().getLocation().getWorld()
                            .playEffect(event.getPlayer().getLocation(), Effect.POTION_SWIRL, 0), 20, 20);

            event.getUser().setMetadata("swirlTask", swirlTask, true);

            return true;
        }
    }

    private static class Decloak implements ClassItem.TimeoutAction {

        private WarsPlugin plugin;

        public Decloak(WarsPlugin plugin) {
            this.plugin = plugin;
        }

        @Override
        public boolean onAbilityTimeout(UserAbilityCooldownEvent event) {
            if (event.getUser().isInGame()) event.getUser().decloak();
            Object swirlTask = event.getUser().getMetadata("swirlTask");
            if (swirlTask == null || !(swirlTask instanceof Integer)) return false;

            plugin.getGameInstance().cancelTask((Integer) swirlTask);
            event.getPlayer()
                    .addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, false, false),
                            true);

            return true;
        }
    }
}
