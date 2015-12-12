package com.ithinkrok.mccw.playerclass;

import com.ithinkrok.mccw.WarsPlugin;
import com.ithinkrok.mccw.data.User;
import com.ithinkrok.mccw.enumeration.PlayerClass;
import com.ithinkrok.mccw.event.UserAttackEvent;
import com.ithinkrok.mccw.event.UserBeginGameEvent;
import com.ithinkrok.mccw.event.UserInteractEvent;
import com.ithinkrok.mccw.inventory.UpgradeBuyable;
import com.ithinkrok.mccw.playerclass.items.ClassItem;
import com.ithinkrok.mccw.playerclass.items.LinearCalculator;
import com.ithinkrok.mccw.strings.Buildings;
import com.ithinkrok.mccw.util.Disguises;
import com.ithinkrok.mccw.util.item.InventoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
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

        for (int level = 1; level <= 2; ++level) {
            addExtraBuyables(new UpgradeBuyable(InventoryUtils
                    .createItemWithNameAndLore(Material.MONSTER_EGG, 1, EntityType.BAT.getTypeId(),
                            plugin.getLocale("upgrades.bat-flight.name", level)), Buildings.MAGETOWER,
                    plugin.getWarsConfig().getClassItemCost(playerClass, "bat" + level), "bat", level));
        }
    }

    @Override
    public void onUserAttack(UserAttackEvent event) {
        if (event.getUser().getPlayer().isFlying()) {
            event.setCancelled(true);
            return;
        }
        super.onUserAttack(event);
    }

    @Override
    public boolean onInteract(UserInteractEvent event) {
        return event.getUser().getPlayer().isFlying() || super.onInteract(event);

    }

    @Override
    public void onUserBeginGame(UserBeginGameEvent event) {
        super.onUserBeginGame(event);

        UserFlyingChanger changer = new UserFlyingChanger(event.getUser());

        changer.startTask(event.getUser().getPlugin());
    }

    private static class UserFlyingChanger implements Runnable {

        private int taskId;
        private User user;
        private boolean bat = false;
        private boolean allowFlight = false;
        private boolean mageTower = false;
        private double oldHealth = 0;

        private UserFlyingChanger(User user) {
            this.user = user;

            user.getPlayer().setExp(0f);
        }

        @Override
        public void run() {
            if (!user.isInGame()) Bukkit.getScheduler().cancelTask(taskId);

            if (!mageTower) {
                if (user.getTeam().getBuildingCount(Buildings.MAGETOWER) < 1) return;
                mageTower = true;
                user.getPlayer().setExp(1f);
                user.sendLocale("unlock.bat-flight.message");

                oldHealth = user.getPlayer().getHealth();
            }

            double newHealth = user.getPlayer().getHealth();
            float change = 0f;

            if(newHealth < oldHealth) change = -0.01f;
            if(user.getPlayer().hasPotionEffect(PotionEffectType.REGENERATION)) change = 0.001f;

            if(change != 0) {
                float exp = user.getPlayer().getExp();
                exp = Math.max(Math.min(exp + change, 1f), 0f);
                user.getPlayer().setExp(exp);
            }

            oldHealth = newHealth;

            if (user.getPlayer().isFlying()) {
                float exp = user.getPlayer().getExp();
                exp = Math.max(exp - flightDecreaseAmount(user.getUpgradeLevel("bat")), 0);
                user.getPlayer().setExp(exp);

                if (!bat) {
                    Disguises.disguise(user, DisguiseType.Bat);
                    bat = true;
                }

                if (exp > 0) return;
                user.getPlayer().setAllowFlight(allowFlight = false);
                user.sendLocale("timeouts.batting.finished");
            } else {
                float exp = user.getPlayer().getExp();
                exp = Math.min(exp + 0.00025f, 1);
                user.getPlayer().setExp(exp);

                if (exp > 0.2f && !allowFlight) {
                    user.getPlayer().setAllowFlight(allowFlight = true);
                    user.sendLocale("cooldowns.bat.finished");
                    user.getPlayer().setFlySpeed(0.05f);
                }

                if (bat) {
                    Disguises.unDisguise(user);
                    bat = false;
                }
            }
        }

        private float flightDecreaseAmount(int level) {
            switch(level) {
                case 1:
                    return 0.006f;
                case 2:
                    return 0.004f;
                default:
                    return 0.01f;
            }
        }

        public void startTask(WarsPlugin plugin) {
            taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this, 1, 1);
        }
    }
}
